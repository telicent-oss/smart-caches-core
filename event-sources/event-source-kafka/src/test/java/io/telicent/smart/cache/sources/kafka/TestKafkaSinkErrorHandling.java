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

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaRetryHandler;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.Strings;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.event.Level;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

// java:S2925 - Thread.sleep is required when waiting on real Kafka/Docker in integration tests
// java:S119 - Generic type parameter names are used for clarity throughout these APIs
@SuppressWarnings({ "java:S2925", "java:S119" })
public class TestKafkaSinkErrorHandling {

    private static final SimpleEvent<Integer, String> EVENT = new SimpleEvent<>(Collections.emptyList(), 1, "Test");

    private final TestLogger kafkaSinkLogger = TestLoggerFactory.getTestLogger(KafkaSink.class);

    private KafkaSink.KafkaSinkBuilder<Integer, String> getBuilder() {
        Properties props = new Properties();
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000);

        return KafkaSink.<Integer, String>create()
                        .bootstrapServers("localhost:9092")
                        .topic(KafkaTestCluster.DEFAULT_TOPIC)
                        .keySerializer(IntegerSerializer.class)
                        .valueSerializer(StringSerializer.class)
                        .producerConfig(props);
    }

    @BeforeMethod
    public void setup() {
        this.kafkaSinkLogger.clearAll();
    }

    @AfterClass
    public void teardown() {
        this.kafkaSinkLogger.clearAll();
    }

    @Test
    public void givenKafkaSink_whenSendingToSink_thenSendFailsImmediately() {
        // Given
        try (KafkaSink<Integer, String> sink = getBuilder().async().build()) {
            // When and Then
            // NB - For some kinds of errors Kafka will detect them almost immediately and the async errors will be
            //      available before send() completes and be thrown immediately
            Assert.assertThrows(SinkException.class, () -> sink.send(EVENT));
        }
    }

    @Test(expectedExceptions = SinkException.class)
    public void givenKafkaSink_whenSendingToSinkSynchronously_thenSendFails() {
        // Given
        try (KafkaSink<Integer, String> sink = getBuilder().noAsync().build()) {
            // When and Then
            sink.send(EVENT);
        }
    }

    @Test
    public void givenKafkaSinkAndCustomCallback_whenSendingToSink_thenSendSucceeds_andCallbackInvoked() {
        // Given
        TrackerCallback callback = new TrackerCallback();

        try (KafkaSink<Integer, String> sink = getBuilder().async(callback).build()) {
            // When and Then
            sink.send(EVENT);
        }

        // And
        Assert.assertEquals(callback.failure.get(), 1);
        Assert.assertEquals(callback.errors.size(), 1);
    }

    @Test
    public void givenKafkaSinkAndCustomRetryHandler_whenSendingToSink_thenSendSucceeds_andMultipleRetriesHappen() {
        // Given
        TrackerRetry retryHandler = new TrackerRetry();
        try (KafkaSink<Integer, String> sink = getBuilder().async().retryHandler(retryHandler).build()) {
            // When and Then
            Assert.assertThrows(SinkException.class, () -> sink.send(EVENT));

            // And
            Awaitility.await("Kafka Sends to be retried")
                      .atMost(Duration.ofSeconds(5))
                      .until(() -> retryHandler.retries.get() == 3);
        }
    }

    @Test
    public void givenKafkaSinkAndCustomRetryHandler_whenSendingToSinkSynchronously_thenSendErrors_andMultipleRetriesHappened() {
        // Given
        TrackerRetry retryHandler = new TrackerRetry();
        try (KafkaSink<Integer, String> sink = getBuilder().noAsync().retryHandler(retryHandler).build()) {
            // When and Then
            Assert.assertThrows(SinkException.class, () -> sink.send(EVENT));

            // And
            Assert.assertEquals(retryHandler.retries.get(), 3);
        }
    }

    @Test
    public void givenKafkaSinkAndCustomRetryHandler_whenSendingToSinkSynchronously_thenSendErrors() {
        // Given
        KafkaRetryHandler retryHandler = new NeverRetry();
        try (KafkaSink<Integer, String> sink = getBuilder().noAsync().retryHandler(retryHandler).build()) {
            // When and Then
            Assert.assertThrows(SinkException.class, () -> sink.send(EVENT));
        }
    }

    @Test
    public void givenKafkaSinkAndFailingRetryHandler_whenSendingToSink_thenOriginalErrorThrown_andRetryPrepErrorLogged() {
        // Given
        KafkaRetryHandler retryHandler = new FailingRetry();
        try (KafkaSink<Integer, String> sink = getBuilder().async().retryHandler(retryHandler).build()) {
            try {
                // When
                sink.send(EVENT);
            } catch (SinkException e) {
                // Then
                Assert.assertNotEquals(e.getMessage(), FailingRetry.FAILURE_MESSAGE);
            } catch (Exception e) {
                Assert.fail("Wrong kind of exception thrown when a SinkException was expected");
            }
        }

        // And
        verifyRetryPrepErrorLogged();
    }

    @Test
    public void givenKafkaSinkAndFailingRetryHandler_whenSendingToSinkSynchronously_thenOriginalErrorThrown_andRetryPrepErrorLogged() {
        // Given
        KafkaRetryHandler retryHandler = new FailingRetry();
        try (KafkaSink<Integer, String> sink = getBuilder().noAsync().retryHandler(retryHandler).build()) {
            try {
                // When
                sink.send(EVENT);
            } catch (SinkException e) {
                // Then
                Assert.assertNotEquals(e.getMessage(), FailingRetry.FAILURE_MESSAGE);
            } catch (Exception e) {
                Assert.fail("Wrong kind of exception (" + e.getClass()
                                                           .getCanonicalName() + ") thrown when a SinkException was expected");
            }
        }

        // And
        verifyRetryPrepErrorLogged();
    }

    private void verifyRetryPrepErrorLogged() {
        Assert.assertTrue(this.kafkaSinkLogger.getAllLoggingEvents()
                                              .stream()
                                              .filter(e -> e.getLevel() == Level.WARN)
                                              .anyMatch(e -> Strings.CS.contains(e.getFormattedMessage(),
                                                                                 FailingRetry.FAILURE_MESSAGE)),
                          "The retry preparation failure should have been logged");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*not a permitted configuration.*")
    @SuppressWarnings("resource")
    public void givenKafkaSinkWithCustomCallbackAndRetryHandler_whenBuilding_thenIllegalArgument() {
        // Given, When and Then
        getBuilder().async(new TrackerCallback()).retryHandler(new TrackerRetry()).build();
    }

    public static final class TrackerCallback implements Callback {
        public final AtomicInteger success = new AtomicInteger(0);
        public final AtomicInteger failure = new AtomicInteger(0);
        public final List<Exception> errors = new ArrayList<>();

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                this.failure.incrementAndGet();
                this.errors.add(exception);
            } else {
                this.success.incrementAndGet();
            }
        }
    }

    public static final class TrackerRetry implements KafkaRetryHandler {
        public final AtomicInteger retries = new AtomicInteger(0);

        @Override
        public boolean isRetryable(Exception e) {
            return true;
        }

        @Override
        public <TKey, TValue> Event<TKey, TValue> prepareEventForRetry(Event<TKey, TValue> event, Exception e) {
            this.retries.incrementAndGet();
            return event;
        }
    }

    public static final class NeverRetry implements KafkaRetryHandler {

        @Override
        public boolean isRetryable(Exception e) {
            return false;
        }

        @Override
        public <TKey, TValue> Event<TKey, TValue> prepareEventForRetry(Event<TKey, TValue> event, Exception e) {
            return null;
        }
    }

    public static final class FailingRetry implements KafkaRetryHandler {

        public static final String FAILURE_MESSAGE = "Something went wrong";

        @Override
        public boolean isRetryable(Exception e) {
            return true;
        }

        @Override
        public <TKey, TValue> Event<TKey, TValue> prepareEventForRetry(Event<TKey, TValue> event, Exception e) {
            throw new RuntimeException(FAILURE_MESSAGE);
        }
    }
}
