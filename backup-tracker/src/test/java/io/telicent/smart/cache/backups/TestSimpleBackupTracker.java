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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSimpleBackupTracker {

    @Test
    public void givenSimpleTracker_whenCheckingState_thenStartingUp() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // Then
            BackupTrackerState state = tracker.getState();

            // When
            Assert.assertEquals(state, BackupTrackerState.STARTING);
        }
    }

    @Test
    public void givenSimpleTracker_whenStartupComplete_thenReady() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When
            tracker.startupComplete();

            // Then
            Assert.assertEquals(tracker.getState(), BackupTrackerState.READY);
        }
    }

    @Test
    public void givenSimpleTracker_whenStartupCompleteTwice_thenReady() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When
            tracker.startupComplete();
            tracker.startupComplete();

            // Then
            Assert.assertEquals(tracker.getState(), BackupTrackerState.READY);
        }
    }

    @Test
    public void givenSimpleTracker_whenStartingBackup_thenBackingUp_andReturnsToReadyWhenFinished() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {

            // When
            tracker.startBackup();

            // Then
            Assert.assertEquals(tracker.getState(), BackupTrackerState.BACKING_UP);

            // And
            tracker.finishBackup();
            Assert.assertEquals(tracker.getState(), BackupTrackerState.READY);
        }
    }

    @Test
    public void givenSimpleTracker_whenStartingRestore_thenRestoring_andReturnsToReadyWhenFinished() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {

            // When
            tracker.startRestore();

            // Then
            Assert.assertEquals(tracker.getState(), BackupTrackerState.RESTORING);

            // And
            tracker.finishRestore();
            Assert.assertEquals(tracker.getState(), BackupTrackerState.READY);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*transition.*not currently permitted")
    public void givenSimpleTracker_whenBackingUp_thenStartingRestoreFails() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When
            tracker.startBackup();

            // Then
            tracker.startRestore();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*transition.*not currently permitted")
    public void givenSimpleTracker_whenRestoring_thenStartingBackupFails() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When
            tracker.startRestore();

            // Then
            tracker.startBackup();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleTracker_whenStartingBackupTwice_thenIllegalState() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When and Then
            tracker.startBackup();
            tracker.startBackup();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleTracker_whenStartingRestoreTwice_thenIllegalState() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When and Then
            tracker.startRestore();
            tracker.startRestore();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleTracker_whenStartingBackupAndRestore_thenIllegalState() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When and Then
            tracker.startBackup();
            tracker.startRestore();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Can't finish.*")
    public void givenSimpleTracker_whenStartingBackup_thenFinishingRestoreIsIllegal() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When
            tracker.startBackup();

            // Then
            tracker.finishRestore();
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Can't finish.*")
    public void givenSimpleTracker_whenStartingRestore_thenFinishingBackupIsIllegal() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When
            tracker.startRestore();

            // Then
            tracker.finishBackup();
        }
    }

    @Test
    public void givenSimpleTracker_whenClosing_thenNoOp() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker()) {
            // When
            tracker.close();

            // Then
            Assert.assertEquals(tracker.getState(), BackupTrackerState.STARTING);
        }
    }

    @Test
    public void givenListenerThatThrowErrors_whenTransitioningStates_thenStateIsStillCorrect() {
        // Given
        try (BackupTracker tracker = new SimpleBackupTracker(List.of((t, x, y) -> {throw new RuntimeException();}))) {
            // When
            tracker.startupComplete();
            tracker.startRestore();
            tracker.finishRestore();

            // Then
            Assert.assertEquals(tracker.getState(), BackupTrackerState.READY);
        }
    }

    @Test
    public void givenListenerThatCountsTransitions_whenTransitioningStates_thenStateIsCorrect_andCountIsCorrect() {
        // Given
        AtomicInteger count = new AtomicInteger(0);
        try (BackupTracker tracker = new SimpleBackupTracker(List.of((t,x, y) -> count.incrementAndGet()))) {
            // When
            tracker.startupComplete();
            tracker.startRestore();
            tracker.finishRestore();

            // Then
            Assert.assertEquals(tracker.getState(), BackupTrackerState.READY);

            // And
            Assert.assertEquals(count.get(), 3);
        }
    }

    @Test
    public void givenNullListener_whenTransitioningStates_thenStateIsCorrect() {
        // Given

        try (BackupTracker tracker = new SimpleBackupTracker(new ArrayList<>())) {
            // When
            tracker.startupComplete();
            tracker.startRestore();
            tracker.finishRestore();
            tracker.startBackup();

            // Then
            Assert.assertEquals(tracker.getState(), BackupTrackerState.BACKING_UP);
        }
    }
}
