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

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;

import java.util.Objects;

/**
 * A close latch sink is a forwarding sink designed to allow its destination to be shared by multiple sinks.
 * <p>
 * To use this sink a shared {@link CloseLatch} instance <strong>MUST</strong> be created and then passed into each
 * instance of this sink during construction along with the destination sink you are intending to share.
 * </p>
 *
 * @param <T>
 */
public class CloseLatchSink<T> extends AbstractTransformingSink<T, T> {

    private final CloseLatch latch;

    /**
     * Creates a new close latch sink
     *
     * @param destination Forwarding destination
     * @param latch       The shared close latch used to track when the destination sink should be closed
     */
    CloseLatchSink(Sink<T> destination, CloseLatch latch) {
        super(destination);
        this.latch = Objects.requireNonNull(latch, "Close Latch cannot be null");
        this.latch.open();
    }

    @Override
    protected T transform(T item) {
        return item;
    }

    @Override
    public void close() {
        if (this.latch.close()) {
            super.close();
        }
    }

    /**
     * Creates a new builder for close latch sinks
     *
     * @param <T> Item type
     * @return Builder
     */
    public static <T> Builder<T> create() {
        return new Builder<>();
    }

    /**
     * A builder for close latch sinks
     *
     * @param <TItem> Item type
     */
    public static class Builder<TItem>
            extends AbstractForwardingSinkBuilder<TItem, TItem, CloseLatchSink<TItem>, CloseLatchSink.Builder<TItem>> {

        private CloseLatch latch;

        /**
         * Use an existing shared {@link CloseLatch} for this sink
         *
         * @param latch Close latch
         * @return Builder
         */
        public Builder<TItem> latch(CloseLatch latch) {
            this.latch = latch;
            return this;
        }

        @Override
        public CloseLatchSink<TItem> build() {
            return new CloseLatchSink<>(this.getDestination(), this.latch);
        }
    }
}
