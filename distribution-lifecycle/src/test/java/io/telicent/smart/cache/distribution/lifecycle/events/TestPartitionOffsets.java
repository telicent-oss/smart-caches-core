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
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestPartitionOffsets extends AbstractJacksonTests {

    @Test
    public void givenEmptyOffsets_whenRoundTripping_thenOk() throws JsonProcessingException {
        // Given
        PartitionOffsets offsets = new PartitionOffsets();

        // When and Then
        verifyRoundTrip(offsets, PartitionOffsets.class);
    }

    @Test
    public void givenNonEmptyOffsets_whenRoundTripping_thenOk() throws JsonProcessingException {
        // Given
        PartitionOffsets offsets = new PartitionOffsets();
        offsets.setOffset("test-0", 77L);
        offsets.setOffset("test-1", 99L);
        offsets.setOffset("test-2", 3L);

        // When
        PartitionOffsets reparsed = verifyRoundTrip(offsets, PartitionOffsets.class);

        // Then
        Assert.assertEquals(reparsed.getOffset("test-0"), 77L);
        Assert.assertEquals(reparsed.getOffset("test-1"), 99L);
        Assert.assertEquals(reparsed.getOffset("test-2"), 3L);
    }
}
