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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Abstract base class for read policies
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public abstract class AbstractReadPolicy<TKey, TValue> implements KafkaReadPolicy<TKey, TValue> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReadPolicy.class);

    /**
     * The Kafka consumer that is being used
     */
    protected Consumer<TKey, TValue> consumer = null;

    /**
     * A map of offset resets that have yet to be applied
     */
    protected final Map<TopicPartition, Long> resetOffsets = new ConcurrentHashMap<>();

    /**
     * We create a basic cache to control the amount of repeated status messages logged that add no value.
     */
    protected static final Cache<String, Boolean> LOGGING_CACHE =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(5))
                    .initialCapacity(1)
                    .maximumSize(1)
                    .build();


    @Override
    public void prepareConsumerConfiguration(Properties props) {
        // Nothing to do
    }

    @Override
    public final void setConsumer(Consumer<TKey, TValue> consumer) {
        if (this.consumer != null) {
            throw new IllegalStateException("Cannot set the consumer multiple times");
        }
        this.consumer = consumer;
    }

    /**
     * Gets the set of unique topics affected by a partition re-balance operation
     *
     * @param partitions Partitions
     * @return Set of affected topics
     */
    protected final Set<String> getAffectedTopics(Collection<TopicPartition> partitions) {
        return partitions.stream().map(TopicPartition::topic).collect(Collectors.toSet());
    }

    /**
     * Gets the set of relevant partitions for a given topic
     *
     * @param partitions Partitions to filter
     * @param topic      Topic
     * @return Relevant partitions
     */
    protected final Set<TopicPartition> getRelevantPartitions(Collection<TopicPartition> partitions, String topic) {
        return partitions.stream()
                         .filter(p -> Objects.equals(p.topic(), topic))
                         .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Seeks to the events in the topic(s) that this read policy wishes to read
     *
     * @param partitions Topic Partitions
     */
    protected void seek(Collection<TopicPartition> partitions) {
        // No-op by default

        // The read policy may already have set some seek settings via the Consumer Config which render this operation
        // unnecessary, but for some read policies they may wish to explicitly seek within partitions.
    }

    /**
     * Logs the positions and lag for the given partitions
     *
     * @param partitions Partitions
     * @param logger     Logger to log to
     */
    protected void logPartitionPositions(Collection<TopicPartition> partitions, Logger logger) {
        if (CollectionUtils.isEmpty(partitions)) {
            return;
        }

        // We want to accurately log the position and lag for each partition
        // Since currentLag() is computed only from local metadata we have to force Kafka to look up the end offsets
        // and positions for each partition prior to asking for the current lag
        this.consumer.endOffsets(partitions);
        partitions.forEach(p -> {
            long position = this.consumer.position(p);
            OptionalLong currentLag = this.consumer.currentLag(p);
            String knownLag = currentLag.isPresent() ? String.format("%,d", currentLag.getAsLong()) : "unknown";
            String key = String.format("%s-%d-%s", p, position, knownLag);
            if (LOGGING_CACHE.getIfPresent(key) == null) {
                FmtLog.info(logger, "Kafka Partition %s is at position %,d with a current lag of %s", p,
                            position, knownLag);
                LOGGING_CACHE.put(key, Boolean.TRUE);
            }
        });
    }

    /**
     * Calculates the total lag for the given partitions, or {@code null} if it cannot be calculated
     *
     * @param partitions Partitions
     * @return Total lag
     */
    protected Long calculateLag(Collection<TopicPartition> partitions) {
        if (CollectionUtils.isEmpty(partitions)) {
            return null;
        }

        this.consumer.endOffsets(partitions);
        AtomicBoolean anyUnknown = new AtomicBoolean(false);
        AtomicLong totalLag = new AtomicLong();
        partitions.forEach(p -> {
            this.consumer.position(p);
            OptionalLong currentLag = this.consumer.currentLag(p);
            if (currentLag.isPresent()) {
                totalLag.addAndGet(currentLag.getAsLong());
            } else {
                anyUnknown.set(true);
            }
        });

        if (anyUnknown.get()) {
            return null;
        } else {
            return totalLag.get();
        }
    }

    @Override
    public void resetOffsets(Map<TopicPartition, Long> offsets) {
        synchronized (this.resetOffsets) {
            // Clear any previously requested offset resets that we were yet to apply
            this.resetOffsets.clear();

            // Try to apply each reset
            Set<String> relevantTopics = this.consumer.subscription();
            for (Map.Entry<TopicPartition, Long> newOffset : offsets.entrySet()) {
                TopicPartition partition = newOffset.getKey();
                if (relevantTopics.contains(partition.topic())) {
                    if (this.consumer.assignment().contains(partition)) {
                        // Currently subscribed to topic and assigned this partition so reset immediately
                        LOGGER.info("Reset offset for topic partition {}-{} to offset {}", partition.topic(),
                                    partition.partition(), newOffset.getValue());
                        this.consumer.seek(partition, newOffset.getValue());
                        markAsReset(partition);
                    } else {
                        ignoreResetForNow(newOffset, "as not currently assigned that partition");
                    }
                } else {
                    ignoreResetForNow(newOffset, "as not currently subscribed to that topic");
                }
            }
        }
    }

    /**
     * Called when {@link #resetOffsets(Map)} applies an offset reset to the given topic partition.
     * <p>
     * This method does nothing by default, derived read policies may wish to override this method in order that they
     * can update any internal state they hold that might cause them to seek away from the explicitly reset offset.
     * </p>
     *
     * @param partition Topic partition
     */
    protected void markAsReset(TopicPartition partition) {
        // No-op by default
    }

    /**
     * Marks that an offset reset has been ignored for now but could be applicable later
     *
     * @param newOffset New Offset to reset to
     * @param reason    Reason for ignoring
     */
    private void ignoreResetForNow(Map.Entry<TopicPartition, Long> newOffset, String reason) {
        LOGGER.info(
                "Ignored reset for topic partition {}-{} {}.  This will be applied if and when we are assigned that partition.",
                newOffset.getKey().topic(), newOffset.getKey().partition(), reason);
        this.resetOffsets.put(newOffset.getKey(), newOffset.getValue());
    }
}
