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
package io.telicent.smart.cache.sources.memory;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.RawHeader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

/**
 * Tests for the {@link SimpleEvent} methods that had no coverage - {@code lastRawHeader()},
 * {@code replaceHeaders()} and {@code hashCode()}.
 */
public class TestSimpleEventMutations {

    private static final int TEST_KEY = 1234;
    private static final String TEST_VALUE = "value";

    private static EventHeader header(String key, String value) {
        return new RawHeader(key, value.getBytes(StandardCharsets.UTF_8));
    }

    private static SimpleEvent<Integer, String> event(EventHeader... headers) {
        return new SimpleEvent<>(List.of(headers), TEST_KEY, TEST_VALUE);
    }

    @Test
    public void givenRepeatedHeader_whenGettingLastRawHeader_thenTheLastValueIsReturned() {
        SimpleEvent<Integer, String> event =
                event(header("Content-Type", "text/plain"), header("Content-Type", "application/json"));

        EventHeader last = event.lastRawHeader("Content-Type");

        Assert.assertNotNull(last);
        Assert.assertEquals(last.value(), "application/json");
    }

    @Test
    public void givenSingleHeader_whenGettingLastRawHeader_thenThatHeaderIsReturned() {
        SimpleEvent<Integer, String> event = event(header("Content-Type", "text/plain"));

        EventHeader last = event.lastRawHeader("Content-Type");

        Assert.assertNotNull(last);
        Assert.assertEquals(last.key(), "Content-Type");
        Assert.assertEquals(last.value(), "text/plain");
    }

    @Test
    public void givenNoMatchingHeader_whenGettingLastRawHeader_thenNullIsReturned() {
        SimpleEvent<Integer, String> event = event(header("Content-Type", "text/plain"));

        Assert.assertNull(event.lastRawHeader("Accept"));
    }

    @Test
    public void givenEvent_whenReplacingHeaders_thenOnlyTheNewHeadersRemain() {
        SimpleEvent<Integer, String> event = event(header("Content-Type", "text/plain"));

        Event<Integer, String> replaced = event.replaceHeaders(Stream.of(header("Accept", "application/json")));

        Assert.assertEquals(replaced.headers().toList(), List.of(header("Accept", "application/json")));
        Assert.assertEquals(replaced.key(), Integer.valueOf(TEST_KEY));
        Assert.assertEquals(replaced.value(), TEST_VALUE);
    }

    @Test
    public void givenEvent_whenReplacingHeadersWithNone_thenNoHeadersRemain() {
        SimpleEvent<Integer, String> event = event(header("Content-Type", "text/plain"));

        Event<Integer, String> replaced = event.replaceHeaders(Stream.empty());

        Assert.assertTrue(replaced.headers().toList().isEmpty());
    }

    @Test
    public void givenEventsWithEqualContent_whenHashing_thenHashCodesMatch() {
        SimpleEvent<Integer, String> a = event(header("Content-Type", "text/plain"));
        SimpleEvent<Integer, String> b = event(header("Content-Type", "text/plain"));

        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void givenEventsDifferingOnlyByHeaderOrder_whenHashing_thenHashCodesMatch() {
        // equals() compares headers as a set, so hashCode() must hash them as a set too
        SimpleEvent<Integer, String> a = event(header("A", "1"), header("B", "2"));
        SimpleEvent<Integer, String> b = event(header("B", "2"), header("A", "1"));

        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }
}
