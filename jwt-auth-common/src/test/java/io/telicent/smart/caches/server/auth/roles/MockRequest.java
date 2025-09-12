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

import java.util.function.Function;

public record MockRequest(boolean authenticated, boolean validPath, Policy rolesPolicy,
                          Policy permissionsPolicy, Function<String, Boolean> hasRole,
                          Function<String, Boolean> hasPermission) {

    private static final Function<String, Boolean> NO = r -> false;
    public static final MockRequest NOT_AUTHENTICATED =
            new MockRequest(false, true, null, null, NO, NO);

    public static final MockRequest INVALID_PATH = new MockRequest(true, false, null, null, NO, NO);

    public static final MockRequest NO_ROLES_OR_PERMISSIONS_NEEDED = new MockRequest(true, true, null, null, NO, NO);

    public static MockRequest withRoles(Policy rolesPolicy) {
        return withRoles(rolesPolicy, NO);
    }

    public static MockRequest withRoles(Policy rolesPolicy, Function<String, Boolean> hasRole) {
        return new MockRequest(true, true, rolesPolicy, null, hasRole, NO);
    }

    public static MockRequest withPermissions(Policy permissionsPolicy) {
        return withPermissions(permissionsPolicy, NO);
    }

    public static MockRequest withPermissions(Policy permissionsPolicy, Function<String, Boolean> hasPermission) {
        return new MockRequest(true, true, null, permissionsPolicy, NO, hasPermission);
    }
}
