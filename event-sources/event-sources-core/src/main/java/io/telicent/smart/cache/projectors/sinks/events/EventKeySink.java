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
 * A sink that outputs just the event key
 *
 * @param <TKey>   Event key type
 * @param <TValue> Event value type
 */
@ToString(callSuper = true)
public class EventKeySink<TKey, TValue> extends AbstractTransformingSink<Event<TKey, TValue>, TKey> {
    /**
     * Creates a new sink with an optional forwarding destination
     * <p>
     * If no forwarding destination is provided then the {@link NullSink} is used.
     * </p>
     *
     * @param destination Forwarding destination
     */
    public EventKeySink(Sink<TKey> destination) {
        super(destination);
    }

    @Override
    protected TKey transform(Event<TKey, TValue> event) {
        return event.key();
    }

    /**
     * Creates a new event key sink builder
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     * @return Event key sink builder
     */
    public static <TKey, TValue> Builder<TKey, TValue> create() {
        return new Builder<>();
    }

    /**
     * A builder for event key sinks
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     */
    public static class Builder<TKey, TValue>
            extends
            AbstractForwardingSinkBuilder<Event<TKey, TValue>, TKey, EventKeySink<TKey, TValue>, Builder<TKey, TValue>> {

        /**
         * Builds an event key sink
         *
         * @return Event key sink
         */
        @Override
        public EventKeySink<TKey, TValue> build() {
            return new EventKeySink<>(this.getDestination());
        }
    }
}
