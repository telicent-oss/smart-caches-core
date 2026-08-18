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
package io.telicent.smart.cache.security.data.plugins.failsafe;

import java.util.Arrays;

/**
 * A holder of raw encoded data
 *
 * @param data Raw encoded data
 */
public record RawBytes(byte[] data) {

    /**
     * A record wrapping an array gets identity-based equals/hashCode, so two instances holding identical
     * bytes would otherwise compare unequal.  Compare and hash the array contents instead.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof RawBytes rawBytes && Arrays.equals(this.data, rawBytes.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    /**
     * Reports the payload length rather than the bytes themselves, so encoded security labels are never
     * written into logs or error messages.
     */
    @Override
    public String toString() {
        return "RawBytes[length=" + (this.data != null ? this.data.length : 0) + "]";
    }
}
