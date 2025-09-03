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

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The splitter sink takes an input event and splits it into several chunked events to avoid falling afoul of size
 * limits on the destination
 * <p>
 * For any input event whose size content exceeds the configured chunk size we split it into chunks, each of at most the
 * configured chunk size.  A {@code Chunk-ID} header of the format {@code id/total} is added to each chunk event so that
 * the chunks can later be recomposed into the full event, even if delivered out of order.
 * TODO Detail other metadata headers
 * </p>
 */
public class SplitterSink<TKey, TValue> implements Sink<Event<TKey, TValue>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplitterSink.class);

    /**
     * The {@code Chunk-ID} header added to events to indicate they have been chunked
     */
    public static final String CHUNK_ID = "Chunk-ID";
    /**
     * The {@code Chunk-Checksum} header added to events to provide integrity checksums for a chunked event
     */
    public static final String CHUNK_CHECKSUM = "Chunk-Checksum";
    /**
     * The {@code Chunk-Hash} header added to events to provide integrity hash for a chunked event
     */
    public static final String CHUNK_HASH = "Chunk-Hash";

    private final Sink<Event<TKey, TValue>> destination;
    private final int chunkSize;
    private final Splitter<TKey, TValue> splitter;
    private final ChunkIntegrityHelper helper = new ChunkIntegrityHelper();

    /**
     * Creates a new splitter sink
     *
     * @param destination Destination sink
     * @param splitter    Splitter
     * @param chunkSize   Chunk Size
     */
    SplitterSink(Sink<Event<TKey, TValue>> destination, Splitter<TKey, TValue> splitter, int chunkSize) {
        this.destination = Objects.requireNonNullElse(destination, NullSink.of());
        this.splitter = Objects.requireNonNull(splitter, "splitter cannot be null");
        if (chunkSize < 1) throw new IllegalArgumentException("chunkSize must be greater than or equal to 1");
        this.chunkSize = chunkSize;
    }

    @Override
    public void send(Event<TKey, TValue> event) {
        int originalLength = this.splitter.calculateSize(event);
        if (originalLength <= this.chunkSize) {
            this.destination.send(event);
        } else {
            // Pre-calculate a checksum and hash for the original input
            byte[] originalIntegrityBytes = this.splitter.integrityBytes(event.value());
            long fullChecksum = this.helper.calculateChecksum(originalIntegrityBytes);
            String fullHash = this.helper.calculateHash(originalIntegrityBytes);

            // Use the splitter to split the original value into chunks
            List<TValue> chunkValues = this.splitter.split(event.value(), this.chunkSize);

            // Convert the chunked values into a series of chunked events that convey the original event in chunks
            // Chunk IDs use a 1 based index so that we get Chunk-ID headers with human-readable values like 1/3, 2/3,
            // 3/3 and so forth
            List<Event<TKey, TValue>> chunks = new ArrayList<>();
            int chunkId = 1;
            for (TValue value : chunkValues) {
                // Copy all the existing headers
                List<EventHeader> headers = event.headers().collect(Collectors.toCollection(ArrayList::new));

                // Add the Chunk-ID header which indicates which Chunk this is, and how many total chunks there are
                // Also calculate and add the Chunk-Checksum and Chunk-Hash headers so we can verify that the data
                // wasn't corrupted by splitting and recombining later in the CombiningProjector
                headers.add(new Header(CHUNK_ID, chunkId + "/" + chunkValues.size()));
                byte[] chunkIntegrityBytes = this.splitter.integrityBytes(value);
                long chunkChecksum = this.helper.calculateChecksum(chunkIntegrityBytes);
                headers.add(new Header(CHUNK_CHECKSUM, chunkChecksum + "/" + fullChecksum));
                String chunkHash = this.helper.calculateHash(chunkIntegrityBytes);
                headers.add(new Header(CHUNK_HASH, chunkHash + "/" + fullHash));

                // Generate a chunk event
                chunks.add(event.replaceHeaders(headers.stream()).replaceValue(value));
                chunkId++;
            }

            try {
                LOGGER.debug("Split event with key {} into {} chunks", event.key(), chunks.size());
                for (Event<TKey, TValue> chunk : chunks) {
                    this.destination.send(chunk);
                }
            } catch (Throwable e) {
                throw new SinkException(
                        "Failed to send event with key " + event.key() + " chunked into " + chunks.size() + " chunks",
                        e);
            }
        }
    }

    @Override
    public void close() {
        this.destination.close();
    }

    /**
     * Gets a new builder for a document splitter sink
     *
     * @return Builder
     */
    public static <TKey, TValue> Builder<TKey, TValue> create() {
        return new Builder<>();
    }

    /**
     * A builder for document splitter sinks
     */
    public static final class Builder<TKey, TValue> extends
            AbstractForwardingSinkBuilder<Event<TKey, TValue>, Event<TKey, TValue>, SplitterSink<TKey, TValue>, Builder<TKey, TValue>> {

        private int chunkSize;
        private Splitter<TKey, TValue> splitter;

        /**
         * Sets the chunk size, documents larger than this will be split into chunks of this size
         *
         * @param chunkSize Chunk size
         * @return Builder
         */
        public Builder<TKey, TValue> chunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        /**
         * Sets the {@link Splitter} that provides the value type specific splitting logic
         *
         * @param splitter Splitter
         * @return Builder
         */
        public Builder<TKey, TValue> splitter(Splitter<TKey, TValue> splitter) {
            this.splitter = splitter;
            return this;
        }

        /**
         * Builds the document splitter sink
         *
         * @return Document splitter sink
         */
        @Override
        public SplitterSink<TKey, TValue> build() {
            return new SplitterSink<>(this.getDestination(), this.splitter, this.chunkSize);
        }
    }
}
