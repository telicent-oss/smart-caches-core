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

import io.telicent.servlet.auth.jwt.jaxrs3.JwtSecurityContext;
import io.telicent.smart.caches.configuration.auth.policy.Policy;
import io.telicent.smart.caches.configuration.auth.policy.PolicyLocator;
import io.telicent.smart.caches.server.auth.roles.TelicentAuthorizationEngine;

/**
 * A Telicent Authorization engine tailored for our JAX-RS base server with JWT authentication
 */
public class JaxRsAuthorizationEngine extends TelicentAuthorizationEngine<JwtAuthorizationContext> {
    @Override
    protected boolean isAuthenticated(JwtAuthorizationContext request) {
        // If not an authenticated request then authorization should not apply, requests will only be
        // unauthenticated if either authentication was disabled, or the requested resource is not subject to
        // authentication, and thus not subject to authorization
        // If the request simply failed authentication then it would have been rejected prior to ever reaching this
        // point
        return request.requestContext().getSecurityContext() != null && request.requestContext()
                                                                               .getSecurityContext() instanceof JwtSecurityContext;
    }

    @Override
    protected boolean isValidPath(JwtAuthorizationContext request) {
        // If no resource information then this isn't a matched resource, i.e. it's going to be a 404, and we need
        // not do any authorization as the servers already going to handle generating a 404 error
        return request.resourceInfo() != null && request.resourceInfo().getResourceMethod() != null;
    }

    @Override
    protected Policy getRolesPolicy(JwtAuthorizationContext request) {
        return PolicyLocator.findRolesPolicyFromAnnotations(request.resourceInfo().getResourceMethod());
    }

    @Override
    protected Policy getPermissionsPolicy(JwtAuthorizationContext request) {
        return PolicyLocator.findPermissionsPolicyFromAnnotations(request.resourceInfo().getResourceMethod());
    }

    @Override
    protected boolean isUserInRole(JwtAuthorizationContext request, String role) {
        return request.requestContext().getSecurityContext().isUserInRole(role);
    }

    @Override
    protected boolean hasPermission(JwtAuthorizationContext jwtAuthorizationContext, String permission) {
        // TODO Needs UserInfo support implementing
        return false;
    }
}
