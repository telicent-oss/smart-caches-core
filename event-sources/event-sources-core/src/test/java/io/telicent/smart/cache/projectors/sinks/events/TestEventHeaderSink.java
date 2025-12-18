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
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.RawHeader;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.telicent.smart.cache.sources.TelicentHeaders.*;

public class TestEventHeaderSink extends AbstractEventSinkTests {

    public static final byte[] EMPTY_BYTES = new byte[0];

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNoGenerators_whenBuilding_thenIllegalArgument() {
        // Given, When and Then
        try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create().build()) {
            Assert.fail("Should have thrown an error");
        }
    }

    @DataProvider(name = "badHeaderParams")
    private Object[][] badHeaderParameters() {
        return new Object[][] {
                { null, null },
                { "name", null },
                { null, "value" },
                { "", "" },
                { "name", "" },
                { "", "value" }
        };
    }

    @Test(dataProvider = "badHeaderParams", expectedExceptions = IllegalArgumentException.class)
    public void givenBadHeaderParameters_whenBuildingFixedHeader_thenIllegalArgument(String name, String value) {
        // Given, When and Then
        try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                   .fixedHeader(name, value)
                                                                   .build()) {
            Assert.fail("Should have thrown an error");
        }
    }

    @Test(dataProvider = "badHeaderParams", expectedExceptions = IllegalArgumentException.class)
    public void givenBadHeaderParameters_whenBuildingFixedIfMissingHeader_thenIllegalArgument(String name,
                                                                                              String value) {
        // Given, When and Then
        try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                   .fixedHeaderIfMissing(name, value)
                                                                   .build()) {
            Assert.fail("Should have thrown an error");
        }
    }

    @DataProvider(name = "badRawHeaderParams")
    private Object[][] badRawHeaderParameters() {
        return new Object[][] {
                { null, (byte[]) null },
                { null, EMPTY_BYTES },
                { "name", (byte[]) null},
                { "name", EMPTY_BYTES },
                { null, "value".getBytes(StandardCharsets.UTF_8) },
                { "", EMPTY_BYTES },
                { "name", EMPTY_BYTES },
                { "", "value".getBytes(StandardCharsets.UTF_8) }
        };
    }

    @Test(dataProvider = "badRawHeaderParams", expectedExceptions = IllegalArgumentException.class)
    public void givenBadRawHeaderParameters_whenBuildingFixedHeader_thenIllegalArgument(String name, byte[] value) {
        // Given, When and Then
        try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                   .fixedHeader(name, value)
                                                                   .build()) {
            Assert.fail("Should have thrown an error");
        }
    }

    @Test(dataProvider = "badRawHeaderParams", expectedExceptions = IllegalArgumentException.class)
    public void givenBadRawHeaderParameters_whenBuildingFixedIfMissingHeader_thenIllegalArgument(String name,
                                                                                                 byte[] value) {
        // Given, When and Then
        try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                   .fixedHeaderIfMissing(name, value)
                                                                   .build()) {
            Assert.fail("Should have thrown an error");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNullGenerator_whenBuilding_thenIllegalArgument() {
        // Given, When and Then
        try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                   .headerGenerator(null)
                                                                   .build()) {
            Assert.fail("Should have thrown an error");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNullContentType_whenBuilding_thenIllegalArgument() {
        // Given, When and Then
        try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                   .addContentType(null)
                                                                   .build()) {
            Assert.fail("Should have thrown an error");
        }
    }

    @Test
    public void givenFixedHeader_whenSendingEvents_thenHeaderIsAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .fixedHeader("Test", "test")
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
    public void givenConditionalFixedHeader_whenSendingEvents_thenHeaderIsAddedWhenNeeded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .fixedHeader("Test", "test")
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
    public void givenSingleGenerator_whenSendingEvents_thenHeaderIsAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(e -> new Header("Test",
                                                                                                        StringUtils.repeat(
                                                                                                                e.key(),
                                                                                                                5)))
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                collector.get().forEach(e -> Assert.assertEquals(e.lastHeader("Test"), e.value()));
            }
        }
    }

    @Test
    public void givenConditionalGenerator_whenSendingEvents_thenHeaderIsAddedForSomeEvents() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(
                                                                               e -> Objects.equals(e.key(), "a") ?
                                                                                    new Header("Test", "test") : null)
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                collector.get().forEach(e -> {
                    if (Objects.equals(e.key(), "a")) {
                        Assert.assertEquals(e.lastHeader("Test"), "test");
                    } else {
                        Assert.assertNull(e.lastHeader("Test"), "Should not have added header to this event");
                    }
                });
            }
        }
    }

    @Test
    public void givenGeneratorWhichProducesNull_whenSendingEvents_thenNoHeaderIsAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(e -> null)
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                collector.get().forEach(e -> Assert.assertEquals(e.headers().count(), 0));
            }
        }
    }

    @Test
    public void givenMultipleGenerators_whenSendingEvents_thenAllHeadersAreAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(e -> new Header("Test", "test"))
                                                                       .headerGenerator(e -> new Header(REQUEST_ID,
                                                                                                        UUID.randomUUID()
                                                                                                            .toString()))
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                collector.get().forEach(e -> {
                    Assert.assertEquals(e.lastHeader("Test"), "test");
                    Assert.assertEquals(e.headers(REQUEST_ID).count(), 1);
                });
            }
        }
    }

    @Test
    public void givenNonIndependentGeneratorsOnSameSink_whenSendingEvents_thenOnlyFirstGeneratorAddsHeader() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(e -> new Header("Test", "test"))
                                                                       .headerGenerator(e -> {
                                                                           if (StringUtils.isNotBlank(
                                                                                   e.lastHeader("Test"))) {
                                                                               return new Header("Another", "value");
                                                                           }
                                                                           return null;
                                                                       })
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                collector.get().forEach(e -> {
                    Assert.assertEquals(e.lastHeader("Test"), "test");
                    Assert.assertNull(e.lastHeader("Another"));
                });
            }
        }
    }

    @Test
    public void givenNonIndependentGeneratorsOnChainedSinks_whenSendingEvents_thenBothHeadersAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(e -> new Header("Test", "test"))
                                                                       .destination(
                                                                               EventHeaderSink.<String, String>create()
                                                                                              .headerGenerator(e -> {
                                                                                                  if (StringUtils.isNotBlank(
                                                                                                          e.lastHeader(
                                                                                                                  "Test"))) {
                                                                                                      return new Header(
                                                                                                              "Another",
                                                                                                              "value");
                                                                                                  }
                                                                                                  return null;
                                                                                              })
                                                                                              .destination(collector))
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                collector.get().forEach(e -> {
                    Assert.assertEquals(e.lastHeader("Test"), "test");
                    Assert.assertEquals(e.lastHeader("Another"), "value");
                });
            }
        }
    }

    @Test
    public void givenSingleGenerator_whenSendingEventsWithExistingHeader_thenHeaderIsAdded_andAllHeaderValuesArePreserved() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .headerGenerator(e -> new Header("Test", "test"))
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink, x -> List.of(new Header("Test", "original")), () -> null);

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

    @Test
    public void givenStandardHeaders_whenSendingEvents_thenExpectedHeadersAreAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .addStandardHeaders("test")
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                Set<String> requestIds = new LinkedHashSet<>();
                collector.get().forEach(e -> {
                    Assert.assertEquals(e.lastHeader(EXEC_PATH), "test");
                    String id = e.lastHeader(REQUEST_ID);
                    Assert.assertTrue(requestIds.add(id), "All Request-ID's should have been unique");
                    Assert.assertNull(e.lastHeader(INPUT_REQUEST_ID));
                });
            }
        }
    }

    @Test
    public void givenStandardHeadersWithoutAppName_whenSendingEvents_thenExpectedHeadersAreAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .addStandardHeaders(null)
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                Set<String> requestIds = new LinkedHashSet<>();
                collector.get().forEach(e -> {
                    Assert.assertNull(e.lastHeader(EXEC_PATH));
                    String id = e.lastHeader(REQUEST_ID);
                    Assert.assertTrue(requestIds.add(id), "All Request-ID's should have been unique");
                    Assert.assertNull(e.lastHeader(INPUT_REQUEST_ID));
                });
            }
        }
    }

    @Test
    public void givenStandardHeaders_whenSendingEventsWithExistingRequestId_thenExpectedHeadersAreAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<String, String> sink = EventHeaderSink.<String, String>create()
                                                                       .addStandardHeaders("test")
                                                                       .destination(collector)
                                                                       .build()) {
                // When
                sendTestEvents(sink, x -> List.of(new Header(REQUEST_ID, x)), () -> null);

                // Then
                Assert.assertEquals(collector.get().size(), KEYS.size());
                Set<String> requestIds = new LinkedHashSet<>();
                collector.get().forEach(e -> {
                    Assert.assertEquals(e.lastHeader(EXEC_PATH), "test");
                    String id = e.lastHeader(REQUEST_ID);
                    Assert.assertTrue(requestIds.add(id), "All Request-ID's should have been unique");
                    String inputId = e.lastHeader(INPUT_REQUEST_ID);
                    Assert.assertEquals(inputId, e.key());
                });
            }
        }
    }

    @Test
    public void givenEventHeaderSink_whenToString_thenBasicOutput() {
        // Given
        try (EventHeaderSink<Integer, String> sink = EventHeaderSink.<Integer, String>create()
                                                                    .fixedHeader("foo", "bar")
                                                                    .build()) {
            // When
            String output = sink.toString();

            // Then
            Assert.assertNotNull(output);
            Assert.assertEquals(output, """
                    EventHeaderSink(super={
                      destination=NullSink(counter=0)
                    })""");
        }
    }

    @Test
    public void givenValidFixedHeader_whenSendingEvents_thenHeaderAdded() {
        // Given
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<Integer, String> sink = EventHeaderSink.<Integer, String>create()
                                                                        .fixedHeader("name", "value")
                                                                        .destination(collector)
                                                                        .build()) {
                // When
                sink.send(new SimpleEvent<>(Collections.emptyList(), 1, "test"));

                // Then
                Assert.assertEquals(collector.get().size(), 1);
                Event<Integer, String> event = collector.get().get(0);
                Assert.assertEquals(event.lastHeader("name"), "value");
            }
        }
    }

    @Test
    public void givenValidFixedRawHeader_whenSendingEvents_thenHeaderAdded() {
        // Given
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            byte[] value = { 1, 2, 3, 4 };
            try (EventHeaderSink<Integer, String> sink = EventHeaderSink.<Integer, String>create()
                                                                        .fixedHeader("name", value)
                                                                        .destination(collector)
                                                                        .build()) {
                // When
                sink.send(new SimpleEvent<>(Collections.emptyList(), 1, "test"));

                // Then
                Assert.assertEquals(collector.get().size(), 1);
                Event<Integer, String> event = collector.get().get(0);
                verifyRawHeaderValue(event, "name", value);
            }
        }
    }

    @Test
    public void givenValidFixedHeaderIfMissing_whenSendingEvents_thenHeaderAddedUnlessAlreadyExisted() {
        // Given
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            try (EventHeaderSink<Integer, String> sink = EventHeaderSink.<Integer, String>create()
                                                                        .fixedHeaderIfMissing("name", "value")
                                                                        .destination(collector)
                                                                        .build()) {
                // When
                sink.send(new SimpleEvent<>(Collections.emptyList(), 1, "test"));
                sink.send(new SimpleEvent<>(List.of(new Header("name", "existing")), 2, "test"));

                // Then
                Assert.assertEquals(collector.get().size(), 2);
                Event<Integer, String> event = collector.get().get(0);
                Assert.assertEquals(event.lastHeader("name"), "value");
                event = collector.get().get(1);
                Assert.assertEquals(event.lastHeader("name"), "existing");
            }
        }
    }

    @Test
    public void givenValidFixedRawHeaderIfMissing_whenSendingEvents_thenHeaderAddedUnlessAlreadyExisted() {
        // Given
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            byte[] value = { 1, 2, 3, 4 };
            byte[] existing = { 5, 6, 7, 8};
            try (EventHeaderSink<Integer, String> sink = EventHeaderSink.<Integer, String>create()
                                                                        .fixedHeaderIfMissing("name", value)
                                                                        .destination(collector)
                                                                        .build()) {
                // When
                sink.send(new SimpleEvent<>(Collections.emptyList(), 1, "test"));
                sink.send(new SimpleEvent<>(List.of(new RawHeader("name", existing)), 2, "test"));

                // Then
                Assert.assertEquals(collector.get().size(), 2);
                Event<Integer, String> event = collector.get().get(0);
                verifyRawHeaderValue(event, "name", value);
                event = collector.get().get(1);
                verifyRawHeaderValue(event, "name", existing);
            }
        }
    }

    private static void verifyRawHeaderValue(Event<Integer, String> event, String key, byte[] expected) {
        Assert.assertEquals(event.headers()
                                 .filter(h -> Objects.equals(h.key(), key))
                                 .findFirst()
                                 .map(EventHeader::rawValue)
                                 .orElse(null), expected);
    }
}
