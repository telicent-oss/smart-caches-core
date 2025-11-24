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
package io.telicent.smart.cache.sources.kafka.policies.manual;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A Kafka Read Policy that uses manual partition assignment to assign itself all available partitions for a topic and
 * reads the events from the beginning
 *
 * @param <TKey>   Key Type
 * @param <TValue> Value Type
 */
public class ManualFromBeginning<TKey, TValue> extends AbstractManualReadPolicy<TKey, TValue> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManualFromBeginning.class);

    @Override
    protected void seek(Collection<TopicPartition> partitions) {
        synchronized (this.resetOffsets) {
            Set<TopicPartition> needsSeek = new HashSet<>(partitions);

            // Do any of the partitions we've assigned ourselves need an offset reset?
            if (!this.resetOffsets.isEmpty()) {
                for (TopicPartition partition : partitions) {
                    long offset = this.resetOffsets.getOrDefault(partition, Long.MIN_VALUE);
                    if (offset != Long.MIN_VALUE) {
                        // If it needs an offset apply it now
                        this.consumer.seek(partition, offset);
                        LOGGER.info("[{}] Resetting offset for topic partition {}-{} to offset {}", partition.topic(),
                                    partition.topic(), partition.partition(), offset);
                        needsSeek.remove(partition);
                    }
                }
            }
            if (!needsSeek.isEmpty()) {
                // For any other assigned partitions seek to the beginning
                this.consumer.seekToBeginning(needsSeek);
            }
        }
    }
}
