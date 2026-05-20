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
package io.telicent.smart.cache.distribution.lifecycle.events.listeners;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.ApplicationStateUpdate;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.Metadata;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.Builder;
import lombok.NonNull;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

/**
 * A decorator for distribution lifecycle listeners that generates acknowledgement events as it accepts an event
 */
@Builder
public class AckingListener implements DistributionLifecycleListener {

    @NonNull
    private final String application, version;
    @NonNull
    private final DistributionLifecycleStateStore stateStore;
    @NonNull
    private final DistributionLifecycleListener listener;
    @NonNull
    private final Sink<Event<UUID, LazyEnvelope>> sink;

    /**
     * Generates an acknowledgement event for passing to the sink
     *
     * @param eventId        Event ID to acknowledge
     * @param distributionId Distribution ID associated with the event
     * @param state          Application state update to provide
     * @return Acknowledgement event
     */
    protected final Event<UUID, LazyEnvelope> ack(UUID eventId, String distributionId, ApplicationState state) {
        LifecycleAcknowledgement ack = LifecycleAcknowledgement.builder()
                                                               .eventId(eventId)
                                                               .distributionId(distributionId)
                                                               .state(new ApplicationStateUpdate(state))
                                                               .build();
        LazyEnvelope envelope =
                LazyEnvelope.of(Envelope.create()
                                        .id(UUID.randomUUID())
                                        .metadata(Metadata.create()
                                                          .generatedBy(this.application)
                                                          .generatorVersion(this.version)
                                                          .generatedAt(
                                                                  Date.from(Instant.now()))
                                                          .documentFormat(LifecycleAcknowledgement.DOCUMENT_FORMAT)
                                                          .build())
                                        .bodyFrom(ack)
                                        .build());
        return new SimpleEvent<>(Collections.emptyList(), envelope.getValue().getId(), envelope);
    }

    @Override
    public void accept(LifecycleAction action) {
        // Acknowledge as Requested and then In-Progress
        if (this.stateStore.getApplicationState(action.getEventId(), this.application) == null) {
            // NB - We only send the Requested ack if this is the first time we've been called for this event, in the
            //      case of the inner listener failing and us having reported Failed state we may later get called again
            //      at which point we just ack back to InProgress and try again
            this.sink.send(ack(action.getEventId(), action.getDistributionId(), ApplicationState.Requested));
        }
        this.sink.send(ack(action.getEventId(), action.getDistributionId(), ApplicationState.InProgress));

        // Carry out the actual response to the action
        try {
            this.listener.accept(action);
            this.sink.send(ack(action.getEventId(), action.getDistributionId(), ApplicationState.Completed));
        } catch (Throwable e) {
            this.sink.send(ack(action.getEventId(), action.getDistributionId(), ApplicationState.Failed));
            throw e;
        }
    }

}
