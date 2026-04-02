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

import io.telicent.smart.cache.projectors.utils.WriteOnceReference;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents an abstract lazy payload
 * <p>
 * This is a payload whose initial deserialization is just to hold the raw bytes, only when the value is actually
 * accessed does the payload get deserialized for real.  This helps to avoid head of line blocking since we are always
 * able to pull a lazy payload from Kafka, it's only when we process the value in application code that we have to
 * handle the potential {@link LazyPayloadException}.
 * </p>
 * <p>
 * Malformed lazy payloads can also be safely serialized back to another DLQ by just copying the original raw bytes back
 * out.
 * </p>
 */
public abstract class LazyPayload<T> {

    public static final int UNKNOWN_SIZE = -1;
    @Getter
    private byte[] rawData;
    private final long sizeInBytes;
    protected final String contentType;
    private final WriteOnceReference<T> value = new WriteOnceReference<>();

    /**
     * Creates a lazily deserialised payload
     *
     * @param contentType Content Type (if known)
     * @param rawData     The raw data for lazy deserialisation
     */
    protected LazyPayload(String contentType, byte[] rawData) {
        this.contentType = contentType;
        this.rawData = Objects.requireNonNull(rawData, "Raw RDF Payload Data cannot be null");
        this.sizeInBytes = this.rawData.length;
    }

    /**
     * Creates a pre-populated payload
     *
     * @param value Payload value
     */
    protected LazyPayload(T value) {
        this(value, null);
    }

    /**
     * Creates a pre-populated payload
     *
     * @param value Payload value
     */
    protected LazyPayload(T value, String contentType) {
        this.value.set(Objects.requireNonNull(value, "Value cannot be null"));
        this.contentType = contentType;
        this.sizeInBytes = UNKNOWN_SIZE;
    }

    /**
     * Gets whether this payload contains raw data i.e. it has been lazily deserialised and the raw data has yet to be
     * deserialised into an actual data structure.
     * <p>
     * Note that if {@link #getValue()} method has already been called and the payload was successfully deserialised
     * then the raw data would have been cleared as a result as it is no longer needed.  Thus, this will only be
     * {@code true} if deserialisation was never attempted, or was attempted but failed due to malformed data.
     * </p>
     *
     * @return True if raw data is present, false otherwise
     */
    public boolean hasRawData() {
        return rawData != null;
    }

    /**
     * Gets the size in bytes of the original payloads raw data, maybe {@code -1} if not created from a byte sequence.
     * <p>
     * If this payload was created from a byte sequence then this method returns the size of that byte sequence.  This
     * value will be set even if the payload has been processed such that the actual raw byte sequence is no longer
     * available, as per notes on {@link #hasRawData()} once we've successfully deserialised the byte sequence we don't
     * hold it in memory, preferring to hold just the deserialised data structure instead.
     * </p>
     *
     * @return Size in bytes, or {@code -1} if not known
     */
    public long sizeInBytes() {
        return this.sizeInBytes;
    }

    /**
     * Gets whether this payload is ready for immediate processing i.e. if it's a lazily deserialised payload has it
     * been deserialised?
     *
     * @return True if ready, false otherwise
     */
    public boolean isReady() {
        return this.value.isSet();
    }

    /**
     * Gets the value for this payload (if any)
     *
     * @return Value
     * @throws LazyPayloadException Thrown if the raw data for this payload cannot be deserialised into a valid value
     */
    public T getValue() {
        return this.value.computeIfAbsent(() -> {
            // Abort if not a lazy payload, if this is the case we should never hit this case as value should be set
            // but this is just extra protection
            if (this.rawData == null) {
                return null;
            }

            T value = deserialize();
            // Upon successfully deserialization clear the raw data as don't need a copy of that as well as the
            // deserialized value we'll now be holding
            clearRawData();
            return value;
        });
    }

    /**
     * Performs the actual deserialization of the payload or throws a {@link LazyPayloadException} if unable to
     * deserialize
     *
     * @return Deserialized value
     * @throws LazyPayloadException Thrown if the payload fails to deserialise
     */
    protected abstract T deserialize();

    private void clearRawData() {
        // Once we've successfully deserialised can stop storing the raw bytes
        this.rawData = null;
    }
}
