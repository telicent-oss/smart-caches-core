/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.telicent.smart.cache.distribution.lifecycle.tracker;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.DistributionLifecycleStateStoreSink;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.observability.LibraryVersion;
import io.telicent.smart.cache.observability.TelicentMetrics;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.Metadata;
import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.projectors.driver.StallAwareProjector;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.kafka.KafkaEvent;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.Builder;
import lombok.NonNull;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Builder
// java:S2143 - java.util.Date is the Jackson-serialised wire type for this model; changing it would alter the JSON format
@SuppressWarnings("java:S2143")
public class DistributionLifecycleProjector implements Projector<Event<UUID, LazyEnvelope>, Event<UUID, LazyEnvelope>>,
        StallAwareProjector<Event<UUID, LazyEnvelope>, Event<UUID, LazyEnvelope>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleProjector.class);
    private static final LongCounter QUARANTINE_COUNTER = TelicentMetrics.getMeter("distribution-lifecycle")
                                                                        .counterBuilder("distribution.lifecycle.quarantined")
                                                                        .setDescription("Lifecycle records successfully written to a dead letter queue")
                                                                        .build();

    @NonNull
    private final DistributionLifecycleStateStore store;
    @NonNull
    private final String application;
    private final Sink<Event<UUID, LazyEnvelope>> dlq;


    @Override
    public void project(Event<UUID, LazyEnvelope> event, Sink<Event<UUID, LazyEnvelope>> sink) {
        try {
            sink.send(event);
        } catch (LifecycleEventRejectedException e) {
            quarantine(event, e);
        } catch (SinkException e) {
            if (e.getCause() instanceof LifecycleEventRejectedException rejection) {
                quarantine(event, rejection);
            } else {
                throw e;
            }
        }
    }

    private void quarantine(Event<UUID, LazyEnvelope> event, LifecycleEventRejectedException rejection) {
        String reason = rejection.toString();
        Stream<EventHeader> headers = Stream.of(new Header(TelicentHeaders.DEAD_LETTER_REASON, reason),
                                                new Header(TelicentHeaders.DEAD_LETTER_EXCEPTION_CLASS,
                                                           rejection.getClass().getName()),
                                                new Header(TelicentHeaders.EXEC_PATH, this.application));
        if (event instanceof KafkaEvent<UUID, LazyEnvelope> kafkaEvent) {
            ConsumerRecord<UUID, LazyEnvelope> record = kafkaEvent.getConsumerRecord();
            headers = Stream.concat(headers, Stream.of(
                    new Header(TelicentHeaders.DEAD_LETTER_SOURCE_TOPIC, record.topic()),
                    new Header(TelicentHeaders.DEAD_LETTER_SOURCE_PARTITION, Integer.toString(record.partition())),
                    new Header(TelicentHeaders.DEAD_LETTER_SOURCE_OFFSET, Long.toString(record.offset()))));
        }

        if (this.dlq == null) {
            LOGGER.error("Cannot quarantine lifecycle event {} for application {} because no lifecycle DLQ is configured: {}",
                         event.key(), this.application, reason, rejection);
            throw new IllegalStateException("Cannot quarantine rejected lifecycle event because no DLQ is configured",
                                            rejection);
        }

        try {
            this.dlq.send(event.addHeaders(headers));
            QUARANTINE_COUNTER.add(1, Attributes.of(AttributeKey.stringKey("application"), this.application));
            LOGGER.error("Quarantined lifecycle event {} for application {}: {}", event.key(), this.application,
                         reason, rejection);
        } catch (RuntimeException dlqFailure) {
            LOGGER.error("Failed to quarantine lifecycle event {} for application {}; leaving it uncommitted", event.key(),
                         this.application, dlqFailure);
            throw new IllegalStateException("Failed to quarantine rejected lifecycle event", dlqFailure);
        }
    }

    @Override
    public void stalled(Sink<Event<UUID, LazyEnvelope>> sink) {
        // When stalled first thing to do is to ensure that the sink has been flushed
        if (sink instanceof DistributionLifecycleStateStoreSink stateStoreSink) {
            stateStoreSink.maybeFlush();
        }

        // When stalled check whether there are any active events we might want to re-trigger
        List<LifecycleAction> active = this.store.activeEvents();
        for (LifecycleAction action : active) {
            ApplicationState state = this.store.getApplicationState(action.getEventId(), this.application);
            if (state == ApplicationState.Failed) {
                // NB - In order to push this back into the sink we have to re-wrap it into an Envelope
                //      We inject fresh metadata into the envelope as generally the consumer only cares about the body
                //      representing the action and not the surrounding metadata
                LOGGER.info("Re-triggering lifecycle event {} for distribution {} due to application reported failure",
                            action.getEventId(), action.getDistributionId());
                this.project(new SimpleEvent<>(Collections.emptyList(), action.getEventId(), LazyEnvelope.of(
                        Envelope.create()
                                .id(UUID.randomUUID())
                                .metadata(Metadata.create()
                                                  .generatedAt(Date.from(Instant.now()))
                                                  .generatedBy("distribution-lifecycle-projector")
                                                  .generatorVersion(LibraryVersion.get("distribution-lifecycle"))
                                                  .documentFormat(LifecycleAction.DOCUMENT_FORMAT)
                                                  .build())
                                .bodyFrom(action)
                                .build())), sink);
            }
        }
    }
}
