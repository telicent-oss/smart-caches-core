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
package io.telicent.smart.caches.security.entitlements;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.telicent.smart.caches.security.labels.SecurityLabels;

public interface EntitlementsProvider {

    /**
     * Obtains the entitlements for the user
     * <p>
     * This might involve reaching out to some external entitlements service, querying some local authorization
     * database, interpreting claims from the users JSON Web Token (JWT), or some combination thereof.  Generally
     * speaking an implementation should only throw {@link MalformedEntitlementsException} if these underlying
     * entitlements sources return bad data in some way.
     * </p>
     * </p>
     * If the user simply does not have any entitlements in the system they should return an empty entitlements object,
     * however that may be represented in concrete terms.  As long as when their
     * {@link io.telicent.smart.caches.security.Authorizer} implementation is used with this "empty" entitlements object
     * it makes correct fail-safe access decisions as discussed on
     * {@link io.telicent.smart.caches.security.Authorizer#canAccess(SecurityLabels)}, i.e., providing an "empty"
     * entitlements object <strong>MUST NOT</strong> cause incorrect access decisions to be made.
     * </p>
     *
     * @param jws    Users verified JWT
     * @param userId User identifier as previously extracted by the plugins
     *               {@link io.telicent.smart.caches.security.identity.IdentityProvider}
     * @return Users entitlements
     * @throws MalformedEntitlementsException Thrown if the users entitlements are malformed or otherwise invalid
     */
    Entitlements<?> entitlementsForUser(Jws<Claims> jws, String userId) throws MalformedEntitlementsException;
}
