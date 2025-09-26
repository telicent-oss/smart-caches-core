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

public class MockAuthorizationEngine extends TelicentAuthorizationEngine<MockRequest> {
    @Override
    protected boolean isAuthenticated(MockRequest request) {
        return request.authenticated();
    }

    @Override
    protected boolean isValidPath(MockRequest request) {
        return request.validPath();
    }

    @Override
    protected Policy getRolesPolicy(MockRequest request) {
        return request.rolesPolicy();
    }

    @Override
    protected Policy getPermissionsPolicy(MockRequest request) {
        return request.permissionsPolicy();
    }

    @Override
    protected boolean isUserInRole(MockRequest request, String role) {
        return request.hasRole().apply(role);
    }

    @Override
    protected boolean hasPermission(MockRequest request, String permission) {
        return request.hasPermission().apply(permission);
    }
}
