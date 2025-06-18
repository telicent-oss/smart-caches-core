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

import io.telicent.smart.cache.backups.BackupManager;
import io.telicent.smart.cache.backups.BackupManagerState;
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.sources.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class TestKafkaPrimaryBackupManager {

    @Test
    public void givenKafkaPrimaryBackupManager_whenStartingUp_thenTransitionSentToSink() {
        // Given
        try (CollectorSink<Event<UUID, BackupTransition>> sink = CollectorSink.of()) {
            try (BackupManager manager = new KafkaPrimaryBackupManager("test", sink)) {

                // When
                manager.startupComplete();

                // Then
                Assert.assertFalse(sink.get().isEmpty());
                Assert.assertEquals(sink.get().size(), 1);
                Event<UUID, BackupTransition> event = sink.get().get(0);
                verifyTransitionEvent(event, BackupManagerState.STARTING, BackupManagerState.READY);
            }
        }
    }

    private static void verifyTransitionEvent(Event<UUID, BackupTransition> event, BackupManagerState expectedFrom,
                                              BackupManagerState expectedTo) {
        Assert.assertNotNull(event);
        BackupTransition transition = event.value();
        Assert.assertNotNull(transition);
        Assert.assertEquals(event.key(), transition.getId());
        Assert.assertEquals(transition.getFrom(), expectedFrom);
        Assert.assertEquals(transition.getTo(), expectedTo);
    }

    @Test
    public void givenKafkaPrimaryBackupManager_whenMakingTransitions_thenTransitionSentToSink() {
        // Given
        try (CollectorSink<Event<UUID, BackupTransition>> sink = CollectorSink.of()) {
            try (BackupManager manager = new KafkaPrimaryBackupManager("test", sink)) {

                // When
                manager.startupComplete();
                manager.startBackup();
                manager.finishBackup();
                manager.startRestore();
                manager.finishRestore();

                // Then
                Assert.assertFalse(sink.get().isEmpty());
                Assert.assertEquals(sink.get().size(), 5);
                Event<UUID, BackupTransition> event = sink.get().get(0);
                verifyTransitionEvent(event, BackupManagerState.STARTING, BackupManagerState.READY);
                event = sink.get().get(1);
                verifyTransitionEvent(event, BackupManagerState.READY, BackupManagerState.BACKING_UP);
                event = sink.get().get(2);
                verifyTransitionEvent(event, BackupManagerState.BACKING_UP, BackupManagerState.READY);
                event = sink.get().get(3);
                verifyTransitionEvent(event, BackupManagerState.READY, BackupManagerState.RESTORING);
                event = sink.get().get(4);
                verifyTransitionEvent(event, BackupManagerState.RESTORING, BackupManagerState.READY);
            }
        }
    }

    @Test
    public void givenKafkaPrimaryBackupManager_whenMakingIllegalTransition_thenNothingSentToSink() {
        // Given
        try (CollectorSink<Event<UUID, BackupTransition>> sink = CollectorSink.of()) {
            try (BackupManager manager = new KafkaPrimaryBackupManager("test", sink)) {

                // When
                Assert.assertThrows(IllegalStateException.class, manager::finishBackup);

                // Then
                Assert.assertTrue(sink.get().isEmpty());
            }
        }
    }
}
