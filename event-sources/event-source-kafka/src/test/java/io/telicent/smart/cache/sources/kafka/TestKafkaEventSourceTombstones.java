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

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestKafkaEventSourceTombstones {

    @AfterMethod
    public void cleanup() {
        MockitoKafkaEventSource.reset();
    }

    private MockKafkaEventSource<Integer, String> createSource(Collection<Event<Integer, String>> events,
                                                               int maxPollRecords, boolean autoCommit) {
        return new MockKafkaEventSource<>(TestKafkaEventSource.DEFAULT_BOOTSTRAP_SERVERS,
                                          Set.of(TestKafkaEventSource.TEST_TOPIC),
                                          TestKafkaEventSource.TEST_GROUP + "-ignore-tombstones",
                                          StringSerializer.class.getCanonicalName(),
                                          StringSerializer.class.getCanonicalName(), maxPollRecords,
                                          KafkaReadPolicies.fromBeginning(), autoCommit, events);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void trailingTombstoneIgnoredWhenCommitting() {
        try (MockKafkaEventSource<Integer, String> source =
                createSource(List.of(new SimpleEvent<>(Collections.emptyList(), 0, "live"),
                                     new SimpleEvent<>(Collections.emptyList(), 1, null)),
                             100, false)){
            MockConsumer<Integer, String> consumer = source.getMockConsumer();
            TopicPartition partition = new TopicPartition(TestKafkaEventSource.TEST_TOPIC, 0);

            Event<Integer, String> event = source.poll(Duration.ofSeconds(1));
            Assert.assertNotNull(event);
            Assert.assertEquals(event.key(), Integer.valueOf(0));
            Assert.assertEquals(event.value(), "live");
            Assert.assertNull(source.poll(Duration.ofSeconds(1)));

            source.processed((List) List.of(event));

            Map<TopicPartition, OffsetAndMetadata> committed = consumer.committed(Set.of(partition));
            Assert.assertEquals(committed.get(partition).offset(), 2L,
                                "The Offset should be past the ignored tombstones");
        }
    }

    private ConsumerRecords<Integer, String> records(TopicPartition partition, List<ConsumerRecord<Integer, String>> records) {
        return new ConsumerRecords<>(Map.of(partition, records));
    }

    @Test
    public void pollIgnoresLaterEventsIfBatchContainsTombstones() {
        TopicPartition partition = new TopicPartition(TestKafkaEventSource.TEST_TOPIC, 0);
        List<ConsumerRecord<Integer, String>> tombstones = new ArrayList<>();
        // max.fetch.records is usually 100.
        for (int i = 0; i < 100; i++) {
            tombstones.add(new ConsumerRecord<>(partition.topic(), partition.partition(), i, i, null));
        }
        // If we add actual event _after_ it'll get ignored.
        ConsumerRecord<Integer, String> liveRecord =
                new ConsumerRecord<>(partition.topic(), partition.partition(), 100L, 100, "live");

        KafkaConsumer<Integer, String> consumer = mock(KafkaConsumer.class);
        when(consumer.poll(any(Duration.class))).thenReturn(records(partition, tombstones),
                                                            records(partition, List.of(liveRecord)));

        KafkaReadPolicy<Integer, String> policy = mock(KafkaReadPolicy.class);
        when(policy.currentLag(any())).thenReturn(0L);

        MockitoKafkaEventSource.setMockConsumer(consumer);
        try (MockitoKafkaEventSource<Integer, String> source =
                new MockitoKafkaEventSource<>(TestKafkaEventSource.DEFAULT_BOOTSTRAP_SERVERS,
                                              Set.of(TestKafkaEventSource.TEST_TOPIC),
                                              TestKafkaEventSource.TEST_GROUP + "-ignored-tombstones",
                                              IntegerDeserializer.class.getCanonicalName(),
                                              StringDeserializer.class.getCanonicalName(), 100, policy, true)) {
            Event<Integer, String> event = source.poll(Duration.ofSeconds(1));
            Assert.assertNotNull(event,
                                 "poll() should continue fetching until it finds an acrual event or times out");
            Assert.assertEquals(event.key(), Integer.valueOf(100));
            Assert.assertEquals(event.value(), "live");
        }
    }
}
