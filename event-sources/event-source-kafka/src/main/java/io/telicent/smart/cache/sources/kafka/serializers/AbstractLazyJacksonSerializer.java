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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.smart.cache.payloads.LazyJacksonPayload;
import io.telicent.smart.cache.payloads.LazyPayloadException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

/**
 * An abstract Kafka serializer that uses Jackson for serialization
 *
 * @param <T> Value type
 */
public class AbstractLazyJacksonSerializer<T, TLazy extends LazyJacksonPayload<T>> extends AbstractJacksonSerdes
        implements Serializer<TLazy> {

    /**
     * Creates a serializer using a default Jackson {@link ObjectMapper}
     */
    public AbstractLazyJacksonSerializer() {
        super();
    }

    /**
     * Creates a serializer using the specified Jackson {@link ObjectMapper}
     *
     * @param objectMapper Object Mapper
     */
    public AbstractLazyJacksonSerializer(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public final byte[] serialize(String topic, TLazy data) {
        return serialize(topic, null, data);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, TLazy data) {
        if (data == null) {
            return null;
        }

        if (data.hasError()) {
            // We can still serialize even if we have a malformed payload if the raw data is available
            // This is useful in that it allows us to safely forward malformed payloads onto DLQ topics
            if (data.hasRawData()) {
                return data.getRawData();
            }
            throw unableToSerialize();
        } else {
            try {
                return this.serialize(data.getValue());
            } catch (LazyPayloadException e) {
                // It's possibly the lazy payload was never deserialized so the error did not occur until we just tried
                // to get the value to deserialize, in this case we can still serialize it by simply passing its raw
                // data onwards as-is
                if (data.hasRawData()) {
                    return data.getRawData();
                }
                throw unableToSerialize();
            } catch (JsonProcessingException e) {
                // If something goes wrong with Jackson serialization then just wrap into a Kafka error
                throw new SerializationException("Jackson failed to serialize lazy payload: " + e.getMessage(), e);
            }
        }
    }

    private static SerializationException unableToSerialize() {
        return new SerializationException(
                "Cannot serialize malformed lazy payload as its raw data is not available");
    }
}
