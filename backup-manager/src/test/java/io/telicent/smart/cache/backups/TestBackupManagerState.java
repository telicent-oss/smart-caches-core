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

public class TestBackupManagerState {

    @DataProvider(name = "legalTransitions")
    private Object[][] legalTransitions() {
        return new Object[][] {
                { BackupManagerState.STARTING, BackupManagerState.STARTING },
                { BackupManagerState.STARTING, BackupManagerState.READY },
                { BackupManagerState.STARTING, BackupManagerState.BACKING_UP },
                { BackupManagerState.STARTING, BackupManagerState.RESTORING },
                { BackupManagerState.READY, BackupManagerState.READY },
                { BackupManagerState.READY, BackupManagerState.BACKING_UP },
                { BackupManagerState.READY, BackupManagerState.RESTORING },
                { BackupManagerState.BACKING_UP, BackupManagerState.READY },
                { BackupManagerState.RESTORING, BackupManagerState.READY },
        };
    }

    @DataProvider(name = "illegalTransitions")
    private Object[][] illegalTransitions() {
        return new Object[][] {
                { BackupManagerState.READY, BackupManagerState.STARTING },
                { BackupManagerState.BACKING_UP, BackupManagerState.RESTORING },
                { BackupManagerState.RESTORING, BackupManagerState.BACKING_UP}
        };
    }

    @Test(dataProvider = "legalTransitions")
    public void givenLegalTransition_whenValidating_thenValid(BackupManagerState from, BackupManagerState to) {
        // Given and When
        boolean isValid = BackupManagerState.canTransition(from, to);

        // Then
        Assert.assertTrue(isValid);
    }

    @Test(dataProvider = "illegalTransitions")
    public void givenIllegalTransition_whenValidating_thenInvalid(BackupManagerState from, BackupManagerState to) {
        // Given and When
        boolean isValid = BackupManagerState.canTransition(from, to);

        // Then
        Assert.assertFalse(isValid);
    }
}
