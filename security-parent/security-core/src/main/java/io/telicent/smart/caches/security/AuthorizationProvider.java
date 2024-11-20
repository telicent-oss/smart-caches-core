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
package io.telicent.smart.caches.security;

import io.telicent.smart.caches.security.labels.SecurityLabels;

/**
 * Interface for authorization providers
 *
 * @param <TEntitlements> Decoded entitlements type
 * @param <TLabels>       Decoded labels type
 */
public interface AuthorizationProvider<TEntitlements, TLabels> {

    /**
     * Prepares an authorizer based on the given entitlements
     * <p>
     * The returned instance is scoped to the lifetime of a single user request so implementors should take that into
     * account when implementing their authorizer, see {@link Authorizer#canAccess(SecurityLabels)} Javadoc for more
     * details.
     * </p>
     *
     * @param entitlements User entitlements
     * @return Authorizer
     */
    Authorizer<TLabels> prepare(TEntitlements entitlements);
}
