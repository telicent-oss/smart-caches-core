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
package io.telicent.smart.cache.server.jaxrs.filters;

import io.telicent.smart.cache.server.jaxrs.applications.CorsConfigurationBuilder;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;


/**
 * A response filter that adds CORS headers to all responses
 *
 * @deprecated Replaced by {@link CrossOriginFilter} which is configured via
 * {@link io.telicent.smart.cache.server.jaxrs.applications.ServerBuilder#withCors(CorsConfigurationBuilder)}
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
@Deprecated(since = "0.12.0", forRemoval = true)
public class CorsResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", DefaultCorsConfiguration.ALLOWED_METHODS);
        responseContext.getHeaders().add("Access-Control-Allow-Headers", DefaultCorsConfiguration.ALLOWED_HEADERS);
        responseContext.getHeaders().add("Access-Control-Expose-Headers", DefaultCorsConfiguration.EXPOSED_HEADERS);
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
    }
}
