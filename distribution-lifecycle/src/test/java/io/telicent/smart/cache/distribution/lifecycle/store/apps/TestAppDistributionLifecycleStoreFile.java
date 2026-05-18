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
package io.telicent.smart.cache.distribution.lifecycle.store.apps;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.events.ApplicationStateUpdate;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleStateTransition;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class TestAppDistributionLifecycleStoreFile {

    public static final String APP_ID = "test";
    private File stateFile;

    @BeforeMethod
    public void setup() throws IOException {
        this.stateFile = Files.createTempFile("app-distro-state", ".json").toFile();
        Assert.assertTrue(this.stateFile.delete());
    }

    private static LifecycleAction action(UUID eventId, String distributionId, DistributionLifecycleState from,
                                          DistributionLifecycleState to) {
        return LifecycleAction.builder()
                              .eventId(eventId)
                              .user("test@test.org")
                              .distributionId(distributionId)
                              .datasetId("dataset")
                              .state(new LifecycleStateTransition(from,
                                                                  to))
                              .build();
    }

    private static LifecycleAcknowledgement ack(UUID eventId, String distributionId, ApplicationState appState) {
        return LifecycleAcknowledgement.builder()
                                       .eventId(eventId)
                                       .distributionId(distributionId)
                                       .state(new ApplicationStateUpdate(
                                               appState))
                                       .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("resource")
    public void givenBadAppId_whenOpeningStore_thenIllegalArgument() {
        // Given, When and Then
        new AppDistributionLifecycleStoreFile(null, this.stateFile);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenBadStateFile_whenOpeningStore_thenIllegalState() throws IOException {
        // Given
        File emptyFile = Files.createTempFile("empty", ".json").toFile();

        // When and Then
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, emptyFile)) {
            Assert.fail("Expected open to fail due to malformed empty state file");
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenStore_whenReusingEventId_thenIllegalState() {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When and Then
            store.add(action(eventId, "distro-z", DistributionLifecycleState.Unregistered,
                             DistributionLifecycleState.Registered));
            store.add(action(eventId, "distro-z", DistributionLifecycleState.Registered,
                             DistributionLifecycleState.Deleted));
        }
    }

    @Test
    public void givenStore_whenAddingDuplicateEvent_thenOk() {
        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = action(eventId, "distro-z", DistributionLifecycleState.Unregistered,
                                        DistributionLifecycleState.Registered);
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            store.add(action);
            store.add(action);

            // Then
            Assert.assertEquals(store.getLifecycleState("distro-z"), DistributionLifecycleState.Registered);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenStore_whenAddingAckForUnknownEvent_thenIllegalState() {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When and Then
            store.add(APP_ID, ack(eventId, "distro", ApplicationState.Requested));
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenStore_whenAddingIllegalTransitionAction_thenIllegalState() {
        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = action(eventId, "distro-z", DistributionLifecycleState.Unregistered,
                                        DistributionLifecycleState.Registered);
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When and Then
            store.add(action);
            store.add(action(UUID.randomUUID(), "distro-z", DistributionLifecycleState.Registered,
                             DistributionLifecycleState.Unregistered));
        }
    }

    @Test
    public void givenStore_whenAddingAckForDifferentApp_thenIgnored() {
        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = action(eventId, "distro-z", DistributionLifecycleState.Unregistered,
                                        DistributionLifecycleState.Registered);
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            store.add(action);
            store.add("other-app", ack(eventId, "distro-z", ApplicationState.Requested));

            // Then
            Assert.assertEquals(store.getLifecycleState("distro-z"), DistributionLifecycleState.Registered);
            Assert.assertNull(store.getApplicationState(eventId, "other-app"));
        }
    }

    @Test
    public void givenEmptyStore_whenClosing_thenStateFileWritten() {
        // Given
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            store.close();
        }

        // Then
        Assert.assertNotEquals(this.stateFile.length(), 0L);
    }

    @Test
    public void givenStore_whenAddingAction_thenListedAsActiveEvents() {
        // Given
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            UUID eventId = UUID.randomUUID();

            // When
            store.add(action(eventId, "distro-1", DistributionLifecycleState.Unregistered,
                             DistributionLifecycleState.Registered));

            // Then
            Assert.assertEquals(store.activeEvents().size(), 1);
            LifecycleAction action = store.activeEvents().getFirst();
            Assert.assertEquals(action.getEventId(), eventId);
            Assert.assertTrue(store.getApplicationStates(eventId).isEmpty());
        }
    }

    @Test
    public void givenStore_whenAddingAction_thenListedAsActiveEvents_andAcknowledgementsToCompletionMarkAsInactive() {
        // Given
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            UUID eventId = UUID.randomUUID();

            // When
            store.add(action(eventId, "distro-x", DistributionLifecycleState.Unregistered,
                             DistributionLifecycleState.Registered));

            // Then
            Assert.assertEquals(store.activeEvents().size(), 1);
            LifecycleAction action = store.activeEvents().getFirst();
            Assert.assertEquals(action.getEventId(), eventId);

            // And
            store.add(APP_ID, ack(eventId, "distro-x", ApplicationState.Requested));
            store.add(APP_ID, ack(eventId, "distro-x", ApplicationState.InProgress));
            store.add(APP_ID, ack(eventId, "distro-x", ApplicationState.Completed));
            Assert.assertTrue(store.activeEvents().isEmpty());
            Assert.assertFalse(store.getApplicationStates(eventId).isEmpty());
            Assert.assertEquals(store.getApplicationStates(eventId).get(APP_ID), ApplicationState.Completed);
        }
    }

    @Test
    public void givenStore_whenAddingSequenceOfActions_thenDistributionLifecycleStateIsAsExpected() {
        // Given
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When and Then
            String distroId = "example";
            Assert.assertEquals(store.getLifecycleState(distroId), DistributionLifecycleState.Unregistered);
            store.add(action(UUID.randomUUID(), distroId, DistributionLifecycleState.Unregistered,
                             DistributionLifecycleState.Registered));
            Assert.assertEquals(store.getLifecycleState(distroId), DistributionLifecycleState.Registered);

            store.add(action(UUID.randomUUID(), distroId, DistributionLifecycleState.Registered,
                             DistributionLifecycleState.Active));
            Assert.assertEquals(store.getLifecycleState(distroId), DistributionLifecycleState.Active);

            store.add(action(UUID.randomUUID(), distroId, DistributionLifecycleState.Active,
                             DistributionLifecycleState.Withdrawn));
            Assert.assertEquals(store.getLifecycleState(distroId), DistributionLifecycleState.Withdrawn);

            store.add(action(UUID.randomUUID(), distroId, DistributionLifecycleState.Withdrawn,
                             DistributionLifecycleState.Deleted));
            Assert.assertEquals(store.getLifecycleState(distroId), DistributionLifecycleState.Deleted);
        }
    }

    @Test
    public void givenStore_whenPopulatingAndClosing_thenReopenedStoreHasPersistedState() {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            store.add(action(eventId, "distro-1", DistributionLifecycleState.Unregistered,
                             DistributionLifecycleState.Registered));
            store.add(APP_ID, ack(eventId, "distro-1", ApplicationState.Requested));
            Assert.assertEquals(store.activeEvents().size(), 1);
            Assert.assertEquals(store.getLifecycleState("distro-1"), DistributionLifecycleState.Registered);
            Assert.assertEquals(store.getApplicationState(eventId, APP_ID), ApplicationState.Requested);
        }

        // Then
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            Assert.assertEquals(store.activeEvents().size(), 1);
            Assert.assertEquals(store.getLifecycleState("distro-1"), DistributionLifecycleState.Registered);
            Assert.assertEquals(store.getApplicationState(eventId, APP_ID), ApplicationState.Requested);
        }
    }
}
