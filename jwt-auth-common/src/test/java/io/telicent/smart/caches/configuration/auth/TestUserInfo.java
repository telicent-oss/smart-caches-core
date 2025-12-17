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
package io.telicent.smart.caches.configuration.auth;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.smart.caches.configuration.auth.policy.TelicentPermissions;
import io.telicent.smart.caches.configuration.auth.policy.TelicentRoles;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestUserInfo {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    public void givenNoData_whenBuildingUserInfo_thenEmpty() {
        // Given and When
        UserInfo userInfo = UserInfo.builder().build();

        // Then
        Assert.assertNull(userInfo.getSub());
        Assert.assertNull(userInfo.getPreferredName());
        Assert.assertTrue(userInfo.getPermissions().isEmpty());
        Assert.assertTrue(userInfo.getRoles().isEmpty());
        Assert.assertTrue(userInfo.getAttributes().isEmpty());
    }

    @Test
    public void givenFullData_whenBuildingUserInfo_thenOk() {
        // Given and When
        UserInfo userInfo = UserInfo.builder()
                                    .sub(UUID.randomUUID().toString())
                                    .preferredName("Mr T. Test")
                                    .roles(List.of(TelicentRoles.USER))
                                    .permissions(List.of(TelicentPermissions.Knowledge.READ,
                                                         TelicentPermissions.Knowledge.WRITE))
                                    .attributes(Map.of("clearance", "O", "age", 42, "active", true, "nationality",
                                                       List.of("GBR", "US")))
                                    .build();

        // Then
        Assert.assertEquals(userInfo.getPreferredName(), "Mr T. Test");
        Assert.assertFalse(userInfo.getRoles().isEmpty());
        Assert.assertTrue(userInfo.getRoles().contains(TelicentRoles.USER));
        Assert.assertFalse(userInfo.getPermissions().isEmpty());
        Assert.assertTrue(userInfo.getPermissions().contains(TelicentPermissions.Knowledge.READ));
        Assert.assertTrue(userInfo.getPermissions().contains(TelicentPermissions.Knowledge.WRITE));
        Assert.assertFalse(userInfo.getAttributes().isEmpty());
        Assert.assertEquals(userInfo.getAttributes().get("clearance"), "O");
    }

    @DataProvider(name = "badJson")
    private Object[][] badJson() {
        return new Object[][] {
                // Array, not an object
                { "[]" },
                // Unterminated object
                { "{ \"sub\": \"foo\", " },
                // Unterminated field
                { "{ \"sub\": \"foo" },
                // Unterminated key
                { "{ \"sub" }
        };
    }

    @Test(dataProvider = "badJson", expectedExceptions = JacksonException.class)
    public void givenBadJson_whenParsingUserInfo_thenFails(String badJson) throws JsonProcessingException {
        // Given, When and Then
        this.json.readValue(badJson, UserInfo.class);
    }

    @DataProvider(name = "goodJson")
    private Object[] goodJson() {
        //@formatter:off
        return new Object[][] {
                {
                    "{ \"sub\": \"foo\", \"preferred_name\": \"Mr T. Test\" }",
                    UserInfo.builder().sub("foo").preferredName("Mr T. Test").build()
                },
                {
                    """
                    {
                      "sub": "foo",
                      "preferred_name": "Mr T. Test",
                      "extra": "test"
                    }
                    """,
                    UserInfo.builder().sub("foo").preferredName("Mr T. Test").build()
                },
                {
                    """
                    {
                      "sub": "foo",
                      "preferred_name": "Mr T. Test",
                      "roles": [
                        "USER"
                      ],
                      "permissions": [
                        "api.knowledge.read",
                        "api.ontology.read",
                        "api.catalog.read"
                      ]
                    }
                    """,
                        UserInfo.builder()
                                .sub("foo")
                                .preferredName("Mr T. Test")
                                .roles(List.of(TelicentRoles.USER))
                                .permissions(List.of(TelicentPermissions.Knowledge.READ,
                                                     TelicentPermissions.Ontology.READ,
                                                     TelicentPermissions.Catalogue.READ
                                ))
                                .build()
                },
                {
                    """
                    {
                      "sub": "foo",
                      "preferred_name": "Mr T. Test",
                      "roles": [
                        "USER",
                        "ADMIN_SYSTEM"
                      ],
                      "permissions": [
                        "api.knowledge.read",
                        "api.ontology.read",
                        "api.catalog.read",
                        "backup.read",
                        "backup.write"
                      ],
                      "attributes": {
                        "clearance": "TS",
                        "age": 42,
                        "nationality": [ "GBR", "US" ]
                      }
                    }
                    """,
                    UserInfo.builder()
                            .sub("foo")
                            .preferredName("Mr T. Test")
                            .roles(List.of(TelicentRoles.USER, TelicentRoles.ADMIN_SYSTEM))
                            .permissions(List.of(TelicentPermissions.Knowledge.READ,
                                                 TelicentPermissions.Ontology.READ,
                                                 TelicentPermissions.Catalogue.READ,
                                                 TelicentPermissions.Backup.READ,
                                                 TelicentPermissions.Backup.WRITE
                            ))
                            .attributes(Map.of("clearance", "TS",
                                               "age", 42,
                                               "nationality", List.of("GBR", "US")))
                            .build()
                }
        };
        //@formatter:on
    }

    @Test(dataProvider = "goodJson")
    public void givenGoodJson_whenParsingUserInfo_thenAsExpected(String goodJson, UserInfo expected) throws
            JsonProcessingException {
        // Given and When
        UserInfo parsed = this.json.readValue(goodJson, UserInfo.class);

        // Then
        Assert.assertEquals(parsed, expected);
    }
}
