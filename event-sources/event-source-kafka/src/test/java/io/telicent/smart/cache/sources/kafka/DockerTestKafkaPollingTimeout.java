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

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.kafka.policies.automatic.AbstractAutoReadPolicy;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.internals.ConsumerCoordinator;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.event.Level;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class DockerTestKafkaPollingTimeout {

    private static final AtomicInteger GROUP_ID = new AtomicInteger();

    private BasicKafkaTestCluster kafka;
    private final TestLogger eventSourceLogger = TestLoggerFactory.getTestLogger(KafkaEventSource.class);
    private final TestLogger readPolicyLogger = TestLoggerFactory.getTestLogger(AbstractAutoReadPolicy.class);
    private final TestLogger consumerCoordinatorLogger = TestLoggerFactory.getTestLogger(ConsumerCoordinator.class);

    @BeforeClass
    public void setup() {
        Utils.logTestClassStarted(this.getClass());
        this.kafka = new BasicKafkaTestCluster();
        this.kafka.setup();
    }

    @AfterClass
    public void teardown() {
        this.kafka.teardown();
        Utils.logTestClassFinished(this.getClass());
    }

    @AfterMethod
    public void testCleanup() throws InterruptedException {
        this.kafka.resetTestTopic();
        this.consumerCoordinatorLogger.clearAll();
        this.eventSourceLogger.clearAll();
        this.readPolicyLogger.clearAll();
    }

    public void verifyLogging(TestLogger logger, Level level, String... searchTerms) {
        List<LoggingEvent> logs =
                logger.getAllLoggingEvents().stream().filter(event -> event.getLevel() == level).toList();
        Assert.assertNotEquals(logs.size(), 0, "Expected at least one logging event at level " + level);
        for (String searchTerm : searchTerms) {
            Assert.assertTrue(logs.stream().anyMatch(event -> event.getFormattedMessage().contains(searchTerm)),
                              "Logs were missing expected message '" + searchTerm + "'");
        }
    }

    @Test
    public void givenKafkaSource_whenLongDelayBetweenPoll_thenReceivesSubsequentEvents_andLoggingAsExpected() throws
            InterruptedException {
        // Given
        KafkaEventSource<Integer, String> source = createSource();
        try {
            // When
            Event<Integer, String> event = source.poll(Duration.ofSeconds(3));
            Assert.assertNull(event);
            Thread.sleep(7500);
            sendTestEvent();

            // Then
            event = source.poll(Duration.ofSeconds(3));
            verifyTestEvent(event);

            // And
            verifyLogging(consumerCoordinatorLogger, Level.WARN, "consumer poll timeout has expired");
            verifyLogging(eventSourceLogger, Level.WARN, "Failed to commit offsets");
            verifyLogging(readPolicyLogger, Level.INFO, "Assigned 1 partitions for Kafka topic(s) tests",
                          "Revoked 1 partitions for Kafka topic(s) tests");
        } finally {
            source.close();
        }
    }

    private void sendTestEvent() {
        try (KafkaSink<Integer, String> sink = createSink()) {
            sink.send(new SimpleEvent<>(Collections.emptyList(), 1, "test"));
        }
    }

    private KafkaEventSource<Integer, String> createSource() {
        return createSource(x -> x);
    }

    private KafkaEventSource<Integer, String> createSource(
            Function<KafkaEventSource.Builder<Integer, String>, KafkaEventSource.Builder<Integer, String>> customiser) {
        return customiser.apply(KafkaEventSource.<Integer, String>create()
                                                .bootstrapServers(this.kafka.getBootstrapServers())
                                                .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                .consumerConfig(this.kafka.getClientProperties())
                                                .consumerConfig(
                                                        ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                                                        "5000")
                                                .consumerConfig(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000")
                                                .consumerGroup(
                                                        "long-poll-delay" + GROUP_ID.incrementAndGet())
                                                .keyDeserializer(IntegerDeserializer.class)
                                                .valueDeserializer(StringDeserializer.class))
                         .build();
    }

    @Test
    public void givenKafkaSource_whenLongDelayBetweenPollExceedsSessionTimeout_thenReceivesSubsequentEvents_andLoggingAsExpected() throws
            InterruptedException {
        // Given
        KafkaEventSource<Integer, String> source = createSource();
        try {
            // When
            Event<Integer, String> event = source.poll(Duration.ofSeconds(3));
            Assert.assertNull(event);
            Thread.sleep(12500);
            sendTestEvent();

            // Then
            event = source.poll(Duration.ofSeconds(3));
            verifyTestEvent(event);

            // And
            verifyLogging(consumerCoordinatorLogger, Level.WARN, "consumer poll timeout has expired");
            verifyLogging(eventSourceLogger, Level.WARN, "Failed to commit offsets");
        } finally {
            source.close();
        }
    }

    private static void verifyTestEvent(Event<Integer, String> event) {
        Assert.assertNotNull(event);
        Assert.assertEquals(event.key(), 1);
        Assert.assertEquals(event.value(), "test");
    }

    private @NotNull KafkaSink<Integer, String> createSink() {
        return KafkaSink.<Integer, String>create()
                        .bootstrapServers(this.kafka.getBootstrapServers())
                        .topic(KafkaTestCluster.DEFAULT_TOPIC)
                        .producerConfig(this.kafka.getClientProperties())
                        .keySerializer(IntegerSerializer.class)
                        .valueSerializer(StringSerializer.class)
                        .noLinger()
                        .noAsync()
                        .build();
    }

    @Test
    public void givenKafkaSource_whenLongDelayBetweenPollAndCommit_thenRereadsEvent_andLoggingAsExpected() throws
            InterruptedException {
        // Given
        KafkaEventSource<Integer, String> source = createSource(AbstractKafkaEventSourceBuilder::commitOnProcessed);
        sendTestEvent();
        try {
            // When
            Event<Integer, String> event = source.poll(Duration.ofSeconds(3));
            verifyTestEvent(event);
            Thread.sleep(7500);
            source.processed(List.of(event));

            // Then
            // NB - In this scenario we failed to commit and when we rejoined the group we have no committed offsets so
            //      we re-read from our previous starting point
            event = source.poll(Duration.ofSeconds(3));
            verifyTestEvent(event);

            // And
            verifyLogging(consumerCoordinatorLogger, Level.WARN, "consumer poll timeout has expired");
            verifyLogging(eventSourceLogger, Level.WARN, "Failed to commit offsets");
            verifyLogging(readPolicyLogger, Level.INFO, "Assigned 1 partitions for Kafka topic(s) tests",
                          "Revoked 1 partitions for Kafka topic(s) tests");
        } finally {
            source.close();
        }
    }

    //TODO
    // flaky
    @Test
    public void givenKafkaSource_whenDecreasingDelayBetweenPollAndCommit_thenRereadsEventUntilDelayShortEnough_andLoggingAsExpected() throws
            InterruptedException {
        // Given
        KafkaEventSource<Integer, String> source = createSource(AbstractKafkaEventSourceBuilder::commitOnProcessed);
        sendTestEvent();
        try {
            // When
            Event<Integer, String> event = source.poll(Duration.ofSeconds(3));
            verifyTestEvent(event);
            for (long delay = 7500; delay > 0; delay -= 2500) {
                Thread.sleep(delay + 50);
                source.processed(List.of(event));

                // Then
                // NB - In this scenario if the delay was too long we would have failed to commit, then on rejoin
                //      re-read the event again.  If the delay was short enough commit would be successful and we'd
                //      reach the end of the topic
                event = source.poll(Duration.ofSeconds(3));
                if (delay >= 5000) {
                    verifyTestEvent(event);
                } else {
                    // Once delay is short enough should no longer read event as commit will be successful
                    Assert.assertNull(event);
                    break;
                }
            }

            // And
            verifyLogging(consumerCoordinatorLogger, Level.WARN, "consumer poll timeout has expired");
            verifyLogging(eventSourceLogger, Level.WARN, "Failed to commit offsets");
            verifyLogging(readPolicyLogger, Level.INFO, "Assigned 1 partitions for Kafka topic(s) tests",
                          "Revoked 1 partitions for Kafka topic(s) tests");
        } finally {
            source.close();
        }
    }

    @Test
    public void givenKafkaSource_whenLongDelayAfterCommit_thenNoFurtherEvents_andLoggingAsExpected() throws
            InterruptedException {
        // Given
        KafkaEventSource<Integer, String> source = createSource(AbstractKafkaEventSourceBuilder::commitOnProcessed);
        sendTestEvent();
        try {
            // When
            Event<Integer, String> event = source.poll(Duration.ofSeconds(3));
            verifyTestEvent(event);
            source.processed(List.of(event));
            Thread.sleep(7500);

            // Then
            // NB - In this scenario we would have committed our offsets but then been booted from the group due to our
            //      delay before calling poll() again, however since our offsets were committed we won't re-read the
            //      event again
            event = source.poll(Duration.ofSeconds(3));
            Assert.assertNull(event);

            // And
            verifyLogging(consumerCoordinatorLogger, Level.WARN, "consumer poll timeout has expired");
            verifyLogging(readPolicyLogger, Level.INFO, "Assigned 1 partitions for Kafka topic(s) tests",
                          "Revoked 1 partitions for Kafka topic(s) tests");
        } finally {
            source.close();
        }
    }
}
