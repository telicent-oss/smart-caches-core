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

import io.telicent.smart.cache.security.data.Authorizer;

/**
 * Interface for parsing security labels
 */
public interface SecurityLabelsParser {

    /**
     * Parses security labels
     * <p>
     * An implementation may choose to cache parsing results so when seeing the same byte sequence repeatedly they can
     * quickly return the same labels object.  This could help with implementing a performant
     * {@link Authorizer} implementation, see discussion on
     * {@link Authorizer#canRead(SecurityLabels)}.
     * </p>
     *
     * @param rawLabels Raw label byte sequence
     * @return Parsed labels
     * @throws MalformedLabelsException Thrown if the byte sequence does not represent a labels schema supported by this
     *                                  parser
     */
    SecurityLabels<?> parseSecurityLabels(byte[] rawLabels);
}
