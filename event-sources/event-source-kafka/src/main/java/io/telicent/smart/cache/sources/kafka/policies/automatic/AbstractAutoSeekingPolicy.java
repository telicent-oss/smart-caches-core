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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * An automatic read policy that seeks within partitions when they are assigned to it guaranteeing that the seek happens
 * only once for each partition
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public abstract class AbstractAutoSeekingPolicy<TKey, TValue> extends AbstractAutoReadPolicy<TKey, TValue> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractAutoSeekingPolicy.class);

    private final Set<TopicPartition> seekedPartitions = new HashSet<>();

    @Override
    protected void markAsReset(TopicPartition partition) {
        synchronized (this.seekedPartitions) {
            // If we've reset this partition then we have already effectively seeked it, and we should not seek again if
            // we are re-assigned it in future
            this.seekedPartitions.add(partition);
        }
    }

    @Override
    protected final void seek(Collection<TopicPartition> partitions) {
        synchronized (this.resetOffsets) {
            synchronized (this.seekedPartitions) {
                // Due to consumer group re-balances we could get reassigned the same partitions during the course of
                // our lifetime in which case we do not want to repeatedly seek to the desired position within the topic
                // as that would cause us to redo work everytime a re-balance happens
                // However, we do permit for offsets to be explicitly reset in which case we will seek to the requested
                // offset
                List<TopicPartition> needsSeek = new ArrayList<>();
                for (TopicPartition partition : partitions) {
                    if (!seekedPartitions.contains(partition) || this.resetOffsets.containsKey(partition)) {
                        needsSeek.add(partition);
                    }
                }
                if (!needsSeek.isEmpty()) {
                    // If we had some offset resets that we previously could not apply we apply them now if possible
                    if (!this.resetOffsets.isEmpty()) {
                        for (TopicPartition partition : needsSeek.stream().toList()) {
                            // Do we have an offset reset for this partition?
                            long offset = this.resetOffsets.getOrDefault(partition, Long.MIN_VALUE);
                            if (offset != Long.MIN_VALUE) {
                                // If so we apply it now, and mark the partition as seeked so we won't override this with
                                // our default seeking behaviour later
                                LOGGER.info("Resetting offset for topic partition {}-{} to offset {}",
                                            partition.topic(),
                                            partition.partition(), offset);
                                this.consumer.seek(partition, offset);

                                // We only want to reset once so mark this reset as applied, and this partition as
                                // seeked
                                this.resetOffsets.remove(partition);
                                seekedPartitions.add(partition);
                                needsSeek.remove(partition);
                            }
                        }
                    }

                    // If we still have partitions for which we need to seek go ahead and defer to the derived
                    // implementations internal seek logic now
                    if (!needsSeek.isEmpty()) {
                        seekInternal(needsSeek);
                        seekedPartitions.addAll(needsSeek);
                    }
                }
            }
        }
    }

    /**
     * Performs the actual seek operation on the given partitions
     *
     * @param partitions Partitions
     */
    protected abstract void seekInternal(Collection<TopicPartition> partitions);
}
