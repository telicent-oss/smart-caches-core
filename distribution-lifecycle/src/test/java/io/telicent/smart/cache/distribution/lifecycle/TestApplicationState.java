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
package io.telicent.smart.cache.distribution.lifecycle;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestApplicationState {

    @DataProvider
    public Object[][] validTransitions() {
        // The manually curated list of legal transitions, if you change ApplicationState#canTransition() in
        // any way then this MUST be appropriately updated
        return new Object[][] {
                { ApplicationState.Requested, ApplicationState.Requested },
                { ApplicationState.Requested, ApplicationState.InProgress },
                { ApplicationState.Requested, ApplicationState.Completed },
                { ApplicationState.Requested, ApplicationState.Failed },
                { ApplicationState.InProgress, ApplicationState.InProgress },
                { ApplicationState.InProgress, ApplicationState.Completed },
                { ApplicationState.InProgress, ApplicationState.Failed },
                { ApplicationState.Completed, ApplicationState.Completed },
                { ApplicationState.Failed, ApplicationState.InProgress},
                { ApplicationState.Failed, ApplicationState.Completed},
                { ApplicationState.Failed, ApplicationState.Failed}
        };
    }

    @DataProvider
    public Object[][] invalidTransitions() {
        // Calculate this as the negation of the validTransitions()
        List<Object[]> legalTransitions = Arrays.stream(validTransitions()).toList();
        List<Object[]> illegalTransitions = new ArrayList<>();
        for (ApplicationState source : ApplicationState.values()) {
            for (ApplicationState target : ApplicationState.values()) {
                if (legalTransitions.stream().anyMatch(t -> t[0] == source && t[1] == target)) {
                    continue;
                }
                illegalTransitions.add(new Object[] { source, target });
            }
        }
        return illegalTransitions.toArray(new Object[0][]);
    }

    @Test(dataProvider = "validTransitions")
    public void givenValidTransition_whenCheckingLegality_thenOk(ApplicationState source,
                                                                 ApplicationState target) {
        // Given and When
        boolean legal = ApplicationState.canTransition(source, target);

        // Then
        Assert.assertTrue(legal);
    }

    @Test(dataProvider = "validTransitions")
    public void givenValidTransition_whenCheckingLegalityOnSourceState_thenOk(ApplicationState source,
                                                                 ApplicationState target) {
        // Given and When
        boolean legal = source.canTransition(target);

        // Then
        Assert.assertTrue(legal);
    }

    @Test(dataProvider = "invalidTransitions")
    public void givenInvalidTransition_whenCheckingLegality_thenFails(ApplicationState source,
                                                                      ApplicationState target) {
        // Given and When
        boolean legal = ApplicationState.canTransition(source, target);

        // Then
        Assert.assertFalse(legal);
    }

    @Test(dataProvider = "invalidTransitions")
    public void givenInvalidTransition_whenCheckingLegalityOnSourceState_thenFails(ApplicationState source,
                                                                      ApplicationState target) {
        // Given and When
        boolean legal = source.canTransition(target);

        // Then
        Assert.assertFalse(legal);
    }

    @Test
    public void givenNullTarget_whenCheckingLegality_thenFails() {
        // Given
        for (ApplicationState state : ApplicationState.values()) {
            // When
            boolean legal = state.canTransition(null);

            // Then
            Assert.assertFalse(legal);
        }
    }

    @Test
    public void givenNullSource_whenCheckingLegality_thenFails() {
        // Given
        for (ApplicationState state : ApplicationState.values()) {
            // When
            boolean legal = ApplicationState.canTransition(null, state);

            // Then
            Assert.assertFalse(legal);
        }
    }

    @Test
    public void givenNullSourceAndTarget_whenCheckingLegality_thenFails() {
        // Given and When
        boolean legal = ApplicationState.canTransition(null, null);

        // Then
        Assert.assertFalse(legal);
    }
}
