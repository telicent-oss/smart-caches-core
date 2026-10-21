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
package io.telicent.smart.cache.sources.kafka;

import io.telicent.smart.cache.sources.DistributionIds;
import io.telicent.smart.cache.sources.DistributionKeyStrategy;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A Kafka {@link Partitioner} that partitions on the Distribution ID portion of a message key only.
 * <p>
 * This exists so that {@link DistributionKeyStrategy#DISTRIBUTION_ID_AND_UUID} can deliver both of the properties the
 * Core Data Management design wants at once.  That strategy appends a UUID to each key so that every event has a
 * unique key and log compaction stays viable, but with Kafka's default partitioner each of those unique keys hashes
 * independently and the per-distribution ordering guarantee is lost.  This partitioner strips the uniqueness suffix
 * before hashing, so a distribution's events still land on a single partition.
 * </p>
 * <p>
 * The hashing is deliberately identical to Kafka's built in partitioning, murmur2 of the key bytes modulo the
 * partition count, so a {@link DistributionKeyStrategy#DISTRIBUTION_ID} keyed event is assigned the same partition
 * whether or not this partitioner is installed.  That means it can be rolled out to a topic's producers without
 * reshuffling existing distributions.
 * </p>
 * <p>
 * Enable it by setting {@code partitioner.class} in a producer's configuration:
 * </p>
 * <pre>
 * partitioner.class=io.telicent.smart.cache.sources.kafka.DistributionIdPartitioner
 * </pre>
 * <p>
 * Records with no key, or whose key is not valid UTF-8 and therefore conveys no Distribution ID, are assigned a
 * random partition, matching the default partitioner's behaviour for unkeyed records.
 * </p>
 */
public class DistributionIdPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        if (numPartitions <= 1) {
            return 0;
        }

        String distributionId = DistributionIds.fromKeyBytes(keyBytes);
        if (distributionId == null) {
            // No usable key, spread across the available partitions as the default partitioner does for unkeyed
            // records.  Prefer partitions that currently have a leader.
            List<PartitionInfo> available = cluster.availablePartitionsForTopic(topic);
            if (!available.isEmpty()) {
                return available.get(ThreadLocalRandom.current().nextInt(available.size())).partition();
            }
            return ThreadLocalRandom.current().nextInt(numPartitions);
        }

        // NB - Same hashing as Kafka's built in partitioning so that a plain Distribution ID key lands on the same
        //      partition with or without this partitioner installed.
        return Utils.toPositive(Utils.murmur2(distributionId.getBytes(StandardCharsets.UTF_8))) % numPartitions;
    }

    @Override
    public void close() {
        // No resources to release
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // No configuration required
    }
}
