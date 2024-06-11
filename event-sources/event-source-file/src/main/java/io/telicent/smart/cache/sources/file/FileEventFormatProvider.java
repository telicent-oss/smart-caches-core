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
package io.telicent.smart.cache.sources.file;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.io.File;

/**
 * A file event format provider
 */
public interface FileEventFormatProvider {

    /**
     * Gets the name of the file event format
     *
     * @return Format name
     */
    String name();

    /**
     * Gets the default file extension for the format
     *
     * @return File extension
     */
    String defaultFileExtension();

    /**
     * Creates a reader for the format
     *
     * @param keyDeserializer   Key deserializer
     * @param valueDeserializer Value deserializer
     * @param <TKey>            Key type
     * @param <TValue>          Value type
     * @return Reader
     */
    <TKey, TValue> FileEventReader<TKey, TValue> createReader(Deserializer<TKey> keyDeserializer,
                                                              Deserializer<TValue> valueDeserializer);

    /**
     * Creates a writer for the format
     *
     * @param keySerializer   Key serializer
     * @param valueSerializer Value serializer
     * @param <TKey>          Key type
     * @param <TValue>        Value type
     * @return Writer
     */
    <TKey, TValue> FileEventWriter<TKey, TValue> createWriter(Serializer<TKey> keySerializer,
                                                              Serializer<TValue> valueSerializer);

    /**
     * Creates a reader/writer for the format
     *
     * @param keyDeserializer   Key deserializer
     * @param valueDeserializer Value deserializer
     * @param keySerializer     Key serializer
     * @param valueSerializer   Value serializer
     * @param <TKey>            Key type
     * @param <TValue>          Value type
     * @return Reader/Writer
     */
    <TKey, TValue> FileEventReaderWriter<TKey, TValue> createReaderWriter(Deserializer<TKey> keyDeserializer,
                                                                          Deserializer<TValue> valueDeserializer,
                                                                          Serializer<TKey> keySerializer,
                                                                          Serializer<TValue> valueSerializer);

    /**
     * Creates a new directory event source for the format
     *
     * @param keyDeserializer   Key deserializer
     * @param valueDeserializer Value deserializer
     * @param source            Source directory
     * @param <TKey>            Key type
     * @param <TValue>          Value type
     * @return Event source
     */
    <TKey, TValue> FileEventSource<TKey, TValue> createSource(Deserializer<TKey> keyDeserializer,
                                                              Deserializer<TValue> valueDeserializer, File source);

    /**
     * Creates a new single file event source for the format
     *
     * @param keyDeserializer   Key deserializer
     * @param valueDeserializer Value deserializer
     * @param source            Source file
     * @param <TKey>            Key type
     * @param <TValue>          Value type
     * @return Event source
     */
    <TKey, TValue> FileEventSource<TKey, TValue> createSingleFileSource(Deserializer<TKey> keyDeserializer,
                                                                        Deserializer<TValue> valueDeserializer,
                                                                        File source);

}
