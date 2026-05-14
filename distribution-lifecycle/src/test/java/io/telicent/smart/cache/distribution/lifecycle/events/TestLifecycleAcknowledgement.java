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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class TestLifecycleAcknowledgement extends AbstractJacksonTests {

    private static LifecycleAcknowledgement basic(String distributionId, ApplicationState appStte) {
        return LifecycleAcknowledgement.builder()
                                       .eventId(UUID.randomUUID())
                                       .distributionId(distributionId)
                                       .state(new ApplicationStateUpdate(
                                               appStte))
                                       .build();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoParameters_whenBuilding_thenNPE() {
        // Given, When and Then
        LifecycleAcknowledgement.builder().build();
    }

    @Test
    public void givenBasicAcknowledgement_whenInspecting_thenOk() {
        // Given
        LifecycleAcknowledgement acknowledgement = basic("test", ApplicationState.InProgress);

        // When and Then
        Assert.assertNotNull(acknowledgement.getEventId());
        Assert.assertEquals(acknowledgement.getDistributionId(), "test");
        Assert.assertEquals(acknowledgement.getState(), new ApplicationStateUpdate(ApplicationState.InProgress));
    }

    @Test
    public void givenBasicAcknowledgement_whenRoundTripping_thenOk() throws JsonProcessingException {
        // Given
        LifecycleAcknowledgement acknowledgement = basic("example", ApplicationState.Failed);

        // When and Then
        verifyRoundTrip(acknowledgement, LifecycleAcknowledgement.class);
    }

}
