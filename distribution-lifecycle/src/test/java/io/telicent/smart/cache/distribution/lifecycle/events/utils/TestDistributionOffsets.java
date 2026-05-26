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
package io.telicent.smart.cache.distribution.lifecycle.events.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.telicent.smart.cache.distribution.lifecycle.events.AbstractJacksonTests;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDistributionOffsets extends AbstractJacksonTests {

    @Test
    public void givenEmptyOffsets_whenRoundTripping_thenOk() throws JsonProcessingException {
        // Given
        DistributionOffsets offsets = new DistributionOffsets();

        // When and Then
        verifyRoundTrip(offsets, DistributionOffsets.class);
    }

    @Test
    public void givenNonEmptyOffsets_whenRoundTripping_thenOk() throws JsonProcessingException {
        // Given
        DistributionOffsets offsets = new DistributionOffsets();
        PartitionOffsets distroA = new PartitionOffsets();
        distroA.setOffset("test-0", 1234L);
        PartitionOffsets distroB = new PartitionOffsets();
        distroB.setOffset("test-1", 779L);
        offsets.setOffsets("a", distroA);
        offsets.setOffsets("b", distroB);

        // When
        DistributionOffsets reparsed = verifyRoundTrip(offsets, DistributionOffsets.class);

        // Then
        Assert.assertEquals(reparsed.getOffsets("a"), distroA);
        Assert.assertEquals(reparsed.getOffsets("b"), distroB);
    }
}
