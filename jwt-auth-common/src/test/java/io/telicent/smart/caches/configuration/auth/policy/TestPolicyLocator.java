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
package io.telicent.smart.caches.configuration.auth.policy;

import io.telicent.smart.caches.configuration.auth.policy.examples.ExampleAdmin;
import io.telicent.smart.caches.configuration.auth.policy.examples.ExampleBase;
import io.telicent.smart.caches.configuration.auth.policy.examples.ExampleUser;
import io.telicent.smart.caches.configuration.auth.policy.examples.Nothing;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class TestPolicyLocator {

    @Test
    public void givenClassWithNoAnnotations_whenLocatingAnnotations_thenNull() throws NoSuchMethodException {
        // Given
        Nothing nothing = new Nothing();
        Method method = nothing.getClass().getMethod("doNothing");

        // When
        Policy roles = PolicyLocator.findRolesPolicyFromAnnotations(method);

        // Then
        Assert.assertNull(roles);
    }

    @Test
    public void givenNullMethod_whenLocatingAnnotations_thenNull() {
        // Given and When
        Policy roles = PolicyLocator.findRolesPolicyFromAnnotations(null);
        Policy perms = PolicyLocator.findPermissionsPolicyFromAnnotations(null);

        // Then
        Assert.assertNull(roles);
        Assert.assertNull(perms);
    }

    @Test
    public void givenClasslessMethod_whenLocatingAnnotations_thenNull() {
        // Given
        Method method = Mockito.mock(Method.class);

        // When
        Policy roles = PolicyLocator.findRolesPolicyFromAnnotations(method);

        // Then
        Assert.assertNull(roles);
    }

    private void verifyPermitAll(Policy policy) {
        Assert.assertEquals(policy.kind(), PolicyKind.ALLOW_ALL);
    }

    private void verifyDenyAll(Policy policy) {
        Assert.assertEquals(policy.kind(), PolicyKind.DENY_ALL);
    }

    private void verifyRoles(Policy policy, String... expected) {
        Assert.assertEquals(policy.kind(), PolicyKind.REQUIRE_ANY);
        Assert.assertEquals(policy.values(), expected);
    }

    private void verifyPermissions(Policy policy, String... expected) {
        Assert.assertEquals(policy.kind(), PolicyKind.REQUIRE_ALL);
        Assert.assertEquals(policy.values(), expected);
    }

    @Test
    public void givenClassWithAnnotations_whenLocatingAnnotationsOnMethodWithoutAnnotation_thenClassAnnotationReturned() throws
            NoSuchMethodException {
        // Given
        ExampleBase example = new ExampleBase();
        Method method = example.getClass().getMethod("defaults");

        // When
        Policy roles = PolicyLocator.findRolesPolicyFromAnnotations(method);

        // Then
        verifyRoles(roles, "USER", "ADMIN");
    }

    @Test
    public void givenClassWithAnnotations_whenLocatingAnnotationsOnMethodWithAnnotation_thenMethodAnnotationReturned() throws
            NoSuchMethodException {
        // Given
        ExampleBase example = new ExampleBase();
        Method method = example.getClass().getMethod("allowAll");

        // When
        Policy roles = PolicyLocator.findRolesPolicyFromAnnotations(method);

        // Then
        verifyPermitAll(roles);
    }

    @Test
    public void givenClassWithAnnotations_whenLocatingAnnotationsOnMethodWithMultipleAnnotation_thenStrictestAnnotationReturned() throws
            NoSuchMethodException {
        // Given
        ExampleBase example = new ExampleBase();
        Method method = example.getClass().getMethod("denyAll");

        // When
        Policy roles = PolicyLocator.findRolesPolicyFromAnnotations(method);

        // Then
        verifyDenyAll(roles);
    }

    @Test
    public void givenClassWithPermissionAnnotations_whenLocatingAnnotationsOnMethodWithoutAnnotations_thenClassAnnotationReturned() throws
            NoSuchMethodException {
        // Given
        ExampleAdmin example = new ExampleAdmin();
        Method method = example.getClass().getMethod("doAdmin");

        // When
        Policy roles = PolicyLocator.findRolesPolicyFromAnnotations(method);
        Policy perms = PolicyLocator.findPermissionsPolicyFromAnnotations(method);

        // Then
        verifyRoles(roles, "ADMIN");
        verifyPermissions(perms, "admin:write");
    }

    @Test
    public void givenClassWithPermissionAnnotations_whenLocatingAnnotationsOnMethodWithAnnotations_thenMethodAnnotationReturned() throws
            NoSuchMethodException {
        // Given
        ExampleAdmin example = new ExampleAdmin();
        Method method = example.getClass().getMethod("reset");

        // When
        Policy roles = PolicyLocator.findRolesPolicyFromAnnotations(method);
        Policy perms = PolicyLocator.findPermissionsPolicyFromAnnotations(method);

        // Then
        verifyRoles(roles, "SYS-ADMIN");
        verifyPermissions(perms, "admin:write", "admin:wipe");
    }

    @DataProvider(name = "userPermissions")
    private Object[][] permissions() {
        return new Object[][] {
                { "whoami", new String[] { "user:read" } },
                { "updateDetails", new String[] { "user:write" } },
                { "active", new String[] { "user:read", "user:write" } },
                { "random", new String[0] }
        };
    }

    @Test(dataProvider = "userPermissions")
    public void givenClassWithPermissionAnnotations_whenLocatingAnnotationsOnMethod_thenExpectedPermissionsFound(
            String methodName, String[] expected) throws
            NoSuchMethodException {
        // Given
        ExampleUser example = new ExampleUser();
        Method method = example.getClass().getMethod(methodName);

        // When
        Policy roles = PolicyLocator.findRolesPolicyFromAnnotations(method);
        Policy perms = PolicyLocator.findPermissionsPolicyFromAnnotations(method);

        // Then
        verifyRoles(roles, "USER", "ADMIN");
        verifyPermissions(perms, expected);
    }
}
