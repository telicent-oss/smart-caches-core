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
package io.telicent.smart.cache.sources.provenance;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.List;

import static io.telicent.smart.cache.sources.TelicentHeaders.DISTRIBUTION_ID;
import static io.telicent.smart.cache.sources.TelicentHeaders.EXEC_PATH;
import static io.telicent.smart.cache.sources.TelicentHeaders.INPUT_REQUEST_ID;
import static io.telicent.smart.cache.sources.TelicentHeaders.REQUEST_ID;
import static io.telicent.smart.cache.sources.TelicentHeaders.SECURITY_LABEL;

public class TestProvenanceRecord {

    private static Event<String, String> eventWith(EventHeader... headers) {
        return new SimpleEvent<>(List.of(headers), "key", "value");
    }

    @Test
    public void givenAllProvenanceHeaders_whenFromEvent_thenAllCaptured() {
        // Given
        Event<String, String> event = eventWith(new Header(REQUEST_ID, "req-1"),
                                                 new Header(INPUT_REQUEST_ID, "in-1"),
                                                 new Header(EXEC_PATH, "adapter, mapper, projector"),
                                                 new Header(DISTRIBUTION_ID, "dist-1"),
                                                 new Header(SECURITY_LABEL, "clearance=secret"));
        Instant ts = Instant.parse("2026-06-12T10:00:00Z");

        // When
        ProvenanceRecord rec = ProvenanceRecord.fromEvent(event, ts);

        // Then
        Assert.assertEquals(rec.requestId(), "req-1");
        Assert.assertEquals(rec.inputRequestId(), "in-1");
        Assert.assertEquals(rec.execPath(), "adapter, mapper, projector");
        Assert.assertEquals(rec.distributionId(), "dist-1");
        Assert.assertEquals(rec.securityLabel(), "clearance=secret");
        Assert.assertEquals(rec.ingestedAt(), ts);
        Assert.assertTrue(rec.hasProvenance());
    }

    @Test
    public void givenExecPathHeader_whenExecPathSteps_thenSplitTrimmedAndOrdered() {
        // Given
        Event<String, String> event = eventWith(new Header(EXEC_PATH, "adapter, mapper ,projector"));

        // When
        List<String> steps = ProvenanceRecord.fromEvent(event).execPathSteps();

        // Then
        Assert.assertEquals(steps, List.of("adapter", "mapper", "projector"));
    }

    @Test
    public void givenNoHeaders_whenFromEvent_thenNullsEmptyStepsAndNoProvenance() {
        // Given
        Event<String, String> event = new SimpleEvent<>(List.of(), "key", "value");

        // When
        ProvenanceRecord rec = ProvenanceRecord.fromEvent(event);

        // Then
        Assert.assertNull(rec.requestId());
        Assert.assertNull(rec.inputRequestId());
        Assert.assertNull(rec.execPath());
        Assert.assertNull(rec.distributionId());
        Assert.assertNull(rec.securityLabel());
        Assert.assertTrue(rec.execPathSteps().isEmpty());
        Assert.assertFalse(rec.hasProvenance());
        Assert.assertNotNull(rec.ingestedAt());
    }

    @Test
    public void givenRepeatedHeader_whenFromEvent_thenLastValueWins() {
        // Given - mirrors the platform rule that the last header value is the one that applies
        Event<String, String> event = eventWith(new Header(DISTRIBUTION_ID, "dist-old"),
                                                 new Header(DISTRIBUTION_ID, "dist-new"));

        // When
        ProvenanceRecord rec = ProvenanceRecord.fromEvent(event);

        // Then
        Assert.assertEquals(rec.distributionId(), "dist-new");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullEvent_whenFromEvent_thenThrows() {
        // Given, When and Then
        ProvenanceRecord.fromEvent(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullTimestamp_whenConstructing_thenThrows() {
        // Given, When and Then
        new ProvenanceRecord(null, null, null, null, null, null);
    }
}