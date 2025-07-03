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
package io.telicent.smart.cache.backups.kafka;

import io.telicent.smart.cache.backups.BackupTracker;
import io.telicent.smart.cache.backups.BackupTrackerState;
import io.telicent.smart.cache.backups.BackupTransitionListener;
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

public class TestKafkaSecondaryBackupTracker {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "application cannot.*")
    public void givenNullAppName_whenCreatingBackupTracker_thenIllegalArgument() {
        // Given, When and Then
        KafkaSecondaryBackupTracker.builder()
                                   .application(null)
                                   .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                                   .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "application cannot.*")
    public void givenEmptyAppName_whenCreatingBackupTracker_thenIllegalArgument() {
        // Given, When and Then
        KafkaSecondaryBackupTracker.builder()
                                   .application("")
                                   .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                                   .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "application cannot.*")
    public void givenBlankAppName_whenCreatingBackupTracker_thenIllegalArgument() {
        // Given, When and Then
        KafkaSecondaryBackupTracker.builder()
                                   .application("   ")
                                   .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                                   .build();
    }

    private Event<UUID, BackupTransition> createEvent(String app, BackupTrackerState from, BackupTrackerState to) {
        BackupTransition transition = BackupTransition.builder()
                                                      .id(UUID.randomUUID())
                                                      .application(app)
                                                      .timestamp(Date.from(Instant.now()))
                                                      .from(from)
                                                      .to(to)
                                                      .build();
        return new SimpleEvent<>(Collections.emptyList(), transition.getId(), transition);
    }

    @DataProvider(name = "transitions")
    private Object[][] transitionEvents() {
        return new Object[][] {
                { Collections.emptyList(), BackupTrackerState.STARTING }, {
                List.of(createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY)),
                BackupTrackerState.READY
        }, {
                        List.of(createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.READY, BackupTrackerState.BACKING_UP)),
                        BackupTrackerState.BACKING_UP
                }, {
                        List.of(createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.READY, BackupTrackerState.BACKING_UP),
                                createEvent("test", BackupTrackerState.BACKING_UP, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.READY, BackupTrackerState.RESTORING)),
                        BackupTrackerState.RESTORING
                }, {
                        List.of(createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.RESTORING),
                                createEvent("test", BackupTrackerState.RESTORING, BackupTrackerState.READY)),
                        BackupTrackerState.READY
                },
                // Events for different applications are ignored
                {
                        List.of(createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.BACKING_UP),
                                createEvent("other", BackupTrackerState.BACKING_UP, BackupTrackerState.READY)),
                        BackupTrackerState.BACKING_UP
                }, {
                        List.of(createEvent("other", BackupTrackerState.STARTING, BackupTrackerState.READY)),
                        BackupTrackerState.STARTING
                },
                // In some scenarios we might get sequences of events that aren't usually legal
                // E.g. Primary in a crash restart loop would issue several identical events in a row
                // This one works as always acceptable to transition to READY state, even if already READY
                {
                        List.of(createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY)),
                        BackupTrackerState.READY
                },
                // If primary crashes during an operation it may not send a transition back to ready
                // However again this is fine as going back to the READY state is always permissible
                {
                        List.of(createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.READY, BackupTrackerState.BACKING_UP),
                                createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY)),
                        BackupTrackerState.READY
                }, {
                        List.of(createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.READY, BackupTrackerState.RESTORING),
                                createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY)),
                        BackupTrackerState.READY
                },
                // Worst case scenario is that some transition events never get sent, and we only see later transitions
                // In this case we may not be correctly sync'd and unfortunately there isn't a lot we can do about this
                // However if we start receiving legal transitions later we should then recover
                {
                        List.of(createEvent("test", BackupTrackerState.BACKING_UP, BackupTrackerState.READY)),
                        BackupTrackerState.STARTING
                }, {
                        List.of(createEvent("test", BackupTrackerState.BACKING_UP, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.STARTING, BackupTrackerState.READY),
                                createEvent("test", BackupTrackerState.READY, BackupTrackerState.BACKING_UP)),
                        BackupTrackerState.BACKING_UP
                },
                // A malicious actor could put arbitrary bad transition events onto the topic BUT we only honour legal
                // transitions so applying these should leave us in our default STARTING state
                {
                        List.of(createEvent("test", BackupTrackerState.READY, BackupTrackerState.STARTING),
                                createEvent("test", BackupTrackerState.BACKING_UP, BackupTrackerState.STARTING),
                                createEvent("test", BackupTrackerState.RESTORING, BackupTrackerState.STARTING),
                                createEvent("test", BackupTrackerState.RESTORING, BackupTrackerState.BACKING_UP),
                                createEvent("test", BackupTrackerState.BACKING_UP, BackupTrackerState.RESTORING)),
                        BackupTrackerState.STARTING
                }
        };
    }

    @Test(dataProvider = "transitions")
    public void givenTransitionEvents_whenUsingKafkaSecondaryBackupTracker_thenStateTransitionsApplied(
            List<Event<UUID, BackupTransition>> events, BackupTrackerState expectedFinalState) {
        // Given
        InMemoryEventSource<UUID, BackupTransition> source = new InMemoryEventSource<>(events);

        // When
        KafkaSecondaryBackupTracker tracker =
                KafkaSecondaryBackupTracker.builder().application("test").eventSource(source).build();

        // Then
        verifyExpectedState(tracker, expectedFinalState);
    }

    @ToString
    private static final class CountingListener implements BackupTransitionListener {

        private final AtomicInteger count = new AtomicInteger(0);

        public int get() {
            return this.count.get();
        }

        @Override
        public void accept(BackupTracker tracker, BackupTrackerState backupTrackerState, BackupTrackerState backupTrackerState2) {
            count.incrementAndGet();
        }
    }

    @Test(dataProvider = "transitions")
    public void givenCountingListener_whenUsingKafkaSecondaryBackupTracker_thenListenersCalled(
            List<Event<UUID, BackupTransition>> events, BackupTrackerState expectedFinalState) {
        // Given
        CountingListener countingListener = new CountingListener();
        InMemoryEventSource<UUID, BackupTransition> source = new InMemoryEventSource<>(events);

        // When
        try (KafkaSecondaryBackupTracker tracker =
                     KafkaSecondaryBackupTracker.builder()
                                                .application("test")
                                                .eventSource(source)
                                                .listeners(List.of(countingListener))
                                                .build()) {
            // Then
            verifyExpectedState(tracker, expectedFinalState);
            if (expectedFinalState != BackupTrackerState.STARTING) {
                Assert.assertTrue(countingListener.get() > 0);
            } else {
                Assert.assertEquals(countingListener.get(), 0);
            }
        }
    }

    @ToString
    private static final class ErroringListener implements BackupTransitionListener {

        @Override
        public void accept(BackupTracker tracker, BackupTrackerState backupTrackerState, BackupTrackerState backupTrackerState2) {
            throw new RuntimeException("oops!");
        }
    }

    @Test(dataProvider = "transitions")
    public void givenErroringListener_whenUsingKafkaSecondaryBackupTracker_thenExpectedStateStillReached(
            List<Event<UUID, BackupTransition>> events, BackupTrackerState expectedFinalState) {
        // Given
        InMemoryEventSource<UUID, BackupTransition> source = new InMemoryEventSource<>(events);

        // When
        try (KafkaSecondaryBackupTracker tracker =
                     KafkaSecondaryBackupTracker.builder()
                                                .application("test")
                                                .eventSource(source)
                                                .listeners(List.of(new ErroringListener()))
                                                .build()) {
            // Then
            verifyExpectedState(tracker, expectedFinalState);
        }
    }

    private static void verifyExpectedState(KafkaSecondaryBackupTracker tracker,
                                            BackupTrackerState expectedFinalState) {
        Awaitility.await()
                  .pollDelay(Duration.ofMillis(100))
                  .pollInterval(Duration.ofMillis(100))
                  .atMost(Duration.ofSeconds(3))
                  .until(() -> tracker.getState() == expectedFinalState);
    }

    @Test(dataProvider = "transitions")
    public void givenGoodAndBadListener_whenUsingKafkaSecondaryBackupTracker_thenGoodListenersCalled(
            List<Event<UUID, BackupTransition>> events, BackupTrackerState expectedFinalState) {
        // Given
        CountingListener countingListener = new CountingListener();
        InMemoryEventSource<UUID, BackupTransition> source = new InMemoryEventSource<>(events);

        // When
        try (KafkaSecondaryBackupTracker tracker =
                     KafkaSecondaryBackupTracker.builder()
                                                .application("test")
                                                .eventSource(source)
                                                .listeners(List.of(new ErroringListener(), countingListener))
                                                .build()) {
            // Then
            verifyExpectedState(tracker, expectedFinalState);
            if (expectedFinalState != BackupTrackerState.STARTING) {
                Assert.assertTrue(countingListener.get() > 0);
            } else {
                Assert.assertEquals(countingListener.get(), 0);
            }
        }
    }
}
