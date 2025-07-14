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
import io.telicent.smart.cache.actions.tracker.model.ActionState;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestSimpleActionTracker {

    @Test
    public void givenSimpleTracker_whenCheckingState_thenStartingUp() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // Then
            ActionState state = tracker.getState();

            // When
            Assert.assertEquals(state, ActionState.STARTING);
        }
    }

    @Test
    public void givenSimpleTracker_whenStartupComplete_thenReady() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When
            tracker.startupComplete();

            // Then
            Assert.assertEquals(tracker.getState(), ActionState.READY);
        }
    }

    @Test
    public void givenSimpleTracker_whenStartupCompleteTwice_thenReady() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When
            tracker.startupComplete();
            tracker.startupComplete();

            // Then
            Assert.assertEquals(tracker.getState(), ActionState.READY);
        }
    }

    @Test
    public void givenSimpleTracker_whenStartingBackup_thenBackingUp_andReturnsToReadyWhenFinished() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {

            // When
            tracker.start("backup");

            // Then
            Assert.assertEquals(tracker.getState(), ActionState.PROCESSING);
            Assert.assertEquals(tracker.getAction(), "backup");

            // And
            tracker.finish("backup");
            Assert.assertEquals(tracker.getState(), ActionState.READY);
            Assert.assertNull(tracker.getAction());
        }
    }

    @Test
    public void givenSimpleTracker_whenStartingRestore_thenRestoring_andReturnsToReadyWhenFinished() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {

            // When
            tracker.start("restore");

            // Then
            Assert.assertEquals(tracker.getState(), ActionState.PROCESSING);
            Assert.assertEquals(tracker.getAction(), "restore");

            // And
            tracker.finish("restore");
            Assert.assertEquals(tracker.getState(), ActionState.READY);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*already in progress")
    public void givenSimpleTracker_whenBackingUp_thenStartingRestoreFails() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When
            tracker.start("backup");

            // Then
            tracker.start("restore");
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*already in progress")
    public void givenSimpleTracker_whenRestoring_thenStartingBackupFails() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When
            tracker.start("restore");

            // Then
            tracker.start("backup");
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleTracker_whenStartingBackupTwice_thenIllegalState() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When and Then
            tracker.start("backup");
            tracker.start("backup");
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleTracker_whenStartingRestoreTwice_thenIllegalState() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When and Then
            tracker.start("restore");
            tracker.start("restore");
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleTracker_whenStartingBackupAndRestore_thenIllegalState() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When and Then
            tracker.start("backup");
            tracker.start("restore");
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Cannot finish.*in progress")
    public void givenSimpleTracker_whenStartingBackup_thenFinishingRestoreIsIllegal() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When
            tracker.start("backup");

            // Then
            tracker.finish("restore");
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Cannot finish.*in progress")
    public void givenSimpleTracker_whenStartingRestore_thenFinishingBackupIsIllegal() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When
            tracker.start("restore");

            // Then
            tracker.finish("backup");
        }
    }

    @Test
    public void givenSimpleTracker_whenClosing_thenNoOp() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test")) {
            // When
            tracker.close();

            // Then
            Assert.assertEquals(tracker.getState(), ActionState.STARTING);
        }
    }

    @Test
    public void givenListenerThatThrowErrors_whenTransitioningStates_thenStateIsStillCorrect() {
        // Given
        try (ActionTracker tracker = new SimpleActionTracker("test", List.of((t, x) -> {throw new RuntimeException();}))) {
            // When
            tracker.startupComplete();
            tracker.start("restore");
            tracker.finish("restore");

            // Then
            Assert.assertEquals(tracker.getState(), ActionState.READY);
        }
    }

    @Test
    public void givenListenerThatCountsTransitions_whenTransitioningStates_thenStateIsCorrect_andCountIsCorrect() {
        // Given
        AtomicInteger count = new AtomicInteger(0);
        try (ActionTracker tracker = new SimpleActionTracker("test", List.of((t, x) -> count.incrementAndGet()))) {
            // When
            tracker.startupComplete();
            tracker.start("restore");
            tracker.finish("restore");

            // Then
            Assert.assertEquals(tracker.getState(), ActionState.READY);

            // And
            Assert.assertEquals(count.get(), 3);
        }
    }

    @Test
    public void givenNullListener_whenTransitioningStates_thenStateIsCorrect() {
        // Given
        ArrayList<ActionTransitionListener> listeners = new ArrayList<>();
        listeners.add(null);
        try (ActionTracker tracker = new SimpleActionTracker("test", listeners)) {
            // When
            tracker.startupComplete();
            tracker.start("restore");
            tracker.finish("restore");
            tracker.start("backup");

            // Then
            Assert.assertEquals(tracker.getState(), ActionState.PROCESSING);
            Assert.assertEquals(tracker.getAction(), "backup");
        }
    }
}
