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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

/**
 * An abstract lazy payload which is deserialized using Jackson
 * <p>
 * This is designed for use as a key/value type when there is a possibility of malformed events that would otherwise
 * cause head of line blocking when attempting to deserialize them.  By delaying deserialization to the application code
 * the underlying event source is not blocked in producing events and the application can handle malformed events
 * appropriately.
 * </p>
 *
 * @param <T> Value type
 */
public abstract class LazyJacksonPayload<T> extends LazyPayload<T> {

    private final ObjectMapper mapper;
    private final Class<T> cls;

    /**
     * Creates a new lazily deserialized payload
     *
     * @param mapper  Jackson Object Mapper appropriately configured to deserialize the payload
     * @param cls     Value type
     * @param rawData Raw data
     */
    protected LazyJacksonPayload(ObjectMapper mapper, Class<T> cls, byte[] rawData) {
        super(null, rawData);
        this.mapper = Objects.requireNonNull(mapper, "Jackson ObjectMapper cannot be null");
        this.cls = Objects.requireNonNull(cls, "Value class cannot be null");
    }

    /**
     * Creates a pre-populated payload
     *
     * @param value  Value
     */
    protected LazyJacksonPayload(T value) {
        super(value);
        this.mapper = null;
        this.cls = null;
    }

    @Override
    protected final T deserialize() {
        try {
            return this.mapper.readValue(this.getRawData(), this.cls);
        } catch (IOException e) {
            throw new LazyPayloadException("Jackson failed to deserialize the payload, see cause for details", e);
        }
    }
}
