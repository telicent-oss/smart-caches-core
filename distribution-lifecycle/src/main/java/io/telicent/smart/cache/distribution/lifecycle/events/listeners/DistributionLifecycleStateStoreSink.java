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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A sink that listens to lifecycle events ({@link LifecycleAction}, {@link LifecycleAcknowledgement} and
 * {@link IngestStatus}) and updates a {@link DistributionLifecycleStateStore} with those.
 * <p>
 * It also triggers {@link DistributionLifecycleListener}'s that allow an application to react to lifecycle events e.g.
 * by deleting data.  Listeners are explicitly triggered on background threads by means of the configured
 * {@link ExecutorService}.  This ensures that listeners do not block our ability to absorb further lifecycle events and
 * gives events that may need time to process the freedom to do so.  Callers should ensure that the configured executor
 * service has sufficient threads to reliably process listeners in a timely manner.
 * </p>
 * <p>
 * The state store is always updated prior to triggering listeners so lifecycle aware services can use the state store
 * as a live reference to what distributions are currently permitted for ingest and access.  See
 * {@link io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState} for more explanation of
 * distribution states.  They can do this even if their listeners have not fully applied lifecycle actions.  For an
 * example a service <strong>MUST</strong> actively prevent access to data from a distribution that is in the
 * {@link io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState#Deleted} state even if actually
 * deleting the data for that distribution is still being handled by a listener.
 * </p>
 */
@ToString
public class DistributionLifecycleStateStoreSink extends AbstractLifecycleListenerSink {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleStateStoreSink.class);

    private final DistributionLifecycleStateStore store;
    private final List<DistributionLifecycleListener> listeners;
    @ToString.Exclude
    private final ExecutorService executor;

    /**
     * Creates a new state store sink
     *
     * @param stateStore State store
     * @param listeners  Lifecycle listeners
     * @param executor   Listener executor service
     */
    @Builder
    DistributionLifecycleStateStoreSink(DistributionLifecycleStateStore stateStore,
                                        List<DistributionLifecycleListener> listeners, ExecutorService executor) {
        this.store = Objects.requireNonNull(stateStore, "State Store cannot be null");
        this.listeners = Objects.requireNonNullElse(listeners, Collections.emptyList());
        this.executor = Objects.requireNonNull(executor, "Listener executor cannot be null");
    }

    /**
     * Calls {@link io.telicent.smart.cache.sources.EventSource#processed(Collection)} with the given event so that the
     * event source is given the opportunity to update committed offsets
     *
     * @param event Event
     */
    private synchronized void commitNow(Event<UUID, LazyEnvelope> event) {
        // Inform the event source we've processed the event only after a successful flush
        // This ensures that we only commit offsets when the state store is up to date
        if (event.source() != null) {
            event.source().processed(List.of(event));
        }
    }

    @Override
    protected void handleIngestStatus(Event<UUID, LazyEnvelope> event, Envelope envelope, IngestStatus status) {
        store.add(envelope.getMetadata().getGeneratedBy(), status);
        commitNow(event);
    }

    @Override
    protected void handleAck(Event<UUID, LazyEnvelope> event, Envelope envelope, LifecycleAcknowledgement ack) {
        store.add(envelope.getMetadata().getGeneratedBy(), ack);
        commitNow(event);
    }

    @Override
    protected void handleAction(Event<UUID, LazyEnvelope> event, Envelope envelope, LifecycleAction action) {
        store.add(action);

        // Once we've been shutdown stop triggering any further listeners
        if (this.executor.isShutdown()) {
            return;
        }

        // Note that we always trigger listeners even if receiving the same event again, this is important to allow the
        // DistributionLifecycleTracker to retrigger events that an application has reported as Failed
        for (DistributionLifecycleListener listener : listeners) {
            try {
                if (listener == null) {
                    continue;
                }
                executor.submit(() -> listener.accept(action));
            } catch (Exception e) {
                LOGGER.warn(
                        "Distribution Lifecycle Listener {} failed to accept transition from {} to {} for distribution {}",
                        listener, action.getState().getFrom(), action.getState().getTo(), action.getDistributionId());
            }
        }

        commitNow(event);
    }

    @Override
    public void close() {
        // Close the store
        try {
            this.store.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to close Distribution Lifecycle State store: ", e);
        }

        // Stop the listener executor and then wait for active listeners to complete
        this.executor.shutdownNow();
        try {
            if (this.executor.awaitTermination(15, TimeUnit.SECONDS)) {
                LOGGER.info("Successfully terminated running distribution lifecycle listeners");
            } else {
                LOGGER.warn("Failed to terminate running distribution lifecycle listeners");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Interrupted waiting for distribution lifecycle listeners to complete");
        }

        // Finally close any listeners which gives them opportunity to release any resources they are holding
        for (DistributionLifecycleListener listener : this.listeners) {
            if (listener == null) {
                continue;
            }
            try {
                listener.close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close distribution lifecycle listener {}", listener, e);
            }
        }
    }
}
