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
import io.telicent.smart.caches.configuration.auth.annotations.*;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * A JAX-RS authorization filter that implements Telicent's role and permissions based authorization policies for
 * services.
 * <p>
 * The filter locates the role annotation (if any), and permissions annotation (if any), that apply to the resource
 * method that has been matched and enforces that users hold at least one of the allowed roles, and all the required
 * permissions.  Requests that do not meet these criteria are rejected with a suitable 401 Unauthorized response.
 * </p>
 */
@Produces
// Declare our priority such that we are applied after authentication has happened
@Priority(Priorities.AUTHORIZATION)
public class TelicentAuthorizationFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelicentAuthorizationFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders httpHeaders;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext.getSecurityContext() == null) {
            // If not an authenticated request then authorization should not apply, requests will only be
            // unauthenticated if either authentication was disabled, or the requested resource is not subject to
            // authentication, and thus not subject to authorization
            // If the request simply failed authentication then it would have been rejected prior to ever reaching this
            // filter due to our declared priority
            return;
        }

        if (resourceInfo == null || this.resourceInfo.getResourceClass() == null) {
            // If no resource information then this isn't a matched resource, i.e. it's going to be a 404, and we need
            // not do any authorization as the servers already going to handle generating a 404 error
            return;
        }

        // Enforce roles annotation, if any
        Annotation roles = AnnotationLocator.findRoleAnnotation(this.resourceInfo.getResourceMethod());
        if (roles instanceof DenyAll) {
            // Resource access is denied to all users
            LOGGER.warn("Request to {} rejected as it is marked as Deny All", this.uriInfo.getRequestUri());
            requestContext.abortWith(Problem.builder()
                                            .title("Unauthorized")
                                            .detail(String.format(
                                                    "Requests to %s are not permitted by this servers authorization policy",
                                                    this.uriInfo.getRequestUri().toString()))
                                            .status(Response.Status.UNAUTHORIZED.getStatusCode())
                                            .build()
                                            .toResponse(this.httpHeaders));
            return;
        } else if (roles instanceof RolesAllowed allowed) {
            // Resource access requires user to have at least one of the listed roles
            if (Arrays.stream(allowed.value()).noneMatch(r -> requestContext.getSecurityContext().isUserInRole(r))) {
                LOGGER.warn("Request to {} rejected as user does not hold any of the allowed roles: {}",
                            this.uriInfo.getRequestUri(), allowed.value());
                requestContext.abortWith(Problem.builder()
                                                .title("Unauthorized")
                                                .detail(String.format(
                                                        "Requests to %s require roles that your user account does not hold",
                                                        this.uriInfo.getRequestUri().toString()))
                                                .status(Response.Status.UNAUTHORIZED.getStatusCode())
                                                .build()
                                                .toResponse(this.httpHeaders));
                return;
            }
            LOGGER.info("Request to {} successfully authorized as user holds role {}", this.uriInfo.getRequestUri(),
                        Arrays.stream(allowed.value())
                              .filter(r -> requestContext.getSecurityContext().isUserInRole(r))
                              .findFirst()
                              .orElse("<unknown>"));
        }
        // If we reached here either this resource was:
        // 1. A @PermitAll resource
        // 2. The user held one of the declared @RolesAllowed
        // 3. No roles annotation was present for this resource method/class so no roles enforcement applies

        // Next up we check whether they have the necessary permissions
        RequirePermissions perms =
                (RequirePermissions) AnnotationLocator.findPermissionsAnnotation(resourceInfo.getResourceMethod());
        if (perms != null) {
            // Resource access requires user to have additional permissions
            // TODO Permissions come from UserInfo which is work TBC
            Object permsContext = null;
            if (!Arrays.stream(perms.value()).allMatch(p -> hasPermission(permsContext, p))) {
                LOGGER.warn("Request to {} rejected as user does not hold all the required permissions: {}",
                            this.uriInfo.getRequestUri(), perms.value());
                requestContext.abortWith(Problem.builder()
                                                .title("Unauthorized")
                                                .status(Response.Status.UNAUTHORIZED.getStatusCode())
                                                .build()
                                                .toResponse(this.httpHeaders));
                return;
            }
            LOGGER.info("Request to {} successfully authorized as user holds all required permissions",
                        this.uriInfo.getRequestUri());
        }
    }

    /**
     * Does the user have the given permissions
     *
     * @param permsContext Permissions context
     * @param p            Permission required
     * @return True if they have the permission, false otherwise
     */
    private boolean hasPermission(Object permsContext, String p) {
        // TODO Implement properly once we have a means to get UserInfo for a user
        return false;
    }
}
