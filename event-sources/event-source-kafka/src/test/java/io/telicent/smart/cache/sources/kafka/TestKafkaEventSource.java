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

import io.telicent.smart.cache.sources.AbstractEventSourceTests;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.EventSourceException;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.clients.consumer.LogTruncationException;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.NoOffsetForPartitionException;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.kafka.common.record.TimestampType.NO_TIMESTAMP_TYPE;

public class TestKafkaEventSource extends AbstractEventSourceTests<Integer, String> {

    public static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String TEST_TOPIC = "test";
    public static final String TEST_GROUP = "test-group";
    protected MockKafkaEventSource<Integer, String> kafkaEventSource;

    @Override
    protected EventSource<Integer, String> createEmptySource() {
        return createSource(Collections.emptyList());
    }

    @Override
    protected final EventSource<Integer, String> createSource(Collection<Event<Integer, String>> events) {
        kafkaEventSource = createMockKafkaEventSource(events);
        return kafkaEventSource;
    }

    /**
     * Provides a mock Kafka event source for testing
     *
     * @param events Events that the mock source should provide
     * @return Mock Kafka Event Source
     */
    protected MockKafkaEventSource<Integer, String> createMockKafkaEventSource(
            Collection<Event<Integer, String>> events) {
        return new MockKafkaEventSource<>(DEFAULT_BOOTSTRAP_SERVERS, Set.of(TEST_TOPIC), TEST_GROUP,
                                          StringSerializer.class.getCanonicalName(),
                                          StringSerializer.class.getCanonicalName(), 100,
                                          KafkaReadPolicies.fromBeginning(), true, events);
    }

    @Override
    protected Collection<Event<Integer, String>> createSampleData(int size) {
        AtomicInteger counter = new AtomicInteger(0);
        return createSampleStrings(size).stream()
                                        .map(s -> new SimpleEvent<>(Collections.emptyList(), counter.getAndIncrement(),
                                                                    s)).collect(Collectors.toList());
    }

    @Override
    public boolean guaranteesImmediateAvailability() {
        return false;
    }

    @Override
    public boolean isUnbounded() {
        return true;
    }

    @DataProvider(name = "kafka-poll-unrecoverable-errors")
    protected Object[][] getKafkaPollUnrecoverableErrors() {
        TopicPartition foo = new TopicPartition("foo", 0);
        return new Object[][] {
                { new NoOffsetForPartitionException(foo) },
                { new OffsetOutOfRangeException("Bad offset") },
                {
                        new LogTruncationException("Kafka log was truncated, data loss has occurred",
                                                   Map.of(foo, 1L),
                                                   Map.of(foo, new OffsetAndMetadata(2L)))
                },
                { new AuthenticationException("User 'foo' is not authenticated") },
                { new AuthorizationException("User 'foo' does not have permission to read topic 'bar'") },
                { new InvalidTopicException("Topic 'bar' is invalid") },
                { new FencedInstanceIdException("Fenced by Kafka") },
                { new UnsupportedVersionException("Version unsupported") }
        };
    }

    @DataProvider(name = "kafka-poll-unrecoverable-errors-2")
    protected Object[][] getKafkaPollUnrecoverableErrors2() {
        return new Object[][] {
                { new IllegalStateException("No topic subscriptions/assignments have been made on this consumer") },
                { new IllegalArgumentException("Invalid timeout") },
                { new ArithmeticException("Timeout too large") },
                {
                        new RecordDeserializationException(null, new TopicPartition(KafkaTestCluster.DEFAULT_TOPIC, 0), 12345L, 12345L,
                                NO_TIMESTAMP_TYPE, null, null, null, "Bad record", null)
                }
        };
    }

    @Test(dataProvider = "kafka-poll-unrecoverable-errors", expectedExceptions = EventSourceException.class)
    public void kafka_poll_failures_unrecoverable_01(KafkaException e) {
        EventSource<Integer, String> source = createEmptySource();
        MockConsumer<Integer, String> mock = this.kafkaEventSource.getMockConsumer();
        mock.setPollException(e);
        source.poll(Duration.ofSeconds(3));
    }

    @Test(dataProvider = "kafka-poll-unrecoverable-errors-2", expectedExceptions = EventSourceException.class)
    public void kafka_poll_failures_unrecoverable_02(RuntimeException e) {
        EventSource<Integer, String> source = createEmptySource();
        MockConsumer<Integer, String> mock = this.kafkaEventSource.getMockConsumer();
        mock.schedulePollTask(() -> {throw e;});
        source.poll(Duration.ofSeconds(3));
    }

    @Test
    public void kafka_poll_failures_recoverable() {
        EventSource<Integer, String> source = createSource(createSampleData(10));
        MockConsumer<Integer, String> mock = this.kafkaEventSource.getMockConsumer();
        mock.wakeup();
        Assert.assertNull(source.poll(Duration.ofSeconds(3)));
    }
}
