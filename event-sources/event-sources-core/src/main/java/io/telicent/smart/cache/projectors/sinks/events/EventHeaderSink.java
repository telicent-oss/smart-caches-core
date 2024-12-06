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
import io.telicent.smart.cache.projectors.sinks.AbstractTransformingSink;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.Header;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A forwarding sink that optionally adds one/more headers onto the event before forwarding it
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public class EventHeaderSink<TKey, TValue> extends AbstractTransformingSink<Event<TKey, TValue>, Event<TKey, TValue>> {
    private final List<Function<Event<TKey, TValue>, Header>> headerGenerators = new ArrayList<>();

    /**
     * Creates a new sink
     *
     * @param destination      Destination Sink
     * @param headerGenerators Header generator functions
     */
    EventHeaderSink(Sink<Event<TKey, TValue>> destination,
                    Collection<Function<Event<TKey, TValue>, Header>> headerGenerators) {
        super(destination);
        this.headerGenerators.addAll(Objects.requireNonNull(headerGenerators, "Header Generators cannot be null"));
        if (this.headerGenerators.isEmpty()) {
            throw new IllegalArgumentException("Header Generators cannot be empty");
        }
    }

    @Override
    protected Event<TKey, TValue> transform(Event<TKey, TValue> event) {
        //@formatter:off
        List<Header> additionalHeaders =
                this.headerGenerators.stream()
                                     .map(g -> g.apply(event))
                                     .filter(Objects::nonNull)
                                     .toList();
        //@formatter:on

        // Either add the additional headers (if any) or return unmodified
        if (!additionalHeaders.isEmpty()) {
            return event.addHeaders(additionalHeaders.stream());
        }
        return event;
    }

    /**
     * Creates a builder for an event processed sink
     *
     * @param <TKey>   Event key type
     * @param <TValue> Event value type
     * @return Builder
     */
    public static <TKey, TValue> Builder<TKey, TValue> create() {
        return new Builder<>();
    }

    /**
     * Builder for event processed sinks
     *
     * @param <TKey>   Event Key type
     * @param <TValue> Event Value type
     */
    public static class Builder<TKey, TValue>
            extends
            AbstractForwardingSinkBuilder<Event<TKey, TValue>, Event<TKey, TValue>, EventHeaderSink<TKey, TValue>, Builder<TKey, TValue>> {

        private final List<Function<Event<TKey, TValue>, Header>> headerGenerators = new ArrayList<>();

        /**
         * Sets a header generator function that generates a header, may be called multiple times to have the sink add
         * multiple headers
         *
         * @param headerGenerator Header generator function, if {@code null} is produced by the generator then no header
         *                        will be added
         * @return Builder
         */
        public Builder<TKey, TValue> headerGenerator(
                Function<Event<TKey, TValue>, Header> headerGenerator) {
            if (headerGenerator != null) {
                this.headerGenerators.add(headerGenerator);
            }
            return this;
        }

        @Override
        public EventHeaderSink<TKey, TValue> build() {
            return new EventHeaderSink<>(this.getDestination(), this.headerGenerators);
        }
    }
}
