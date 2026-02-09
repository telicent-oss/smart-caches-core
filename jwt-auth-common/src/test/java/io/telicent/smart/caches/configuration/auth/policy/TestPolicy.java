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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Objects;

public class TestPolicy {

    @Test
    public void givenNullPolicies_whenCombining_thenNull() {
        // Given and When
        Policy combined = Policy.combine(Policy.NONE, Policy.NONE);

        // Then
        Assert.assertNull(combined);
    }

    @Test
    public void givenFirstNonNullPolicy_whenCombining_thenNotNull() {
        // Given and When
        Policy combined = Policy.combine(Policy.ALLOW_ALL, Policy.NONE);

        // Then
        Assert.assertEquals(combined, Policy.ALLOW_ALL);
    }

    @Test
    public void givenSecondNonNullPolicy_whenCombining_thenNotNull() {
        // Given and When
        Policy combined = Policy.combine(Policy.NONE, Policy.ALLOW_ALL);

        // Then
        Assert.assertEquals(combined, Policy.ALLOW_ALL);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*different kinds")
    public void givenDifferentKindsOfPolicy_whenCombining_thenIllegalArgument() {
        // Given
        Policy x = Policy.ALLOW_ALL;
        Policy y = Policy.DENY_ALL;

        // When and Then
        Policy.combine(x, y);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*different sources")
    public void givenDifferentSourcesOfPolicy_whenCombining_thenIllegalArgument() {
        // Given
        Policy x = Policy.requireAny("roles", "test");
        Policy y = Policy.requireAny("permissions", "test");

        // When and Then
        Policy.combine(x, y);
    }

    @DataProvider(name = "combinablePolicies")
    private Object[][] combinablePolicies() {
        //@formatter:off
        return new Object[][] {
                { Policy.ALLOW_ALL, Policy.ALLOW_ALL, Policy.ALLOW_ALL },
                { Policy.DENY_ALL, Policy.DENY_ALL, Policy.DENY_ALL },
                {
                        Policy.requireAny("roles",TelicentRoles.USER),
                        Policy.requireAny("roles",TelicentRoles.ADMIN_SYSTEM),
                        Policy.requireAny("roles", TelicentRoles.USER, TelicentRoles.ADMIN_SYSTEM),
                },
                {
                    Policy.requireAll("permissions",TelicentPermissions.Knowledge.READ),
                    Policy.requireAll("permissions", TelicentPermissions.Knowledge.READ_WRITE),
                    Policy.requireAll("permissions", TelicentPermissions.Knowledge.READ_WRITE)
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Notifications.READ),
                    Policy.requireAll("permissions", TelicentPermissions.Notifications.WRITE),
                    Policy.requireAll("permissions", TelicentPermissions.Notifications.READ_WRITE)
                },
                {
                        Policy.requireAll("permissions", TelicentPermissions.UserPreferences.READ),
                        Policy.requireAll("permissions", TelicentPermissions.UserPreferences.WRITE),
                        Policy.requireAll("permissions", TelicentPermissions.UserPreferences.READ_WRITE)
                },
                // Duplicate USER role should be suppressed when combining
                // Appears in different orders, and sometimes multiple times, to check ordering and multiple occurrences
                // in a single policy isn't a factor
                {
                    Policy.requireAny("roles",TelicentRoles.USER),
                    Policy.requireAny("roles", TelicentRoles.USER, TelicentRoles.ADMIN_SYSTEM),
                    Policy.requireAny("roles", TelicentRoles.USER, TelicentRoles.ADMIN_SYSTEM),
                },
                {
                    Policy.requireAny("roles",TelicentRoles.USER),
                    Policy.requireAny("roles", TelicentRoles.USER, TelicentRoles.ADMIN_SYSTEM, TelicentRoles.USER),
                    Policy.requireAny("roles", TelicentRoles.USER, TelicentRoles.ADMIN_SYSTEM),
                },
                {
                    Policy.requireAny("roles",TelicentRoles.USER),
                    Policy.requireAny("roles", TelicentRoles.ADMIN_SYSTEM, TelicentRoles.USER),
                    Policy.requireAny("roles", TelicentRoles.USER, TelicentRoles.ADMIN_SYSTEM),
                }
        };
        //@formatter:on
    }

    @Test(dataProvider = "combinablePolicies")
    public void givenCompatiblePolicies_whenCombining_thenCombined(Policy x, Policy y, Policy expected) {
        // Given and When
        Policy combined = Policy.combine(x, y);

        // Then
        Assert.assertEquals(combined, expected);
    }

    @DataProvider(name = "differentPolicies")
    private Object[][] differentPolicies() {
        //@formatter:off
        return new Object[][] {
                // Comparing against null
                { Policy.NONE, Policy.ALLOW_ALL },
                { Policy.DENY_ALL, Policy.NONE },
                // Different policy sources
                {
                    Policy.requireAny("roles",TelicentRoles.USER),
                    Policy.requireAny("permissions",TelicentPermissions.Knowledge.READ) },
                // Different policy kinds
                {
                    Policy.requireAny("roles","test"),
                    Policy.ALLOW_ALL
                },
                {
                    Policy.requireAny("roles",TelicentRoles.USER),
                    Policy.requireAll("roles",TelicentRoles.USER),
                },
                // Different policy values
                {
                    Policy.requireAll("permissions",TelicentPermissions.Knowledge.WRITE),
                    Policy.requireAll("permissions", TelicentPermissions.Knowledge.READ_WRITE),
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Knowledge.READ, TelicentPermissions.Knowledge.COMPACT),
                    Policy.requireAll("permissions", TelicentPermissions.Knowledge.WRITE, TelicentPermissions.Knowledge.COMPACT)
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Catalogue.WRITE, TelicentPermissions.Catalogue.COMPACT),
                    Policy.requireAll("permissions", TelicentPermissions.Catalogue.READ, TelicentPermissions.Ontology.COMPACT),
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Backup.READ, TelicentPermissions.Backup.WRITE),
                    Policy.requireAll("permissions", TelicentPermissions.Backup.RESTORE, TelicentPermissions.Backup.DELETE),
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Client.READ),
                    Policy.requireAll("permissions", TelicentPermissions.Client.WRITE)
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Users.READ),
                    Policy.requireAll("permissions", TelicentPermissions.Users.WRITE)
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Groups.READ),
                    Policy.requireAll("permissions", TelicentPermissions.Groups.WRITE)
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Roles.READ),
                    Policy.requireAll("permissions", TelicentPermissions.Roles.WRITE)
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Permissions.READ),
                    Policy.requireAll("permissions", TelicentPermissions.Permissions.WRITE)
                },
                // equals() implementation is order sensitive
                // Changing this would require larger changes to current Policy implementation
                {
                    Policy.requireAny("roles", TelicentRoles.USER, TelicentRoles.ADMIN_USERS),
                    Policy.requireAny("roles", TelicentRoles.ADMIN_USERS, TelicentRoles.USER)
                }
        };
        //@formatter:on
    }

    @Test(dataProvider = "differentPolicies")
    public void givenDifferentPolicies_whenComparingForEquality_thenNotEquals(Policy x, Policy y) {
        // Given, When and Then
        Assert.assertNotEquals(x, y);
        Assert.assertNotEquals(y, x);
        Assert.assertNotEquals(Objects.hashCode(x), Objects.hashCode(y));
    }

    @DataProvider(name = "equivalentPolicies")
    private Object[][] equivalentPolicies() {
        //@formatter:off
        return new Object[][] {
                // Simple equivalent policies
                { Policy.NONE, Policy.NONE },
                { Policy.ALLOW_ALL, Policy.ALLOW_ALL },
                { Policy.DENY_ALL, Policy.DENY_ALL },
                // Equivalent policy constructed in different ways
                {
                  Policy.requireAny("roles", TelicentRoles.USER),
                  Policy.requireAny("roles", TelicentRoles.USER)
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Ontology.READ_WRITE),
                    Policy.requireAll("permissions",
                          TelicentPermissions.Ontology.READ,
                          TelicentPermissions.Ontology.WRITE),
                },
                {
                    Policy.requireAll("permissions", TelicentPermissions.Catalogue.READ_WRITE),
                    Policy.requireAll("permissions",
                            TelicentPermissions.Catalogue.READ,
                            TelicentPermissions.Catalogue.WRITE)
                }
        };
        //@formatter:on
    }

    @Test(dataProvider = "equivalentPolicies")
    public void givenEquivalentPolicies_whenComparingForEquality_thenEquals(Policy x, Policy y) {
        // Given, When and Then
        Assert.assertEquals(x, y);
        Assert.assertEquals(y, x);
        Assert.assertEquals(Objects.hashCode(x), Objects.hashCode(y));
    }

    @Test
    public void givenPolicy_whenComparingToNonPolicy_thenNotEquals() {
        // Given
        Policy x = Policy.requireAny(TelicentRoles.USER, TelicentRoles.ADMIN_SYSTEM);

        // When and Then
        // NB - While IntelliJ will show compiler warnings here these assertions are intentional to ensure that the
        //      various edge cases of the equals() implementation are properly tested
        Assert.assertFalse(x.equals(null));
        Assert.assertNotEquals(x, new Object());
        Assert.assertNotEquals(new Object(), x);
    }

}
