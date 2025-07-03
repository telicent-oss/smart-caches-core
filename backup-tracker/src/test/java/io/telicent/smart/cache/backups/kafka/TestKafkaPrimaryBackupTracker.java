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
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.sources.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class TestKafkaPrimaryBackupTracker {

    @Test
    public void givenKafkaPrimaryBackupTracker_whenStartingUp_thenTransitionSentToSink() {
        // Given
        try (CollectorSink<Event<UUID, BackupTransition>> sink = CollectorSink.of()) {
            try (BackupTracker tracker = new KafkaPrimaryBackupTracker("test", sink)) {

                // When
                tracker.startupComplete();

                // Then
                Assert.assertFalse(sink.get().isEmpty());
                Assert.assertEquals(sink.get().size(), 1);
                Event<UUID, BackupTransition> event = sink.get().get(0);
                verifyTransitionEvent(event, BackupTrackerState.STARTING, BackupTrackerState.READY);
            }
        }
    }

    private static void verifyTransitionEvent(Event<UUID, BackupTransition> event, BackupTrackerState expectedFrom,
                                              BackupTrackerState expectedTo) {
        Assert.assertNotNull(event);
        BackupTransition transition = event.value();
        Assert.assertNotNull(transition);
        Assert.assertEquals(event.key(), transition.getId());
        Assert.assertEquals(transition.getFrom(), expectedFrom);
        Assert.assertEquals(transition.getTo(), expectedTo);
    }

    @Test
    public void givenKafkaPrimaryBackupTracker_whenMakingTransitions_thenTransitionSentToSink() {
        // Given
        try (CollectorSink<Event<UUID, BackupTransition>> sink = CollectorSink.of()) {
            try (BackupTracker tracker = new KafkaPrimaryBackupTracker("test", sink)) {

                // When
                tracker.startupComplete();
                tracker.startBackup();
                tracker.finishBackup();
                tracker.startRestore();
                tracker.finishRestore();

                // Then
                Assert.assertFalse(sink.get().isEmpty());
                Assert.assertEquals(sink.get().size(), 5);
                Event<UUID, BackupTransition> event = sink.get().get(0);
                verifyTransitionEvent(event, BackupTrackerState.STARTING, BackupTrackerState.READY);
                event = sink.get().get(1);
                verifyTransitionEvent(event, BackupTrackerState.READY, BackupTrackerState.BACKING_UP);
                event = sink.get().get(2);
                verifyTransitionEvent(event, BackupTrackerState.BACKING_UP, BackupTrackerState.READY);
                event = sink.get().get(3);
                verifyTransitionEvent(event, BackupTrackerState.READY, BackupTrackerState.RESTORING);
                event = sink.get().get(4);
                verifyTransitionEvent(event, BackupTrackerState.RESTORING, BackupTrackerState.READY);
            }
        }
    }

    @Test
    public void givenKafkaPrimaryBackupTracker_whenMakingIllegalTransition_thenNothingSentToSink() {
        // Given
        try (CollectorSink<Event<UUID, BackupTransition>> sink = CollectorSink.of()) {
            try (BackupTracker tracker = new KafkaPrimaryBackupTracker("test", sink)) {

                // When
                Assert.assertThrows(IllegalStateException.class, tracker::finishBackup);

                // Then
                Assert.assertTrue(sink.get().isEmpty());
            }
        }
    }
}
