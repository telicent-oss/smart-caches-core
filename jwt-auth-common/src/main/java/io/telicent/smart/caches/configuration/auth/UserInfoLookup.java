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
package io.telicent.smart.caches.configuration.auth;

import java.io.Closeable;

/**
 * Provides the ability to exchange an authenticated users access token (JWT) for User Info about them
 */
public interface UserInfoLookup extends Closeable {
    /**
     * Lookup user info by calling the pre-configured remote {@code /userinfo} endpoint
     *
     * @param bearerToken The access token (JWT) to exchange for the User Info
     * @return The UserInfo object
     * @throws UserInfoLookupException on network/non-200/parse errors
     */
    UserInfo lookup(String bearerToken) throws UserInfoLookupException;
}
