package io.telicent.smart.cache.distribution.lifecycle.tracker;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.observability.LibraryVersion;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.Metadata;
import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.driver.StallAwareProjector;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.Builder;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Builder
public class DistributionLifecycleProjector implements Projector<Event<UUID, LazyEnvelope>, Event<UUID, LazyEnvelope>>,
        StallAwareProjector<Event<UUID, LazyEnvelope>, Event<UUID, LazyEnvelope>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleProjector.class);

    @NonNull
    private final DistributionLifecycleStateStore store;
    @NonNull
    private final String application;
    private final Sink<Event<UUID, LazyEnvelope>> dlq;


    @Override
    public void project(Event<UUID, LazyEnvelope> event, Sink<Event<UUID, LazyEnvelope>> sink) {
        try {
            sink.send(event);
        } catch (Throwable e) {
            if (this.dlq != null) {
                try {
                    this.dlq.send(event.addHeaders(
                            Stream.of(new Header(TelicentHeaders.DEAD_LETTER_REASON, e.getMessage()))));
                } catch (Throwable dlqErr) {
                    LOGGER.warn("Failed to send bad lifecycle event (failed due to {}) to DLQ: {}", e.getMessage(),
                                dlqErr.getMessage());
                }
            } else {
                // No DLQ just throw upwards
                throw e;
            }
        }
    }

    @Override
    public void stalled(Sink<Event<UUID, LazyEnvelope>> sink) {
        // When stalled check whether there are any active events we might want to re-trigger
        List<LifecycleAction> active = this.store.activeEvents();
        for (LifecycleAction action : active) {
            ApplicationState state = this.store.getApplicationState(action.getEventId(), this.application);
            if (state == ApplicationState.Failed) {
                // NB - In order to push this back into the sync we have to re-wrap it into an Envelope
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
