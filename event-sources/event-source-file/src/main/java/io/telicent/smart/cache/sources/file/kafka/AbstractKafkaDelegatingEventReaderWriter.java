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
package io.telicent.smart.cache.sources.file.kafka;

import io.telicent.smart.cache.sources.file.FileEventAccessMode;
import io.telicent.smart.cache.sources.file.FileEventReaderWriter;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Objects;

/**
 * An abstract event reader/writer that delegates the deserialization and serialization of event keys and values to
 * Kafka serializers
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public abstract class AbstractKafkaDelegatingEventReaderWriter<TKey, TValue>
        implements FileEventReaderWriter<TKey, TValue> {
    /**
     * Fake topic to pass to Kafka serializers and deserializers
     */
    public static final String FAKE_TOPIC_FILE = "file";
    /**
     * Key serializer
     */
    protected final Serializer<TKey> keySerializer;
    /**
     * Value serializer
     */
    protected final Serializer<TValue> valueSerializer;
    /**
     * Key deserializer
     */
    protected final Deserializer<TKey> keyDeserializer;
    /**
     * Value deserializer
     */
    protected final Deserializer<TValue> valueDeserializer;

    /**
     * File event access mode
     */
    protected final FileEventAccessMode mode;

    /**
     * Creates a new instance
     *
     * @param mode              File Event access mode
     * @param keyDeserializer   Key deserializer
     * @param valueDeserializer Value deserializer
     * @param keySerializer     Key serializer
     * @param valueSerializer   Value serializer
     */
    public AbstractKafkaDelegatingEventReaderWriter(FileEventAccessMode mode, Deserializer<TKey> keyDeserializer,
                                                    Deserializer<TValue> valueDeserializer,
                                                    Serializer<TKey> keySerializer,
                                                    Serializer<TValue> valueSerializer) {
        this.mode = mode;
        if (mode.requiresDeserializers()) {
            Objects.requireNonNull(keyDeserializer, "Key Deserializer cannot be null");
            Objects.requireNonNull(valueDeserializer, "Value Deserializer cannot be null");
        }
        if (mode.requiresSerializers()) {
            Objects.requireNonNull(keySerializer, "Key Serializer cannot be null");
            Objects.requireNonNull(valueSerializer, "Value Serializer cannot be null");
        }
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
    }

    /**
     * Ensures that write operations are permitted on this instance
     *
     * @param mode Event access mode
     */
    public static void ensureWritesPermitted(FileEventAccessMode mode) {
        if (mode == FileEventAccessMode.ReadOnly) {
            throw new IllegalStateException("This instance not configured for write operations");
        }
    }

    /**
     * Ensures that read operations are permitted on this instance
     *
     * @param mode Event access mode
     */
    public static void ensureReadsPermitted(FileEventAccessMode mode) {
        if (mode == FileEventAccessMode.WriteOnly) {
            throw new IllegalStateException("This instance not configured for read operations");
        }
    }

    @Override
    public FileEventAccessMode getMode() {
        return this.mode;
    }
}
