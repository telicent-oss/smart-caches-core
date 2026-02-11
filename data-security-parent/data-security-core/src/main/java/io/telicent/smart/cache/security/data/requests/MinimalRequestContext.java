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
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * A minimal request context implementation that only has the JWT and username available, generally a more complete
 * implementation should be created by extending this.
 */
@AllArgsConstructor
@SuperBuilder
public class MinimalRequestContext implements RequestContext {

    @NonNull
    private final Jws<Claims> jwt;
    @NonNull
    private final String username;
    private final UserInfo userInfo;

    @Override
    public Jws<Claims> verifiedJwt() {
        return this.jwt;
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public UserInfo userInfo() {
        return this.userInfo;
    }

    @Override
    public List<String> requestHeader(String header) {
        return Collections.emptyList();
    }

    @Override
    public URI requestUri() {
        return null;
    }

    @Override
    public String requestPath() {
        return null;
    }

    @Override
    public String requestMethod() {
        return null;
    }
}
