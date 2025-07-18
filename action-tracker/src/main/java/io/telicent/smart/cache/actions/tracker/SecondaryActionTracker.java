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
package io.telicent.smart.cache.actions.tracker;

import io.telicent.smart.cache.actions.tracker.listeners.ActionTransitionListener;
import io.telicent.smart.cache.actions.tracker.model.ActionState;
import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
import io.telicent.smart.cache.projectors.NoOpProjector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.driver.ProjectorDriver;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * An action tracker that synchronises its state based on transition events from Kafka
 * <p>
 * Intended for use in conjunction with {@link PrimaryActionTracker}, see JavaDoc on that class for more
 * information.
 * </p>
 */
@ToString(callSuper = true)
public final class SecondaryActionTracker extends SimpleActionTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecondaryActionTracker.class);

    @ToString.Exclude
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final EventSource<UUID, ActionTransition> eventSource;
    @ToString.Exclude
    private final ProjectorDriver<UUID, ActionTransition, Event<UUID, ActionTransition>> driver;
    @ToString.Exclude
    private final Future<?> future;

    /**
     * Creates a new action tracker
     *
     * @param application Application ID
     * @param eventSource Event Source from which to read transition events
     */
    @Builder
    private SecondaryActionTracker(String application, EventSource<UUID, ActionTransition> eventSource,
                                   List<ActionTransitionListener> listeners) {
        super(application, listeners);
        this.eventSource = Objects.requireNonNull(eventSource, "Event Source cannot be null");

        // Set up a ProjectorDriver that reads transition events from the event source and updates this action tracker
        // accordingly, this has the side effect of also triggering the onTransition() method as normal so derived
        // implementations can choose to further respond to transitions as needed
        this.driver = ProjectorDriver.<UUID, ActionTransition, Event<UUID, ActionTransition>>create()
                                     .source(this.eventSource)
                                     .unlimited()
                                     .pollTimeout(Duration.ofSeconds(5))
                                     .projector(new NoOpProjector<>())
                                     .destination(new ActionTransitionSink(this, this.application, this.eventSource))
                                     // Action transition topic should be low throughput so processing speed warnings
                                     // have no value to us
                                     .disabledProcessingSpeedWarnings()
                                     .build();
        this.future = this.executor.submit(this.driver);
    }

    /**
     * Closes the action tracker
     * <p>
     * This stops the transition projection so the tracker will no longer keep in sync with its primary and releases
     * the thread pool used to execute the projection
     * </p>
     */
    @Override
    public void close() {
        LOGGER.info("Closing Secondary Action Tracker...");
        try {
            // Cancel the driver, this stops us receiving more events, we wait a little while to give the cancellation
            // chance to take effect
            this.driver.cancel();
            this.future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Ignored
            LOGGER.warn("Interrupted while waiting for transition projection to complete");
        } catch (ExecutionException e) {
            LOGGER.warn("Transition projection exited abnormally: {}", e.getMessage());
        } catch (TimeoutException e) {
            // Ignored
            LOGGER.warn("Timed out waiting for transition projection to complete");
        } finally {
            try {
                // Close the event source, cancelling the driver should close this but no harm in doing it again here
                this.eventSource.close();
            } catch (Throwable e) {
                // Ignored
                LOGGER.warn("Error closing transition event source: {}", e.getMessage());
            } finally {
                // Finally clean up our executor service as we no longer need the threads
                this.executor.shutdownNow();
            }
        }
        LOGGER.info("Secondary Action Tracker closed");
    }

    /**
     * Resets the action value to {@code null}
     */
    private void resetAction() {
        this.action = null;
    }

    /**
     * A sink which applies the action transitions to the tracker
     */
    @Builder
    private static class ActionTransitionSink implements Sink<Event<UUID, ActionTransition>> {

        @NonNull
        private final SecondaryActionTracker tracker;
        private final String application;
        private final EventSource<UUID, ActionTransition> eventSource;

        @Override
        public void send(Event<UUID, ActionTransition> event) {
            // Ignore transitions that don't pertain to this application
            ActionTransition transition = event.value();
            if (!Objects.equals(transition.getApplication(), this.application)) {
                return;
            }

            LOGGER.info("Received backup transition from {} to {} for action '{}'", transition.getFrom(),
                        transition.getTo(), transition.getAction());

            try {
                // This basically applies the appropriate transition by calling the ActionTracker method that would have
                // resulted in PrimaryActionTracker sending this transition event
                // Note that since any illegal transitions MUST have been rejected on the primary there's no need to cover
                // illegal transition cases
                switch (transition.getFrom()) {
                    case STARTING:
                        switch (transition.getTo()) {
                            case READY:
                                sendProcessed(event);
                                tracker.startupComplete();
                                tracker.resetAction();
                                break;
                            case PROCESSING:
                                tracker.start(transition.getAction());
                                break;
                        }
                        break;
                    case READY:
                        if (transition.getTo() == ActionState.PROCESSING) {
                            tracker.start(transition.getAction());
                        }
                        break;
                    case PROCESSING:
                        if (transition.getTo() == ActionState.READY) {
                            sendProcessed(event);
                            tracker.finish(transition.getAction());
                        }
                        break;
                }

                LOGGER.info("Action Tracker now in state {}", this.tracker.getState());
            } catch (IllegalStateException e) {
                LOGGER.warn("State transition from {} to {} not permitted: {}", transition.getFrom(),
                            transition.getTo(), e.getMessage());
                LOGGER.warn("Action Tracker state may not be correctly in sync with primary");
            }
        }

        private void sendProcessed(Event<UUID, ActionTransition> event) {
            // Whenever we receive an event that transitions us back into the READY state tell the event source we've
            // processed the events.  For Kafka sources this has the effect of committing our offsets so we don't need
            // to fully replay the control topic next time we are started.
            // It's always safe to commit once we reach the READY state as the ActionState machine always permits
            // us to transition to/from the ready state from any other state
            // Note that we call this method prior to applying the transition locally and triggering any listeners,
            // otherwise those listeners could prevent us successfully committing e.g. if they decide to exit the
            // process, and put us in a permanent crash restart loop
            this.eventSource.processed(List.of(event));
        }
    }
}
