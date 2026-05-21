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
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.DistributionLifecycleListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.DistributionLifecycleStateStoreSink;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.observability.LibraryVersion;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.Metadata;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.driver.ProjectorDriver;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.Builder;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * A distribution lifecycle tracker that allows lifecycle aware applications to respond to distribution lifecycle
 * events
 */
@ToString
public final class DistributionLifecycleTracker implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleTracker.class);

    @ToString.Exclude
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final EventSource<UUID, LazyEnvelope> eventSource;
    @ToString.Exclude
    private final ProjectorDriver<UUID, LazyEnvelope, Event<UUID, LazyEnvelope>> driver;
    @ToString.Exclude
    private final Future<?> future;
    private final List<DistributionLifecycleListener> listeners;
    private final DistributionLifecycleStateStore stateStore;

    /**
     * Creates a new action tracker
     *
     * @param application     Application ID
     * @param eventSource     Event Source from which to read lifecycle events
     * @param stateStore      State store used to track lifecycle event
     * @param listeners       Listeners to those lifecycle events
     * @param listenerThreads Configures the number of background threads used to fire off listener events, this should
     *                        be configured appropriately depending on whether listeners may require significant time to
     *                        process events
     */
    @Builder
    private DistributionLifecycleTracker(String application, EventSource<UUID, LazyEnvelope> eventSource,
                                         DistributionLifecycleStateStore stateStore,
                                         List<DistributionLifecycleListener> listeners, int listenerThreads,
                                         Sink<Event<UUID, LazyEnvelope>> dlq) {
        this.eventSource = Objects.requireNonNull(eventSource, "Event Source cannot be null");
        this.stateStore = Objects.requireNonNull(stateStore, "Distribution Lifecycle State store cannot be null");
        this.listeners = Objects.requireNonNullElse(listeners, Collections.emptyList());
        if (this.listeners.isEmpty()) {
            LOGGER.warn(
                    "Distribution Lifecycle Tracker created without any listeners, applications should register at least one listener in order to respond to lifecycle events");
        }
        if (listenerThreads <= 0) {
            throw new IllegalArgumentException("Listener Threads must be greater than zero");
        }

        // Set up a ProjectorDriver that reads lifecycle events from the event source and updates the state store while
        // firing off the registered application listeners
        DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                      .stateStore(this.stateStore)
                                                                                      .listeners(this.listeners)
                                                                                      .executor(
                                                                                              Executors.newFixedThreadPool(
                                                                                                      listenerThreads))
                                                                                      .build();
        this.driver = ProjectorDriver.<UUID, LazyEnvelope, Event<UUID, LazyEnvelope>>create()
                                     .source(this.eventSource)
                                     .unlimited()
                                     .pollTimeout(Duration.ofSeconds(10))
                                     .projector(DistributionLifecycleProjector.builder()
                                                                              .store(this.stateStore)
                                                                              .application(application)
                                                                              .dlq(dlq)
                                                                              .build())
                                     .destination(sink)
                                     // Distribution Lifecycle topic should be low throughput so processing speed
                                     // warnings have no value to us
                                     .disabledProcessingSpeedWarnings()
                                     .build();

        // Inspect the state store and immediately re-trigger any events for which our application had not ack'd as
        // Completed
        for (LifecycleAction action : this.stateStore.activeEvents()) {
            ApplicationState state = this.stateStore.getApplicationState(action.getEventId(), application);
            if (state != ApplicationState.Completed) {
                // NB - In order to push this back into the sink and re-trigger listeners we have to re-wrap it into an
                //      Envelope
                //      We inject fresh metadata into the envelope as generally the consumer only cares about the body
                //      representing the action and not the surrounding metadata
                driver.getProjector()
                      .project(new SimpleEvent<>(Collections.emptyList(), action.getEventId(), LazyEnvelope.of(
                              Envelope.create()
                                      .id(UUID.randomUUID())
                                      .metadata(Metadata.create()
                                                        .generatedAt(Date.from(Instant.now()))
                                                        .generatedBy("distribution-lifecycle-tracker")
                                                        .generatorVersion(LibraryVersion.get("distribution-lifecycle"))
                                                        .documentFormat(LifecycleAction.DOCUMENT_FORMAT)
                                                        .build())
                                      .bodyFrom(action)
                                      .build())), sink);
            }
        }

        // Start the lifecycle tracker running on a background thread
        this.future = this.executor.submit(this.driver);
    }

    /**
     * Closes the action tracker
     * <p>
     * This stops the distribution lifecycle tracker projection so the tracker will no longer keep in sync with
     * distribution lifecycle topic and respond to events
     * </p>
     */
    @Override
    public void close() {
        LOGGER.info("Closing Distribution Lifecycle Tracker...");
        try {
            // Cancel the driver, this stops us receiving more events, we wait a little while to give the cancellation
            // chance to take effect
            this.driver.cancel();
            this.future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Ignored
            LOGGER.warn("Interrupted while waiting for distribution lifecycle tracker projection to complete");
        } catch (ExecutionException e) {
            LOGGER.warn("Distribution Lifecycle tracker projection exited abnormally: {}", e.getMessage());
        } catch (TimeoutException e) {
            // Ignored
            LOGGER.warn("Timed out waiting for distribution lifecycle tracker projection to complete");
        } finally {
            try {
                // Close the event source, cancelling the driver should close this but no harm in doing it again here
                this.eventSource.close();
            } catch (Throwable e) {
                // Ignored
                LOGGER.warn("Error closing distribution lifecycle event source: {}", e.getMessage());
            } finally {
                // Finally clean up our executor service as we no longer need the threads
                this.executor.shutdownNow();
            }
        }
        LOGGER.info("Distribution Lifecycle Tracker closed");
    }

    /**
     * Gets whether the tracker is actively running
     *
     * @return Running
     */
    public boolean isRunning() {
        return !this.future.isDone() && !this.future.isCancelled();
    }
}
