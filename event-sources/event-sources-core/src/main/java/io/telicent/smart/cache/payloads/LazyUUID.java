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
package io.telicent.smart.cache.payloads;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * A lazy payload wrapper for {@link UUID}
 * <p>
 * This applies the same deserialisation logic as Kafka's {@code UUIDDeserializer} i.e. the raw data is decoded to a
 * string using the configured character set (UTF-8 by default) and then parsed via {@link UUID#fromString(String)}.
 * Unlike that deserialiser the parsing is deferred until {@link #getValue()} is called, so a malformed UUID no longer
 * causes head of line blocking by failing during the {@code poll()} of the underlying event source.  Instead the
 * application receives an event it can inspect, report on, and forward to a Dead Letter Queue (DLQ).
 * </p>
 * <p>
 * This is primarily intended for use as an event <strong>key</strong> type, keys are frequently never inspected by
 * applications at all so eagerly parsing them offers no benefit and only risks blocking a pipeline.
 * </p>
 */
public class LazyUUID extends LazyPayload<UUID> {

    /**
     * The character set used to decode the raw data when no explicit character set is supplied, this matches the
     * default used by Kafka's {@code UUIDDeserializer}
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * The maximum number of characters of the raw data included in error messages and {@link #toString()}, this
     * guards against a rogue producer writing an enormous key and that then bloating our logs
     */
    private static final int MAX_RAW_DATA_IN_TO_STRING = 64;

    private final Charset charset;

    /**
     * Creates a lazily deserialised UUID
     *
     * @param rawData Raw data
     * @param charset Character set used to decode the raw data, {@link #DEFAULT_CHARSET} if {@code null}
     */
    protected LazyUUID(byte[] rawData, Charset charset) {
        super(null, rawData);
        this.charset = Objects.requireNonNullElse(charset, DEFAULT_CHARSET);
    }

    /**
     * Creates a pre-populated UUID payload
     *
     * @param value UUID value
     */
    protected LazyUUID(UUID value) {
        super(value);
        this.charset = DEFAULT_CHARSET;
    }

    /**
     * Creates a new lazy UUID from raw data using the {@link #DEFAULT_CHARSET}
     *
     * @param rawData Raw data
     * @return Lazy UUID
     */
    public static LazyUUID of(byte[] rawData) {
        return new LazyUUID(rawData, DEFAULT_CHARSET);
    }

    /**
     * Creates a new lazy UUID from raw data using the given character set
     *
     * @param rawData Raw data
     * @param charset Character set used to decode the raw data, {@link #DEFAULT_CHARSET} if {@code null}
     * @return Lazy UUID
     */
    public static LazyUUID of(byte[] rawData, Charset charset) {
        return new LazyUUID(rawData, charset);
    }

    /**
     * Creates a new pre-populated lazy UUID from the given value
     *
     * @param value UUID value
     * @return Lazy UUID
     */
    public static LazyUUID of(UUID value) {
        return new LazyUUID(value);
    }

    /**
     * Creates a new pre-populated lazy UUID wrapping a freshly generated random UUID
     *
     * @return Lazy UUID
     */
    public static LazyUUID random() {
        return new LazyUUID(UUID.randomUUID());
    }

    @Override
    protected final UUID deserialize() {
        try {
            return UUID.fromString(new String(this.getRawData(), this.charset));
        } catch (IllegalArgumentException e) {
            throw new LazyPayloadException("Failed to parse '" + summariseRawData() + "' as a UUID", e);
        }
    }

    /**
     * Decodes the raw data for use in error messages and {@link #toString()}, truncating it so a rogue producer that
     * writes an enormous key can't bloat our logs
     *
     * @return Decoded raw data, truncated if overly long, or an empty string if no raw data is held
     */
    private String summariseRawData() {
        if (!this.hasRawData()) {
            return "";
        }
        final String raw = new String(this.getRawData(), this.charset);
        return raw.length() > MAX_RAW_DATA_IN_TO_STRING ? raw.substring(0, MAX_RAW_DATA_IN_TO_STRING) + "..." : raw;
    }

    /**
     * Gets the value for this payload, or {@code null} if it is malformed
     * <p>
     * This is a convenience for the common case where a caller wants to use a well-formed UUID but doesn't want to
     * handle the {@link LazyPayloadException} that {@link #getValue()} throws for malformed data.  As with
     * {@link #getValue()} the deserialisation outcome is cached so this is safe to call repeatedly.
     * </p>
     *
     * @return UUID value, or {@code null} if the raw data is not a valid UUID
     */
    public UUID getValueOrNull() {
        try {
            return this.getValue();
        } catch (LazyPayloadException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Two lazy UUIDs are equal if they deserialise to the same {@link UUID}.  Malformed UUIDs are compared by their raw
     * data instead, so a malformed key forwarded to a DLQ still compares equal to the key that was read.
     * </p>
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LazyUUID otherUuid)) {
            return false;
        }

        final UUID ours = this.getValueOrNull();
        final UUID theirs = otherUuid.getValueOrNull();
        if (ours != null || theirs != null) {
            return Objects.equals(ours, theirs);
        }
        // Both malformed so fall back to comparing the raw data
        return Arrays.equals(this.getRawData(), otherUuid.getRawData());
    }

    @Override
    public int hashCode() {
        final UUID value = this.getValueOrNull();
        return value != null ? value.hashCode() : Arrays.hashCode(this.getRawData());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This <strong>NEVER</strong> throws for malformed data as it is frequently used for logging the key of an event
     * that has just been rejected.
     * </p>
     */
    @Override
    public String toString() {
        final UUID value = this.getValueOrNull();
        return value != null ? value.toString() : "<malformed UUID: " + summariseRawData() + ">";
    }
}
