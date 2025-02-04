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

import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.offsets.OffsetStore;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

/**
 * A Kafka read policy that reads events from specific offsets, as defined by an external offset store, onwards from all
 * assigned partitions (which are assigned automatically via Kafka Consumer groups).
 *
 * @param <TKey>   Key Type
 * @param <TValue> Value Type
 */
public class AutoFromExternalOffsetsStore<TKey, TValue> extends AbstractOffsetSelectingPolicy<TKey, TValue> {

    private final OffsetStore offsets;

    /**
     * Creates a new policy that reads from the offsets in an external offsets store
     *
     * @param offsets       Offset store that provides the offsets to start reading from
     * @param defaultOffset Default offset to use for any partition whose offset is not yet tracked
     */
    public AutoFromExternalOffsetsStore(OffsetStore offsets, long defaultOffset) {
        super(defaultOffset);
        this.offsets = Objects.requireNonNull(offsets, "Offsets Store cannot be null");
    }

    /**
     * Selects the offset to read from for the given partition
     *
     * @param partition Partition
     * @return Offset to read from
     */
    @Override
    protected long selectOffset(TopicPartition partition) {
        String consumerGroup = this.consumer.groupMetadata().groupId();
        String key =
                KafkaEventSource.externalOffsetStoreKey(partition.topic(), partition.partition(), consumerGroup);
        if (this.offsets.hasOffset(key)) {
            return this.offsets.loadOffset(key);
        } else {
            return this.defaultOffset;
        }
    }
}
