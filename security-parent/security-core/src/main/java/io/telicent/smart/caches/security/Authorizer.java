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

import io.telicent.smart.caches.security.entitlements.Entitlements;
import io.telicent.smart.caches.security.labels.SecurityLabels;

/**
 * Interface for authorizers, an authorizer is used to make access decisions within the Platform
 * <p>
 * Authorizers are created via a
 * {@link io.telicent.smart.caches.security.plugins.SecurityPlugin#prepareAuthorizer(Entitlements)} call using a users
 * set of entitlements.  An instance is scoped to the lifetime of a single user request so implementors should consider
 * that lifetime in making their implementation decisions, see {@link #canAccess(SecurityLabels)} documentation for more
 * discussion on this.
 * </p>
 */
public interface Authorizer extends AutoCloseable {

    /**
     * Constant {@code false} value for use in implementations to make the code clearer to read
     */
    boolean FORBIDDEN = false;

    /**
     * Determines whether access is permitted based on the given security labels
     * <p>
     * Implementations should use the entitlements that were used to prepare this instance when
     * {@link io.telicent.smart.caches.security.plugins.SecurityPlugin#prepareAuthorizer(Entitlements)} was called.  As
     * the instance is scoped to the lifetime of a single user request implementations may wish to cache the result of
     * access decisions for its lifetime in order to improve performance as often large swathes of the data may be
     * labelled with the same label.
     * </p>
     * <p>
     * With that in mind implementors should also consider whether their
     * {@link io.telicent.smart.caches.security.labels.SecurityLabelsParser} implementation caches the parsing of byte
     * sequences into {@link SecurityLabels} instances, at least for frequently seen labels.
     * </p>
     * <p>
     * Implementations should always fail-safe, by this we mean if they detect that the provided labels are somehow
     * malformed, or they cannot make a clear access decision, then that decision <strong>MUST</strong> always default
     * to {@code false} and deny access.
     * </p>
     *
     * @param labels Security Labels
     * @return True if access is permitted, false if access is forbidden
     */
    boolean canAccess(SecurityLabels<?> labels);
}
