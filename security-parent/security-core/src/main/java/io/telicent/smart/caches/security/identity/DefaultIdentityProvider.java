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
 * The default implementation of an identity provider
 */
public class DefaultIdentityProvider implements IdentityProvider {

    /**
     * Singleton instance fo the default identity provider
     */
    public static final IdentityProvider INSTANCE = new DefaultIdentityProvider();

    /**
     * Private constructor to prevent direct instantiation, use {@link #INSTANCE} to get the singleton instance
     */
    private DefaultIdentityProvider() {

    }

    @Override
    public String identityForUser(Jws<Claims> jws) {
        // TODO Allow other fields in preference to the sub field
        return jws.getPayload().getSubject();
    }
}
