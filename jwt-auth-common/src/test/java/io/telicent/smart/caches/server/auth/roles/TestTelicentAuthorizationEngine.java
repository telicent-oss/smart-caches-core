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

import java.util.List;
import java.util.function.Function;

public class TestTelicentAuthorizationEngine {

    private final MockAuthorizationEngine engine = new MockAuthorizationEngine();

    private void verifyReason(List<String> reasons, String... expectedReasons) {
        for (String expectedReason : expectedReasons) {
            Assert.assertTrue(reasons.stream().anyMatch(r -> Strings.CI.contains(r, expectedReason)),
                              "No reasons contained expected message '" + expectedReason + "'");
        }
    }

    private void verifyNoReason(List<String> reasons, String... unexpected) {
        for (String unexpectedReason : unexpected) {
            Assert.assertFalse(reasons.stream().anyMatch(r -> Strings.CI.contains(r, unexpectedReason)),
                               "Reasons contained unexpected message '" + unexpectedReason + "'");
        }
    }

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
    public void givenRequestWithNoRolesOrPermissionsRequired_whenAuthorizing_thenAllowed_andReasonsAreCorrect() {
        // Given
        MockRequest request = MockRequest.NO_ROLES_OR_PERMISSIONS_NEEDED;

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);

        // And
        verifyReason(result.reasons(), "no roles required", "no permissions required");
        verifyReason(result.loggingReasons(), "no roles required", "no permissions required");
    }

    @Test
    public void givenRequestWithDenyAll_whenAuthorizing_thenDenied_andReasonsAreCorrect() {
        // Given
        MockRequest request = MockRequest.withRoles(Policy.DENY_ALL);

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);

        // And
        verifyReason(result.reasons(), TelicentAuthorizationEngine.DENIED_TO_ALL_USERS);
        verifyReason(result.loggingReasons(), TelicentAuthorizationEngine.DENIED_TO_ALL_USERS);
    }

    @Test
    public void givenRequestWithPermitAll_whenAuthorizing_thenAllowed_andReasonsAreCorrect() {
        // Given
        MockRequest request = MockRequest.withRoles(Policy.ALLOW_ALL);

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);

        // And
        verifyReason(result.reasons(), TelicentAuthorizationEngine.ALL_USERS_PERMITTED);
        verifyReason(result.loggingReasons(), TelicentAuthorizationEngine.ALL_USERS_PERMITTED);
    }

    @Test
    public void givenRequestWithRolesAllowedAndUserHasNoRoles_whenAuthorizing_thenDenied_andReasonsAreCorrect() {
        // Given
        MockRequest request = MockRequest.withRoles(Policy.requireAny("roles", "USER", "ADMIN"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);

        // And
        verifyReason(result.reasons(), "requires roles");
        verifyNoReason(result.reasons(), "ADMIN");
        verifyReason(result.loggingReasons(), "requires roles", "USER", "ADMIN");
    }

    @Test
    public void givenRequestWithRolesAllowedAndUserHasAdminRoles_whenAuthorizing_thenAllowed_andReasonsAreCorrect() {
        // Given
        MockRequest request =
                MockRequest.withRoles(Policy.requireAny("roles", "USER", "ADMIN"), userHas("ADMIN"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);

        // And
        verifyReason(result.reasons(), "one/more required roles");
        verifyNoReason(result.reasons(), "ADMIN");
        verifyReason(result.loggingReasons(), "holds roles", "USER", "ADMIN");
    }

    private static Function<String, Boolean> userHas(String... values) {
        return r -> Strings.CS.equalsAny(r, values);
    }

    @Test
    public void givenRequestWithRolesAllowedAndUserHasNoMatchingRoles_whenAuthorizing_thenDenied_andReasonsAreCorrect() {
        // Given
        MockRequest request =
                MockRequest.withRoles(Policy.requireAny("roles", "USER", "ADMIN"), userHas("OTHER"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);

        // And
        verifyReason(result.reasons(), "requires roles");
        verifyNoReason(result.reasons(), "ADMIN");
        verifyReason(result.loggingReasons(), "requires roles", "USER", "ADMIN");
    }

    @Test
    public void givenRequestWithPermissionsRequiredAndUserHasNoPermissions_whenAuthorizing_thenDenied_andReasonsAreCorrect() {
        // Given
        MockRequest request =
                MockRequest.withPermissions(Policy.requireAll("permissions", "read", "write"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);

        // And
        verifyReason(result.reasons(), "requires permissions");
        verifyNoReason(result.reasons(), "read", "write");
        verifyReason(result.loggingReasons(), "requires permissions", "read", "write");
    }

    @Test
    public void givenRequestWithPermissionsRequiredAndUserHasPartialPermissions_whenAuthorizing_thenDenied_andReasonsAreCorrect() {
        // Given
        MockRequest request =
                MockRequest.withPermissions(Policy.requireAll("permissions", "read", "write"),
                                            userHas("read"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);

        // And
        verifyReason(result.reasons(), "requires permissions");
        verifyNoReason(result.reasons(), "read",  "write");
        verifyReason(result.loggingReasons(), "requires permissions", "write");
        verifyNoReason(result.loggingReasons(), "read");
    }

    @Test
    public void givenRequestWithPermissionsRequiredAndUserHasAllPermissions_whenAuthorizing_thenAllowed_andReasonsAreCorrect() {
        // Given
        MockRequest request =
                MockRequest.withPermissions(Policy.requireAll("permissions", "read", "write"),
                                            userHas("read", "write"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);

        // And
        verifyReason(result.reasons(), "all required permissions");
        verifyNoReason(result.reasons(), "read", "write");
        verifyReason(result.loggingReasons(), "all required permissions", "read", "write");
    }

    @Test
    public void givenRequestWithNullPolicyKind_whenAuthorizing_thenDenied_andReasonsAreCorrect() {
        // Given
        MockRequest request =
                new MockRequest(true, true, new Policy(null, "test", new String[] { "foo", "bar" }), null, null, null);

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);

        // And
        verifyReason(result.reasons(), TelicentAuthorizationEngine.NO_POLICY_KIND_DECLARED);
        verifyReason(result.loggingReasons(), TelicentAuthorizationEngine.NO_POLICY_KIND_DECLARED);
    }
}
