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
package io.telicent.smart.cache.server.jaxrs.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Container for library version information
 */
public class VersionInfo {

    private final Map<String, Properties> libraries = new HashMap<>();

    /**
     * Creates new empty version information
     */
    public VersionInfo() {
    }

    /**
     * Adds a library version to the container
     *
     * @param library    Library
     * @param properties Library version information
     */
    @JsonAnySetter
    public void addLibraryVersion(String library, Properties properties) {
        this.libraries.put(library, properties);
    }

    /**
     * Gets the library versions from the container
     *
     * @return Library versions
     */
    @JsonAnyGetter
    public Map<String, Properties> getLibraryVersions() {
        return this.libraries;
    }
}
