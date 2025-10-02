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
import org.apache.commons.lang3.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.function.Function;

public class TestTelicentAuthorizationEngine {

    private final MockAuthorizationEngine engine = new MockAuthorizationEngine();

    @Test
    public void givenUnauthenticatedRequest_whenAuthorizing_thenNotApplicable() {
        // Given
        MockRequest request = MockRequest.NOT_AUTHENTICATED;

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.NOT_APPLICABLE);
    }

    @Test
    public void givenInvalidPathRequest_whenAuthorizing_thenNotApplicable() {
        // Given
        MockRequest request = MockRequest.INVALID_PATH;

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.NOT_APPLICABLE);
    }

    @Test
    public void givenRequestWithNoRolesOrPermissionsRequired_whenAuthorizing_thenAllowed() {
        // Given
        MockRequest request = MockRequest.NO_ROLES_OR_PERMISSIONS_NEEDED;

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);
    }

    @Test
    public void givenRequestWithDenyAll_whenAuthorizing_thenDenied() {
        // Given
        MockRequest request = MockRequest.withRoles(Policy.DENY_ALL);

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);
    }

    @Test
    public void givenRequestWithPermitAll_whenAuthorizing_thenAllowed() {
        // Given
        MockRequest request = MockRequest.withRoles(Policy.ALLOW_ALL);

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);
    }

    @Test
    public void givenRequestWithRolesAllowedAndUserHasNoRoles_whenAuthorizing_thenDenied() {
        // Given
        MockRequest request = MockRequest.withRoles(Policy.requireAny("role", new String[] { "USER", "ADMIN" }));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);
    }

    @Test
    public void givenRequestWithRolesAllowedAndUserHasAdminRoles_whenAuthorizing_thenAllowed() {
        // Given
        MockRequest request =
                MockRequest.withRoles(Policy.requireAny("role", new String[] { "USER", "ADMIN" }), userHas("ADMIN"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);
    }

    private static Function<String, Boolean> userHas(String... values) {
        return r -> Strings.CS.equalsAny(r, values);
    }

    @Test
    public void givenRequestWithRolesAllowedAndUserHasNoMatchingRoles_whenAuthorizing_thenDenied() {
        // Given
        MockRequest request =
                MockRequest.withRoles(Policy.requireAny("role", new String[] { "USER", "ADMIN" }), userHas("OTHER"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);
    }

    @Test
    public void givenRequestWithPermissionsRequiredAndUserHasNoPermissions_whenAuthorizing_thenDenied() {
        // Given
        MockRequest request =
                MockRequest.withPermissions(Policy.requireAll("permissions", new String[] { "read", "write" }));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);
    }

    @Test
    public void givenRequestWithPermissionsRequiredAndUserHasPartialPermissions_whenAuthorizing_thenDenied() {
        // Given
        MockRequest request =
                MockRequest.withPermissions(Policy.requireAll("permissions", new String[] { "read", "write" }),
                                            userHas("read"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);
    }

    @Test
    public void givenRequestWithPermissionsRequiredAndUserHasAllPermissions_whenAuthorizing_thenAllowed() {
        // Given
        MockRequest request =
                MockRequest.withPermissions(Policy.requireAll("permissions", new String[] { "read", "write" }),
                                            userHas("read", "write"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);
    }

    @Test
    public void givenRequestWithNullPolicy_whenAuthorizing_thenDenied() {
        // Given
        MockRequest request =
                new MockRequest(true, true, new Policy(null, "test", new String[] { "foo", "bar" }), null, null, null);

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);
    }
}
