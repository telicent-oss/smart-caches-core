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
package io.telicent.smart.cache.sources.kafka.policies.automatic;

import org.apache.kafka.common.TopicPartition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A Kafka read policy that reads events from specific offsets onwards from all assigned partitions (which are assigned
 * automatically via Kafka Consumer groups).
 *
 * @param <TKey>   Key Type
 * @param <TValue> Value Type
 */
public class AutoFromOffset<TKey, TValue> extends AbstractOffsetSelectingPolicy<TKey, TValue> {

    private final Map<TopicPartition, Long> desiredOffsets;

    /**
     * Creates a new policy that defaults to reading all partitions from offset zero
     */
    public AutoFromOffset() {
        this(null, 0);
    }

    /**
     * Creates a new policy that reads from the given offsets
     *
     * @param desiredOffsets Desired starting offsets for specific partitions
     * @param defaultOffset  Default offset to use for any partition not explicitly specified
     */
    public AutoFromOffset(Map<TopicPartition, Long> desiredOffsets, long defaultOffset) {
        super(defaultOffset);
        this.desiredOffsets = desiredOffsets != null ? new HashMap<>(desiredOffsets) : Collections.emptyMap();
    }

    /**
     * Selects the offset to read from for the given partition
     *
     * @param partition Partition
     * @return Offset to read from
     */
    @Override
    protected long selectOffset(TopicPartition partition) {
        return desiredOffsets.getOrDefault(partition, defaultOffset);
    }
}
