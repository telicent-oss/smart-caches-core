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
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.TopicExistenceChecker;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * A distribution lifecycle tracker that allows lifecycle aware applications to respond to distribution lifecycle
 * events
 * <p>
 * This manages a background tracker projection thread (using {@link ProjectorDriver} with
 * {@link DistributionLifecycleProjector} and {@link DistributionLifecycleStateStoreSink}) that listens for lifecycle
 * events from the event source passing them through the projector which uses the sink to update your state store
 * ({@link DistributionLifecycleStateStore}) and trigger the applications lifecycle listeners
 * ({@link DistributionLifecycleListener}).
 * </p>
 */
@ToString
public final class DistributionLifecycleTracker implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleTracker.class);
    protected static final Duration DEFAULT_TRACKER_STARTUP_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration CLEANUP_TIMEOUT = Duration.ofSeconds(5);

    @ToString.Exclude
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final EventSource<UUID, LazyEnvelope> eventSource;
    @ToString.Exclude
    private final ProjectorDriver<UUID, LazyEnvelope, Event<UUID, LazyEnvelope>> driver;
    @ToString.Exclude
    private final Future<?> future;
    private final List<DistributionLifecycleListener> listeners;
    @Getter
    private final DistributionLifecycleStateStore stateStore;
    private TrackerState trackerState = TrackerState.CREATED;
    private final Duration trackerCheckInterval;
    private long lastTrackerCheck;

    /**
     * Creates a new action tracker
     *
     * @param application           Application ID
     * @param eventSource           Event Source from which to read lifecycle events
     * @param stateStore            State store used to track lifecycle event
     * @param listeners             Listeners to those lifecycle events
     * @param listenerThreads       Configures the number of background threads used to fire off listener events, this
     *                              should be configured appropriately depending on whether listeners may require
     *                              significant time to process events
     * @param flushFrequency        How frequently {@link DistributionLifecycleStateStore#flush()} is called on the
     *                              state store
     * @param pollTimeout           Poll timeout when polling the event source for lifecycle events
     * @param dlq                   DLQ to which malformed/unprocessable lifecycle events should be forwarded
     * @param trackerStartupTimeout How long to wait to ensure that the tracker projection background thread is running
     *                              and stable.  This wait is used twice, once to ensure the background thread is
     *                              running and once again to allow time for the projection to catch up if lag is
     *                              particularly high
     * @param trackerCheckInterval  How frequently to check that the tracker projection background thread is still
     *                              running
     * @throws NullPointerException     If any of the required parameters are {@code null}
     * @throws IllegalArgumentException If the configured listeners threads is invalid
     * @throws IllegalStateException    If the provided event source is not usable
     */
    @Builder
    private DistributionLifecycleTracker(String application, EventSource<UUID, LazyEnvelope> eventSource,
                                         DistributionLifecycleStateStore stateStore,
                                         List<DistributionLifecycleListener> listeners, int listenerThreads,
                                         Sink<Event<UUID, LazyEnvelope>> dlq, Duration flushFrequency,
                                         Duration pollTimeout, Duration trackerStartupTimeout,
                                         Duration trackerCheckInterval) {
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
        this.trackerCheckInterval = Objects.requireNonNullElse(trackerCheckInterval, Duration.ofSeconds(10));
        if (this.trackerCheckInterval.isNegative() || this.trackerCheckInterval.isZero()) {
            throw new IllegalArgumentException("Tracker Check interval must be > 0");
        }

        // The rest of the constructor occurs in a try {} finally {} block
        // This is so that if any of the startup checks fail, and we mark the tracker state as FAILED, then we clean up
        // the resources we've been given that may leak
        try {
            // Actively validate the given event source is usable
            this.trackerState = TrackerState.STARTING;
            LOGGER.info("Distribution Lifecycle Tracker is starting and performing startup checks...");
            if (eventSource.isClosed()) {
                this.trackerState = TrackerState.FAILED;
                throw new IllegalStateException("Provided event source has already been closed");
            } else if (eventSource.isExhausted()) {
                this.trackerState = TrackerState.FAILED;
                throw new IllegalStateException("Provided event source has already been exhausted");
            } else if (eventSource instanceof KafkaEventSource<UUID, LazyEnvelope> kafkaSource) {
                TopicExistenceChecker checker = kafkaSource.getTopicExistenceChecker();
                if (!checker.allTopicsExist(Duration.ofSeconds(10))) {
                    this.trackerState = TrackerState.FAILED;
                    throw new IllegalStateException(
                            "Provided Kafka event source uses topics (" + StringUtils.join(kafkaSource.getTopics(),
                                                                                           ", ") + ") one/more of which do not exist");
                }
            }
            LOGGER.info("Verified that provided event source appears to be a valid source of lifecycle events");

            // Set up a ProjectorDriver that reads lifecycle events from the event source and updates the state store while
            // firing off the registered application listeners
            //@formatter:off
            DistributionLifecycleStateStoreSink sink
                    = DistributionLifecycleStateStoreSink.builder()
                                                          .stateStore(this.stateStore)
                                                          .listeners(this.listeners)
                                                          .executor(Executors.newFixedThreadPool(listenerThreads))
                                                          .flushFrequency(flushFrequency)
                                                          .build();
            this.driver = ProjectorDriver.<UUID, LazyEnvelope, Event<UUID, LazyEnvelope>>create()
                                         .source(this.eventSource)
                                         .unlimited()
                                         .pollTimeout(Objects.requireNonNullElse(pollTimeout, Duration.ofSeconds(5)))
                                         .projector(DistributionLifecycleProjector.builder()
                                                                                  .store(this.stateStore)
                                                                                  .application(application)
                                                                                  .dlq(dlq)
                                                                                  .build())
                                         .destination(sink)
                                         .threadName("DistributionLifecycleTracker")
                                         // Distribution Lifecycle topic should be low throughput so processing speed
                                         // warnings have no value to us
                                         .disabledProcessingSpeedWarnings()
                                         .build();
            //@formatter:on

            // Inspect the state store and immediately re-trigger any events for which our application had not ack'd as
            // Completed
            LOGGER.info("Re-triggering any active lifecycle events...");
            int retriggered = 0;
            for (LifecycleAction action : this.stateStore.activeEvents()) {
                ApplicationState state = this.stateStore.getApplicationState(action.getEventId(), application);
                // While activeEvents() should only return events that aren't completed if the state store being used is
                // tracking multiple applications states then it's possible that our application has acknowledged this event
                // as completed BUT other applications MAY not have done.  Thus, we need this extra non-completion check in
                // case the active event has been completed by our application.
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
                                                            .generatorVersion(
                                                                    LibraryVersion.get("distribution-lifecycle"))
                                                            .documentFormat(LifecycleAction.DOCUMENT_FORMAT)
                                                            .build())
                                          .bodyFrom(action)
                                          .build())), sink);
                    retriggered++;
                }
            }
            LOGGER.info("Re-triggered {} active lifecycle events", retriggered);

            // Start the lifecycle tracker running on a background thread
            LOGGER.info("Starting tracker projection...");
            this.future = this.executor.submit(this.driver);

            // Wait a little bit to ensure that the tracker is not going to fail immediately
            // This is necessary for several reasons:
            // 1) The tracker projection is on a background thread so may not immediately be started
            // 2) Depending on the event source establishing the initial connection may take a few seconds to know whether
            //    the connection is valid, and we can poll() events from it
            // 3) While for Kafka event sources we've already established the lifecycle topic exists we haven't yet
            //    established that we're actually able to read events from it
            Duration startupTimeout = getStartupTimeout(trackerStartupTimeout);
            try {
                LOGGER.info("Performing startup checks on tracker projection...");
                this.future.get(startupTimeout.toMillis(), TimeUnit.MILLISECONDS);

                // If we get here, and not a timeout exception this means the projection already completed, this most likely
                // means an exhausted/misbehaving event source as other error conditions would manifest as a visible failure
                // and fall into one of the catch blocks
                this.trackerState = TrackerState.FAILED;
                LOGGER.error("Tracker projection exited prematurely - likely bad/misbehaving event source");
                throw prematureExit();
            } catch (InterruptedException e) {
                this.trackerState = TrackerState.FAILED;
                LOGGER.error("Interrupted during startup checks");
                throw new IllegalStateException(
                        "Interrupted while waiting to see if tracker projection is running successfully");
            } catch (ExecutionException e) {
                this.trackerState = TrackerState.FAILED;
                LOGGER.error("Tracker projection failed: ", e);
                throw new IllegalStateException("Tracker projection failed, see cause for details", e);
            } catch (TimeoutException e) {
                // This is the expected good outcome, if we get a timeout that means our projection thread is still
                // running and hasn't immediately exited/errored, therefore we can assume it is stable
            } finally {
                // If we failed to start up make sure to clean up our projector and executor service
                // Most likely the driver already failed if we're here but not harmful to explicitly cancel it as well
                if (this.trackerState == TrackerState.FAILED) {
                    this.driver.cancel();
                    this.executor.shutdownNow();
                }
            }

            // We've now established that the tracker is running, next we need to ensure that it is up to date with the
            // lifecycle events otherwise our application may make the wrong decisions about how to handle distributions
            Long remaining = eventSource.remaining();
            long start = System.currentTimeMillis();
            while (remaining != null && remaining > 0) {
                Duration elapsed = Duration.ofMillis(System.currentTimeMillis() - start);
                if (elapsed.compareTo(startupTimeout) >= 0) {
                    this.trackerState = TrackerState.FAILED;
                    LOGGER.error("Timed out waiting for tracker to catch up with lifecycle events after {}",
                                 startupTimeout);
                    throw new IllegalStateException(
                            "Tracker projection has lag of " + remaining + " meaning we cannot make up to date decisions about distribution lifecycle");
                }

                LOGGER.info("Tracker has current lag of {}, waiting for it to catch up...", remaining);

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    if (this.future.isDone() || this.future.isCancelled()) {
                        this.trackerState = TrackerState.FAILED;
                        LOGGER.error("Tracker projection exited unexpectedly while catching up while lifecycle events");
                        this.executor.shutdownNow();
                        throw prematureExit();
                    }
                }
                remaining = eventSource.remaining();

                // NB - We explicitly try and force a flush as otherwise if the sink isn't flushed the state store might
                //      not be flushed, and the event offsets might not be committed back to the event source.  If we
                //      fail to catch up within the timeout we'd then be in a crash-restart loop because we'd not have
                //      progressed our state of processing the lifecycle topic and be stuck forever in this state.
                sink.maybeFlush();
            }

            // Only if we reach the end of the constructor do we consider the tracker to be running
            this.lastTrackerCheck = System.currentTimeMillis();
            this.trackerState = TrackerState.RUNNING;
            LOGGER.info("Tracker reached running state, current lag is {} events", eventSource.remaining());
        } finally {
            if (this.trackerState == TrackerState.FAILED) {
                // If startup checks failed we clean up the resources we've been passed that might otherwise leak
                // This also increases the chance that even if the caller ignores the error we've thrown to tell them
                // we couldn't create a valid tracker that continuing to use the resources, like the state store,
                // continue to throw errors
                LOGGER.warn("Failed to startup Distribution Lifecycle Tracker, cleaning up resources...");
                this.listeners.forEach(DistributionLifecycleTracker::cleanup);
                cleanup(stateStore);
                cleanup(this.executor);
                LOGGER.warn("Failed to startup Distribution Lifecycle Tracker, resources cleaned up");
            }
        }
    }

    /**
     * Cleans up {@link AutoCloseable}'s ensuring any errors are logged and don't prevent further cleanups from being
     * applied
     *
     * @param closeable Closeable to clean-up
     */
    private static void cleanup(AutoCloseable closeable) {
        try {
            if (closeable instanceof ExecutorService executorService) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(CLEANUP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                    LOGGER.warn("Timed out cleaning up executor service {}", closeable.getClass().getSimpleName());
                }
            } else {
                closeable.close();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Interrupted while cleaning up {}", closeable.getClass().getSimpleName());
        } catch (Throwable t) {
            LOGGER.warn("Failed to clean up {}:", closeable.getClass().getSimpleName(), t);
        }
    }

    /**
     * Produces standardised error when the projector exits prematurely
     *
     * @return Error
     */
    private static IllegalStateException prematureExit() {
        return new IllegalStateException("Tracker projection exited prematurely");
    }

    /**
     * Gets the tracker startup timeout to use when making the initial tracker projection startup check
     * <p>
     * If the configured timeout is {@code null}, zero or negative then the {@link #DEFAULT_TRACKER_STARTUP_TIMEOUT} is
     * used instead.
     * </p>
     *
     * @param trackerStartupTimeout Configured timeout
     * @return Startup timeout, possibly the default if configured timeout is invalid
     */
    private static Duration getStartupTimeout(Duration trackerStartupTimeout) {
        if (trackerStartupTimeout == null || trackerStartupTimeout.isZero() || trackerStartupTimeout.isNegative()) {
            return DEFAULT_TRACKER_STARTUP_TIMEOUT;
        }
        return trackerStartupTimeout;
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
        if (this.trackerState == TrackerState.CLOSED) {
            return;
        }

        LOGGER.info("Closing Distribution Lifecycle Tracker...");
        this.trackerState = TrackerState.CLOSING;
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
            this.trackerState = TrackerState.CLOSED;
        }
        LOGGER.info("Distribution Lifecycle Tracker closed");
    }

    /**
     * Gets whether the tracker is actively running
     *
     * @return Running
     */
    public boolean isRunning() {
        return switch (this.trackerState) {
            case CREATED, STARTING -> true;
            case RUNNING -> !this.future.isDone() && !this.future.isCancelled();
            case FAILED, CLOSING, CLOSED -> false;
        };
    }

    /**
     * Gets the current tracker state, if the state was {@link TrackerState#RUNNING} this will periodically double-check
     * whether the tracker is actually still running
     *
     * @return Current tracker state
     */
    public TrackerState getTrackerState() {
        if (this.trackerState == TrackerState.RUNNING) {
            // Check at most every interval seconds
            Duration elapsed = Duration.ofMillis(System.currentTimeMillis() - this.lastTrackerCheck);
            if (elapsed.compareTo(this.trackerCheckInterval) >= 0) {
                this.lastTrackerCheck = System.currentTimeMillis();
                if (this.future.isDone()) {
                    LOGGER.error("Detected tracker projection is no longer running, check earlier log for details");
                    this.trackerState = TrackerState.FAILED;
                }
            }
        }

        return this.trackerState;
    }
}
