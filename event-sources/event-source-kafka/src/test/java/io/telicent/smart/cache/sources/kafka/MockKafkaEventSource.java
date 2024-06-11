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
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

public class MockKafkaEventSource<TKey, TValue> extends KafkaEventSource<TKey, TValue> {

    private MockConsumer<TKey, TValue> mock;

    /**
     * Creates a new event source backed by a Kafka topic
     *
     * @param bootstrapServers       Kafka Bootstrap servers
     * @param topics                 Kafka topic(s) to subscribe to
     * @param groupId                Kafka Consumer Group ID
     * @param keyDeserializerClass   Key deserializer class
     * @param valueDeserializerClass Value deserializer class
     * @param maxPollRecords         Maximum events to retrieve and buffer in one Kafka
     *                               {@link KafkaConsumer#poll(Duration)} request.
     * @param autoCommit             Whether the event source will automatically commit Kafka positions
     * @param policy                 Kafka Read Policy to control what events to read from the configured topic
     */
    public MockKafkaEventSource(String bootstrapServers, Set<String> topics, String groupId,
                                String keyDeserializerClass, String valueDeserializerClass, int maxPollRecords,
                                KafkaReadPolicy policy, boolean autoCommit, Collection<Event<TKey, TValue>> events) {
        super(bootstrapServers, topics, groupId, keyDeserializerClass, valueDeserializerClass, maxPollRecords,
              new MockReadPolicy(policy, events), autoCommit, null, Duration.ofMinutes(1), null);
    }

    @Override
    protected Consumer<TKey, TValue> createConsumer(Properties props) {
        this.mock = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        return this.mock;
    }

    /**
     * Gets the mock consumer, used to modify mock behaviour during tests
     *
     * @return Mock consumer
     */
    MockConsumer<TKey, TValue> getMockConsumer() {
        return this.mock;
    }

    @Override
    protected AdminClient createAdminClient(Properties props) {
        return null;
    }
}
