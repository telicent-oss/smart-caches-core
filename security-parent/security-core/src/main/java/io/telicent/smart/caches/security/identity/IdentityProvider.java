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
package io.telicent.smart.caches.security.identity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * Interface for identity providers, these translate from the users authentication token into a unique identifier for
 * the user, see {@link #identityForUser(Jws)} for more discussion.
 */
public interface IdentityProvider {

    /**
     * Given a verified secure JWT return a string representing the users identity
     * <p>
     * Typically this would be a unique identifier for the user, either an email address or UUID.  Note that since email
     * addresses may be reused across multiple identity providers an implementation may also wish to consider other
     * fields within the JWT such as the {@code iss} claim in order to provide a truly unique identifier.
     * </p>
     *
     * @param jws Verified Secure JWT
     * @return User identity
     */
    String identityForUser(Jws<Claims> jws);
}
