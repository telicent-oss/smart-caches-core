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

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.events.splitter.DefaultChunkIntegrityHelper;
import io.telicent.smart.cache.projectors.sinks.events.splitter.Splitter;
import io.telicent.smart.cache.projectors.sinks.events.splitter.SplitterSink;
import io.telicent.smart.cache.sources.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An event source that recombines events split by a {@link SplitterSink} back into the original events
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public final class CombiningEventSource<TKey, TValue> implements EventSource<TKey, TValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CombiningEventSource.class);

    private final EventSource<TKey, TValue> source;
    private final Splitter<TKey, TValue> splitter;
    private final Map<String, CombiningCollector<TKey, TValue>> collectors = new LinkedHashMap<>();
    private final DefaultChunkIntegrityHelper integrityHelper = new DefaultChunkIntegrityHelper();
    private final Sink<Event<TKey, TValue>> dlq;

    /**
     * Creates a new combining event source
     *
     * @param source   Event Source
     * @param splitter Splitter to handle the value specific recombining
     * @param dlq      DLQ to send bad chunk events to, if not specified then bad events will cause an
     *                 {@link EventSourceException} to be raised
     */
    public CombiningEventSource(EventSource<TKey, TValue> source, Splitter<TKey, TValue> splitter,
                                Sink<Event<TKey, TValue>> dlq) {
        this.source = Objects.requireNonNull(source, "Event Source cannot be null");
        this.splitter = Objects.requireNonNull(splitter, "Splitter cannot be null");
        this.dlq = dlq;
    }

    @Override
    public boolean availableImmediately() {
        return false;
    }

    @Override
    public boolean isExhausted() {
        return this.source.isExhausted();
    }

    @Override
    public void close() {
        this.collectors.clear();
        this.source.close();
    }

    @Override
    public boolean isClosed() {
        return this.source.isClosed();
    }

    @Override
    public Event<TKey, TValue> poll(Duration timeout) {
        long start = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();
        Event<TKey, TValue> event = this.source.poll(timeout);
        // Null means no events available, as soon as we hit this return null ourselves
        if (event == null) {
            return null;
        }

        while ((System.currentTimeMillis() - start) < timeoutMillis) {
            // If our event has been set back to null this indicates it was either:
            //
            // 1 - An invalid chunk event, OR
            // 2 - A chunk event for an event for which we don't yet have all chunks
            //
            // In either case we need to poll() the underlying event source again for the next available event, we
            // continue to do this until either we reach a non-chunk event that we can return, or we receive enough
            // chunk events to return a combined event
            if (event == null) {
                // NB - If we're having to poll() again remember to reduce the timeout based on time already elapsed
                //      otherwise we're violating the poll() contract
                long remainingTimeout = timeoutMillis - (System.currentTimeMillis() - start);
                event = this.source.poll(Duration.ofMillis(remainingTimeout));

                // Null means no events available, as soon as we hit this return null ourselves
                if (event == null) {
                    return null;
                }
            }

            if (isChunkedEvent(event)) {
                // Add this to our chunk collectors, creating a new one if necessary
                String splitId = event.lastHeader(TelicentHeaders.SPLIT_ID);
                if (StringUtils.isBlank(splitId)) {
                    // If we don't have a valid Split-ID to use for correlation throw this event away
                    event = handleError(event, new IllegalArgumentException(
                            "Chunk event is missing mandatory " + TelicentHeaders.SPLIT_ID + " header"), null);
                    continue;
                }
                CombiningCollector<TKey, TValue> collector = this.collectors.get(splitId);
                ChunkInfo<Integer> chunkId =
                        this.getChunkHeaderInfo(event, TelicentHeaders.CHUNK_ID, TelicentHeaders.CHUNK_TOTAL, false,
                                                Integer::parseInt);
                if (chunkId == null) {
                    // NB - getChunkHeaderInfo() already calls handleError() if the Chunk Information is bad, ignore
                    //      this event and continue
                    event = null;
                    continue;
                }
                if (collector == null) {
                    // This is a previously unknown chunked event, create a new collector for it
                    collector = new CombiningCollector<>(chunkId.original());
                    this.collectors.put(splitId, collector);
                } else if (chunkId.original() != collector.getExpectedChunks()) {
                    // This is a previously known chunked event, double check that the metadata hasn't diverged
                    event = handleError(event, new IllegalStateException(String.format(
                            "Total declared chunks %d on this event does not match previously declared total chunks %d",
                            chunkId.original(), collector.getExpectedChunks())), null);
                    continue;
                }

                // Validate the chunk
                ValidatedChunkInfo validatedChunkInfo = isValidChunk(event);
                if (!validatedChunkInfo.valid()) {
                    // NB - isValidChunk() will already have called handleError() appropriately based on the reason the
                    //      chunk was invalid, ignore this event and continue
                    event = null;
                    continue;
                }
                collector.setChunk(chunkId.chunk, event);

                // Do we now have all the events for any of our chunks?
                if (collector.isComplete()) {
                    TValue combinedValue = this.splitter.combine(collector.values());
                    if (!isValidCombinedValue(event, validatedChunkInfo, combinedValue)) {
                        event = null;
                        continue;
                    }

                    // Create the recombined event
                    return recombine(event, splitId, combinedValue, collector);
                }

                // If we've got here and haven't recombined set the event to null so we'll poll for more events and see
                // if we can't get further chunks to recombine
                event = null;

            } else {
                // Not a chunked event so no need to combine, return as-is
                return event;
            }
        }

        // If we failed to recombine any events prior to the timeout expiring return null
        return null;
    }

    private boolean isValidCombinedValue(Event<TKey, TValue> event, ValidatedChunkInfo validatedChunkInfo,
                                         TValue combinedValue) {
        if (combinedValue == null) {
            return handleError(event,
                               new IllegalStateException("Failed to recombine chunked events for key " + event.key()),
                               false);
        }

        // Validate that the checksum algorithm is recognised and the checksums match
        if (!Objects.equals(validatedChunkInfo.checksums().originalPrefix(),
                            this.integrityHelper.checksumAlgorithm())) {
            return handleError(event, new IllegalArgumentException(String.format(
                    "Original Checksum algorithm mismatch.  Configured to support algorithm %s but checksum algorithm for received events was %s",
                    this.integrityHelper.checksumAlgorithm(), validatedChunkInfo.checksums().originalPrefix())), false);
        }
        byte[] combinedIntegrityBytes = this.splitter.integrityBytes(combinedValue);
        long actualChecksum = this.integrityHelper.calculateChecksum(combinedIntegrityBytes);
        if (actualChecksum != validatedChunkInfo.checksums().original()) {
            return handleError(event, new IllegalStateException(String.format(
                    "Original Checksum mismatch.  Expected checksum was %d but actual checksum for recombined data was %d",
                    validatedChunkInfo.checksums().original(), actualChecksum)), false);
        }

        // Validate that the hash algorithm is recognised and the hashes match
        if (!Objects.equals(validatedChunkInfo.hashes().originalPrefix(),
                            this.integrityHelper.hashAlgorithm())) {
            return handleError(event, new IllegalArgumentException(String.format(
                    "Original Hash algorithm mismatch.  Configured to support algorithm %s but hash algorithm for received events was %s",
                    this.integrityHelper.hashAlgorithm(), validatedChunkInfo.hashes().originalPrefix())), false);
        }
        String actualHash = this.integrityHelper.calculateHash(combinedIntegrityBytes);
        if (!Strings.CI.equals(actualHash, validatedChunkInfo.hashes().original())) {
            return handleError(event, new IllegalStateException(String.format(
                    "Original Hash mismatch.  Expected hash was %s but actual hash for recombined data was %s",
                    validatedChunkInfo.hashes().original(), actualHash)), false);
        }

        // Both checksum and hash were valid
        return true;
    }

    private Event<TKey, TValue> recombine(Event<TKey, TValue> event, String splitId, TValue combinedValue,
                                          CombiningCollector<TKey, TValue> collector) {
        List<EventHeader> headers = event.headers().collect(Collectors.toCollection(ArrayList::new));
        headers.removeIf(TelicentHeaders.IS_CHUNK_HEADER);
        Event<TKey, TValue> combinedEvent = event.replaceHeaders(headers.stream()).replaceValue(combinedValue);
        this.collectors.remove(splitId);
        LOGGER.debug("Recombined {} chunk events for Split-ID {} with key {}", collector.getExpectedChunks(), splitId,
                     event.key());
        return combinedEvent;
    }

    private ValidatedChunkInfo isValidChunk(Event<TKey, TValue> event) {
        // Firstly parse the chunk checksum and header information out from the event headers, fail validation if this is missing
        // NB - We don't call handleError() in these cases because getChunkHeaderInfo() already calls that if appropriate
        ChunkInfo<Long> checksums =
                getChunkHeaderInfo(event, TelicentHeaders.CHUNK_CHECKSUM, TelicentHeaders.ORIGINAL_CHECKSUM, true,
                                   Long::parseLong);
        if (checksums == null) {
            return new ValidatedChunkInfo(false, null, null);
        }
        ChunkInfo<String> hashes =
                getChunkHeaderInfo(event, TelicentHeaders.CHUNK_HASH, TelicentHeaders.ORIGINAL_HASH, true,
                                   Function.identity());
        if (hashes == null) {
            return new ValidatedChunkInfo(false, checksums, null);
        }

        // Next verify the checksum
        if (!Objects.equals(checksums.chunkPrefix(), this.integrityHelper.checksumAlgorithm())) {
            return handleError(event, new IllegalStateException(String.format(
                                       "Chunk Checksum Algorithm mismatch.  Configured to support algorithm %s but checksum algorithm for received event was %s",
                                       this.integrityHelper.checksumAlgorithm(), checksums.chunkPrefix())),
                               new ValidatedChunkInfo(false, checksums, hashes));
        }
        byte[] chunkIntegrityBytes = this.splitter.integrityBytes(event.value());
        long actualChecksum = this.integrityHelper.calculateChecksum(chunkIntegrityBytes);
        if (actualChecksum != checksums.chunk()) {
            return handleError(event, new IllegalStateException(String.format(
                    "Chunk Checksum mismatch.  Expected checksum was %d but actual checksum for received chunk data was %d",
                    checksums.chunk(), actualChecksum)), new ValidatedChunkInfo(false, checksums, hashes));
        }

        // Finally verify the hash, do this last as this will be the most expensive of the integrity checks (relatively speaking)
        if (!Objects.equals(hashes.chunkPrefix(), this.integrityHelper.hashAlgorithm())) {
            return handleError(event, new IllegalStateException(String.format(
                                       "Chunk Hash Algorithm mismatch.  Configured to support algorithm %s but hash algorithm for received event was %s",
                                       this.integrityHelper.hashAlgorithm(), checksums.chunkPrefix())),
                               new ValidatedChunkInfo(false, checksums, hashes));
        }
        String actualHash = this.integrityHelper.calculateHash(chunkIntegrityBytes);
        if (!Strings.CI.equals(actualHash, hashes.chunk())) {
            return handleError(event, new IllegalStateException(String.format(
                    "Chunk Hash mismatch.  Expected hash was %s but actual hash for received chunk data was %s",
                    hashes.chunk(), actualHash)), new ValidatedChunkInfo(false, checksums, hashes));
        }

        // Both checksum and hash matched declared values
        return new ValidatedChunkInfo(true, checksums, hashes);
    }

    private boolean isChunkedEvent(Event<TKey, TValue> event) {
        return event.headers().anyMatch(TelicentHeaders.IS_CHUNK_HEADER);
    }

    /**
     * Gets the declared Chunk Header information
     *
     * @param event       Event
     * @param chunkHeader Header to extract chunk information from
     * @param parser      Function to parse the header values
     */
    private <T> ChunkInfo<T> getChunkHeaderInfo(Event<TKey, TValue> event, String chunkHeader, String originalHeader,
                                                boolean hasPrefix, Function<String, T> parser) {
        String rawChunkHeader = event.lastHeader(chunkHeader);
        String rawOriginalHeader = event.lastHeader(originalHeader);
        if (StringUtils.isBlank(rawChunkHeader)) {
            return handleError(event, new IllegalArgumentException(
                    "Chunks do not have required " + chunkHeader + " header present"), null);
        } else if (StringUtils.isBlank(rawOriginalHeader)) {
            return handleError(event, new IllegalArgumentException(
                    "Chunks do not have required " + originalHeader + " header present"), null);
        } else {
            String chunkPrefix = null, originalPrefix = null;
            if (hasPrefix) {
                chunkPrefix = extractPrefix(rawChunkHeader);
                if (StringUtils.isBlank(chunkPrefix)) {
                    return handleError(event, new IllegalArgumentException(
                            chunkHeader + " does not have required '<id>:' prefix"), null);
                }
                rawChunkHeader = rawChunkHeader.substring(rawChunkHeader.indexOf(':') + 1);
                originalPrefix = extractPrefix(rawOriginalHeader);
                if (StringUtils.isBlank(originalPrefix)) {
                    return handleError(event, new IllegalArgumentException(
                            originalHeader + " does not have required '<id>:' prefix"), null);
                }
                rawOriginalHeader = rawOriginalHeader.substring(rawOriginalHeader.indexOf(':') + 1);
            }
            try {
                T chunkValue = parser.apply(rawChunkHeader);
                try {
                    T originalValue = parser.apply(rawOriginalHeader);
                    return new ChunkInfo<>(chunkPrefix, chunkValue, originalPrefix, originalValue);
                } catch (Throwable t) {
                    return handleError(event, new IllegalArgumentException(
                            "Invalid " + originalHeader + " value (not parseable): " + rawOriginalHeader, t), null);
                }
            } catch (Throwable t) {
                return handleError(event, new IllegalArgumentException(
                        "Invalid " + chunkHeader + " value (not parseable): " + rawChunkHeader, t), null);
            }
        }
    }

    private static String extractPrefix(String rawChunkHeader) {
        if (rawChunkHeader.indexOf(':') != -1) {
            return rawChunkHeader.substring(0, rawChunkHeader.indexOf(':'));
        }
        return null;
    }

    /**
     * Handles an error processing a chunk event, throwing it if necessary
     *
     * @param event       Event
     * @param t           Error
     * @param returnValue Return value to return after handling the error, this is a convenience to simplify code logic
     *                    so that we don't have to call this method and then return what we want but rather can return
     *                    it via this method
     * @param <T>         Return type
     * @return Always {@code null}
     */
    private <T> T handleError(Event<TKey, TValue> event, Throwable t, T returnValue) {
        LOGGER.error("Received bad chunk event for key {}: {}", event.key(), t.getMessage());

        if (this.dlq == null) {
            throw new EventSourceException("Received bad chunk event for key " + event.key(), t);
        } else {
            try {
                this.dlq.send(
                        event.addHeaders(Stream.of(new Header(TelicentHeaders.DEAD_LETTER_REASON, t.getMessage()))));
            } catch (Throwable e) {
                LOGGER.warn("Failed to send bad chunk event to DLQ: {}", e.getMessage());
            }
        }
        return returnValue;
    }

    @Override
    public Long remaining() {
        return this.source.remaining();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void processed(Collection<Event> processedEvents) {
        this.source.processed(processedEvents);
    }

    @Override
    public void interrupt() {
        this.source.interrupt();
    }

    private record ValidatedChunkInfo(boolean valid, ChunkInfo<Long> checksums, ChunkInfo<String> hashes) {
    }

    private record ChunkInfo<T>(String chunkPrefix, T chunk, String originalPrefix, T original) {
    }
}
