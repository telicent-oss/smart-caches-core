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
import io.telicent.smart.cache.actions.tracker.listeners.SendToSinkListener;
import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

/**
 * An action tracker that upon state transitions sends notifications to a sink, typically this will be a
 * {@link io.telicent.smart.cache.sources.kafka.sinks.KafkaSink} for production usage.
 * <p>
 * This is intended for use by applications which need to coordinate actions across multiple microservices, the primary
 * microservice that controls actions should use this tracker, and any secondary microservices should use
 * {@link SecondaryActionTracker} to keep their state machines in sync with the primary.  Secondary microservices may
 * wish to configure listener functions on their {@link SecondaryActionTracker} if they need to actively respond to
 * state transitions in some way.
 * </p>
 */
@ToString(callSuper = true)
public final class PrimaryActionTracker extends SimpleActionTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrimaryActionTracker.class);

    private final Sink<Event<UUID, ActionTransition>> sink;

    @Builder
    PrimaryActionTracker(String application, Sink<Event<UUID, ActionTransition>> sink) {
        this(application, sink, 3, Duration.ofSeconds(5), Duration.ofSeconds(30));
    }

    /**
     * Creates a new Kafka primary backup tracker
     *
     * @param application Application ID
     * @param sink        Sink to send transition events to, this in production <strong>SHOULD</strong> be something
     *                    like {@link io.telicent.smart.cache.sources.kafka.sinks.KafkaSink} but using the interface
     *                    allows us to inject other sinks for testing
     */
    @Builder
    PrimaryActionTracker(String application, Sink<Event<UUID, ActionTransition>> sink, int maxRetries,
                         Duration minRetryInterval, Duration maxRetryInterval) {
        super(application, List.of(new LogCurrentStateListener(), SendToSinkListener.builder()
                                                                                    .sink(sink)
                                                                                    .maxRetries(maxRetries)
                                                                                    .minRetryInterval(minRetryInterval)
                                                                                    .maxRetryInterval(maxRetryInterval)
                                                                                    .build()));
        this.sink = Objects.requireNonNull(sink);
    }

    /**
     * Closes the destination sink for transition events
     */
    @Override
    public void close() {
        LOGGER.info("Closing Primary Action Tracker...");
        this.sink.close();
        LOGGER.info("Primary Action Tracker closed");
    }

    @ToString
    private static final class LogCurrentStateListener implements ActionTransitionListener {

        @Override
        public void accept(ActionTracker tracker, ActionTransition transition) {
            LOGGER.info("Action Tracker now in state {} with action '{}'", transition.getTo(), transition.getAction());
        }
    }

}
