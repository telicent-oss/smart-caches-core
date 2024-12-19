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
package io.telicent.smart.cache.projectors.sinks;

import io.telicent.smart.cache.projectors.SinkException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class TestCollectorSink extends AbstractSinkTests {

    @Test
    public void givenItems_whenUsingCollectorSink_thenItemsAreCollected() throws SinkException {
        // Given
        List<String> values = Arrays.asList("a", "b", "c");

        // When and Then
        verifyCollectorSink(values);
    }

    @Test
    public void givenEmptyItems_whenUsingCollectorSink_thenNothingIsCollected() throws SinkException {
        // Given
        List<String> values = Collections.emptyList();

        // When and Then
        verifyCollectorSink(values);
    }

    @Test
    public void givenNullItems_whenUsingCollectorSink_thenItemIsCollected() throws SinkException {
        // Given
        List<String> values = Collections.singletonList(null);

        // When and Then
        verifyCollectorSink(values);
    }

    @Test
    public void givenCollectorSink_whenToString_thenBasicOutput() {
        // Given
        try (CollectorSink<String> sink = CollectorSink.of()) {
            // When
            String output = sink.toString();

            // Then
            Assert.assertEquals(output, "CollectorSink()");
        }
    }

    protected void verifyCollectorSink(List<String> values) {
        // When
        try (CollectorSink<String> sink = new CollectorSink<>()) {
            values.forEach(sink::send);

            // Then
            verifyCollectedValues(sink, values);

            // And
            // After close() the sink should be emptied
            sink.close();
            Assert.assertEquals(sink.get().size(), 0);
        }
    }
}
