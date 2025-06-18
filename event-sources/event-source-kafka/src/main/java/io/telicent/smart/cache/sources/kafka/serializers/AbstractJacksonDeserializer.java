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
package io.telicent.smart.cache.sources.kafka.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Objects;

/**
 * An abstract Jackson based Kafka deserializer
 *
 * @param <T> Value type
 */
public class AbstractJacksonDeserializer<T> extends AbstractJacksonSerdes implements Deserializer<T> {

    private final Class<T> cls;

    /**
     * Creates a new deserializer using the default Jackson Object Mapper
     *
     * @param cls Value type
     */
    public AbstractJacksonDeserializer(Class<T> cls) {
        this(new ObjectMapper(), cls);
    }

    /**
     * Creates a new deserializer using the specified Jackson Object Mapper
     * @param objectMapper Object Mapper
     * @param cls Value type
     */
    public AbstractJacksonDeserializer(ObjectMapper objectMapper, Class<T> cls) {
        super(objectMapper);
        this.cls = Objects.requireNonNull(cls, "Class to deserialize cannot be null");
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        return deserialize(topic, null, data);
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        try {
            return this.deserialize(data, this.cls);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }
}
