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
import io.telicent.smart.cache.distribution.lifecycle.events.utils.DistributionOffsets;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.PartitionOffsets;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestIngestStatus extends AbstractJacksonTests {

    @Test
    public void givenEmptyStatus_whenRoundTripping_thenOk() throws JsonProcessingException {
        // Given
        IngestStatus status = IngestStatus.builder().offsets(new DistributionOffsets()).build();

        // When and Then
        verifyRoundTrip(status, IngestStatus.class);
    }

    @Test
    public void givenNonEmptyStatus_whenRoundTripping_thenOk() throws JsonProcessingException {
        // Given
        DistributionOffsets offsets = new DistributionOffsets();
        PartitionOffsets distroA = new PartitionOffsets();
        distroA.setOffset("test-0", 456L);
        offsets.setOffsets("a", distroA);
        PartitionOffsets distroB = new PartitionOffsets();
        distroB.setOffset("test-1", 789L);
        offsets.setOffsets("b", distroB);
        IngestStatus status = IngestStatus.builder().offsets(offsets).build();

        // When
        IngestStatus reparsed = verifyRoundTrip(status, IngestStatus.class);

        // Then
        Assert.assertEquals(reparsed.getOffset("a", "test-0"), 456L);
        Assert.assertEquals(reparsed.getOffset("b", "test-1"), 789L);
        Assert.assertNull(reparsed.getOffset("c", "test-2"));
    }
}
