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
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import io.telicent.smart.cache.sources.Event;
import lombok.ToString;

/**
 * A sink that outputs just the event value
 *
 * @param <TKey>   Event key type
 * @param <TValue> Event value type
 */
@ToString(callSuper = true)
public class EventValueSink<TKey, TValue> extends AbstractTransformingSink<Event<TKey, TValue>, TValue> {
    /**
     * Creates a new sink with an optional forwarding destination
     * <p>
     * If no forwarding destination is provided then the {@link NullSink} is used.
     * </p>
     *
     * @param destination Forwarding destination
     */
    public EventValueSink(Sink<TValue> destination) {
        super(destination);
    }

    @Override
    protected TValue transform(Event<TKey, TValue> event) {
        return event.value();
    }

    /**
     * Creates a builder for an event value sink
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     * @return Event value sink builder
     */
    public static <TKey, TValue> Builder<TKey, TValue> create() {
        return new Builder<>();
    }

    /**
     * A builder for an event value sink
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     */
    public static class Builder<TKey, TValue> extends
            AbstractForwardingSinkBuilder<Event<TKey, TValue>, TValue, EventValueSink<TKey, TValue>, Builder<TKey, TValue>> {

        /**
         * Builds an event value sink
         *
         * @return Event value sink
         */
        @Override
        public EventValueSink<TKey, TValue> build() {
            return new EventValueSink<>(this.getDestination());
        }
    }
}
