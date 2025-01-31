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

import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Properties;
import java.util.Set;

/**
 * An extended Kafka Event Source that allows injecting an arbitrary consumer, generally one created by mocking with
 * Mockito.
 * <p>
 * Note that this differs from {@link MockKafkaEventSource} in that this requires the caller to set up their mock with
 * all the behaviour needed for their test, whereas with {@link MockKafkaEventSource} the Kafka libraries handle much of
 * that for you.
 * </p>
 * <p>
 * However for some tests, where we want to induce specific error code paths this approach is more useful.
 * </p>
 *
 * @param <TKey>
 * @param <TValue>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MockitoKafkaEventSource<TKey, TValue> extends KafkaEventSource<TKey, TValue> {

    private static final ThreadLocal<KafkaConsumer> MOCK_CONSUMER = new ThreadLocal<>();
    private static final ThreadLocal<AdminClient> MOCK_ADMIN_CLIENT = new ThreadLocal<>();

    public static void setMockConsumer(KafkaConsumer mockConsumer) {
        MOCK_CONSUMER.set(mockConsumer);
    }

    public static void reset() {
        MOCK_CONSUMER.remove();
        MOCK_ADMIN_CLIENT.remove();
    }

    public static void setMockAdminClient(AdminClient mockAdminClient) {
        MOCK_ADMIN_CLIENT.set(mockAdminClient);
    }

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
    public MockitoKafkaEventSource(String bootstrapServers, Set<String> topics, String groupId,
                                   String keyDeserializerClass, String valueDeserializerClass, int maxPollRecords,
                                   KafkaReadPolicy policy, boolean autoCommit) {
        super(bootstrapServers, topics, groupId, keyDeserializerClass, valueDeserializerClass, maxPollRecords,
              policy, autoCommit, null, Duration.ofMinutes(1), null);
    }

    @Override
    protected Consumer<TKey, TValue> createConsumer(Properties props) {
        return MOCK_CONSUMER.get();
    }

    @Override
    protected AdminClient createAdminClient(Properties props) {
        return MOCK_ADMIN_CLIENT.get();
    }
}
