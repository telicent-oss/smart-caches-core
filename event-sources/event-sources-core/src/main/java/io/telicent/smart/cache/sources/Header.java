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
import java.util.Objects;

/**
 * Default implementation of an {@link EventHeader}
 */
public record Header(String key, String value) implements EventHeader {

    @Override
    public String toString() {
        return String.format("%s: %s", this.key, this.value);
    }

    @Override
    public byte[] rawValue() {
        return this.value != null ? this.value.getBytes(StandardCharsets.UTF_8) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EventHeader header)) return false;
        return Objects.equals(key, header.key()) && Objects.deepEquals(this.rawValue(), header.rawValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, Arrays.hashCode(this.rawValue()));
    }
}
