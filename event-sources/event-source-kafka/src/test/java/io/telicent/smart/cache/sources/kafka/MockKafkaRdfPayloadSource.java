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

import io.telicent.smart.cache.payloads.RdfPayload;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.IntegerDeserializer;

import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

public class MockKafkaRdfPayloadSource extends KafkaRdfPayloadSource<Integer> {

    /**
     * Creates a new event source backed by a Kafka topic
     *
     * @param bootstrapServers Kafka Bootstrap servers
     * @param topics            Kafka topic(s) to subscribe to
     * @param groupId          Kafka Consumer Group ID
     * @param maxPollRecords   Maximum events to retrieve and buffer in one Kafka
     *                         {@link org.apache.kafka.clients.consumer.KafkaConsumer#poll(Duration)} request.
     * @param autoCommit       Whether the event source will automatically commit Kafka positions
     * @param policy           Kafka Read Policy to control what events to read from the configured topic
     */
    public MockKafkaRdfPayloadSource(String bootstrapServers, Set<String> topics, String groupId, int maxPollRecords,
                                     KafkaReadPolicy<Integer, RdfPayload> policy, boolean autoCommit,
                                     Collection<Event<Integer, RdfPayload>> events) {
        super(bootstrapServers, topics, groupId, IntegerDeserializer.class.getCanonicalName(), maxPollRecords,
              new MockReadPolicy<>(policy, events), autoCommit, null, Duration.ofMinutes(1), null);
    }

    @Override
    protected Consumer<Integer, RdfPayload> createConsumer(Properties props) {
        MockConsumer<Integer, RdfPayload> mock = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        return mock;
    }

    @Override
    protected AdminClient createAdminClient(Properties props) {
        return null;
    }
}
