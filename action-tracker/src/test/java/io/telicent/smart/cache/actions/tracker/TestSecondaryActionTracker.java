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

import io.telicent.smart.cache.actions.tracker.model.ActionState;
import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.ToString;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSecondaryActionTracker {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "application cannot.*")
    public void givenNullAppName_whenCreatingBackupTracker_thenIllegalArgument() {
        // Given, When and Then
        SecondaryActionTracker.builder()
                              .application(null)
                              .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                              .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "application cannot.*")
    public void givenEmptyAppName_whenCreatingBackupTracker_thenIllegalArgument() {
        // Given, When and Then
        SecondaryActionTracker.builder()
                              .application("")
                              .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                              .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "application cannot.*")
    public void givenBlankAppName_whenCreatingBackupTracker_thenIllegalArgument() {
        // Given, When and Then
        SecondaryActionTracker.builder()
                              .application("   ")
                              .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                              .build();
    }

    private Event<UUID, ActionTransition> createEvent(String app, ActionState from, ActionState to, String action) {
        ActionTransition transition = ActionTransition.builder()
                                                      .id(UUID.randomUUID())
                                                      .application(app)
                                                      .action(action)
                                                      .timestamp(Date.from(Instant.now()))
                                                      .from(from)
                                                      .to(to)
                                                      .build();
        return new SimpleEvent<>(Collections.emptyList(), transition.getId(), transition);
    }

    @DataProvider(name = "transitions")
    private Object[][] transitionEvents() {
        //@formatter:off
        return new Object[][] {
                {
                        Collections.emptyList(),
                        ActionState.STARTING,
                        null
                },
                {
                        List.of(createEvent("test", ActionState.STARTING, ActionState.READY, null)),
                        ActionState.READY,
                        null
                },
                {
                        List.of(createEvent("test", ActionState.STARTING, ActionState.READY, null),
                                createEvent("test", ActionState.READY, ActionState.PROCESSING, "backup")),
                        ActionState.PROCESSING,
                        "backup"
                },
                {
                        List.of(createEvent("test", ActionState.STARTING, ActionState.READY, null),
                                createEvent("test", ActionState.READY, ActionState.PROCESSING, "backup"),
                                createEvent("test", ActionState.PROCESSING, ActionState.READY, "backup"),
                                createEvent("test", ActionState.READY, ActionState.PROCESSING, "restore")),
                        ActionState.PROCESSING,
                        "restore"
                },
                {
                        List.of(createEvent("test", ActionState.STARTING, ActionState.PROCESSING, "restore"),
                                createEvent("test", ActionState.PROCESSING, ActionState.READY, "restore")),
                        ActionState.READY,
                        null
                },
                // Events for different applications are ignored
                {
                        List.of(createEvent("test", ActionState.STARTING, ActionState.PROCESSING, "backup"),
                                createEvent("other", ActionState.PROCESSING, ActionState.READY, "backup")),
                        ActionState.PROCESSING,
                        "backup"
                },
                {
                        List.of(createEvent("other", ActionState.STARTING, ActionState.READY, null)),
                        ActionState.STARTING,
                        null
                },
                // In some scenarios we might get sequences of events that aren't usually legal
                // E.g. Primary in a crash restart loop would issue several identical events in a row
                // This one works as always acceptable to transition to READY state, even if already READY
                {
                        List.of(createEvent("test", ActionState.STARTING, ActionState.READY, null),
                                createEvent("test", ActionState.STARTING, ActionState.READY, null),
                                createEvent("test", ActionState.STARTING, ActionState.READY, null),
                                createEvent("test", ActionState.STARTING, ActionState.READY, null),
                                createEvent("test", ActionState.STARTING, ActionState.READY, null)),
                        ActionState.READY,
                        null
                },
                // If primary crashes during an operation it may not send a transition back to ready
                // However again this is fine as going back to the READY state is always permissible
                {
                        List.of(createEvent("test", ActionState.STARTING, ActionState.READY, null),
                                createEvent("test", ActionState.READY, ActionState.PROCESSING, "backup"),
                                createEvent("test", ActionState.STARTING, ActionState.READY, null)),
                        ActionState.READY,
                        null
                },
                {
                        List.of(createEvent("test", ActionState.STARTING, ActionState.READY, null),
                                createEvent("test", ActionState.READY, ActionState.PROCESSING, "restore"),
                                createEvent("test", ActionState.STARTING, ActionState.READY, null)),
                        ActionState.READY,
                        null
                },
                // Worst case scenario is that some transition events never get sent, and we only see later transitions
                // In this case we may not be correctly sync'd and unfortunately there isn't a lot we can do about this
                // However if we start receiving legal transitions later we should then recover
                {
                        List.of(createEvent("test", ActionState.PROCESSING, ActionState.READY, "backup")),
                        ActionState.STARTING,
                        null
                },
                {
                        List.of(createEvent("test", ActionState.PROCESSING, ActionState.READY, "restore"),
                                createEvent("test", ActionState.STARTING, ActionState.READY, null),
                                createEvent("test", ActionState.READY, ActionState.PROCESSING, "backup")),
                        ActionState.PROCESSING,
                        "backup"
                },
                // A malicious actor could put arbitrary bad transition events onto the topic BUT we only honour legal
                // transitions so applying these should leave us in our default STARTING state
                {
                        List.of(createEvent("test", ActionState.READY, ActionState.STARTING, null),
                                createEvent("test", ActionState.PROCESSING, ActionState.STARTING, "backup"),
                                createEvent("test", ActionState.PROCESSING, ActionState.STARTING, "restore"),
                                createEvent("test", ActionState.PROCESSING, ActionState.PROCESSING, "backup"),
                                createEvent("test", ActionState.PROCESSING, ActionState.PROCESSING, "restore")),
                        ActionState.STARTING,
                        null
                }
        };
        //@formatter:on
    }

    @Test(dataProvider = "transitions")
    public void givenTransitionEvents_whenUsingKafkaSecondaryBackupTracker_thenStateTransitionsApplied(
            List<Event<UUID, ActionTransition>> events, ActionState expectedFinalState, String expectedFinalAction) {
        // Given
        InMemoryEventSource<UUID, ActionTransition> source = new InMemoryEventSource<>(events);

        // When
        SecondaryActionTracker tracker =
                SecondaryActionTracker.builder().application("test").eventSource(source).build();

        // Then
        verifyExpectedState(tracker, expectedFinalState);
        Assert.assertEquals(tracker.getAction(), expectedFinalAction);
    }

    @ToString
    private static final class CountingListener implements ActionTransitionListener {

        private final AtomicInteger count = new AtomicInteger(0);

        public int get() {
            return this.count.get();
        }

        @Override
        public void accept(ActionTracker tracker, ActionTransition transition) {
            count.incrementAndGet();
        }
    }

    @Test(dataProvider = "transitions")
    public void givenCountingListener_whenUsingKafkaSecondaryBackupTracker_thenListenersCalled(
            List<Event<UUID, ActionTransition>> events, ActionState expectedFinalState, String expectedFinalAction) {
        // Given
        CountingListener countingListener = new CountingListener();
        InMemoryEventSource<UUID, ActionTransition> source = new InMemoryEventSource<>(events);

        // When
        try (SecondaryActionTracker tracker = SecondaryActionTracker.builder()
                                                                    .application("test")
                                                                    .eventSource(source)
                                                                    .listeners(List.of(countingListener))
                                                                    .build()) {
            // Then
            verifyExpectedState(tracker, expectedFinalState);
            Assert.assertEquals(tracker.getAction(), expectedFinalAction);
            if (expectedFinalState != ActionState.STARTING) {
                Assert.assertTrue(countingListener.get() > 0);
            } else {
                Assert.assertEquals(countingListener.get(), 0);
            }
        }
    }

    @ToString
    private static final class ErroringListener implements ActionTransitionListener {

        @Override
        public void accept(ActionTracker tracker, ActionTransition transition) {
            throw new RuntimeException("oops!");
        }
    }

    @Test(dataProvider = "transitions")
    public void givenErroringListener_whenUsingKafkaSecondaryBackupTracker_thenExpectedStateStillReached(
            List<Event<UUID, ActionTransition>> events, ActionState expectedFinalState, String expectedFinalAction) {
        // Given
        InMemoryEventSource<UUID, ActionTransition> source = new InMemoryEventSource<>(events);

        // When
        try (SecondaryActionTracker tracker = SecondaryActionTracker.builder()
                                                                    .application("test")
                                                                    .eventSource(source)
                                                                    .listeners(List.of(new ErroringListener()))
                                                                    .build()) {
            // Then
            verifyExpectedState(tracker, expectedFinalState);
            Assert.assertEquals(tracker.getAction(), expectedFinalAction);
        }
    }

    private static void verifyExpectedState(SecondaryActionTracker tracker, ActionState expectedFinalState) {
        Awaitility.await()
                  .pollDelay(Duration.ofMillis(100))
                  .pollInterval(Duration.ofMillis(100))
                  .atMost(Duration.ofSeconds(3))
                  .until(() -> tracker.getState() == expectedFinalState);
    }

    @Test(dataProvider = "transitions")
    public void givenGoodAndBadListener_whenUsingKafkaSecondaryBackupTracker_thenGoodListenersCalled(
            List<Event<UUID, ActionTransition>> events, ActionState expectedFinalState, String expectedFinalAction) {
        // Given
        CountingListener countingListener = new CountingListener();
        InMemoryEventSource<UUID, ActionTransition> source = new InMemoryEventSource<>(events);

        // When
        try (SecondaryActionTracker tracker = SecondaryActionTracker.builder()
                                                                    .application("test")
                                                                    .eventSource(source)
                                                                    .listeners(List.of(new ErroringListener(),
                                                                                       countingListener))
                                                                    .build()) {
            // Then
            verifyExpectedState(tracker, expectedFinalState);
            Assert.assertEquals(tracker.getAction(), expectedFinalAction);
            if (expectedFinalState != ActionState.STARTING) {
                Assert.assertTrue(countingListener.get() > 0);
            } else {
                Assert.assertEquals(countingListener.get(), 0);
            }
        }
    }
}
