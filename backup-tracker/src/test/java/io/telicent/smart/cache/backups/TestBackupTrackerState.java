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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestBackupTrackerState {

    @DataProvider(name = "legalTransitions")
    private Object[][] legalTransitions() {
        return new Object[][] {
                { BackupTrackerState.STARTING, BackupTrackerState.STARTING },
                { BackupTrackerState.STARTING, BackupTrackerState.READY },
                { BackupTrackerState.STARTING, BackupTrackerState.BACKING_UP },
                { BackupTrackerState.STARTING, BackupTrackerState.RESTORING },
                { BackupTrackerState.READY, BackupTrackerState.READY },
                { BackupTrackerState.READY, BackupTrackerState.BACKING_UP },
                { BackupTrackerState.READY, BackupTrackerState.RESTORING },
                { BackupTrackerState.BACKING_UP, BackupTrackerState.READY },
                { BackupTrackerState.RESTORING, BackupTrackerState.READY },
        };
    }

    @DataProvider(name = "illegalTransitions")
    private Object[][] illegalTransitions() {
        return new Object[][] {
                { BackupTrackerState.READY, BackupTrackerState.STARTING },
                { BackupTrackerState.BACKING_UP, BackupTrackerState.RESTORING },
                { BackupTrackerState.RESTORING, BackupTrackerState.BACKING_UP}
        };
    }

    @Test(dataProvider = "legalTransitions")
    public void givenLegalTransition_whenValidating_thenValid(BackupTrackerState from, BackupTrackerState to) {
        // Given and When
        boolean isValid = BackupTrackerState.canTransition(from, to);

        // Then
        Assert.assertTrue(isValid);
    }

    @Test(dataProvider = "illegalTransitions")
    public void givenIllegalTransition_whenValidating_thenInvalid(BackupTrackerState from, BackupTrackerState to) {
        // Given and When
        boolean isValid = BackupTrackerState.canTransition(from, to);

        // Then
        Assert.assertFalse(isValid);
    }
}
