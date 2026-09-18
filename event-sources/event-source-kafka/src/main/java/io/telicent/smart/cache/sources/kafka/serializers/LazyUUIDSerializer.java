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

import io.telicent.smart.cache.payloads.LazyUUID;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.UUIDSerializer;

import java.util.Map;

/**
 * A Kafka serializer for the {@link LazyUUID} type
 * <p>
 * Well-formed values are serialized by delegating to Kafka's {@link UUIDSerializer} so the on the wire format is
 * identical to that produced by applications using a bare {@link java.util.UUID} key.  Where the original raw bytes are
 * still held they are written back out verbatim and this is what allows a malformed key to be safely forwarded to the
 * DLQ.
 * </p>
 */
// java:S1168 - a null return is the Kafka tombstone marker; an empty array would emit a zero-length record instead
@SuppressWarnings("java:S1168")
public class LazyUUIDSerializer implements Serializer<LazyUUID> {

    private final UUIDSerializer serializer = new UUIDSerializer();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.serializer.configure(configs, isKey);
    }

    @Override
    public final byte[] serialize(String topic, LazyUUID data) {
        return serialize(topic, null, data);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, LazyUUID data) {
        if (data == null) {
            return null;
        }

        // If we still hold the raw data, i.e. the payload was never deserialised or failed to deserialise, then write
        // that back out as-is.  This is what permits malformed keys to be written to a DLQ.
        if (data.hasRawData()) {
            return data.getRawData();
        }
        if (data.hasError()) {
            throw new SerializationException("Cannot serialise malformed UUID as its raw data is not available");
        }

        return this.serializer.serialize(topic, headers, data.getValue());
    }

    @Override
    public void close() {
        this.serializer.close();
    }
}
