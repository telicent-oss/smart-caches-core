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
package io.telicent.smart.cache.sources.combiner;

import io.telicent.smart.cache.sources.Event;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Collector used by {@link CombiningEventSource} to track chunked events
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
class CombiningCollector<TKey, TValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CombiningCollector.class);

    @Getter
    private final int expectedChunks;
    @Getter
    private final List<Event<TKey, TValue>> chunks;

    /**
     * Creates a new combining collector
     *
     * @param expectedChunks Expected Chunks
     */
    public CombiningCollector(int expectedChunks) {
        this.expectedChunks = expectedChunks;

        // Initialise the chunks array to all null, this allows us to populate it with the chunks as they arrive
        // regardless of the order they arrive in otherwise we'd have to track which chunk was which and sort later
        this.chunks = new ArrayList<>(expectedChunks);
        for (int i = 0; i < expectedChunks; i++) {
            this.chunks.add(null);
        }
    }

    /**
     * Sets a chunk event
     *
     * @param chunkId Chunk ID, 1 based index
     * @param chunk   Chunk event
     */
    public void setChunk(int chunkId, Event<TKey, TValue> chunk) {
        // NB - Chunk-IDs use a 1 based index e.g. 2/3 so if this is Chunk 2 then it goes at Index 1 in the 0 based
        // indexed list
        if (this.chunks.get(chunkId - 1) != null) {
            LOGGER.debug("Duplicate Chunk {} received for key {}", chunkId, chunk.key());
        }
        this.chunks.set(chunkId - 1, chunk);
    }

    /**
     * Is the chunked event complete?  i.e. have we received all the chunks necessary to recombine it back into the
     * original event
     *
     * @return True if complete, false otherwise
     */
    public boolean isComplete() {
        return this.chunks.stream().noneMatch(Objects::isNull);
    }

    /**
     * Gets the chunk values
     *
     * @return Chunk values
     */
    public List<TValue> values() {
        return this.chunks.stream().map(Event::value).toList();
    }
}
