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
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JAX-RS Response Filter that logs any failed responses
 */
@Provider
public class FailureLoggingFilter implements ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailureLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (responseContext.getStatus() >= 400) {
            if (responseContext.hasEntity()) {
                if (responseContext.getEntity() instanceof Problem problem) {
                    // If we have a Problem object then can log a detailed error
                    LOGGER.error("{} {} produced error status {} with problem {}: {}",
                                 requestContext.getMethod(),
                                 requestContext.getUriInfo().getRequestUri().toString(), responseContext.getStatus(),
                                 problem.getTitle(), StringUtils.isNotBlank(problem.getDetail()) ? problem.getDetail() :
                                                     "<no further details>");
                    return;
                }
            }
            // Otherwise just log the request and the status
            LOGGER.error("{} {} produced error status {}",
                         requestContext.getMethod(),
                         requestContext.getUriInfo().getRequestUri().toString(), responseContext.getStatus());
        }
    }
}
