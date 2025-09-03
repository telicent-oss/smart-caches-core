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
package io.telicent.smart.cache.projectors.sinks.events.splitter;

import io.telicent.smart.cache.sources.Event;

import java.util.List;

/**
 * Helper interface for use with the {@link SplitterSink}
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public interface Splitter<TKey, TValue> {

    /**
     * Calculates the size of the event in bytes
     *
     * @param event Event
     * @return Size in bytes
     */
    int calculateSize(Event<TKey, TValue> event);

    /**
     * Gets the portion of the value to use for calculating checksums and hashes for data integrity validation
     *
     * @param value Value
     * @return Integrity bytes
     */
    byte[] integrityBytes(TValue value);

    /**
     * Splits the value into chunks of at most the given size
     *
     * @param value     Value
     * @param chunkSize Chunk size
     * @return Chunked values
     */
    List<TValue> split(TValue value, int chunkSize);

    /**
     * Recombines the chunked value into a single value
     *
     * @param values Chunked values
     * @return Value
     */
    TValue combine(List<TValue> values);
}
