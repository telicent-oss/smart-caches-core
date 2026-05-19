package io.telicent.smart.cache.distribution.lifecycle.events.listeners;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.ApplicationStateUpdate;
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
        this.sink.send(ack(action.getEventId(), action.getDistributionId(), ApplicationState.Requested));
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
