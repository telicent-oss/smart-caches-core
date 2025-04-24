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
package io.telicent.smart.cache.sources;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface for Event Headers which are Key Value pairs
 */
public interface EventHeader {

    /**
     * Header Key (aka Name)
     *
     * @return Key
     */
    String key();

    /**
     * Header value as a string
     *
     * @return String value
     */
    String value();

    /**
     * Header value as a raw byte sequence
     *
     * @return Raw byte value
     */
    @JsonIgnore
    byte[] rawValue();
}
