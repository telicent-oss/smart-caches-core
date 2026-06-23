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
package io.telicent.smart.cache.sources.kafka.tombstones;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.Utils;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.common.serialization.*;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test suite that ensures that tombstones are correctly ignored or made visible per configuration, it also serves to
 * validate that a given Kafka Serializer and Deserializer combination correctly handles {@code null} values
 *
 * @param <TValue> Value type
 */
public abstract class AbstractKafkaTombstoneTests<TValue> {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private BasicKafkaTestCluster kafka;

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
    }

    protected abstract TValue exemplarValue();

    protected abstract Class<? extends Deserializer<TValue>> valueDeserializerClass();

    protected abstract Class<? extends Serializer<TValue>> valueSerializerClass();

    protected <TKey> Event<TKey, TValue> event(TKey key, TValue value, Collection<EventHeader> headers) {
        return new SimpleEvent<>(headers, key, value);
    }

    protected <TKey> Event<TKey, TValue> tombstone(TKey key) {
        return new SimpleEvent<>(Collections.emptyList(), key, null);
    }

    protected <TKey> void insertTestEvents(Class<? extends Serializer<TKey>> keySerializerClass,
                                           Class<? extends Serializer<TValue>> valueSerializerClass,
                                           List<Event<TKey, TValue>> events) {
        try (KafkaSink<TKey, TValue> sink = KafkaSink.<TKey, TValue>create()
                                                     .bootstrapServers(this.kafka.getBootstrapServers())
                                                     .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                     .keySerializer(keySerializerClass)
                                                     .valueSerializer(valueSerializerClass)
                                                     .lingerMs(5)
                                                     .build()) {
            for (Event<TKey, TValue> event : events) {
                sink.send(event);
            }
        }
    }

    private <TKey> KafkaEventSource<TKey, TValue> source(Class<? extends Deserializer<TKey>> keyDeserializerClass,
                                                         Class<? extends Deserializer<TValue>> valueDeserializerClass,
                                                         boolean ignoreTombstones) {
        return KafkaEventSource.<TKey, TValue>create()
                               .bootstrapServers(this.kafka.getBootstrapServers())
                               .consumerConfig(this.kafka.getClientProperties())
                               .topic(KafkaTestCluster.DEFAULT_TOPIC)
                               .consumerGroup("tombstones-" + COUNTER.incrementAndGet())
                               .keyDeserializer(keyDeserializerClass)
                               .valueDeserializer(valueDeserializerClass)
                               .autoCommit()
                               .ignoreTombstones(ignoreTombstones)
                               .build();
    }

    protected <TKey> List<Event<TKey, TValue>> consumeAll(Class<? extends Deserializer<TKey>> keyDeserializerClass,
                                                          Class<? extends Deserializer<TValue>> valueDeserializerClass,
                                                          boolean ignoreTombstones) {
        EventSource<TKey, TValue> source =
                source(keyDeserializerClass, valueDeserializerClass, ignoreTombstones);
        List<Event<TKey, TValue>> events = new ArrayList<>();
        try {
            Event<TKey, TValue> next = source.poll(Duration.ofSeconds(5));
            while (next != null) {
                events.add(next);
                next = source.poll(Duration.ofSeconds(3));
            }

            return events;
        } finally {
            source.close();
        }

    }

    protected List<Event<String, TValue>> manyEvents(String baseKey, TValue value, int total) {
        List<Event<String, TValue>> events = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            events.add(event(baseKey + "/" + i, value, List.of()));
        }
        return events;
    }

    protected void manyTombstones(List<String> keys, List<Event<String, TValue>> events) {
        for (String key : keys) {
            events.add(event(key, null, List.of()));
        }
    }

    @Test
    public void givenTopicWithTombstones_whenConsumingWithTombstonesIgnored_thenTombstonesAreNotReturned() {
        // Given
        UUID key = UUID.randomUUID();
        insertTestEvents(UUIDSerializer.class, valueSerializerClass(),
                         List.of(event(key, exemplarValue(), List.of()), tombstone(key)));

        // When
        List<Event<UUID, TValue>> events =
                consumeAll(UUIDDeserializer.class, valueDeserializerClass(), true);

        // Then
        Assert.assertEquals(events.size(), 1);
    }

    @Test
    public void givenTopicWithTombstones_whenConsumingWithTombstonesVisible_thenTombstonesAreReturned() {
        // Given
        UUID key = UUID.randomUUID();
        insertTestEvents(UUIDSerializer.class, valueSerializerClass(),
                         List.of(event(key, exemplarValue(), List.of()), tombstone(key)));

        // When
        List<Event<UUID, TValue>> events =
                consumeAll(UUIDDeserializer.class, valueDeserializerClass(), false);

        // Then
        Assert.assertEquals(events.size(), 2);
    }

    @Test
    public void givenTopicWithManyTombstones_whenConsumingWithTombstonesIgnored_thenTombstonesAreNotReturned() {
        // Given
        String baseKey = UUID.randomUUID().toString();
        List<Event<String, TValue>> input = manyEvents(baseKey, exemplarValue(), 1_000);
        manyTombstones(input.stream().map(Event::key).toList(), input);
        insertTestEvents(StringSerializer.class, valueSerializerClass(), input);

        // When
        List<Event<String, TValue>> events =
                consumeAll(StringDeserializer.class, valueDeserializerClass(),
                           true);

        // Then
        Assert.assertEquals(events.size(), 1_000);
    }

    @Test
    public void givenTopicWithManyTombstones_whenConsumingWithTombstonesVisible_thenTombstonesAreReturned() {
        // Given
        String baseKey = UUID.randomUUID().toString();
        List<Event<String, TValue>> input = manyEvents(baseKey, exemplarValue(), 500);
        manyTombstones(input.stream().map(Event::key).toList(), input);
        insertTestEvents(StringSerializer.class, valueSerializerClass(), input);

        // When
        List<Event<String, TValue>> events =
                consumeAll(StringDeserializer.class, valueDeserializerClass(),
                           false);

        // Then
        Assert.assertEquals(events.size(), 1_000);
    }
}
