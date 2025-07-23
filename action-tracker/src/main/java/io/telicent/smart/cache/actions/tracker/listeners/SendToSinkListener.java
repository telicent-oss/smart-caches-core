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
package io.telicent.smart.cache.actions.tracker.listeners;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.telicent.smart.cache.actions.tracker.ActionTracker;
import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An action transition listener that sends the transition events to a {@link Sink}
 */
@ToString
public class SendToSinkListener implements ActionTransitionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendToSinkListener.class);

    @NonNull
    private final Sink<Event<UUID, ActionTransition>> sink;
    @ToString.Exclude
    private final Retry retry;

    /**
     * Creates a new listener
     *
     * @param sink             Destination sink
     * @param maxRetries       Maximum retries for sending transition events
     * @param minRetryInterval Minimum retry interval between attempting to send transition events
     * @param maxRetryInterval Maximum retry interval between attempting to send transition events
     */
    @Builder
    SendToSinkListener(@NonNull Sink<Event<UUID, ActionTransition>> sink, int maxRetries,
                       @NonNull Duration minRetryInterval, @NonNull Duration maxRetryInterval) {
        this.sink = Objects.requireNonNull(sink, "Sink cannot be null");

        // Validate and set up retry configuration
        if (maxRetries < 1) {
            throw new IllegalArgumentException("maxRetries cannot be less than 1");
        }
        Objects.requireNonNull(minRetryInterval, "minRetryInterval cannot be null");
        Objects.requireNonNull(maxRetryInterval, "maxRetryInterval cannot be null");
        if (Duration.ZERO.compareTo(minRetryInterval) >= 0) {
            throw new IllegalArgumentException("minRetryInterval MUST be greater than 0");
        }
        if (Duration.ZERO.compareTo(maxRetryInterval) >= 0) {
            throw new IllegalArgumentException("maxRetryInterval MUST be greater than 0");
        }
        if (minRetryInterval.compareTo(maxRetryInterval) >= 0) {
            throw new IllegalArgumentException("minRetryInterval MUST be less than maxRetryInterval");
        }
        RetryConfig config = RetryConfig.custom()
                                        .failAfterMaxAttempts(true)
                                        .maxAttempts(maxRetries)
                                        .intervalFunction(IntervalFunction.ofExponentialBackoff(minRetryInterval, 2,
                                                                                                maxRetryInterval))
                                        .build();
        this.retry = Retry.of("send-to-sink-listener-" + UUID.randomUUID(), config);
    }

    @Override
    public void accept(ActionTracker tracker, ActionTransition transition) {
        SimpleEvent<UUID, ActionTransition> event =
                new SimpleEvent<>(Collections.emptyList(), transition.getId(), transition);
        AtomicInteger attempts = new AtomicInteger(0);
        this.retry.executeRunnable(() -> {
            if (attempts.incrementAndGet() > 1) {
                LOGGER.warn("Previous attempt to send transition event failed, trying again... (attempt {} of {})",
                            attempts.get(), this.retry.getRetryConfig().getMaxAttempts());
            }
            sink.send(event);
            LOGGER.info("Sent transition event from {} to {}", transition.getFrom(), transition.getTo());
        });
    }
}
