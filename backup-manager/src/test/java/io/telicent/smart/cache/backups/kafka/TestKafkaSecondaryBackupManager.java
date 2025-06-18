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

import io.telicent.smart.cache.backups.BackupManagerState;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.Getter;
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
import java.util.function.BiConsumer;

public class TestKafkaSecondaryBackupManager {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "application cannot.*")
    public void givenNullAppName_whenCreatingBackupManager_thenIllegalArgument() {
        // Given, When and Then
        KafkaSecondaryBackupManager.builder()
                                   .application(null)
                                   .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                                   .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "application cannot.*")
    public void givenEmptyAppName_whenCreatingBackupManager_thenIllegalArgument() {
        // Given, When and Then
        KafkaSecondaryBackupManager.builder()
                                   .application("")
                                   .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                                   .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "application cannot.*")
    public void givenBlankAppName_whenCreatingBackupManager_thenIllegalArgument() {
        // Given, When and Then
        KafkaSecondaryBackupManager.builder()
                                   .application("   ")
                                   .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                                   .build();
    }

    private Event<UUID, BackupTransition> createEvent(String app, BackupManagerState from, BackupManagerState to) {
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
                { Collections.emptyList(), BackupManagerState.STARTING }, {
                List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY)),
                BackupManagerState.READY
        }, {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.READY, BackupManagerState.BACKING_UP)),
                        BackupManagerState.BACKING_UP
                }, {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.READY, BackupManagerState.BACKING_UP),
                                createEvent("test", BackupManagerState.BACKING_UP, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.READY, BackupManagerState.RESTORING)),
                        BackupManagerState.RESTORING
                }, {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.RESTORING),
                                createEvent("test", BackupManagerState.RESTORING, BackupManagerState.READY)),
                        BackupManagerState.READY
                },
                // Events for different applications are ignored
                {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.BACKING_UP),
                                createEvent("other", BackupManagerState.BACKING_UP, BackupManagerState.READY)),
                        BackupManagerState.BACKING_UP
                }, {
                        List.of(createEvent("other", BackupManagerState.STARTING, BackupManagerState.READY)),
                        BackupManagerState.STARTING
                },
                // In some scenarios we might get sequences of events that aren't usually legal
                // E.g. Primary in a crash restart loop would issue several identical events in a row
                // This one works as always acceptable to transition to READY state, even if already READY
                {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY)),
                        BackupManagerState.READY
                },
                // If primary crashes during an operation it may not send a transition back to ready
                // However again this is fine as going back to the READY state is always permissible
                {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.READY, BackupManagerState.BACKING_UP),
                                createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY)),
                        BackupManagerState.READY
                }, {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.READY, BackupManagerState.RESTORING),
                                createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY)),
                        BackupManagerState.READY
                },
                // Worst case scenario is that some transition events never get sent, and we only see later transitions
                // In this case we may not be correctly sync'd and unfortunately there isn't a lot we can do about this
                // However if we start receiving legal transitions later we should then recover
                {
                        List.of(createEvent("test", BackupManagerState.BACKING_UP, BackupManagerState.READY)),
                        BackupManagerState.STARTING
                }, {
                        List.of(createEvent("test", BackupManagerState.BACKING_UP, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.READY, BackupManagerState.BACKING_UP)),
                        BackupManagerState.BACKING_UP
                },
                // A malicious actor could put arbitrary bad transition events onto the topic BUT we only honour legal
                // transitions so applying these should leave us in our default STARTING state
                {
                        List.of(createEvent("test", BackupManagerState.READY, BackupManagerState.STARTING),
                                createEvent("test", BackupManagerState.BACKING_UP, BackupManagerState.STARTING),
                                createEvent("test", BackupManagerState.RESTORING, BackupManagerState.STARTING),
                                createEvent("test", BackupManagerState.RESTORING, BackupManagerState.BACKING_UP),
                                createEvent("test", BackupManagerState.BACKING_UP, BackupManagerState.RESTORING)),
                        BackupManagerState.STARTING
                }
        };
    }

    @Test(dataProvider = "transitions")
    public void givenTransitionEvents_whenUsingKafkaSecondaryBackupManager_thenStateTransitionsApplied(
            List<Event<UUID, BackupTransition>> events, BackupManagerState expectedFinalState) {
        // Given
        InMemoryEventSource<UUID, BackupTransition> source = new InMemoryEventSource<>(events);

        // When
        KafkaSecondaryBackupManager manager =
                KafkaSecondaryBackupManager.builder().application("test").eventSource(source).build();

        // Then
        verifyExpectedState(manager, expectedFinalState);
    }

    @ToString
    private static final class CountingListener implements BiConsumer<BackupManagerState, BackupManagerState> {

        private final AtomicInteger count = new AtomicInteger(0);

        public int get() {
            return this.count.get();
        }

        @Override
        public void accept(BackupManagerState backupManagerState, BackupManagerState backupManagerState2) {
            count.incrementAndGet();
        }
    }

    @Test(dataProvider = "transitions")
    public void givenCountingListener_whenUsingKafkaSecondaryBackupManager_thenListenersCalled(
            List<Event<UUID, BackupTransition>> events, BackupManagerState expectedFinalState) {
        // Given
        CountingListener countingListener = new CountingListener();
        InMemoryEventSource<UUID, BackupTransition> source = new InMemoryEventSource<>(events);

        // When
        try (KafkaSecondaryBackupManager manager =
                     KafkaSecondaryBackupManager.builder()
                                                .application("test")
                                                .eventSource(source)
                                                .listeners(List.of(countingListener))
                                                .build()) {
            // Then
            verifyExpectedState(manager, expectedFinalState);
            if (expectedFinalState != BackupManagerState.STARTING) {
                Assert.assertTrue(countingListener.get() > 0);
            } else {
                Assert.assertEquals(countingListener.get(), 0);
            }
        }
    }

    @ToString
    private static final class ErroringListener implements BiConsumer<BackupManagerState, BackupManagerState> {

        @Override
        public void accept(BackupManagerState backupManagerState, BackupManagerState backupManagerState2) {
            throw new RuntimeException("oops!");
        }
    }

    @Test(dataProvider = "transitions")
    public void givenErroringListener_whenUsingKafkaSecondaryBackupManager_thenExpectedStateStillReached(
            List<Event<UUID, BackupTransition>> events, BackupManagerState expectedFinalState) {
        // Given
        InMemoryEventSource<UUID, BackupTransition> source = new InMemoryEventSource<>(events);

        // When
        try (KafkaSecondaryBackupManager manager =
                     KafkaSecondaryBackupManager.builder()
                                                .application("test")
                                                .eventSource(source)
                                                .listeners(List.of(new ErroringListener()))
                                                .build()) {
            // Then
            verifyExpectedState(manager, expectedFinalState);
        }
    }

    private static void verifyExpectedState(KafkaSecondaryBackupManager manager,
                                            BackupManagerState expectedFinalState) {
        Awaitility.await()
                  .pollDelay(Duration.ofMillis(100))
                  .pollInterval(Duration.ofMillis(100))
                  .atMost(Duration.ofSeconds(3))
                  .until(() -> manager.getState() == expectedFinalState);
    }

    @Test(dataProvider = "transitions")
    public void givenGoodAndBadListener_whenUsingKafkaSecondaryBackupManager_thenGoodListenersCalled(
            List<Event<UUID, BackupTransition>> events, BackupManagerState expectedFinalState) {
        // Given
        CountingListener countingListener = new CountingListener();
        InMemoryEventSource<UUID, BackupTransition> source = new InMemoryEventSource<>(events);

        // When
        try (KafkaSecondaryBackupManager manager =
                     KafkaSecondaryBackupManager.builder()
                                                .application("test")
                                                .eventSource(source)
                                                .listeners(List.of(new ErroringListener(), countingListener))
                                                .build()) {
            // Then
            verifyExpectedState(manager, expectedFinalState);
            if (expectedFinalState != BackupManagerState.STARTING) {
                Assert.assertTrue(countingListener.get() > 0);
            } else {
                Assert.assertEquals(countingListener.get(), 0);
            }
        }
    }
}
