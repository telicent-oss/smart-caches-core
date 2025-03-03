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
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.projectors.sinks.Sinks;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class TestEventValueSink extends AbstractEventSinkTests {
    
    @Test
    public void givenSink_whenSendingEvents_thenOnlyValuesAreOutput() {
        // Given
        CollectorSink<String> collector = CollectorSink.of();
        try (EventValueSink<String, String> sink = new EventValueSink<>(collector)) {
            // When
            sendTestEvents(sink);

            // Then
            List<String> actual = collector.get();
            Assert.assertEquals(actual.size(), KEYS.size());
            Assert.assertNotEquals(actual, KEYS);
        }
    }

    @Test
    public void givenBuilder_whenBuilding_thenOK() {
        // Given and When
        try (EventValueSink<String, String> sink = EventValueSink.<String, String>create()
                                                                 .destination(Sinks.<String>collect().build())
                                                                 .build()) {
            // Then
            Assert.assertNotNull(sink);
        }
    }

    @Test
    public void givenSink_whenToString_thenBasicOutput() {
        // Given
        try (EventValueSink<String, String> sink = new EventValueSink<>(NullSink.of())) {
            // When
            String output = sink.toString();

            // Then
            Assert.assertNotNull(output);
            Assert.assertEquals(output, """
                    EventValueSink(super={
                      destination=NullSink(counter=0)
                    })""");
        }
    }
}
