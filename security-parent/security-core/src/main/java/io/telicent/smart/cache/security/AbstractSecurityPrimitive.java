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
package io.telicent.smart.cache.security;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AbstractSecurityPrimitive implements SecurityPrimitive {
    private final short schema;
    private final byte[] encoded;

    @Override
    public byte[] encoded() {
        return this.encoded;
    }

    @Override
    public short schema() {
        return this.schema;
    }

    @Override
    public String toString() {
        return this.getClass()
                   .getSimpleName() + "{ schema=" + this.schema + ", encodedSize=" + this.encoded.length + "}";
    }
}
