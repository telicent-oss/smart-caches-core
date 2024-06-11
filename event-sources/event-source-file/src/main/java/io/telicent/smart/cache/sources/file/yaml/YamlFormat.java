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
package io.telicent.smart.cache.sources.file.yaml;

import io.telicent.smart.cache.sources.file.*;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.io.File;

/**
 * YAML file event format provider
 */
public class YamlFormat implements FileEventFormatProvider {

    /**
     * Name of the YAML event format
     */
    public static final String NAME = "yaml";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public <TKey, TValue> FileEventReader<TKey, TValue> createReader(Deserializer<TKey> keyDeserializer,
                                                                     Deserializer<TValue> valueDeserializer) {
        return new YamlEventReaderWriter<>(keyDeserializer, valueDeserializer);
    }

    @Override
    public <TKey, TValue> FileEventWriter<TKey, TValue> createWriter(Serializer<TKey> keySerializer,
                                                                     Serializer<TValue> valueSerializer) {
        return new YamlEventReaderWriter<>(keySerializer, valueSerializer);
    }

    @Override
    public <TKey, TValue> FileEventReaderWriter<TKey, TValue> createReaderWriter(Deserializer<TKey> keyDeserializer,
                                                                                 Deserializer<TValue> valueDeserializer,
                                                                                 Serializer<TKey> keySerializer,
                                                                                 Serializer<TValue> valueSerializer) {
        return new YamlEventReaderWriter<>(keyDeserializer, valueDeserializer, keySerializer, valueSerializer);
    }

    @Override
    public <TKey, TValue> FileEventSource<TKey, TValue> createSource(Deserializer<TKey> keyDeserializer,
                                                                     Deserializer<TValue> valueDeserializer,
                                                                     File source) {
        return new YamlFileEventSource<>(source, keyDeserializer, valueDeserializer);
    }

    @Override
    public <TKey, TValue> FileEventSource<TKey, TValue> createSingleFileSource(Deserializer<TKey> keyDeserializer,
                                                                               Deserializer<TValue> valueDeserializer,
                                                                               File source) {
        return new SingleFileEventSource<>(source, new YamlEventReaderWriter<>(keyDeserializer, valueDeserializer));
    }

    @Override
    public String defaultFileExtension() {
        return ".yaml";
    }
}
