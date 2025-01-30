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
package io.telicent.smart.cache.sources.kafka.policies;

import io.telicent.smart.cache.sources.kafka.policies.automatic.AutoFromExternalOffsetsStore;
import io.telicent.smart.cache.sources.offsets.MemoryOffsetStore;
import io.telicent.smart.cache.sources.offsets.OffsetStore;
import org.testng.annotations.Test;

public class TestAutoFromExternalOffsets extends AbstractReadPolicyTests<String, String>{
    @Override
    protected KafkaReadPolicy<String, String> createPolicy() {
        return new AutoFromExternalOffsetsStore<>(new MemoryOffsetStore(), 0L);
    }

    @Override
    protected boolean modifiesConsumerConfiguration() {
        return false;
    }

    @Override
    protected boolean seeksOnAssignment() {
        return true;
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullOffsetsStore_whenConstructingAutoFromExternalOffsets_thenNPE() {
        // Given, When and Then
        new AutoFromExternalOffsetsStore<>(null, 0L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*>= 0")
    public void givenNegativeDefaultOffset_whenConstructingAutoFromExternalOffsets_thenIllegalArgument() {
        // GIven
        OffsetStore store = new MemoryOffsetStore();
        long defaultOffset = -1L;

        // When and Then
        new AutoFromExternalOffsetsStore<>(store, defaultOffset);
    }
}
