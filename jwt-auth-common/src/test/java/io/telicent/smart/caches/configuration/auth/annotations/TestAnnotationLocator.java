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
package io.telicent.smart.caches.configuration.auth.annotations;

import io.telicent.smart.caches.configuration.auth.annotations.examples.ExampleAdmin;
import io.telicent.smart.caches.configuration.auth.annotations.examples.ExampleBase;
import io.telicent.smart.caches.configuration.auth.annotations.examples.ExampleUser;
import io.telicent.smart.caches.configuration.auth.annotations.examples.Nothing;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class TestAnnotationLocator {

    @Test
    public void givenClassWithNoAnnotations_whenLocatingAnnotations_thenNull() throws NoSuchMethodException {
        // Given
        Nothing nothing = new Nothing();
        Method method = nothing.getClass().getMethod("doNothing");

        // When
        Annotation roles = AnnotationLocator.findRoleAnnotation(method);

        // Then
        Assert.assertNull(roles);
    }

    private void verifyPermitAll(Annotation annotation) {
        Assert.assertTrue(annotation instanceof PermitAll);
    }

    private void verifyDenyAll(Annotation annotation) {
        Assert.assertTrue(annotation instanceof DenyAll);
    }

    private void verifyRoles(Annotation annotation, String... expected) {
        Assert.assertTrue(annotation instanceof RolesAllowed);
        RolesAllowed rolesAllowed = (RolesAllowed) annotation;
        Assert.assertEquals(rolesAllowed.value(), expected);
    }

    private void verifyPermissions(Annotation annotation, String... expected) {
        Assert.assertTrue(annotation instanceof RequirePermissions);
        RequirePermissions requirePermissions = (RequirePermissions) annotation;
        Assert.assertEquals(requirePermissions.value(), expected);
    }

    @Test
    public void givenClassWithAnnotations_whenLocatingAnnotationsOnMethodWithoutAnnotation_thenClassAnnotationReturned() throws
            NoSuchMethodException {
        // Given
        ExampleBase example = new ExampleBase();
        Method method = example.getClass().getMethod("defaults");

        // When
        Annotation roles = AnnotationLocator.findRoleAnnotation(method);

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
        Annotation roles = AnnotationLocator.findRoleAnnotation(method);

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
        Annotation roles = AnnotationLocator.findRoleAnnotation(method);

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
        Annotation roles = AnnotationLocator.findRoleAnnotation(method);
        Annotation perms = AnnotationLocator.findPermissionsAnnotation(method);

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
        Annotation roles = AnnotationLocator.findRoleAnnotation(method);
        Annotation perms = AnnotationLocator.findPermissionsAnnotation(method);

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
    public void givenClassWithPermissionAnnotations_whenLocatingAnnotationsOnMethod_thenExpectedPermissionsFound(String methodName, String[] expected) throws
            NoSuchMethodException {
        // Given
        ExampleUser example = new ExampleUser();
        Method method = example.getClass().getMethod(methodName);

        // When
        Annotation roles = AnnotationLocator.findRoleAnnotation(method);
        Annotation perms = AnnotationLocator.findPermissionsAnnotation(method);

        // Then
        verifyRoles(roles, "USER", "ADMIN");
        verifyPermissions(perms, expected);
    }
}
