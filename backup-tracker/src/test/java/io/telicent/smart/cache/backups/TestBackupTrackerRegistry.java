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

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestBackupTrackerRegistry {

    @BeforeClass
    public void setup() {
        BackupTrackerRegistry.reset();
    }

    @AfterMethod
    public void cleanup() {
        BackupTrackerRegistry.reset();
    }

    @Test
    public void givenNothingRegistered_whenRetrievingInstance_thenNull() {
        // Given and When
        BackupTracker tracker = BackupTrackerRegistry.getInstance();

        // Then
        Assert.assertNull(tracker);
    }

    @Test
    public void givenSomethingRegistered_whenRetrievingInstance_thenSame() {
        // Given
        BackupTracker tracker = new SimpleBackupTracker();
        BackupTrackerRegistry.setInstance(tracker);

        // When
        BackupTracker retrieved = BackupTrackerRegistry.getInstance();

        // Then
        Assert.assertSame(tracker, retrieved);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSomethingRegistered_whenRegisteringAgain_thenIllegalState() {
        // Given
        BackupTracker tracker = new SimpleBackupTracker();
        BackupTrackerRegistry.setInstance(tracker);

        // When and Then
        BackupTrackerRegistry.setInstance(tracker);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullTracker_whenRegistering_thenNPE() {
        // Given, When and Then
        BackupTrackerRegistry.setInstance(null);
    }
}
