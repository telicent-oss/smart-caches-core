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
package io.telicent.smart.cache.distribution.lifecycle.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class TestLifecycleAction extends AbstractJacksonTests {

    private static LifecycleAction basic(String distributionId, DistributionLifecycleState from,
                                         DistributionLifecycleState to) {
        return LifecycleAction.builder()
                              .eventId(UUID.randomUUID())
                              .distributionId(distributionId)
                              .datasetId("dataset")
                              .user("admin@example.org")
                              .state(new LifecycleStateTransition(
                                      from, to))
                              .build();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoParameters_whenBuilding_thenNPE() {
        // Given, When and Then
        LifecycleAction.builder().build();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenPartialParameters_whenBuilding_thenNPE() {
        // Given, When and Then
        LifecycleAction.builder().eventId(UUID.randomUUID()).build();
    }

    @Test
    public void givenBasicAction_whenInspecting_thenOk() {
        // Given
        LifecycleAction acknowledgement =
                basic("test", DistributionLifecycleState.Unregistered, DistributionLifecycleState.Registered);

        // When and Then
        Assert.assertNotNull(acknowledgement.getEventId());
        Assert.assertEquals(acknowledgement.getDistributionId(), "test");
        Assert.assertEquals(acknowledgement.getDatasetId(), "dataset");
        Assert.assertEquals(acknowledgement.getState(),
                            new LifecycleStateTransition(DistributionLifecycleState.Unregistered,
                                                         DistributionLifecycleState.Registered));
    }

    @Test
    public void givenBasicAction_whenRoundTripping_thenOk() throws JsonProcessingException {
        // Given
        LifecycleAction action =
                basic("example", DistributionLifecycleState.Active, DistributionLifecycleState.Withdrawn);

        // When and Then
        verifyRoundTrip(action, LifecycleAction.class);
    }

}
