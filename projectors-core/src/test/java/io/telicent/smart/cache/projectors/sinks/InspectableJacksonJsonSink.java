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
package io.telicent.smart.cache.projectors.sinks;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;

/**
 * An extension of the {@link JacksonJsonSink} purely for test purposes, so we can examine the autoconfigured output and
 * object mapper without having to expose that on the original class
 */
public class InspectableJacksonJsonSink<T> extends JacksonJsonSink<T> {

    /**
     * Creates a new sink with default options (standard out and compact printing)
     */
    public InspectableJacksonJsonSink() {
        super();
    }

    /**
     * Creates a new sink with custom options and default destination of {@link System#out}
     *
     * @param prettyPrint Whether to pretty print output
     */
    public InspectableJacksonJsonSink(boolean prettyPrint) {
        super(prettyPrint);
    }

    /**
     * Gets the configured output stream
     *
     * @return Output stream
     */
    public OutputStream getOutputStream() {
        return this.output;
    }

    /**
     * Gets the configured object mapper
     *
     * @return Object mapper
     */
    public ObjectMapper getObjectMapper() {
        return this.mapper;
    }
}
