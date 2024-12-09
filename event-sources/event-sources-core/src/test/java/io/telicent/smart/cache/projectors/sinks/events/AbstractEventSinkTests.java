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
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class AbstractEventSinkTests {
    public static final List<String> KEYS = Arrays.asList("a", "b", "c", "d", "e");

    /**
     * Sends test events based on the {@link #KEYS} array to the given sink, events have no headers or sources
     *
     * @param sink Sink
     */
    protected static void sendTestEvents(Sink<Event<String, String>> sink) {
        sendTestEvents(sink, x -> null, () -> null);
    }

    /**
     * Sends test events based on the {@link #KEYS} array filling the headers and sources using the supplied functions
     *
     * @param sink            Sink
     * @param headerGenerator Header Generator function
     * @param sourceSupplier  Source supplier
     */
    protected static void sendTestEvents(Sink<Event<String, String>> sink,
                                         Function<String, Collection<Header>> headerGenerator,
                                         Supplier<EventSource<String, String>> sourceSupplier) {
        KEYS.forEach(k -> sink.send(
                new SimpleEvent<>(headerGenerator.apply(k), k, StringUtils.repeat(k, 5), sourceSupplier.get())));
    }

    /**
     * Sends test events based on the {@link #KEYS} array with no headers and the given source
     *
     * @param sink   Sink
     * @param source Event Source
     */
    protected static void sendTestEvents(Sink<Event<String, String>> sink, EventSource<String, String> source) {
        sendTestEvents(sink, x -> null, () -> source);
    }
}
