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

import org.apache.commons.lang3.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.MissingFormatArgumentException;

public class TestTelicentPermissions {

    @DataProvider(name = "resourceNames")
    private Object[][] resourceNames() {
        return new Object[][] {
                { "test" },
                { "foo" },
                { "white space" }
        };
    }

    @Test(dataProvider = "resourceNames")
    public void givenResourceName_whenConstructingApiPermissions_thenPermissionsIncludeResourceName(
            String resourceName) {
        // Given and When
        String[] readWritePerms = TelicentPermissions.readWritePermissions(resourceName);
        String compact = TelicentPermissions.compactPermission(resourceName);

        // Then
        for (String perm : readWritePerms) {
            Assert.assertTrue(Strings.CS.contains(perm, resourceName));
        }
        Assert.assertTrue(Strings.CS.contains(compact, resourceName));
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "resource.*not be null")
    public void givenNullResourceName_whenConstructingApiPermissions_thenNPE() {
        // Given, When and Then
        TelicentPermissions.readWritePermissions(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "resource.*empty/blank")
    public void givenEmptyResourceName_whenConstructingApiPermissions_thenIllegalArgument() {
        // Given, When and Then
        TelicentPermissions.readWritePermissions("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "resource.*empty/blank")
    public void givenBlankResourceName_whenConstructingApiPermissions_thenIllegalArgument() {
        // Given, When and Then
        TelicentPermissions.readWritePermissions("    ");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "template.*not be null")
    public void givenNullTemplate_whenConstructingApiPermission_thenNPE() {
        // Given, When and Then
        TelicentPermissions.resourcePermission(null, "test");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*at least one.*placeholder")
    public void givenTemplateWithNoPlaceholder_whenConstructingApiPermission_thenIllegalArgument() {
        // Given, When and Then
        TelicentPermissions.resourcePermission("template", "test");
    }

    @Test(expectedExceptions = MissingFormatArgumentException.class)
    public void givenTemplateWithExcessPlaceholders_whenConstructingApiPermission_thenMissingFormatArgument() {
        // Given, When and Then
        TelicentPermissions.resourcePermission("template.%s.%s", "test");
    }
}
