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

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AbstractConsumerMocks {

    public static void mockSubscribeUnsubscribe(KafkaConsumer consumer) {
        Set<String> topics = new LinkedHashSet<>();
        doAnswer(invocation -> {
            topics.clear();
            topics.addAll(invocation.getArgument(0, Collection.class));
            return null;
        }).when(consumer).subscribe(any(Collection.class), any());
        doAnswer(invocation -> {
            topics.clear();
            return null;
        }).when(consumer).unsubscribe();
        when(consumer.subscription()).thenReturn(topics);
    }

    public static void mockAssignment(KafkaConsumer consumer) {
        Set<TopicPartition> partitions = new LinkedHashSet<>();
        doAnswer(invocation -> {
            partitions.addAll(invocation.getArgument(0, Collection.class));
            return null;
        }).when(consumer).assign(any());
        when(consumer.assignment()).thenReturn(partitions);
    }

    public static TopicPartition mockPartition(KafkaConsumer consumer, String topic) {
        TopicPartition partition = new TopicPartition(topic, 0);
        when(consumer.partitionsFor(topic)).thenReturn(asPartitionInfo(partition));
        return partition;
    }

    public static void mockConsumerGroup(KafkaConsumer consumer) {
        ConsumerGroupMetadata group = mock(ConsumerGroupMetadata.class);
        when(group.groupId()).thenReturn(TestKafkaEventSource.TEST_GROUP);
        when(consumer.groupMetadata()).thenReturn(group);
    }

    public static List<PartitionInfo> asPartitionInfo(TopicPartition partition) {
        Node n = mock(Node.class);
        Node[] ns = new Node[] { n };
        return Collections.singletonList(new PartitionInfo(partition.topic(), partition.partition(), n, ns, ns));
    }
}
