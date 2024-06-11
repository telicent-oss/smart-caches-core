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
package io.telicent.smart.cache.sources.file.rdf;

import io.telicent.smart.cache.sources.file.FileEventSource;
import io.telicent.smart.cache.sources.file.NumericFilenameComparator;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.File;

/**
 * A file event source that just reads RDF files directly
 */
public class RdfFileEventSource<TKey, TValue> extends FileEventSource<TKey, TValue> {
    /**
     * Creates a new file event source that reads RDF files directly
     *
     * @param sourceDir         Source directory containing the RDF files to treat as events
     * @param keyDeserializer   Key deserializer
     * @param valueDeserializer Value deserializer
     */
    public RdfFileEventSource(File sourceDir, Deserializer<TKey> keyDeserializer,
                              Deserializer<TValue> valueDeserializer) {
        super(sourceDir, new NumericallyNamedRdfFilter(), new NumericFilenameComparator(),
              new RdfEventReaderWriter<>(keyDeserializer, valueDeserializer));
    }
}
