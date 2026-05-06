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

public class TestDistributionLifecycleState {

    @DataProvider
    public Object[][] validTransitions() {
        // The manually curated list of legal transitions, if you change DistributionLifecycleState#canTransition() in
        // any way then this MUST be appropriately updated
        return new Object[][] {
                { DistributionLifecycleState.Unregistered, DistributionLifecycleState.Registered },
                { DistributionLifecycleState.Registered, DistributionLifecycleState.Active },
                { DistributionLifecycleState.Registered, DistributionLifecycleState.Deleted },
                { DistributionLifecycleState.Active, DistributionLifecycleState.Withdrawn },
                { DistributionLifecycleState.Active, DistributionLifecycleState.Deleted },
                { DistributionLifecycleState.Withdrawn, DistributionLifecycleState.Active },
                { DistributionLifecycleState.Withdrawn, DistributionLifecycleState.Deleted }
        };
    }

    @DataProvider
    public Object[][] invalidTransitions() {
        // Calculate this as the negation of the validTransitions()
        List<Object[]> legalTransitions = Arrays.stream(validTransitions()).toList();
        List<Object[]> illegalTransitions = new ArrayList<>();
        for (DistributionLifecycleState source : DistributionLifecycleState.values()) {
            for (DistributionLifecycleState target : DistributionLifecycleState.values()) {
                if (legalTransitions.stream().anyMatch(t -> t[0] == source && t[1] == target)) {
                    continue;
                }
                illegalTransitions.add(new Object[] { source, target });
            }
        }
        return illegalTransitions.toArray(new Object[0][]);
    }

    @Test(dataProvider = "validTransitions")
    public void givenValidTransition_whenCheckingLegality_thenOk(DistributionLifecycleState source,
                                                                 DistributionLifecycleState target) {
        // Given and When
        boolean legal = DistributionLifecycleState.canTransition(source, target);

        // Then
        Assert.assertTrue(legal);
    }

    @Test(dataProvider = "validTransitions")
    public void givenValidTransition_whenCheckingLegalityOnSourceState_thenOk(DistributionLifecycleState source,
                                                                 DistributionLifecycleState target) {
        // Given and When
        boolean legal = source.canTransition(target);

        // Then
        Assert.assertTrue(legal);
    }

    @Test(dataProvider = "invalidTransitions")
    public void givenInvalidTransition_whenCheckingLegality_thenFails(DistributionLifecycleState source,
                                                                      DistributionLifecycleState target) {
        // Given and When
        boolean legal = DistributionLifecycleState.canTransition(source, target);

        // Then
        Assert.assertFalse(legal);
    }

    @Test(dataProvider = "invalidTransitions")
    public void givenInvalidTransition_whenCheckingLegalityOnSourceState_thenFails(DistributionLifecycleState source,
                                                                      DistributionLifecycleState target) {
        // Given and When
        boolean legal = source.canTransition(target);

        // Then
        Assert.assertFalse(legal);
    }
}
