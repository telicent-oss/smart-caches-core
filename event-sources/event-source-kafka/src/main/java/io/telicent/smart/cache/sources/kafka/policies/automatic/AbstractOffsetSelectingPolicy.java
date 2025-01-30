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

import java.util.Collection;

/**
 * An automatic read policy that seeks within partitions when they are assigned to it based upon previously provided
 * offsets.  It guarantees that the seek happens only once for each partition.
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public abstract class AbstractOffsetSelectingPolicy<TKey, TValue> extends AbstractAutoSeekingPolicy<TKey, TValue> {
    protected final long defaultOffset;

    /**
     * Creates a new policy
     *
     * @param defaultOffset The default offset to seek to if no more specific offset available for a partition
     */
    public AbstractOffsetSelectingPolicy(long defaultOffset) {
        if (defaultOffset < 0) {
            throw new IllegalArgumentException("defaultOffset must be >= 0");
        }
        this.defaultOffset = defaultOffset;
    }

    @Override
    protected final void seekInternal(Collection<TopicPartition> partitions) {
        for (TopicPartition partition : partitions) {
            long offset = selectOffset(partition);
            this.consumer.seek(partition, offset);
        }
    }

    /**
     * Selects the offset to seek to for the given partition
     *
     * @param partition Partition
     * @return Offset to seek to
     */
    protected abstract long selectOffset(TopicPartition partition);
}
