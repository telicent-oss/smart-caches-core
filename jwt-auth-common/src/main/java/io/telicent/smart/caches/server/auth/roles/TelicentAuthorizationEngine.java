/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.caches.server.auth.roles;

import io.telicent.smart.caches.configuration.auth.annotations.RequirePermissions;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract authorization engine that enforces the Telicent Roles and Permissions based authorization model for API
 * access
 */
public abstract class TelicentAuthorizationEngine<TRequest> {

    /**
     * Indicates whether the current request is authenticated
     *
     * @param request Request
     * @return Whether the current request is authenticated
     */
    protected abstract boolean isAuthenticated(TRequest request);

    /**
     * Checks whether the request is to a valid path i.e. if it's to an invalid path then that'll result in a 404 error
     * anyway so no need to authorize it
     *
     * @param request Request
     * @return True if a valid path, false otherwise
     */
    protected abstract boolean isValidPath(TRequest request);

    /**
     * Gets the roles annotation that applies to the request
     *
     * @param request Request
     * @return Roles annotation, should be one of {@link DenyAll}, {@link RolesAllowed} or {@link PermitAll}, or
     * {@code null} if no roles annotation for the request
     */
    protected abstract Annotation getRolesAnnotation(TRequest request);

    /**
     * Gets the permissions annotation that applies to the request
     *
     * @param request Request
     * @return Permissions annotation if applicable, {@code null} if none for this request
     */
    protected abstract RequirePermissions getPermissionsAnnotation(TRequest request);

    /**
     * Checks whether the user has a given role
     *
     * @param request Request
     * @param role    Role
     * @return True if they have the role, false otherwise
     */
    protected abstract boolean isUserInRole(TRequest request, String role);

    /**
     * Checks whether the user has a given permission
     *
     * @param request    Request
     * @param permission Permission required
     * @return True if they have the permission, false otherwise
     */
    protected abstract boolean hasPermission(TRequest request, String permission);

    /**
     * Authorizes the given request
     *
     * @param request Request
     * @return Authorization result
     */
    public final AuthorizationResult authorize(TRequest request) {
        if (!isAuthenticated(request)) {
            return new AuthorizationResult(AuthorizationStatus.NOT_APPLICABLE,
                                           List.of("Authorization only applies to resources that require authentication"));
        } else if (!isValidPath(request)) {
            return new AuthorizationResult(AuthorizationStatus.NOT_APPLICABLE,
                                           List.of("Not a valid path so will produce a 404 error"));
        }
        List<String> successReasons = new ArrayList<>();

        // Enforce roles annotation, if any
        Annotation roles = getRolesAnnotation(request);
        if (roles instanceof DenyAll) {
            // Resource access is denied to all users
            return new AuthorizationResult(AuthorizationStatus.DENIED, "denied to all users");
        } else if (roles instanceof RolesAllowed allowed) {
            // Resource access requires user to have at least one of the listed roles
            boolean anyRoleMatched = false;
            for (String role : allowed.value()) {
                if (isUserInRole(request, role)) {
                    anyRoleMatched = true;
                    successReasons.add("user holds role " + role);
                }
            }
            if (!anyRoleMatched) {
                return new AuthorizationResult(AuthorizationStatus.DENIED,
                                               "requires roles that your user account does not hold");
            }
        } else if (roles instanceof PermitAll) {
            successReasons.add("all users permitted");
        } else if (roles == null) {
            successReasons.add("no roles required");
        }
        // If we reached here either this resource was:
        // 1. A @PermitAll resource
        // 2. The user held at least one of the declared @RolesAllowed
        // 3. No roles annotation was present for this resource method/class so no roles enforcement applies

        // Next up we check whether they have the necessary permissions
        RequirePermissions perms = getPermissionsAnnotation(request);
        if (perms != null) {
            // Resource access requires user to have additional permissions
            if (!Arrays.stream(perms.value()).allMatch(p -> hasPermission(request, p))) {
                return new AuthorizationResult(AuthorizationStatus.DENIED,
                                               "requires permissions that your user account does not hold");
            }
            successReasons.add("user holds all required permissions");
        } else {
            successReasons.add("no permissions required");
        }

        // If we reach the end then we're successfully authorized
        return new AuthorizationResult(AuthorizationStatus.ALLOWED, successReasons);
    }

}
