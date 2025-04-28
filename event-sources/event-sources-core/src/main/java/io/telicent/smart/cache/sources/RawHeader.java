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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * Basic implementation of a Header that has a raw byte sequence value
 *
 * @param key      Key
 * @param rawValue Raw value
 */
public record RawHeader(String key, byte[] rawValue) implements EventHeader {
    @Override
    public String value() {
        return this.rawValue != null ? new String(this.rawValue, StandardCharsets.UTF_8) : null;
    }

    @Override
    public String toString() {
        return String.format("%s: %,d bytes (%s)", this.key, this.rawValue != null ? this.rawValue.length : 0,
                             this.rawValue != null ? Base64.getEncoder().encodeToString(this.rawValue) : null);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EventHeader header)) return false;
        return Objects.equals(key, header.key()) && Objects.deepEquals(rawValue, header.rawValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, Arrays.hashCode(rawValue));
    }
}
