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

/**
 * A lazy payload wrapper for the {@link Envelope}
 * <p>
 * Uses the shared {@link Envelope#JSON} object mapper to ensure consistent handling of the actual {@link Envelope}
 * type.
 * </p>
 */
public class LazyEnvelope extends LazyJacksonPayload<Envelope> {
    /**
     * Creates a lazily deserialized payload
     *
     * @param rawData Raw data
     */
    protected LazyEnvelope(byte[] rawData) {
        super(Envelope.JSON, Envelope.class, rawData);
    }

    /**
     * Creates a populated payload
     *
     * @param value Envelope value
     */
    protected LazyEnvelope(Envelope value) {
        super(value);
    }

    /**
     * Creates a new lazy envelope from raw data
     *
     * @param rawData Raw data
     * @return Lazy envelope
     */
    public static LazyEnvelope of(byte[] rawData) {
        return new LazyEnvelope(rawData);
    }

    /**
     * Creates a new populated lazy envelope from a value
     *
     * @param envelope Envelope value
     * @return Lazy envelope
     */
    public static LazyEnvelope of(Envelope envelope) {
        return new LazyEnvelope(envelope);
    }
}
