/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.cache.distribution.lifecycle.events.listeners;

import io.telicent.smart.cache.distribution.lifecycle.events.IngestStatus;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.LazyPayloadException;
import io.telicent.smart.cache.payloads.Metadata;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.sources.Event;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * A sink that's designed to listen to lifecycle events ({@link LifecycleAction}, {@link LifecycleAcknowledgement} and
 * {@link IngestStatus}) arriving from an event source and trigger behaviours from those.
 * <p>
 * This sink provides a number of methods that <strong>MUST</strong> be overridden to define how each type of lifecycle
 * event should be handled.
 * </p>
 */
public abstract class AbstractLifecycleListenerSink implements Sink<Event<UUID, LazyEnvelope>> {
    @Override
    public void send(Event<UUID, LazyEnvelope> item) {
        LazyEnvelope lazyEnvelope = item.value();
        if (lazyEnvelope == null) {
            return;
        }

        try {
            Envelope envelope = lazyEnvelope.getValue();

            switch (envelope.getMetadata().getDocumentFormat()) {
                case LifecycleAction.DOCUMENT_FORMAT ->
                        handleAction(item, envelope, envelope.getBodyAs(LifecycleAction.class));
                case LifecycleAcknowledgement.DOCUMENT_FORMAT ->
                        handleAck(item, envelope, envelope.getBodyAs(LifecycleAcknowledgement.class));
                case IngestStatus.DOCUMENT_FORMAT ->
                        handleIngestStatus(item, envelope, envelope.getBodyAs(IngestStatus.class));
                default -> handleUnknownPayload(item, envelope);
            }
        } catch (LazyPayloadException e) {
            handleBadPayload(item, e);
        }
    }

    /**
     * Called when a malformed payload is encountered i.e. the event's value cannot be successfully deserialized
     * <p>
     * If not overridden then this method throws a {@link io.telicent.smart.cache.projectors.SinkException} that wraps
     * the {@link LazyPayloadException}.
     * </p>
     *
     * @param item Bad event
     * @param e    Error thrown attempt to deserialize the value
     */
    protected void handleBadPayload(Event<UUID, LazyEnvelope> item, LazyPayloadException e) {
        throw new SinkException("Malformed lifecycle event encountered", e);
    }

    /**
     * Called when an unknown payload is encountered i.e. the event is valid and can be deserialized but the declared
     * {@link Metadata#getDocumentFormat()} does not map to one of the known lifecycle event types
     * <p>
     * If not overridden then this method throws a {@link SinkException} .
     * </p>
     *
     * @param event    Event
     * @param envelope Envelope containing the unknown payload
     */
    protected void handleUnknownPayload(Event<UUID, LazyEnvelope> event, Envelope envelope) {
        throw new SinkException("Unknown lifecycle event format " + envelope.getMetadata().getDocumentFormat());
    }

    /**
     * Called when an {@link IngestStatus} event is received
     *
     * @param event    Event
     * @param envelope Envelope with metadata about the event
     * @param status   Ingest status that was wrapped in the envelope
     */
    protected abstract void handleIngestStatus(Event<UUID, LazyEnvelope> event, Envelope envelope, IngestStatus status);

    /**
     * Called when an {@link LifecycleAcknowledgement} event is received
     *
     * @param event    Event
     * @param envelope Envelope with metadata about the event
     * @param ack      Lifecycle acknowledgement that was wrapped in the envelope
     */
    protected abstract void handleAck(Event<UUID, LazyEnvelope> event, Envelope envelope, LifecycleAcknowledgement ack);

    /**
     * Called when an {@link LifecycleAction} event is received
     *
     * @param event    Event
     * @param envelope Envelope with metadata about the event
     * @param action   Lifecycle action that was wrapped in the envelope
     */
    protected abstract void handleAction(Event<UUID, LazyEnvelope> event, Envelope envelope, LifecycleAction action);

    @Override
    public abstract void close();
}
