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
package io.telicent.smart.cache.security.data.requests;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.telicent.smart.caches.configuration.auth.UserInfo;

import java.net.URI;
import java.util.List;

/**
 * Interface for request context that supplies user and request information to Data Security plugins
 */
public interface RequestContext {

    /**
     * The cryptographically verified JWT for the user
     *
     * @return Verified JWT
     */
    Jws<Claims> verifiedJwt();

    /**
     * The username for the user
     *
     * @return Username
     */
    String username();

    /**
     * The User Info for the user (if available)
     * @return User Info, or {@code null} if unavailable
     */
    UserInfo userInfo();

    /**
     * Access the value(s) for a given request header
     * <p>
     * Depending on the connection protocol used for the request there may be no headers present.
     * </p>
     *
     * @param header Header name
     * @return Values, an empty list if no such headers are present
     */
    List<String> requestHeader(String header);

    /**
     * For HTTP requests provides access to the full Request URI
     *
     * @return Request URI, or {@code null} if not applicable
     */
    URI requestUri();

    /**
     * For API requests provides access to the path within the API
     * <p>
     * This may be present even if {@link #requestUri()} is {@code null} since requests arriving via other connection
     * protocols likely still have some concept of request path/routing even if they aren't HTTP APIs.
     * </p>
     *
     * @return Request path
     */
    String requestPath();

    /**
     * Supplies the request method API requests where multiple operations are supported on the same path, for HTTP
     * requests this is the HTTP verb e.g. {@code GET} or {@code POST}
     *
     * @return Request method, or {@code null} if not applicable
     */
    String requestMethod();
}
