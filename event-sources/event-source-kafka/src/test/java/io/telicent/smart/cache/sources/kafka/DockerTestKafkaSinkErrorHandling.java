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

import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.RawHeader;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

// java:S2925 - Thread.sleep is required when waiting on real Kafka/Docker in integration tests
// java:S3577 - test support class, not a test class - no tests to run
@SuppressWarnings({ "java:S2925", "java:S3577" })
public class DockerTestKafkaSinkErrorHandling {

    private final KafkaTestCluster kafka = new BasicKafkaTestCluster();
    private final AtomicInteger consumerId = new AtomicInteger(0);

    protected static final int ONE_MB = 1024 * 1024;
    /**
     * An event where the value is intentionally above Kafka's default record size limit so should always result in a
     * producer error
     */
    private static final SimpleEvent<Integer, Bytes> TOO_LARGE_EVENT =
            new SimpleEvent<>(Collections.emptyList(), 1, Bytes.wrap(new byte[ONE_MB * 2]));

    /**
     * An event where the value is very near Kafka's default record size limit so may fail to send depending on how we
     * modify it, e.g., by adding extra headers
     */
    private static final SimpleEvent<Integer, Bytes> NEARLY_TOO_LARGE_EVENT =
            new SimpleEvent<>(Collections.emptyList(), 1, Bytes.wrap(new byte[ONE_MB - (4 * 1024)]));

    @BeforeClass
    public void setup() {
        Utils.logTestClassStarted(DockerTestKafkaSinkErrorHandling.class);
        this.kafka.setup();
    }

    @AfterMethod
    public void cleanup() {
        this.kafka.resetTestTopic();
    }

    @AfterClass
    public void teardown() {
        this.kafka.teardown();
        Utils.logTestClassFinished(DockerTestKafkaSinkErrorHandling.class);
    }

    private KafkaSink.KafkaSinkBuilder<Integer, Bytes> getBuilder() {
        Properties props = new Properties();
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);

