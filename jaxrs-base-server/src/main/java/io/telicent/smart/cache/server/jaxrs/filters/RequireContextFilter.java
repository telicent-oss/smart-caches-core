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

import io.telicent.smart.cache.server.jaxrs.annotations.RequireContextAttribute;
import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * A filter that ensured required servlet context attributes have been appropriately populated, if not it aborts the
 * request with a 503 Service Unavailable response.
 * <p>
 * The filter looks for {@link RequireContextAttribute} annotations on the resource method, resource class and parent
 * classes thereof.  If any are found then it ensures that the requirements defined by those attributes are met, if they
 * are not then it aborts the request building a {@link Problem} response based upon the
 * {@link RequireContextAttribute#errorTitle()} and {@link RequireContextAttribute#errorDetail()}.  The response is
 * always a {@code 503 Service Unavailable} response.
 * </p>
 * <p>
 * This makes it easier to implement the pattern of having a
 * {@link io.telicent.smart.cache.server.jaxrs.init.ServerConfigInit} or {@link jakarta.servlet.ServletContextListener}
 * initialise some common objects used by resources within the API.  You can then enforce that the configuration was
 * successful without needing to have boilerplate code in every resource method that uses those object(s).
 * </p>
 */
@Provider
@Priority(Priorities.USER)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class RequireContextFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (resourceInfo != null) {
            List<RequireContextAttribute> requirements =
                    AnnotationsLocator.findAnnotations(resourceInfo.getResourceMethod(),
                                                       resourceInfo.getResourceClass(), RequireContextAttribute.class);

            if (!requirements.isEmpty()) {
                for (RequireContextAttribute requirement : requirements) {
                    if (!meetsRequirement(requirement)) {
                        abortRequest(requestContext, requirement);
                    }
                }
            }
        }
    }

    /**
     * Aborts the request with a suitable {@link Problem} response
     *
     * @param requestContext Request Context to abort
     * @param requirement    Requirement that wasn't met
     */
    private void abortRequest(ContainerRequestContext requestContext, RequireContextAttribute requirement) {
        requestContext.abortWith(Problem.builder()
                                        .status(Response.Status.SERVICE_UNAVAILABLE.getStatusCode())
                                        .title(requirement.errorTitle())
                                        .detail(requirement.errorDetail())
                                        .type("Service Unavailable")
                                        .build()
                                        .toResponse(this.httpHeaders));
    }

    /**
     * Checks whether the servlet context meets this requirement
     *
     * @param requirement Requirement
     * @return True if the requirement met, false if not met
     */
    private boolean meetsRequirement(RequireContextAttribute requirement) {
        Object value = this.servletContext.getAttribute(requirement.value());
        if (value == null) {
            return false;
        }
        if (!requirement.type().isAssignableFrom(value.getClass())) {
            return false;
        }
        for (Class<?> forbidden : requirement.forbiddenTypes()) {
            if (Objects.equals(forbidden, value.getClass())) {
                return false;
            }
        }
        return true;
    }
}
