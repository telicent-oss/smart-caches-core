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

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

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

    private static DenyAll denyAll() {
        return new DenyAll() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DenyAll.class;
            }
        };
    }

    private static PermitAll permitAll() {
        return new PermitAll() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return PermitAll.class;
            }
        };
    }

    private static RolesAllowed rolesAllowed(final String... roles) {
        return new RolesAllowed() {
            @Override
            public String[] value() {
                return roles;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return RolesAllowed.class;
            }
        };
    }

    @Test
    public void givenRequestWithDenyAll_whenAuthorizing_thenDenied() {
        // Given
        Annotation annotation = denyAll();
        MockRequest request = MockRequest.withRoles(annotation);

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);
    }

    @Test
    public void givenRequestWithPermitAll_whenAuthorizing_thenAllowed() {
        // Given
        Annotation annotation = permitAll();
        MockRequest request = MockRequest.withRoles(annotation);

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);
    }

    @Test
    public void givenRequestWithRolesAllowedAndUserHasNoRoles_whenAuthorizing_thenDenied() {
        // Given
        Annotation annotation = rolesAllowed("USER", "ADMIN");
        MockRequest request = MockRequest.withRoles(annotation);

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.DENIED);
    }

    @Test
    public void givenRequestWithRolesAllowedAndUserHasAdminRoles_whenAuthorizing_thenAllowed() {
        // Given
        Annotation annotation = rolesAllowed("USER", "ADMIN");
        MockRequest request = MockRequest.withRoles(annotation, r -> Strings.CS.equals(r, "ADMIN"));

        // When
        AuthorizationResult result = engine.authorize(request);

        // Then
        Assert.assertEquals(result.status(), AuthorizationStatus.ALLOWED);
    }
}
