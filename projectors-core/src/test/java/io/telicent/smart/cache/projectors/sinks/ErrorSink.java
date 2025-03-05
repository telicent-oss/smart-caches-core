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
import io.telicent.smart.cache.projectors.SinkException;
import lombok.ToString;

import java.util.function.Supplier;

/**
 * A Sink that throws errors when sent items, intended purely for unit testing
 *
 * @param <T> Input type
 */
@ToString
public class ErrorSink<T> implements Sink<T> {
    private final Supplier<SinkException> errorSupplier;

    /**
     * Creates a new error sink with a default error message of {@code Failed}
     */
    public ErrorSink() {
        this(() -> new SinkException("Failed"));
    }

    /**
     * Creates a new error sink
     *
     * @param errorSupplier Error supplier
     */
    public ErrorSink(Supplier<SinkException> errorSupplier) {
        this.errorSupplier = errorSupplier;
    }

    @Override
    public void send(T item) {
        throw this.errorSupplier.get();
    }

    @Override
    public void close() {
        // Nothing to do
    }
}
