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
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static io.telicent.smart.cache.distribution.lifecycle.store.apps.TestAppDistributionLifecycleStoreFile.ack;
import static io.telicent.smart.cache.distribution.lifecycle.store.apps.TestAppDistributionLifecycleStoreFile.action;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestAppDistributionLifecycleStoreFileOps {

    public static final String APP_ID = "test";
    private File stateFile;

    @BeforeMethod
    public void setup() throws IOException {
        this.stateFile = Files.createTempFile("app-distro-state", ".json").toFile();
        Assert.assertTrue(this.stateFile.delete());
    }

    private static void verify(AppDistributionLifecycleStoreFile store, UUID eventId) {
        Assert.assertEquals(store.activeEvents().size(), 1);
        Assert.assertEquals(store.getLifecycleState("distro-1"), DistributionLifecycleState.Registered);
        Assert.assertEquals(store.getApplicationState(eventId, APP_ID), ApplicationState.Requested);
    }

    private static void populate(AppDistributionLifecycleStoreFile store, UUID eventId) {
        store.add(action(eventId, "distro-1", DistributionLifecycleState.Unregistered,
                         DistributionLifecycleState.Registered));
        store.add(APP_ID, ack(eventId, "distro-1", ApplicationState.Requested));
        verify(store, eventId);
    }

    private static void corrupt(File stateFile) throws IOException {
        try (FileWriter writer = new FileWriter(stateFile)) {
            writer.write("junk");
        }
    }

    @Test
    public void givenStore_whenPopulatingAndClosing_thenReopenedStoreHasPersistedState() {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            populate(store, eventId);
        }

        // Then
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            verify(store, eventId);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenStore_whenPopulatingAndMakingStateFileWriteFail_thenClosingStoreFails() {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            populate(store, eventId);
            try (MockedStatic<Files> mock = mockStatic(Files.class)) {
                mock.when(() -> Files.move(any(), any(), any())).thenThrow(new IOException("Bad write"));

                // Then
                store.close();
                Assert.fail("Attempting to close() with a non-writeable state file should fail");
            }
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*is from a different application.*")
    public void givenStore_whenPopulatingAndClosing_thenReopeningWithDifferentAppIdFails() {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            populate(store, eventId);
        }

        // Then
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile("other", this.stateFile)) {
            verify(store, eventId);
        }
    }

    @Test
    public void givenStore_whenPopulatingAndClosingAndRenamingToTempFile_thenReopenedStoreHasRecoveredPersistedState() throws
            IOException {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            populate(store, eventId);
        }
        Files.copy(this.stateFile.toPath(),
                   new File(this.stateFile + AppDistributionLifecycleStoreFile.TMP_EXTENSION).toPath());
        corrupt(this.stateFile);

        // Then
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            verify(store, eventId);
        }
    }

    @Test
    public void givenStore_whenPopulatingAndClosingAndRenamingToBakFile_thenReopenedStoreHasRecoveredPersistedState() throws
            IOException {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            populate(store, eventId);
        }
        Files.copy(this.stateFile.toPath(),
                   new File(this.stateFile + AppDistributionLifecycleStoreFile.BAK_EXTENSION).toPath());
        corrupt(this.stateFile);

        // Then
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            verify(store, eventId);
        }
    }

    @Test
    public void givenStore_whenPopulatingAndClosingAndRenamingToBakFileWithCorruptTempFile_thenReopenedStoreHasRecoveredPersistedState() throws
            IOException {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            populate(store, eventId);
        }
        Files.copy(this.stateFile.toPath(),
                   new File(this.stateFile + AppDistributionLifecycleStoreFile.BAK_EXTENSION).toPath());
        corrupt(this.stateFile);
        corrupt(new File(this.stateFile + AppDistributionLifecycleStoreFile.TMP_EXTENSION));

        // Then
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            verify(store, eventId);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenStore_whenPopulatingAndClosingAndCorruptingAllFiles_thenReopeningStoreFails() throws
            IOException {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            populate(store, eventId);
        }
        corrupt(this.stateFile);
        corrupt(new File(this.stateFile + AppDistributionLifecycleStoreFile.TMP_EXTENSION));
        corrupt(new File(this.stateFile + AppDistributionLifecycleStoreFile.BAK_EXTENSION));

        // Then
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            Assert.fail("Should have failed to open due to corrupted state files");
        }
    }

    @Test
    public void givenStore_whenPopulatingAndClosingAndLeavingOnlyCorruptTempFiles_thenReopenedStoreHasNoState() throws
            IOException {
        // Given
        UUID eventId = UUID.randomUUID();
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            // When
            populate(store, eventId);
        }
        this.stateFile.delete();
        corrupt(new File(this.stateFile + AppDistributionLifecycleStoreFile.TMP_EXTENSION));
        corrupt(new File(this.stateFile + AppDistributionLifecycleStoreFile.BAK_EXTENSION));

        // Then
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, this.stateFile)) {
            Assert.assertTrue(store.getLifecycleStates().isEmpty());
        }
    }

    @Test
    public void givenStore_whenFileHasNoParent_thenOpenedOk() {
        // Given
        new File("state.json").delete();
        File stateFile = mock(File.class);
        when(stateFile.getPath()).thenReturn("state.json");
        when(stateFile.toPath()).thenReturn(Path.of("state.json"));
        when(stateFile.exists()).thenReturn(false);
        when(stateFile.getParentFile()).thenReturn(null);

        // When
        try (AppDistributionLifecycleStoreFile store = new AppDistributionLifecycleStoreFile(APP_ID, stateFile)) {
            // Then
            Assert.assertTrue(store.activeEvents().isEmpty());
        } finally {
            new File("state.json").delete();
        }
    }
}
