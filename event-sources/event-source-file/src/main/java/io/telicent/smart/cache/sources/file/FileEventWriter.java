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
import java.io.OutputStream;

/**
 * A file event writer
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public interface FileEventWriter<TKey, TValue> {
    /**
     * Writes an event to a file
     *
     * @param event Event
     * @param f     File
     * @throws IOException Thrown if the event cannot be written to the file
     */
    void write(Event<TKey, TValue> event, File f) throws IOException;

    /**
     * Writes an event to a file
     *
     * @param event  Event
     * @param output File
     * @throws IOException Thrown if the event cannot be written to the file
     */
    void write(Event<TKey, TValue> event, OutputStream output) throws IOException;
}
