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

import io.telicent.smart.cache.actions.tracker.model.ActionState;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestActionTrackerState {

    @DataProvider(name = "legalTransitions")
    private Object[][] legalTransitions() {
        return new Object[][] {
                { ActionState.STARTING, ActionState.STARTING },
                { ActionState.STARTING, ActionState.READY },
                { ActionState.STARTING, ActionState.PROCESSING },
                { ActionState.READY, ActionState.READY },
                { ActionState.READY, ActionState.PROCESSING },
                { ActionState.PROCESSING, ActionState.READY },
        };
    }

    @DataProvider(name = "illegalTransitions")
    private Object[][] illegalTransitions() {
        return new Object[][] {
                { ActionState.READY, ActionState.STARTING },
                { ActionState.PROCESSING, ActionState.PROCESSING },
        };
    }

    @Test(dataProvider = "legalTransitions")
    public void givenLegalTransition_whenValidating_thenValid(ActionState from, ActionState to) {
        // Given and When
        boolean isValid = ActionState.canTransition(from, to);

        // Then
        Assert.assertTrue(isValid);
    }

    @Test(dataProvider = "illegalTransitions")
    public void givenIllegalTransition_whenValidating_thenInvalid(ActionState from, ActionState to) {
        // Given and When
        boolean isValid = ActionState.canTransition(from, to);

        // Then
        Assert.assertFalse(isValid);
    }
}
