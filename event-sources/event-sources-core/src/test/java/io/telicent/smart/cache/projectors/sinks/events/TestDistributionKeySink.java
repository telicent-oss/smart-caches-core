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
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.DistributionIds;
import io.telicent.smart.cache.sources.DistributionKeyStrategy;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class TestDistributionKeySink extends AbstractEventSinkTests {

    private static final String DISTRIBUTION_ID = "https://telicent.io/datasets/acled#2026-08-release";
    private static final String OTHER_DISTRIBUTION_ID = "https://telicent.io/datasets/bbcm#2026-08-release";

    private static Event<String, String> event(String key, String distributionIdHeader) {
        List<EventHeader> headers = distributionIdHeader != null ? List.of(
                new Header(TelicentHeaders.DISTRIBUTION_ID, distributionIdHeader)) : Collections.emptyList();
        return new SimpleEvent<>(headers, key, "value");
    }

    private static DistributionKeySink.Builder<String, String> sink() {
        DistributionKeySink.Builder<String, String> builder = DistributionKeySink.create();
        return builder.keyEncoder(Function.identity());
    }

    /** Keeps collected output available after the sink under test is closed. */
    private static Sink<Event<String, String>> nonClosing(CollectorSink<Event<String, String>> collector) {
        return new Sink<>() {
            @Override
            public void send(Event<String, String> item) {
                collector.send(item);
            }

            @Override
            public void close() {
                // The owning test closes the collector after making assertions.
            }
        };
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoKeyEncoder_whenBuilding_thenNullPointer() {
        // Given, When and Then
        DistributionKeySink<String, String> built = DistributionKeySink.<String, String>create().build();
        Assert.fail("Should have thrown, got " + built);
    }

    @Test
    public void givenHeaderOnlyEvent_whenSending_thenKeyIsSetFromHeader() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().destination(nonClosing(collector)).build()) {
                // When
                sink.send(event(null, DISTRIBUTION_ID));
            }

            // Then
            Assert.assertEquals(collector.get().size(), 1);
            Event<String, String> output = collector.get().get(0);
            Assert.assertEquals(output.key(), DISTRIBUTION_ID);
            Assert.assertEquals(output.lastHeader(TelicentHeaders.DISTRIBUTION_ID), DISTRIBUTION_ID);
        }
    }

    @Test
    public void givenKeyOnlyEvent_whenSending_thenHeaderIsBackfilled() {
        // Given - the design keeps the header for backwards compatibility, so an event that has a key should gain one
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().destination(nonClosing(collector)).build()) {
                // When
                sink.send(event(DISTRIBUTION_ID, null));
            }

            // Then
            Event<String, String> output = collector.get().get(0);
            Assert.assertEquals(output.key(), DISTRIBUTION_ID);
            Assert.assertEquals(output.lastHeader(TelicentHeaders.DISTRIBUTION_ID), DISTRIBUTION_ID);
        }
    }

    @Test
    public void givenBackfillDisabled_whenSending_thenHeaderIsNotAdded() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().backfillHeader(false)
                                                                  .destination(nonClosing(collector))
                                                                  .build()) {
                // When
                sink.send(event(DISTRIBUTION_ID, null));
            }

            // Then
            Event<String, String> output = collector.get().get(0);
            Assert.assertEquals(output.key(), DISTRIBUTION_ID);
            Assert.assertNull(output.lastHeader(TelicentHeaders.DISTRIBUTION_ID));
        }
    }

    @Test
    public void givenKeyAndHeaderDisagree_whenSending_thenKeyWins() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().destination(nonClosing(collector)).build()) {
                // When
                sink.send(event(DISTRIBUTION_ID, OTHER_DISTRIBUTION_ID));
            }

            // Then
            Event<String, String> output = collector.get().get(0);
            Assert.assertEquals(output.key(), DISTRIBUTION_ID);
            // The existing header is left alone, we only backfill when it is missing
            Assert.assertEquals(output.lastHeader(TelicentHeaders.DISTRIBUTION_ID), OTHER_DISTRIBUTION_ID);
        }
    }

    @Test
    public void givenCompositeStrategy_whenSending_thenKeysAreUniqueAndDecodeBack() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().strategy(
                    DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID).destination(nonClosing(collector)).build()) {
                // When
                sink.send(event(null, DISTRIBUTION_ID));
                sink.send(event(null, DISTRIBUTION_ID));
            }

            // Then
            List<Event<String, String>> outputs = collector.get();
            Assert.assertEquals(outputs.size(), 2);
            Assert.assertNotEquals(outputs.get(0).key(), outputs.get(1).key());
            for (Event<String, String> output : outputs) {
                Assert.assertEquals(DistributionIds.fromKeyString(output.key()), DISTRIBUTION_ID);
                Assert.assertEquals(output.lastHeader(TelicentHeaders.DISTRIBUTION_ID), DISTRIBUTION_ID);
            }
        }
    }

    @Test
    public void givenCompositeStrategyAndAlreadyKeyedEvent_whenSending_thenSuffixIsNotDoubled() {
        // Given - re-keying must be idempotent in shape, a pipeline may pass through several key sinks
        String existingKey = DISTRIBUTION_ID + DistributionIds.KEY_SEPARATOR + java.util.UUID.randomUUID();
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().strategy(
                    DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID).destination(nonClosing(collector)).build()) {
                // When
                sink.send(event(existingKey, null));
            }

            // Then
            String key = collector.get().get(0).key();
            Assert.assertEquals(DistributionIds.fromKeyString(key), DISTRIBUTION_ID);
            Assert.assertEquals(key.split(DistributionIds.KEY_SEPARATOR).length,
                                existingKey.split(DistributionIds.KEY_SEPARATOR).length);
        }
    }

    @Test
    public void givenEventWithNoDistributionId_whenSending_thenForwardedUnmodified() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().destination(nonClosing(collector)).build()) {
                // When
                sink.send(event(null, null));
            }

            // Then
            Event<String, String> output = collector.get().get(0);
            Assert.assertNull(output.key());
            Assert.assertNull(output.lastHeader(TelicentHeaders.DISTRIBUTION_ID));
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenRequireDistributionId_whenSendingEventWithout_thenIllegalState() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().requireDistributionId()
                                                                  .destination(nonClosing(collector))
                                                                  .build()) {
                // When and Then
                sink.send(event(null, null));
            }
        }
    }

    @Test
    public void givenDisabled_whenSending_thenForwardedUnmodified() {
        // Given
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().enabled(false).destination(nonClosing(collector)).build()) {
                // When
                sink.send(event(null, DISTRIBUTION_ID));
            }

            // Then
            Event<String, String> output = collector.get().get(0);
            Assert.assertNull(output.key(), "Keying is disabled so the event's key must be left alone");
            Assert.assertEquals(output.lastHeader(TelicentHeaders.DISTRIBUTION_ID), DISTRIBUTION_ID);
        }
    }

    @Test
    public void givenDisabledAndRequireDistributionId_whenSendingEventWithout_thenForwardedUnmodified() {
        // Given - disabling takes precedence, it must not start failing events
        try (CollectorSink<Event<String, String>> collector = CollectorSink.of()) {
            try (DistributionKeySink<String, String> sink = sink().enabled(false)
                                                                  .requireDistributionId()
                                                                  .destination(nonClosing(collector))
                                                                  .build()) {
                // When
                sink.send(event(null, null));
            }

            // Then
            Assert.assertEquals(collector.get().size(), 1);
        }
    }
}
