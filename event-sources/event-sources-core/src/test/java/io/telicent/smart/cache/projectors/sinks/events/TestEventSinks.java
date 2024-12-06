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

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.projectors.sinks.Sinks;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestEventSinks {

    public static final List<String> KEYS = Arrays.asList("a", "b", "c", "d", "e");

    @Test
    public void givenEventKeySink_whenSendingEvents_thenOnlyKeysAreOutput() {
        // Given
        CollectorSink<String> collector = CollectorSink.of();
        EventKeySink<String, String> sink = new EventKeySink<>(collector);

        // When
        sendTestEvents(sink);

        // Then
        List<String> actual = collector.get();
        Assert.assertEquals(actual.size(), KEYS.size());
        Assert.assertEquals(actual, KEYS);
    }

    @Test
    public void givenEventValueSink_whenSendingEvents_thenOnlyValuesAreOutput() {
        // Given
        CollectorSink<String> collector = CollectorSink.of();
        EventValueSink<String, String> sink = new EventValueSink<>(collector);

        // When
        sendTestEvents(sink);

        // Then
        List<String> actual = collector.get();
        Assert.assertEquals(actual.size(), KEYS.size());
        Assert.assertNotEquals(actual, KEYS);
    }

    @Test
    public void givenEventKeySinkBuilder_whenBuilding_thenCanSendToResultingSink() {
        // Given and When
        try (EventKeySink<String, String> sink = EventKeySink.<String, String>create()
                                                             .destination(Sinks.<String>collect().build())
                                                             .build()) {
            // Then
            sendTestEvents(sink);
        }
    }

    @Test
    public void givenEventValueSinkBuilder_whenBuilding_thenCanSendToResultingSink() {
        // Given and When
        try (EventValueSink<String, String> sink = EventValueSink.<String, String>create()
                                                                 .destination(Sinks.<String>collect().build())
                                                                 .build()) {
            // Then
            sendTestEvents(sink);
        }
    }

    private static void sendTestEvents(Sink<Event<String, String>> sink) {
        KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5))));
    }

    @SuppressWarnings("resource")
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*>= 1")
    public void givenNegativeBatchSize_whenBuildingEventProcessedSink_thenIllegalArgument() {
        new EventProcessedSink<>(-1);
    }

    @Test
    public void givenEventProcessedSinkWithoutBatching_whenSendingEvents_thenNoBatchesArePresent() {
        // Given
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create()
                                                                         .noBatching()
                                                                         .build()) {
            // When
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());
            sendTestEvents(sink, source);

            // Then
            Assert.assertEquals(sink.incompleteBatches(), 0);
            Assert.assertEquals(sink.batchedEvents(), 0);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*has been closed")
    public void givenEventProcessedSinkWithClosedSource_whenSendingEvents_thenIllegalState() {
        // Given
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create().build()) {
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());
            source.close();

            // When and Then
            sendTestEvents(sink, source);
        }
    }

    @Test
    public void givenEventProcessedSinkWithBatching_whenSendingEvents_thenBatchesArePresent() {
        // Given
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create()
                                                                         .batchSize(100)
                                                                         .build()) {
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());

            // When
            sendTestEvents(sink, source);

            // Then
            Assert.assertEquals(sink.incompleteBatches(), 1);
            Assert.assertEquals(sink.batchedEvents(), KEYS.size());
        }
    }

    @Test
    public void givenEventProcessedSinkWithBatching_whenSendingMoreEventsThanBatchSize_thenSomeBatchesArePresent() {
        // Given
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create()
                                                                         .batchSize(3)
                                                                         .build()) {
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());

            // When
            sendTestEvents(sink, source);

            // Then
            Assert.assertEquals(sink.incompleteBatches(), 1);
            Assert.assertEquals(sink.batchedEvents(), KEYS.size() - 3);
        }
    }

    private static void sendTestEvents(Sink<Event<String, String>> sink, EventSource<String, String> source) {
        KEYS.forEach(k -> sink.send(new SimpleEvent<>(null, k, StringUtils.repeat(k, 5), source)));
    }

    @Test
    public void givenEventProcessedSinkWithNoSource_whenSendingEvents_thenNoBatchesPresent() {
        // Given
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create()
                                                                         .noBatching()
                                                                         .build()) {
            // When
            sendTestEvents(sink, null);

            // Then
            Assert.assertEquals(sink.incompleteBatches(), 0);
            Assert.assertEquals(sink.batchedEvents(), 0);
        }
    }

    @Test
    public void givenEventProcessedSinkWithBatching_whenSendingEventsMatchingBatchSize_thenNoBatchsPresent() {
        // Given
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create()
                                                                         .batchSize(KEYS.size())
                                                                         .build()) {
            InMemoryEventSource<String, String> source = new InMemoryEventSource<>(Collections.emptyList());

            // When
            sendTestEvents(sink, source);

            // Then
            Assert.assertEquals(sink.incompleteBatches(), 0);
            Assert.assertEquals(sink.batchedEvents(), 0);
        }
    }

    @Test
    public void givenEventProcessedSinkWithBatching_whenSendingNothing_thenNoBatchesPresent() {
        // Given
        try (EventProcessedSink<String, String> sink = EventProcessedSink.<String, String>create()
                                                                         .batchSize(10)
                                                                         .build()) {
            // When and Then
            Assert.assertEquals(sink.incompleteBatches(), 0);
            Assert.assertEquals(sink.batchedEvents(), 0);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNoGenerators_whenBuildingEventHeaderSink_thenIllegalState() {
        // Given, When and Then
        try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create().build()) {
            Assert.fail("Should have thrown an error");
        }
    }

    @Test
    public void givenEventHeaderSinkWithSingleGenerator_whenSendingEvents_thenHeaderIsAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(e -> new Header("Test", "test"))
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                collector.get().forEach(e -> Assert.assertEquals(e.lastHeader("Test"), "test"));
            }
        }
    }

    @Test
    public void givenEventHeaderSinkWithMultipleGenerators_whenSendingEvents_thenAllHeadersAreAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(e -> new Header("Test", "test"))
                                                                       .headerGenerator(e -> new Header(
                                                                               TelicentHeaders.REQUEST_ID,
                                                                               UUID.randomUUID().toString()))
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                collector.get().forEach(e -> {
                    Assert.assertEquals(e.lastHeader("Test"), "test");
                    Assert.assertEquals(e.headers(TelicentHeaders.REQUEST_ID).count(), 1);
                });
            }
        }
    }

    @Test
    public void givenEventHeaderSinkWithSingleGenerator_whenSendingEventsWithExistingHeader_thenHeaderIsAdded_andAllHeaderValuesArePreserved() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(e -> new Header("Test", "test"))
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                KEYS.forEach(k -> sink.send(
                        new SimpleEvent<>(List.of(new Header("Test", "original")), k,
                                          StringUtils.repeat(k, 5))));

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                collector.get().forEach(e -> {
                    Assert.assertEquals(e.lastHeader("Test"), "test");
                    Assert.assertEquals(e.headers("Test").count(), 2);
                    Assert.assertEquals(e.headers("Test").findFirst().orElse(null), "original");
                });
            }
        }
    }
}
