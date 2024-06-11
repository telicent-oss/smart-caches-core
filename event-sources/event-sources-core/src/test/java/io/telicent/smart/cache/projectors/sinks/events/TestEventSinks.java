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
package io.telicent.smart.cache.projectors.sinks.events;

import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.projectors.sinks.Sinks;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestEventSinks {

    public static final List<String> KEYS = Arrays.asList("a", "b", "c", "d", "e");

    @Test
    public void event_key_01() {
        CollectorSink<String> collector = CollectorSink.of();
        EventKeySink<String, String> sink = new EventKeySink<>(collector);

        KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5))));

        List<String> actual = collector.get();
        Assert.assertEquals(actual.size(), KEYS.size());
        Assert.assertEquals(actual, KEYS);
    }

    @Test
    public void event_value_01() {
        List<String> keys = Arrays.asList("a", "b", "c", "d", "e");
        CollectorSink<String> collector = CollectorSink.of();
        EventValueSink<String, String> sink = new EventValueSink<>(collector);

        keys.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5))));

        List<String> actual = collector.get();
        Assert.assertEquals(actual.size(), keys.size());
        Assert.assertNotEquals(actual, keys);
    }

    @Test
    public void builders_01() {
        try (EventKeySink<String, String> sink =
                EventKeySink.<String, String>create()
                            .destination(Sinks.<String>collect().build())
                            .build()) {
            KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5))));
        }
    }

    @Test
    public void builders_02() {
        try (EventValueSink<String, String> sink =
                EventValueSink.<String, String>create()
                            .destination(Sinks.<String>collect().build())
                            .build()) {
            KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5))));
        }
    }

    @SuppressWarnings("resource")
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*>= 1")
    public void processed_bad_01() {
        new EventProcessedSink<>(-1);
    }

    @Test
    public void processed_01() {
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create().noBatching().build()) {
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());
            KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5), source)));
            Assert.assertEquals(sink.incompleteBatches(), 0);
            Assert.assertEquals(sink.batchedEvents(), 0);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*has been closed")
    public void processed_02() {
        try(EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create().build()) {
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());
            source.close();
            KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5), source)));
        }
    }

    @Test
    public void processed_03() {
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create().batchSize(100).build()) {
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());
            KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5), source)));
            Assert.assertEquals(sink.incompleteBatches(), 1);
            Assert.assertEquals(sink.batchedEvents(), KEYS.size());
        }
    }

    @Test
    public void processed_04() {
        try(EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create().batchSize(3).build()) {
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());
            KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5), source)));
            Assert.assertEquals(sink.incompleteBatches(), 1);
            Assert.assertEquals(sink.batchedEvents(), KEYS.size() - 3);
        }
    }

    @Test
    public void processed_05() {
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create().noBatching().build()) {
            KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5), null)));
            Assert.assertEquals(sink.incompleteBatches(), 0);
            Assert.assertEquals(sink.batchedEvents(), 0);
        }
    }

    @Test
    public void processed_06() {
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create().batchSize(KEYS.size()).build()) {
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());
            KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5), source)));
            Assert.assertEquals(sink.incompleteBatches(), 0);
            Assert.assertEquals(sink.batchedEvents(), 0);
        }
    }

    @Test
    public void processed_07() {
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create().batchSize(10).build()) {
            Assert.assertEquals(sink.incompleteBatches(), 0);
            Assert.assertEquals(sink.batchedEvents(), 0);
        }
    }
}
