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
package io.telicent.smart.cache.backups;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSimpleBackupManager {

    @Test
    public void givenSimpleManager_whenCheckingState_thenStartingUp() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // Then
            BackupManagerState state = manager.getState();

            // When
            Assert.assertEquals(state, BackupManagerState.STARTING);
        }
    }

    @Test
    public void givenSimpleManager_whenStartupComplete_thenReady() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When
            manager.startupComplete();

            // Then
            Assert.assertEquals(manager.getState(), BackupManagerState.READY);
        }
    }

    @Test
    public void givenSimpleManager_whenStartupCompleteTwice_thenReady() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When
            manager.startupComplete();
            manager.startupComplete();

            // Then
            Assert.assertEquals(manager.getState(), BackupManagerState.READY);
        }
    }

    @Test
    public void givenSimpleManager_whenStartingBackup_thenBackingUp_andReturnsToReadyWhenFinished() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {

            // When
            manager.startBackup();

            // Then
            Assert.assertEquals(manager.getState(), BackupManagerState.BACKING_UP);

            // And
            manager.finishBackup();
            Assert.assertEquals(manager.getState(), BackupManagerState.READY);
        }
    }

    @Test
    public void givenSimpleManager_whenStartingRestore_thenRestoring_andReturnsToReadyWhenFinished() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {

            // When
            manager.startRestore();

            // Then
            Assert.assertEquals(manager.getState(), BackupManagerState.RESTORING);

            // And
            manager.finishRestore();
            Assert.assertEquals(manager.getState(), BackupManagerState.READY);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*transition.*not currently permitted")
    public void givenSimpleManager_whenBackingUp_thenStartingRestoreFails() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When
            manager.startBackup();

            // Then
            manager.startRestore();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*transition.*not currently permitted")
    public void givenSimpleManager_whenRestoring_thenStartingBackupFails() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When
            manager.startRestore();

            // Then
            manager.startBackup();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleManager_whenStartingBackupTwice_thenIllegalState() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When and Then
            manager.startBackup();
            manager.startBackup();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleManager_whenStartingRestoreTwice_thenIllegalState() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When and Then
            manager.startRestore();
            manager.startRestore();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleManager_whenStartingBackupAndRestore_thenIllegalState() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When and Then
            manager.startBackup();
            manager.startRestore();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Can't finish.*")
    public void givenSimpleManager_whenStartingBackup_thenFinishingRestoreIsIllegal() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When
            manager.startBackup();

            // Then
            manager.finishRestore();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Can't finish.*")
    public void givenSimpleManager_whenStartingRestore_thenFinishingBackupIsIllegal() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When
            manager.startRestore();

            // Then
            manager.finishBackup();
        }
    }

    @Test
    public void givenSimpleManager_whenClosing_thenNoOp() {
        // Given
        try (BackupManager manager = new SimpleBackupManager()) {
            // When
            manager.close();

            // Then
            Assert.assertEquals(manager.getState(), BackupManagerState.STARTING);
        }
    }

    @Test
    public void givenListenerThatThrowErrors_whenTransitioningStates_thenStateIsStillCorrect() {
        // Given
        try (BackupManager manager = new SimpleBackupManager(List.of((x, y) -> {throw new RuntimeException();}))) {
            // When
            manager.startupComplete();
            manager.startRestore();
            manager.finishRestore();

            // Then
            Assert.assertEquals(manager.getState(), BackupManagerState.READY);
        }
    }

    @Test
    public void givenListenerThatCountsTransitions_whenTransitioningStates_thenStateIsCorrect_andCountIsCorrect() {
        // Given
        AtomicInteger count = new AtomicInteger(0);
        try (BackupManager manager = new SimpleBackupManager(List.of((x, y) -> count.incrementAndGet()))) {
            // When
            manager.startupComplete();
            manager.startRestore();
            manager.finishRestore();

            // Then
            Assert.assertEquals(manager.getState(), BackupManagerState.READY);

            // And
            Assert.assertEquals(count.get(), 3);
        }
    }
}
