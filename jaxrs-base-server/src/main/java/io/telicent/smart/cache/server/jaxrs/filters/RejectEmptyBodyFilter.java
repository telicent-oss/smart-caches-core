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

import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.IOException;

/**
 * A JAX-RS request filter that rejects {@code POST}/{@code PUT}/{@code PATCH} requests where the JAX-RS resource is
 * expecting a request body and none is provided
 */
@Provider
public class RejectEmptyBodyFilter implements ContainerRequestFilter {

    public static final String TITLE = "Missing Request Body";
    @Context
    private ResourceInfo resourceInfo;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders httpHeaders;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // This filter only applies to POST/PUT/PATCH requests
        if (Strings.CI.equalsAny(requestContext.getMethod(), HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) {
            // Can only apply to requests that are mapped to valid JAX-RS Resource methods
            if (resourceInfo != null) {
                Consumes consumes = resourceInfo.getResourceMethod().getDeclaredAnnotation(Consumes.class);
                // Can only apply to requests that have a @Consumes annotation, i.e. those that expect a request body
                if (consumes != null) {
                    // Only reject requests if there is no request body present
                    // NB - We don't check for wrong Content-Type as JAX-RS should already handle routing/rejecting
                    //      such requests appropriately
                    if (httpHeaders.getMediaType() == null) {
                        requestContext.abortWith(Problem.builder()
                                                        .status(Response.Status.BAD_REQUEST.getStatusCode())
                                                        .title(TITLE)
                                                        .type("BadRequest")
                                                        .detail(String.format(
                                                                "%s /%s requests require a non-empty request body. Acceptable request body formats: %s",
                                                                requestContext.getMethod(), this.uriInfo.getPath(),
                                                                StringUtils.join(consumes.value(), ", ")))
                                                        .build()
                                                        .toResponse(httpHeaders));
                    }
                }
            }
        }
    }
}
