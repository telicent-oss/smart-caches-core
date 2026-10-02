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
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * A lazy deserialiser for the {@link LazyUUID} type
 * <p>
 * This is a drop in replacement for Kafka's {@link UUIDDeserializer} that never fails on malformed data.  Instead, it
 * captures the raw bytes, leaving the actual parsing to {@link LazyUUID#getValue()}, i.e. until the
 * application actually needs to inspect the key.  A malformed key therefore no longer causes head of line blocking by
 * throwing during {@code poll()}, applications get an event they can forward to a Dead Letter Queue (DLQ) instead.
 * </p>
 * <p>
 * The character set used to decode the raw bytes is configured exactly as for {@link UUIDDeserializer} i.e. via the
 * {@value #KEY_ENCODING_CONFIG}, {@value #VALUE_ENCODING_CONFIG} or {@value #ENCODING_CONFIG} properties, the first two
 * of which take precedence over the last.  It defaults to {@link LazyUUID#DEFAULT_CHARSET}.
 * </p>
 */
public class LazyUUIDDeserializer implements Deserializer<LazyUUID> {

    /**
     * Configuration property specifying the character set used to decode keys
     */
    public static final String KEY_ENCODING_CONFIG = "key.deserializer.encoding";
    /**
     * Configuration property specifying the character set used to decode values
     */
    public static final String VALUE_ENCODING_CONFIG = "value.deserializer.encoding";
    /**
     * Configuration property specifying the character set used to decode keys and values, the key/value specific
     * properties take precedence over this
     */
    public static final String ENCODING_CONFIG = "deserializer.encoding";

    private Charset charset = LazyUUID.DEFAULT_CHARSET;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        final Object specific = configs.get(isKey ? KEY_ENCODING_CONFIG : VALUE_ENCODING_CONFIG);
        final Object encoding = specific != null ? specific : configs.get(ENCODING_CONFIG);
        if (encoding instanceof String encodingName) {
            try {
                this.charset = Charset.forName(encodingName);
            } catch (IllegalArgumentException e) {
                throw new SerializationException(
                        "Error when deserializing byte[] to UUID due to unsupported encoding " + encodingName, e);
            }
        }
    }

    @Override
    public final LazyUUID deserialize(String topic, byte[] data) {
        return deserialize(topic, null, data);
    }

    @Override
    public LazyUUID deserialize(String topic, Headers headers, byte[] data) {
        if (data == null) {
            return null;
        }
        return LazyUUID.of(data, this.charset);
    }

}
