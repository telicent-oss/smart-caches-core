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
package io.telicent.smart.caches.server.auth.roles;

import io.telicent.smart.caches.configuration.auth.policy.Policy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Abstract authorization engine that enforces the Telicent Roles and Permissions based authorization model for API
 * access
 */
public abstract class TelicentAuthorizationEngine<TRequest> {

    /**
     * Reason given in {@link AuthorizationResult}'s when the provided {@link Policy} does not provide a
     * {@link io.telicent.smart.caches.configuration.auth.policy.PolicyKind}
     */
    public static final String NO_POLICY_KIND_DECLARED = "no policy kind declared";
    /**
     * Reason given in {@link AuthorizationResult}'s when the policy denies access to all users
     */
    public static final String DENIED_TO_ALL_USERS = "denied to all users";
    /**
     * Reason given in {@link AuthorizationResult}'s when the policy allows access to all users
     */
    public static final String ALL_USERS_PERMITTED = "all users permitted";

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
     * @return Roles policy for the request, or {@code null} if none for the request
     */
    protected abstract Policy getRolesPolicy(TRequest request);

    /**
     * Gets the permissions annotation that applies to the request
     *
     * @param request Request
     * @return Permissions policy for the request, {@code null} if none for the request
     */
    protected abstract Policy getPermissionsPolicy(TRequest request);

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
                                           "Authorization only applies to resources that require authentication");
        } else if (!isValidPath(request)) {
            return new AuthorizationResult(AuthorizationStatus.NOT_APPLICABLE,
                                           "Not a valid path so will produce a 404 error");
        }
        List<String> successReasons = new ArrayList<>();
        List<String> successLoggingReasons = new ArrayList<>();

        // Enforce roles policy, if any
        Policy rolesPolicy = getRolesPolicy(request);
        AuthorizationResult rolesResult =
                applyPolicy(request, rolesPolicy, successReasons, successLoggingReasons, this::isUserInRole,
                            "no roles required");
        if (rolesResult != null) {
            return rolesResult;
        }

        // If we reached here either then the user satisfied the roles policy, or one did not exist

        // Next up we check whether they have the necessary permissions
        Policy permsPolicy = getPermissionsPolicy(request);
        AuthorizationResult permissionsResult =
                applyPolicy(request, permsPolicy, successReasons, successLoggingReasons, this::hasPermission,
                            "no permissions required");
        if (permissionsResult != null) {
            return permissionsResult;
        }

        // If we reach the end then we've satisfied all policies and are successfully authorized
        return new AuthorizationResult(AuthorizationStatus.ALLOWED, successReasons, successLoggingReasons);
    }

    /**
     * Applies a policy
     *
     * @param request        Request
     * @param policy         Policy
     * @param successReasons Success reasons to append to if authorization is successful
     * @return Authorization result if authorization fails
     */
    protected final AuthorizationResult applyPolicy(final TRequest request, final Policy policy,
                                                    final List<String> successReasons,
                                                    final List<String> successLoggingReasons,
                                                    final BiFunction<TRequest, String, Boolean> policyChecker,
                                                    final String noPolicyMessage) {
        if (policy != null) {
            if (policy.kind() == null) {
                return new AuthorizationResult(AuthorizationStatus.DENIED, NO_POLICY_KIND_DECLARED,
                                               NO_POLICY_KIND_DECLARED);
            }
            switch (policy.kind()) {
                case DENY_ALL:
                    // Resource access is denied to all users
                    return new AuthorizationResult(AuthorizationStatus.DENIED, DENIED_TO_ALL_USERS,
                                                   DENIED_TO_ALL_USERS);
                case REQUIRE_ANY:
                    // Resource access requires user to have at least one of the listed values
                    List<String> matched = new ArrayList<>();
                    for (String value : policy.values()) {
                        if (policyChecker.apply(request, value)) {
                            matched.add(value);
                        }
                    }
                    if (matched.isEmpty()) {
                        return deniedByPolicy(request, policy, policyChecker);
                    } else {
                        successReasons.add("user holds one/more required " + policy.source());
                        successLoggingReasons.add(
                                "user holds " + policy.source() + " (" + StringUtils.join(matched, ",") + ")");
                    }
                    break;
                case REQUIRE_ALL:
                    // Resource access requires user to have all listed values
                    if (!Arrays.stream(policy.values()).allMatch(p -> policyChecker.apply(request, p))) {
                        return deniedByPolicy(request, policy, policyChecker);
                    }
                    successReasons.add("user holds all required " + policy.source());
                    successLoggingReasons.add(
                            "user holds all required " + policy.source() + " (" + StringUtils.join(policy.values(),
                                                                                                   ",") + ")");
                    break;
                case ALLOW_ALL:
                    // Resource access allowed for all users
                    successReasons.add(ALL_USERS_PERMITTED);
                    successLoggingReasons.add(ALL_USERS_PERMITTED);
                    break;
                default:
                    // NB - This is future proofing against us introducing a new policy kind and forgetting to implement
                    //      its enforcement logic here, this way we fail safely by denying access if we don't know how
                    //      to enforce the policy kind
                    return new AuthorizationResult(AuthorizationStatus.DENIED, "unknown policy kind",
                                                   "unknown policy kind (" + policy.kind() + ")");
            }
        } else {
            // No policy defined
            successReasons.add(noPolicyMessage);
            successLoggingReasons.add(noPolicyMessage);
        }
        return null;
    }

    /**
     * Generates a {@link AuthorizationStatus#DENIED} result
     *
     * @param request       Request
     * @param policy        Policy that was not satisfied
     * @param policyChecker Policy checker function
     * @return Denied authorization result
     */
    protected final AuthorizationResult deniedByPolicy(TRequest request, Policy policy,
                                                       BiFunction<TRequest, String, Boolean> policyChecker) {
        // Build a more detailed failure reason for logging
        StringBuilder loggingReason = new StringBuilder();
        loggingReason.append("requires ").append(policy.source()).append(" that the user does not hold (");
        boolean first = true;
        for (String value : policy.values()) {
            if (!policyChecker.apply(request, value)) {
                if (first) {
                    first = false;
                } else {
                    loggingReason.append(", ");
                }
                loggingReason.append(value);
            }
        }
        loggingReason.append(")");

        // Return the DENIED result
        return new AuthorizationResult(AuthorizationStatus.DENIED,
                                       "requires " + policy.source() + " that your user account does not hold",
                                       loggingReason.toString());
    }

}
