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
package io.telicent.smart.cache.sources.file.text;

import io.telicent.smart.cache.sources.file.FileEventSource;
import io.telicent.smart.cache.sources.file.NumericFilenameComparator;
import io.telicent.smart.cache.sources.file.NumericallyNamedWithExtensionFilter;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.File;
import java.io.FileFilter;

/**
 * A file event source where the events are plain text encoded, see {@link PlainTextEventReaderWriter} for more detail
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public class PlainTextFileEventSource<TKey, TValue> extends FileEventSource<TKey, TValue> {
    /**
     * The default file filter used to select plain text event files
     */
    public static final FileFilter PLAINTEXT_FILTER = new NumericallyNamedWithExtensionFilter(".txt");

    /**
     * Creates a new file event source
     *
     * @param sourceDir         Source directory containing the events
     * @param valueDeserializer Value deserializer
     */
    public PlainTextFileEventSource(File sourceDir, Deserializer<TValue> valueDeserializer) {
        super(sourceDir, PLAINTEXT_FILTER, new NumericFilenameComparator(),
              new PlainTextEventReaderWriter<>(valueDeserializer));
    }
}
