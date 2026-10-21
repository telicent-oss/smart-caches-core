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
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestDistributionIdPartitioner {

    private static final String TOPIC = "knowledge";
    private static final String DISTRIBUTION_ID = "https://telicent.io/datasets/acled#2026-08-release";

    private static Cluster cluster(int numPartitions) {
        Node node = new Node(0, "localhost", 9092);
        List<PartitionInfo> partitions = new ArrayList<>();
        for (int i = 0; i < numPartitions; i++) {
            partitions.add(new PartitionInfo(TOPIC, i, node, new Node[] { node }, new Node[] { node }));
        }
        return new Cluster("test-cluster", List.of(node), partitions, Collections.emptySet(), Collections.emptySet());
    }

    private static byte[] utf8(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static int partition(DistributionIdPartitioner partitioner, Cluster cluster, String key) {
        byte[] rawKey = key != null ? utf8(key) : null;
        return partitioner.partition(TOPIC, key, rawKey, null, null, cluster);
    }

    @Test
    public void givenCompositeKeys_whenPartitioning_thenAllLandOnOnePartition() {
        // Given - this is the whole point, the composite strategy would otherwise spread a distribution's events
        try (DistributionIdPartitioner partitioner = new DistributionIdPartitioner()) {
            Cluster cluster = cluster(16);
            Set<Integer> partitions = new HashSet<>();

            // When
            for (int i = 0; i < 100; i++) {
                partitions.add(partition(partitioner, cluster,
                                         DISTRIBUTION_ID + DistributionIds.KEY_SEPARATOR + UUID.randomUUID()));
            }

            // Then
            Assert.assertEquals(partitions.size(), 1, "All of a distribution's events must share a partition");
        }
    }

    @Test
    public void givenPlainAndCompositeKeys_whenPartitioning_thenSamePartition() {
        // Given
        try (DistributionIdPartitioner partitioner = new DistributionIdPartitioner()) {
            Cluster cluster = cluster(16);

            // When
            int plain = partition(partitioner, cluster, DISTRIBUTION_ID);
            int composite = partition(partitioner, cluster,
                                      DISTRIBUTION_ID + DistributionIds.KEY_SEPARATOR + UUID.randomUUID());

            // Then
            Assert.assertEquals(composite, plain);
        }
    }

    @Test
    public void givenPlainKey_whenPartitioning_thenMatchesKafkaBuiltInHashing() {
        // Given - so that installing this partitioner does not reshuffle existing distributions
        try (DistributionIdPartitioner partitioner = new DistributionIdPartitioner()) {
            Cluster cluster = cluster(16);
            int expected = Utils.toPositive(Utils.murmur2(utf8(DISTRIBUTION_ID))) % 16;

            // When and Then
            Assert.assertEquals(partition(partitioner, cluster, DISTRIBUTION_ID), expected);
        }
    }

    @Test
    public void givenDifferentDistributions_whenPartitioning_thenSpreadAcrossPartitions() {
        // Given
        try (DistributionIdPartitioner partitioner = new DistributionIdPartitioner()) {
            Cluster cluster = cluster(16);
            Set<Integer> partitions = new HashSet<>();

            // When
            for (int i = 0; i < 100; i++) {
                partitions.add(partition(partitioner, cluster, "https://telicent.io/datasets/dataset-" + i));
            }

            // Then
            Assert.assertTrue(partitions.size() > 1,
                              "Different distributions should not all collide on a single partition");
        }
    }

    @Test
    public void givenSinglePartitionTopic_whenPartitioning_thenZero() {
        // Given
        try (DistributionIdPartitioner partitioner = new DistributionIdPartitioner()) {
            Cluster cluster = cluster(1);

            // When and Then
            Assert.assertEquals(partition(partitioner, cluster, DISTRIBUTION_ID), 0);
            Assert.assertEquals(partition(partitioner, cluster, null), 0);
        }
    }

    @Test
    public void givenNoKey_whenPartitioning_thenValidPartition() {
        // Given
        try (DistributionIdPartitioner partitioner = new DistributionIdPartitioner()) {
            Cluster cluster = cluster(4);

            // When and Then
            for (int i = 0; i < 50; i++) {
                int partition = partition(partitioner, cluster, null);
                Assert.assertTrue(partition >= 0 && partition < 4, "Partition " + partition + " is out of range");
            }
        }
    }

    @Test
    public void givenNonUtf8Key_whenPartitioning_thenValidPartition() {
        // Given - a legacy binary key conveys no Distribution ID so it is treated as unkeyed
        try (DistributionIdPartitioner partitioner = new DistributionIdPartitioner()) {
            Cluster cluster = cluster(4);
            byte[] rawKey = new byte[] { (byte) 0xC3, (byte) 0x28 };

            // When and Then
            for (int i = 0; i < 50; i++) {
                int partition = partitioner.partition(TOPIC, rawKey, rawKey, null, null, cluster);
                Assert.assertTrue(partition >= 0 && partition < 4, "Partition " + partition + " is out of range");
            }
        }
    }
}
