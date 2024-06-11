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

import java.io.File;
import java.util.Comparator;
import java.util.Objects;

/**
 * A file based event source that yields a single event based on a single file
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public class SingleFileEventSource<TKey, TValue> extends FileEventSource<TKey, TValue> {
    /**
     * Creates a new single file event source
     *
     * @param sourceFile Source file  to use as the single event
     * @param reader     File event reader to use to convert the files into events
     */
    public SingleFileEventSource(File sourceFile,
                                 FileEventReaderWriter<TKey, TValue> reader) {
        super(sourceFile.getParentFile(),
              f -> Objects.equals(f.getAbsolutePath(), sourceFile.getAbsolutePath()),
              Comparator.naturalOrder(), reader);
    }
}
