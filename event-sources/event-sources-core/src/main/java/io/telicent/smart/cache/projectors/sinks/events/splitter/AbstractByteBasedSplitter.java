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
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Abstract implementation of a {@link Splitter} for a value type where the field that we want to split is represented
 * as a byte array
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public abstract class AbstractByteBasedSplitter<TKey, TValue> implements Splitter<TKey, TValue> {

    /**
     * Gets the byte array field of the value that is to be split
     *
     * @param value Value
     * @return Byte array
     */
    protected abstract byte[] getByteField(TValue value);

    @Override
    public int calculateSize(Event<TKey, TValue> event) {
        return this.getByteField(event.value()).length;
    }

    @Override
    public byte[] integrityBytes(TValue value) {
        return this.getByteField(value);
    }

    /**
     * Creates a new value based on the original value and a chunk of the original byte array field
     *
     * @param original Original value
     * @param chunk    Chunk data
     * @return Split value
     */
    protected abstract TValue splitValue(TValue original, byte[] chunk);

    @Override
    public List<TValue> split(TValue value, int chunkSize) {
        // Calculate how many chunks we will have, this is the original input size divided by the chunk size
        // Remember to add 1 extra chunk if the input size is not an exact multiple of the chunk size
        byte[] originalBytes = this.getByteField(value);
        int originalLength = originalBytes.length;
        int totalChunks = (originalLength / chunkSize) + (originalLength % chunkSize == 0 ? 0 : 1);
        List<TValue> chunks = new ArrayList<>(totalChunks);
        for (int start = 0, chunkId = 1; start < originalLength; start += chunkSize, chunkId++) {
            // Be careful that we don't over-copy data.  In particular if this is the last chunk we need to cap the
            // end of the chunk at the original input length otherwise we'd copy null bytes over which would cause
            // issues later when recombining
            int end = Math.min(start + chunkSize, originalLength);
            byte[] chunkData = Arrays.copyOfRange(originalBytes, start, end);

            chunks.add(splitValue(value, chunkData));
        }

        return chunks;
    }

    /**
     * Creates a new combined value based on the split values and the recombined byte array field
     *
     * @param values   Split values
     * @param combined Recombined byte array field
     * @return Combined Value
     */
    protected abstract TValue combineValues(List<TValue> values, byte[] combined);

    @Override
    public TValue combine(List<TValue> values) {
        List<byte[]> chunkBytes = values.stream().map(this::getByteField).toList();
        byte[] combinedData =
                new byte[chunkBytes.stream().map(v -> v.length).reduce(0, Integer::sum)];
        int start = 0;
        for (byte[] chunk : chunkBytes) {
            ArrayUtils.arraycopy(chunk, 0, combinedData, start, chunk.length);
            start += chunk.length;
        }
        return combineValues(values, combinedData);
    }
}