        return KafkaSink.<Integer, Bytes>create()
                        .bootstrapServers(this.kafka.getBootstrapServers())
                        .topic(KafkaTestCluster.DEFAULT_TOPIC)
                        .keySerializer(IntegerSerializer.class)
                        .valueSerializer(BytesSerializer.class)
                        .producerConfig(props)
                        .producerConfig(this.kafka.getClientProperties());
    }

    private KafkaEventSource<Integer, Bytes> getSource() {
        Properties props = new Properties();
        props.put(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG, 5000);

        return KafkaEventSource.<Integer, Bytes>create()
                               .bootstrapServers(this.kafka.getBootstrapServers())
                               .topic(KafkaTestCluster.DEFAULT_TOPIC)
                               .keyDeserializer(IntegerDeserializer.class)
                               .valueDeserializer(BytesDeserializer.class)
                               .consumerGroup("error-handling-" + consumerId.incrementAndGet())
                               .consumerConfig(props)
                               .consumerConfig(this.kafka.getClientProperties())
                               .fromBeginning()
                               // NB - Because the DLQ retry handler strips the value these events become tombstones
                               //      which our KafkaEventSource ignores by default so have to explicitly not ignore
                               //      them in order to test that our DLQ retry handler is working
                               .ignoreTombstones(false)
                               .build();
    }

    @Test
    public void givenKafkaSink_whenSendingEvent_thenOk() {
        // Given
        try (KafkaSink<Integer, Bytes> sink = getBuilder().noAsync().build()) {
            // When
            sink.send(NEARLY_TOO_LARGE_EVENT);

            // Then
            KafkaEventSource<Integer, Bytes> source = this.getSource();
            Event<Integer, Bytes> event = source.poll(Duration.ofSeconds(5));
            Assert.assertNotNull(event);
        }
    }

    @Test
    public void givenKafkaSink_whenSendingEventMadeTooLargeByHeaders_thenFails() {
        // Given
        try (KafkaSink<Integer, Bytes> sink = getBuilder().noAsync().build()) {
            // When and Then
            Assert.assertThrows(SinkException.class, () -> sink.send(NEARLY_TOO_LARGE_EVENT.addHeaders(
                    Stream.of(new RawHeader(TelicentHeaders.DEAD_LETTER_REASON, new byte[1024 * 32])))));
        }
    }

    @Test
    public void givenKafkaSinkWithDlqRetryHandler_whenSendingEventMadeTooLargeByOtherHeaders_thenSendsWithoutValue() {
        // Given
        try (KafkaSink<Integer, Bytes> sink = getBuilder().noAsync().forDlq().build()) {
            // When
            sink.send(NEARLY_TOO_LARGE_EVENT.addHeaders(
                    Stream.of(new RawHeader("Test", new byte[1024 * 32]))));

            // Then
            verifySentWithoutValue("Test");
        }
    }

    @Test
    public void givenKafkaSinkWithDlqRetryHandler_whenSendingEventMadeTooLargeByDlqHeaders_thenSendsWithoutValue() {
        // Given
        try (KafkaSink<Integer, Bytes> sink = getBuilder().async().forDlq().build()) {
            // When
            sink.send(NEARLY_TOO_LARGE_EVENT.addHeaders(Stream.of(
                    new RawHeader(TelicentHeaders.DEAD_LETTER_REASON, "The reason".getBytes(StandardCharsets.UTF_8)),
                    new RawHeader(TelicentHeaders.DEAD_LETTER_EXCEPTION_CLASS, new byte[1024 * 32]))));

            // Then
            verifySentWithoutValue(TelicentHeaders.DEAD_LETTER_REASON, TelicentHeaders.DEAD_LETTER_EXCEPTION_CLASS);
        }
    }

    private void verifySentWithoutValue(String... expectedHeaders) {
        KafkaEventSource<Integer, Bytes> source = this.getSource();
        try {
            Event<Integer, Bytes> event = source.poll(Duration.ofSeconds(10));
            Assert.assertNotNull(event, "No events available on DLQ topic");
            Assert.assertNull(event.value(),
                              "DLQ Retry Handler should have stripped the value to allow the event to send");
            for (String header : expectedHeaders) {
                Assert.assertNotNull(event.lastRawHeader(header), "Expected a " + header + " present on event");
            }
        } finally {
            source.close();
        }
    }

    @Test
    public void givenKafkaSink_whenSendingTooLargeEventToSink_thenSendFailsImmediately() {
        // Given
        try (KafkaSink<Integer, Bytes> sink = getBuilder().async().build()) {
            // When and Then
            Assert.assertThrows(SinkException.class, () -> sink.send(TOO_LARGE_EVENT));
        }
    }

    @Test(expectedExceptions = SinkException.class)
    public void givenKafkaSink_whenSendingTooLargeEventToSinkSynchronously_thenSendFails() {
        // Given
        try (KafkaSink<Integer, Bytes> sink = getBuilder().noAsync().build()) {
            // When and Then
            sink.send(TOO_LARGE_EVENT);
        }
    }

    @Test
    public void givenKafkaSinkAndCustomCallback_whenSendingToSink_thenSendSucceeds_andCallbackInvoked() {
        // Given
        TestKafkaSinkErrorHandling.TrackerCallback tracker = new TestKafkaSinkErrorHandling.TrackerCallback();

        try (KafkaSink<Integer, Bytes> sink = getBuilder().async(tracker).build()) {
            // When and Then
            sink.send(TOO_LARGE_EVENT);
        }

        // And
        Assert.assertEquals(tracker.failure.get(), 1);
        Assert.assertEquals(tracker.errors.size(), 1);
        Assert.assertTrue(tracker.errors.getFirst() instanceof RecordTooLargeException);
    }

    @Test
    public void givenKafkaSinkAndCustomRetryHandler_whenSendingTooLargeEventToSink_thenSendFails_andSendWasRetried() {
        // Given
        TestKafkaSinkErrorHandling.TrackerRetry<Integer, Bytes> retryHandler = new TestKafkaSinkErrorHandling.TrackerRetry<>();
        try (KafkaSink<Integer, Bytes> sink = getBuilder().async().retryHandler(retryHandler).build()) {
            // When and Then
            Assert.assertThrows(SinkException.class, () -> sink.send(TOO_LARGE_EVENT));

            // And
            Assert.assertEquals(retryHandler.retries.get(), 3);
        }
    }

    @Test
    public void givenKafkaSinkAndCustomRetryHandler_whenSendingTooLargeEventToSinkSynchronously_thenSendFails_andSendWasRetried() {
        // Given
        TestKafkaSinkErrorHandling.TrackerRetry<Integer, Bytes> retryHandler = new TestKafkaSinkErrorHandling.TrackerRetry<>();
        try (KafkaSink<Integer, Bytes> sink = getBuilder().noAsync().retryHandler(retryHandler).build()) {
            // When and Then
            Assert.assertThrows(SinkException.class, () -> sink.send(TOO_LARGE_EVENT));

            // And
            Assert.assertEquals(retryHandler.retries.get(), 3);
        }
    }
}
