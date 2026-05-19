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

import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.events.IngestStatus;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.sources.Event;
import lombok.Builder;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A sink that listens to lifecycle events ({@link LifecycleAction}, {@link LifecycleAcknowledgement} and
 * {@link IngestStatus}) and updates a {@link DistributionLifecycleStateStore} with those.
 * <p>
 * It also triggers {@link DistributionLifecycleListener}'s that allow an application to react to lifecycle events e.g.
 * by deleting data.
 * </p>
 */
@ToString
public class DistributionLifecycleStateStoreSink extends AbstractLifecycleListenerSink {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleStateStoreSink.class);

    private final DistributionLifecycleStateStore store;
    private final List<DistributionLifecycleListener> listeners;
    @ToString.Exclude
    private final ExecutorService executor;
    private final Duration flushFrequency;
    private Instant lastFlush, nextFlush;
    @ToString.Exclude
    private Event<UUID, LazyEnvelope> mostRecentEvent;

    /**
     * Creates a new state store sink
     *
     * @param stateStore     State store
     * @param listeners      Lifecycle listeners
     * @param executor       Listener executor service
     * @param flushFrequency How frequently should updates to the state store be explicitly flushed and events reported
     *                       as processed to their source
     */
    @Builder
    DistributionLifecycleStateStoreSink(DistributionLifecycleStateStore stateStore,
                                        List<DistributionLifecycleListener> listeners, ExecutorService executor,
                                        Duration flushFrequency) {
        this.store = Objects.requireNonNull(stateStore, "State Store cannot be null");
        this.listeners = Objects.requireNonNullElse(listeners, Collections.emptyList());
        this.executor = Objects.requireNonNull(executor, "Listener executor cannot be null");
        this.flushFrequency = Objects.requireNonNullElse(flushFrequency, Duration.ofMinutes(3));
        if (this.flushFrequency.isNegative()) {
            throw new IllegalArgumentException("Flush Frequency cannot be negative");
        }
        updateFlushInstants();
    }

    /**
     * Updates the last and next flush instants
     */
    private void updateFlushInstants() {
        this.lastFlush = Instant.now();
        this.nextFlush = this.lastFlush.plus(this.flushFrequency);
    }

    /**
     * Maybe call {@link DistributionLifecycleStateStore#flush()} if the flush interval has been exceeded
     *
     * @param event Event
     */
    private void maybeFlush(Event<UUID, LazyEnvelope> event) {
        if (Instant.now().isAfter(this.nextFlush)) {
            LOGGER.debug("Triggering flush of Distribution Lifecycle State Store");
            flushNow(event);
        } else if (event.source() != null) {
            // We keep track of the most recent unflushed event, as and when we succesfully flush then we inform the
            // events source we've processed it which has the effect of committing offsets
            this.mostRecentEvent = event;
        }
    }

    /**
     * Calls {@link DistributionLifecycleStateStore#flush()} and if that succeeds, calls
     * {@link io.telicent.smart.cache.sources.EventSource#processed(Collection)} with the given event
     *
     * @param event Event
     */
    private void flushNow(Event<UUID, LazyEnvelope> event) {
        this.store.flush();

        // Inform the event source we've processed the event only after a successful flush
        // This ensures that we only commit offsets when the state store is up to date
        if (event.source() != null) {
            event.source().processed(List.of(event));
            this.mostRecentEvent = null;
        }

        updateFlushInstants();
    }

    @Override
    protected void handleIngestStatus(Event<UUID, LazyEnvelope> event, Envelope envelope, IngestStatus status) {
        // TODO IngestStatus tracking will be covered in future PRs
        maybeFlush(event);
    }

    @Override
    protected void handleAck(Event<UUID, LazyEnvelope> event, Envelope envelope, LifecycleAcknowledgement ack) {
        store.add(envelope.getMetadata().getGeneratedBy(), ack);

        maybeFlush(event);
    }

    @Override
    protected void handleAction(Event<UUID, LazyEnvelope> event, Envelope envelope, LifecycleAction action) {
        DistributionLifecycleState oldState = this.store.getLifecycleState(action.getDistributionId());
        store.add(action);
        DistributionLifecycleState newState = this.store.getLifecycleState(action.getDistributionId());

        // Trigger listeners if the state has actually changed
        if (oldState != newState) {
            // Once we've been shutdown stop triggering any further listeners
            if (this.executor.isShutdown()) {
                return;
            }

            for (DistributionLifecycleListener listener : listeners) {
                try {
                    if (listener == null) {
                        continue;
                    }
                    executor.submit(() -> listener.accept(action));
                } catch (Throwable e) {
                    LOGGER.warn(
                            "Distribution Lifecycle Listener {} failed to accept transition from {} to {} for distribution {}",
                            listener, action.getState().getFrom(), action.getState().getTo(),
                            action.getDistributionId());
                }
            }
        } else {
            LOGGER.trace(
                    "Distribution Lifecycle Event {} represented a transition that was already known to this application, no action taken",
                    action.getEventId());
        }

        maybeFlush(event);
    }

    @Override
    public void close() {
        // If we have unflushed events then flush now before we close the store
        if (this.mostRecentEvent != null) {
            this.flushNow(this.mostRecentEvent);
        }

        // Close the store, this will also cause it to be flushed again
        try {
            this.store.close();
        } catch (Throwable e) {
            LOGGER.warn("Failed to close Distribution Lifecycle State store: ", e);
        }

        // Stop the listener executor and then wait for active listeners to complete
        // We always call plain shutdown() first as that stops the executor accepting any new tasks
        this.executor.shutdown();
        this.executor.shutdownNow();
        try {
            if (this.executor.awaitTermination(15, TimeUnit.SECONDS)) {
                LOGGER.info("Successfully terminated running distribution lifecycle listeners");
            } else {
                LOGGER.warn(
                        "Failed to terminate running distribution lifecycle listeners");
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted waiting for distribution lifecycle listeners to complete");
        }
    }
}
