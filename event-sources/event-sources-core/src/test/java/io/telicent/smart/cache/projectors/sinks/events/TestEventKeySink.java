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

import java.util.List;

public class TestEventKeySink extends AbstractEventSinkTests {

    @Test
    public void givenBuilder_whenBuilding_thenOK() {
        // Given and When
        try (EventKeySink<String, String> sink = EventKeySink.<String, String>create()
                                                             .destination(Sinks.<String>collect().build())
                                                             .build()) {
            // Then
            Assert.assertNotNull(sink);
        }
    }

    @Test
    public void givenSink_whenSendingEvents_thenOnlyKeysAreOutput() {
        // Given
        CollectorSink<String> collector = CollectorSink.of();
        try (EventKeySink<String, String> sink = new EventKeySink<>(collector)) {
            // When
            sendTestEvents(sink);

            // Then
            List<String> actual = collector.get();
            Assert.assertEquals(actual.size(), KEYS.size());
            Assert.assertEquals(actual, KEYS);
        }
    }

    @Test
    public void givenSink_whenToString_thenBasicOutput() {
        // Given
        try (EventKeySink<String, String> sink = new EventKeySink<>(NullSink.of())) {
            // When
            String output = sink.toString();

            // Then
            Assert.assertNotNull(output);
            Assert.assertEquals(output, """
                    EventKeySink(super={
                      destination=NullSink(counter=0)
                    })""");
        }
    }
}
