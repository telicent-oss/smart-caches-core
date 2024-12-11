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
package io.telicent.smart.cache.security.entitlements;

/**
 * Interface for entitlements parsers
 */
public interface EntitlementsParser {

    /**
     * Parses entitlements from raw byte sequence
     *
     * @param rawEntitlements Raw entitlements byte sequence
     * @return Entitlements
     * @throws MalformedEntitlementsException Thrown if the entitlements are invalid
     */
    Entitlements<?> parseEntitlements(byte[] rawEntitlements) throws MalformedEntitlementsException;
}
