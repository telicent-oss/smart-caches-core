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
package io.telicent.smart.cache.security.data.labels;

import io.telicent.smart.cache.security.data.SecurityPrimitive;

/**
 * Interface for holding security labels
 *
 * @param <T> Decoded labels type
 */
public interface SecurityLabels<T> extends SecurityPrimitive {

    /**
     * The security label decoded into a suitable concrete representation for a security plugin to process
     *
     * @return Decoded label representation
     */
    T decodedLabels();

    /**
     * A human-readable format of the security labels intended for debugging purposes
     * <p>
     * This is intentionally a separate method instead of overriding {@link Object#toString()}, which an implementation
     * may still choose to do so.  The idea here is that producing a human-readable debugging string may involve
     * significantly more work than just producing a quick string representation, and we want to allow for both.  For
     * example the {@link Object#toString()} override might just print the size of the encoded label, whereas the
     * implementation of this method should decode the label and render it in a way that better aids debugging.
     * </p>
     *
     * @return Debugging representation of the label
     */
    String toDebugString();
}
