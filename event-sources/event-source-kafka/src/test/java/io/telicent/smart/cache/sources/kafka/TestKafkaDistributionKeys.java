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

import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.projectors.sinks.events.DistributionKeySink;
import io.telicent.smart.cache.sources.DistributionIds;
import io.telicent.smart.cache.sources.DistributionKeyStrategy;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("java:S6213")
public class TestKafkaDistributionKeys {

    private static final String DISTRIBUTION_ID = "https://telicent.io/datasets/acled#2026-08-release";
    private static final String OTHER_DISTRIBUTION_ID = "https://telicent.io/datasets/bbcm#2026-08-release";

    private static Event<Bytes, String> event(Bytes key, String distributionIdHeader) {
        List<EventHeader> headers = distributionIdHeader != null ? List.of(
                new Header(TelicentHeaders.DISTRIBUTION_ID, distributionIdHeader)) : Collections.emptyList();
        return new SimpleEvent<>(headers, key, "value");
    }

    private static Bytes bytes(String value) {
        return Bytes.wrap(value.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void givenBytesKey_whenDecoding_thenDistributionId() {
        // Given, When and Then - MUST decode the wrapped bytes, Bytes.toString() is a debug representation
        Assert.assertEquals(KafkaDistributionKeys.fromKey(bytes(DISTRIBUTION_ID)), DISTRIBUTION_ID);
    }

    @Test
    public void givenCompositeBytesKey_whenDecoding_thenSuffixIsStripped() {
        // Given
        Bytes key = bytes(DISTRIBUTION_ID + DistributionIds.KEY_SEPARATOR + UUID.randomUUID());

        // When and Then
        Assert.assertEquals(KafkaDistributionKeys.fromKey(key), DISTRIBUTION_ID);
    }

    @Test
    public void givenNonUtf8BytesKey_whenDecoding_thenNull() {
        // Given
        Bytes key = Bytes.wrap(new byte[] { (byte) 0xC3, (byte) 0x28 });

        // When and Then
        Assert.assertNull(KafkaDistributionKeys.fromKey(key));
    }

    @Test
    public void givenNonBytesKey_whenDecoding_thenDelegatesToDistributionIds() {
        // Given, When and Then
        Assert.assertEquals(KafkaDistributionKeys.fromKey(DISTRIBUTION_ID), DISTRIBUTION_ID);
        Assert.assertNull(KafkaDistributionKeys.fromKey(UUID.randomUUID()));
        Assert.assertNull(KafkaDistributionKeys.fromKey(null));
    }

    @Test
    public void givenEventWithBytesKeyAndHeader_whenResolving_thenKeyWins() {
        // Given
        Event<Bytes, String> event = event(bytes(DISTRIBUTION_ID), OTHER_DISTRIBUTION_ID);

        // When and Then
        Assert.assertEquals(KafkaDistributionKeys.resolve(event), DISTRIBUTION_ID);
    }

    @Test
    public void givenEventWithHeaderOnly_whenResolving_thenHeaderIsUsed() {
        // Given - i.e. an event from a pipeline that predates message keys
        Event<Bytes, String> event = event(null, DISTRIBUTION_ID);

        // When and Then
        Assert.assertEquals(KafkaDistributionKeys.resolve(event), DISTRIBUTION_ID);
    }

    @Test
    public void givenEventWithNeither_whenResolving_thenNull() {
        // Given, When and Then
        Assert.assertNull(KafkaDistributionKeys.resolve(event(null, null)));
        Assert.assertNull(KafkaDistributionKeys.resolve((Event<?, ?>) null));
        Assert.assertNull(KafkaDistributionKeys.resolve((ConsumerRecord<?, ?>) null));
    }

    private static ConsumerRecord<Bytes, String> record(Bytes key, String distributionIdHeader) {
        ConsumerRecord<Bytes, String> record = new ConsumerRecord<>("knowledge", 0, 0L, key, "value");
        if (distributionIdHeader != null) {
            record.headers()
                  .add(TelicentHeaders.DISTRIBUTION_ID, distributionIdHeader.getBytes(StandardCharsets.UTF_8));
        }
        return record;
    }

    @Test
    public void givenRecordWithKeyAndHeader_whenResolving_thenKeyWins() {
        // Given, When and Then
        Assert.assertEquals(KafkaDistributionKeys.resolve(record(bytes(DISTRIBUTION_ID), OTHER_DISTRIBUTION_ID)),
                            DISTRIBUTION_ID);
    }

    @Test
    public void givenRecordWithHeaderOnly_whenResolving_thenHeaderIsUsed() {
        // Given, When and Then
        Assert.assertEquals(KafkaDistributionKeys.resolve(record(null, DISTRIBUTION_ID)), DISTRIBUTION_ID);
    }

    @Test
    public void givenRecordWithCompositeKey_whenResolving_thenSuffixIsStripped() {
        // Given
        Bytes key = bytes(DISTRIBUTION_ID + DistributionIds.KEY_SEPARATOR + UUID.randomUUID());

        // When and Then
        Assert.assertEquals(KafkaDistributionKeys.resolve(record(key, null)), DISTRIBUTION_ID);
    }

    @Test
    public void givenRecordWithNeither_whenResolving_thenNull() {
        // Given, When and Then
        Assert.assertNull(KafkaDistributionKeys.resolve(record(null, null)));
    }

    @Test
    public void givenRecordWithBlankHeader_whenResolving_thenNull() {
        // Given, When and Then
        Assert.assertNull(KafkaDistributionKeys.resolve(record(null, "   ")));
    }

    @Test
    public void givenDistributionId_whenEncodingBytesKey_thenWireCompatibleWithStringSerializer() {
        // Given, When
        Bytes key = KafkaDistributionKeys.toBytesKey(DISTRIBUTION_ID, DistributionKeyStrategy.DISTRIBUTION_ID);

        // Then - this equality is what lets a Bytes keyed producer and a String keyed consumer interoperate
        Assert.assertNotNull(key);
        Assert.assertEquals(key.get(), DISTRIBUTION_ID.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void givenNoDistributionId_whenEncodingBytesKey_thenNull() {
        // Given, When and Then
        Assert.assertNull(KafkaDistributionKeys.toBytesKey(null, DistributionKeyStrategy.DISTRIBUTION_ID));
    }

    @Test
    public void givenBytesKeySink_whenSendingHeaderOnlyEvent_thenKeyIsSet() {
        // Given
        try (CollectorSink<Event<Bytes, String>> collector = CollectorSink.of()) {
            DistributionKeySink.Builder<Bytes, String> builder = KafkaDistributionKeys.bytesKeySink();
            DistributionKeySink<Bytes, String> sink = builder.destination(collector).build();
            try {
                // When
                sink.send(event(null, DISTRIBUTION_ID));

                // Then - assert before closing the transforming sink, which closes its destination.
                Event<Bytes, String> output = collector.get().get(0);
                Assert.assertEquals(output.key(), bytes(DISTRIBUTION_ID));
                Assert.assertEquals(output.lastHeader(TelicentHeaders.DISTRIBUTION_ID), DISTRIBUTION_ID);
            } finally {
                sink.close();
            }
        }
    }

    @Test
    public void givenBytesKeySink_whenSendingAlreadyKeyedEvent_thenKeyIsPreserved() {
        // Given
        try (CollectorSink<Event<Bytes, String>> collector = CollectorSink.of()) {
            DistributionKeySink.Builder<Bytes, String> builder = KafkaDistributionKeys.bytesKeySink();
            DistributionKeySink<Bytes, String> sink = builder.destination(collector).build();
            try {
                // When - the Bytes aware resolver MUST see this key, otherwise the header would win
                sink.send(event(bytes(DISTRIBUTION_ID), OTHER_DISTRIBUTION_ID));

                // Then
                Assert.assertEquals(collector.get().get(0).key(), bytes(DISTRIBUTION_ID));
            } finally {
                sink.close();
            }
        }
    }

    @Test
    public void givenStringKeySink_whenSendingHeaderOnlyEvent_thenKeyIsSet() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            DistributionKeySink.Builder<String, String> builder = KafkaDistributionKeys.stringKeySink();
            DistributionKeySink<String, String> sink = builder.destination(collector).build();
            try {
                // When
                List<EventHeader> headers = List.of(new Header(TelicentHeaders.DISTRIBUTION_ID, DISTRIBUTION_ID));
                sink.send(new SimpleEvent<String, String>(headers, null, "value"));

                // Then
                Assert.assertEquals(collector.get().get(0).key(), DISTRIBUTION_ID);
            } finally {
                sink.close();
            }
        }
    }
}
