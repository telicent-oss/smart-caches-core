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
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.RawHeader;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.RecordBatch;
import org.apache.kafka.common.record.TimestampType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Tests for the {@link KafkaEvent} mutation API and for {@code hashCode()}.
 * <p>
 * {@link KafkaEvent#equals(Object)} compares against the {@link Event} interface rather than against
 * {@code KafkaEvent}, so two different {@link Event} implementations can be equal.  {@code hashCode()} must therefore
 * agree across implementations, which is asserted here against {@link SimpleEvent}.
 * </p>
 */
@SuppressWarnings("java:S119")
public class TestKafkaEventMutations {

    private static final String TEST_TOPIC = "test";
    private static final int TEST_PARTITION = 0;
    private static final long TEST_OFFSET = 0L;
    private static final int TEST_KEY = 1234;
    private static final String TEST_VALUE = "value";

    private static <TKey, TValue> ConsumerRecord<TKey, TValue> consumerRecord(TKey key, TValue value, Headers headers) {
        return new ConsumerRecord<>(TEST_TOPIC, TEST_PARTITION, TEST_OFFSET, RecordBatch.NO_TIMESTAMP,
                                    TimestampType.NO_TIMESTAMP_TYPE, ConsumerRecord.NULL_SIZE, ConsumerRecord.NULL_SIZE,
                                    key, value, headers, Optional.empty());
    }

    /**
     * @param keysAndValues Alternating header keys and values
     * @return Kafka headers
     */
    private static Headers kafkaHeaders(String... keysAndValues) {
        RecordHeaders headers = new RecordHeaders();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            headers.add(new RecordHeader(keysAndValues[i], keysAndValues[i + 1].getBytes(StandardCharsets.UTF_8)));
        }
        return headers;
    }

    private static KafkaEvent<Integer, String> kafkaEvent(Headers headers) {
        return new KafkaEvent<>(consumerRecord(TEST_KEY, TEST_VALUE, headers), null);
    }

    private static EventHeader header(String key, String value) {
        return new RawHeader(key, value.getBytes(StandardCharsets.UTF_8));
    }

    private static List<EventHeader> headersOf(Event<?, ?> event) {
        return event.headers().toList();
    }

    @Test
    public void givenRepeatedHeader_whenGettingLastRawHeader_thenTheLastValueIsReturned() {
        KafkaEvent<Integer, String> event = kafkaEvent(kafkaHeaders("Content-Type", "text/plain",
                                                                   "Content-Type", "application/json"));

        EventHeader last = event.lastRawHeader("Content-Type");

        Assert.assertNotNull(last);
        Assert.assertEquals(last.key(), "Content-Type");
        Assert.assertEquals(last.value(), "application/json");
    }

    @Test
    public void givenNoMatchingHeader_whenGettingLastRawHeader_thenNullIsReturned() {
        KafkaEvent<Integer, String> event = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));

        Assert.assertNull(event.lastRawHeader("Accept"));
    }

    @Test
    public void givenEvent_whenReplacingKey_thenOnlyTheKeyChanges() {
        KafkaEvent<Integer, String> event = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));

        Event<String, String> replaced = event.replaceKey("new-key");

        Assert.assertEquals(replaced.key(), "new-key");
        Assert.assertEquals(replaced.value(), TEST_VALUE);
        Assert.assertEquals(headersOf(replaced), headersOf(event));
    }

    @Test
    public void givenEvent_whenReplacingValue_thenOnlyTheValueChanges() {
        KafkaEvent<Integer, String> event = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));

        Event<Integer, Integer> replaced = event.replaceValue(4321);

        Assert.assertEquals(replaced.key(), Integer.valueOf(TEST_KEY));
        Assert.assertEquals(replaced.value(), Integer.valueOf(4321));
        Assert.assertEquals(headersOf(replaced), headersOf(event));
    }

    @Test
    public void givenEvent_whenReplacingKeyAndValue_thenBothChange() {
        KafkaEvent<Integer, String> event = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));

        Event<String, Integer> replaced = event.replace("new-key", 4321);

        Assert.assertEquals(replaced.key(), "new-key");
        Assert.assertEquals(replaced.value(), Integer.valueOf(4321));
        Assert.assertEquals(headersOf(replaced), headersOf(event));
    }

    @Test
    public void givenEvent_whenReplacingHeaders_thenOnlyTheNewHeadersRemain() {
        KafkaEvent<Integer, String> event = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));

        Event<Integer, String> replaced = event.replaceHeaders(Stream.of(header("Accept", "application/json")));

        Assert.assertEquals(headersOf(replaced), List.of(header("Accept", "application/json")));
        Assert.assertEquals(replaced.key(), Integer.valueOf(TEST_KEY));
        Assert.assertEquals(replaced.value(), TEST_VALUE);
    }

    @Test
    public void givenEvent_whenAddingHeaders_thenOriginalAndNewHeadersArePresent() {
        KafkaEvent<Integer, String> event = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));

        Event<Integer, String> added = event.addHeaders(Stream.of(header("Accept", "application/json")));

        Assert.assertEquals(headersOf(added),
                            List.of(header("Content-Type", "text/plain"), header("Accept", "application/json")));
    }

    @Test
    public void givenEventsWithEqualContent_whenHashing_thenHashCodesMatch() {
        KafkaEvent<Integer, String> a = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));
        KafkaEvent<Integer, String> b = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));

        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void givenEqualEventsOfDifferentImplementations_whenHashing_thenHashCodesMatch() {
        // equals() compares against the Event interface, so these two are equal despite being different
        // implementations - which means hashCode() has to agree as well, otherwise any Set/Map of events misbehaves
        KafkaEvent<Integer, String> kafka = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));
        SimpleEvent<Integer, String> simple =
                new SimpleEvent<>(List.of(header("Content-Type", "text/plain")), TEST_KEY, TEST_VALUE);

        Assert.assertEquals(kafka, simple);
        Assert.assertEquals(simple, kafka);
        Assert.assertEquals(kafka.hashCode(), simple.hashCode());
    }

    @Test
    public void givenEventsWithDifferentValues_whenComparing_thenTheyAreNotEqual() {
        KafkaEvent<Integer, String> event = kafkaEvent(kafkaHeaders("Content-Type", "text/plain"));
        Event<Integer, String> other = event.replaceValue("something-else");

        Assert.assertNotEquals(event, other);
    }
}
