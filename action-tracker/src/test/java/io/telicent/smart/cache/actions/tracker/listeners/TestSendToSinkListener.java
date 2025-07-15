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

import io.telicent.smart.cache.actions.tracker.ActionTracker;
import io.telicent.smart.cache.actions.tracker.model.ActionState;
import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.sources.Event;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class TestSendToSinkListener {

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullSink_whenBuildingListener_thenNPE() {
        // Given, When and Then
        SendToSinkListener.builder().sink(null).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*less than 1")
    public void givenZeroRetries_whenBuildingListener_thenIllegalArgument() {
        // Given, When and Then
        SendToSinkListener.builder()
                          .sink(NullSink.of())
                          .maxRetries(0)
                          .minRetryInterval(Duration.ofMillis(1))
                          .maxRetryInterval(Duration.ofMillis(2))
                          .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*less than 1")
    public void givenNegativeRetries_whenBuildingListener_thenIllegalArgument() {
        // Given, When and Then
        SendToSinkListener.builder()
                          .sink(NullSink.of())
                          .maxRetries(-5)
                          .minRetryInterval(Duration.ofMillis(1))
                          .maxRetryInterval(Duration.ofMillis(2))
                          .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*greater than 0")
    public void givenZeroMinInterval_whenBuildingListener_thenIllegalArgument() {
        // Given, When and Then
        SendToSinkListener.builder()
                          .sink(NullSink.of())
                          .maxRetries(3)
                          .minRetryInterval(Duration.ZERO)
                          .maxRetryInterval(Duration.ofMillis(2))
                          .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*greater than 0")
    public void givenZeroMaxInterval_whenBuildingListener_thenIllegalArgument() {
        // Given, When and Then
        SendToSinkListener.builder()
                          .sink(NullSink.of())
                          .maxRetries(3)
                          .minRetryInterval(Duration.ofMillis(1))
                          .maxRetryInterval(Duration.ZERO)
                          .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*less than.*")
    public void givenSameInterval_whenBuildingListener_thenIllegalArgument() {
        // Given, When and Then
        SendToSinkListener.builder()
                          .sink(NullSink.of())
                          .maxRetries(3)
                          .minRetryInterval(Duration.ofMillis(1))
                          .maxRetryInterval(Duration.ofMillis(1))
                          .build();
    }

    private static ActionTransition createTransition() {
        return ActionTransition.builder()
                               .id(UUID.randomUUID())
                               .timestamp(Date.from(Instant.now()))
                               .application("test")
                               .from(ActionState.STARTING)
                               .to(ActionState.READY)
                               .action("test")
                               .build();
    }

    @Test
    public void givenSinkThatSucceedsEventually_whenUsingListener_thenRetriesUntilSuccess() {
        // Given
        try (CollectorSink<Event<UUID, ActionTransition>> collector = CollectorSink.of()) {
            CountdownSink sink = new CountdownSink(collector, 3, () -> new SinkException("currently unable to send"));
            SendToSinkListener listener = SendToSinkListener.builder()
                                                            .sink(sink)
                                                            .maxRetries(3)
                                                            .minRetryInterval(Duration.ofMillis(5))
                                                            .maxRetryInterval(Duration.ofMillis(30))
                                                            .build();
            ActionTransition transition = createTransition();
            ActionTracker tracker = Mockito.mock(ActionTracker.class);

            // When
            listener.accept(tracker, transition);

            // Then
            Assert.assertEquals(collector.get().size(), 1);
            Assert.assertEquals(collector.get().get(0).value(), transition);
        }
    }

    @Test(expectedExceptions = SinkException.class, expectedExceptionsMessageRegExp = "broken")
    public void givenSinkThatAlwaysFails_whenUsingListener_thenRetriesUntilFailure() {
        // Given
        Sink<Event<UUID, ActionTransition>> sink = e -> {throw new SinkException("broken");};
        SendToSinkListener listener = SendToSinkListener.builder()
                                                        .sink(sink)
                                                        .maxRetries(3)
                                                        .minRetryInterval(Duration.ofMillis(5))
                                                        .maxRetryInterval(Duration.ofMillis(30))
                                                        .build();
        ActionTransition transition = createTransition();
        ActionTracker tracker = Mockito.mock(ActionTracker.class);

        // When and Then
        listener.accept(tracker, transition);
    }

}
