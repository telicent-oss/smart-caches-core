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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class TestNullSink extends AbstractSinkTests {

    @Test
    public void givenNoItems_whenSendingToNull_thenCountIsZero() {
        // Given, When and Then
        verifyNullSink(Collections.emptyList());
    }

    @Test
    public void givenItems_whenSendingToNull_thenCountIsCorrect() {
        // Given, When and Then
        verifyNullSink(Arrays.asList("a", "b", "c"));
    }

    @Test
    public void givenNullSink_whenToString_thenBasicOutput() {
        // Given
        try (NullSink<String> sink = NullSink.of()) {

            // When
            String output = sink.toString();

            // Then
            Assert.assertNotNull(output);
            Assert.assertEquals(output, "NullSink(counter=0)");
        }
    }

    @Test
    public void givenNullSink_whenSomeItemsOutput_thenToStringOutputIncludesCorrectCount() {
        // Given
        try (NullSink<String> sink = NullSink.of()) {
            // When
            for (int i = 0; i < 100; i++) {
                sink.send(Integer.toString(i));
            }

            // Then
            String output = sink.toString();
            Assert.assertNotNull(output);
            Assert.assertEquals(output, "NullSink(counter=100)");
        }
    }

    protected void verifyNullSink(List<String> values) {
        // When
        try (NullSink<String> sink = new NullSink<>()) {
            values.forEach(sink::send);

            // Then
            Assert.assertEquals(sink.count(), values.size());

            // And
            // After close() the counter should be reset
            sink.close();
            Assert.assertEquals(sink.count(), 0);
        }
    }
}
