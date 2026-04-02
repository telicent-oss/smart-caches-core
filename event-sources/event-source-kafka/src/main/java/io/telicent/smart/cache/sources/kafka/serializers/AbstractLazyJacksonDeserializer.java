/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.cache.sources.kafka.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.smart.cache.payloads.LazyJacksonPayload;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Objects;

/**
 * An abstract Jackson based Kafka deserializer that produces types derived from {@link LazyJacksonPayload} in order to
 * avoid Head of Line blocking
 *
 * @param <T> Value type
 */
public abstract class AbstractLazyJacksonDeserializer<T, TLazy extends LazyJacksonPayload<T>>
        extends AbstractJacksonSerdes implements Deserializer<TLazy> {

    protected final Class<T> cls;

    /**
     * Creates a new deserializer using the default Jackson Object Mapper
     *
     * @param cls Value type
     */
    public AbstractLazyJacksonDeserializer(Class<T> cls) {
        this(new ObjectMapper(), cls);
    }

    /**
     * Creates a new deserializer using the specified Jackson Object Mapper
     *
     * @param objectMapper Object Mapper
     * @param cls          Value type
     */
    public AbstractLazyJacksonDeserializer(ObjectMapper objectMapper, Class<T> cls) {
        super(objectMapper);
        this.cls = Objects.requireNonNull(cls, "Class to deserialize cannot be null");
    }

    @Override
    public final TLazy deserialize(String topic, byte[] data) {
        return deserialize(topic, null, data);
    }

    @Override
    public final TLazy deserialize(String topic, Headers headers, byte[] data) {
        if (data == null) {
            return null;
        }
        return createLazyPayload(topic, headers, data);
    }

    /**
     * Implements the actual creation of the {@link LazyJacksonPayload} derived payload type
     * <p>
     * Implementation should pass the {@link ObjectMapper} and value type stored in the deserializers {@code mapper} and
     * {@code cls} fields onwards to the lazy payload implementation
     * </p>
     *
     * @param topic   Kafka topic
     * @param headers Kafka headers
     * @param data    Raw data
     * @return Lazy Jackson payload
     */
    protected abstract TLazy createLazyPayload(String topic, Headers headers, byte[] data);
}
