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

import io.telicent.smart.cache.sources.Event;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A file event reader
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public interface FileEventReader<TKey, TValue> {
    /**
     * Reads an event from a file
     *
     * @param f File
     * @return Event
     * @throws IOException Thrown if the file cannot be read as an event
     */
    Event<TKey, TValue> read(File f) throws IOException;

    /**
     * Reads an event from an input stream
     *
     * @param input Input stream
     * @return Event
     * @throws IOException Thrown if the stream cannot be read as an event
     */
    Event<TKey, TValue> read(InputStream input) throws IOException;
}
