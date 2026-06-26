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
package io.telicent.smart.cache.server.jaxrs.auth;

import io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin;
import io.telicent.smart.cache.security.data.requests.MinimalRequestContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.List;

/**
 * A JAX-RS Request Context for use by {@link DataSecurityPlugin} API
 */
@SuperBuilder
public final class JaxRsRequestContext extends MinimalRequestContext {

    @NonNull
    private ContainerRequestContext request;


    @Override
    public List<String> requestHeader(String header) {
        return this.request.getHeaders().get(header);
    }

    @Override
    public URI requestUri() {
        return this.request.getUriInfo().getRequestUri();
    }

    @Override
    public String requestPath() {
        return this.request.getUriInfo().getPath();
    }

    @Override
    public String requestMethod() {
        return this.request.getMethod();
    }

    /**
     * Closes the context
     */
    public void close() {
        // Free up the reference to the JAX-RS request
        this.request = null;
    }
}
