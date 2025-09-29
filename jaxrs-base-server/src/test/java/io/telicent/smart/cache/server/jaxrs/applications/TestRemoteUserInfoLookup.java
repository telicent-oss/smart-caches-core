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

package io.telicent.smart.cache.server.jaxrs.applications;

import io.jsonwebtoken.Jwts;
import io.telicent.servlet.auth.jwt.verifier.aws.AwsElbKeyUrlRegistry;
import io.telicent.smart.cache.server.jaxrs.init.JwtAuthInitializer;
import io.telicent.smart.cache.server.jaxrs.init.TestInit;
import io.telicent.smart.cache.server.jaxrs.resources.DataResource;
import io.telicent.smart.cache.server.jaxrs.utils.RandomPortProvider;
import io.telicent.smart.caches.configuration.auth.RemoteUserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfo;
import io.telicent.smart.caches.configuration.auth.UserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfoLookupException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.Map;

public class TestRemoteUserInfoLookup extends AbstractAppEntrypoint {
    private static final RandomPortProvider PORT = new RandomPortProvider(22335);

    private final Client client = ClientBuilder.newClient();

    private MockKeyServer keyServer;

    @BeforeClass
    public void setup() throws Exception {
        this.keyServer = new MockKeyServer(12346);

        this.keyServer.start();
        this.keyServer.registerAsAwsRegion("custom");
    }


    @BeforeMethod
    public void testSetup() {
        DataResource.reset();
        TestInit.reset();
    }

    @AfterClass
    public void teardown() {
        this.client.close();
        this.keyServer.stop();
        AwsElbKeyUrlRegistry.reset();
    }

    @Override
    protected ServerBuilder buildServer() {
        return ServerBuilder.create().application(MockApplicationWithAuth.class)
                .port(PORT.newPort()).displayName("Test");
    }

    @DataProvider(name = "keyIds")
    public Object[][] keyIds() {
        return this.keyServer.getKeyIds();
    }

    @Test(dataProvider = "keyIds")
    public void givenServer_whenUsingRemoteUserInfoLookup_thenUserInfoReturned(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);

        try (Server server = builder.build()) {
            server.start();

            Key privateKey = keyServer.getPrivateKey(keyId);
            String token = Jwts.builder()
                    .subject("test")
                    .header().keyId(keyId).and()
                    .signWith(privateKey)
                    .compact();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            try (UserInfoLookup lookup = new RemoteUserInfoLookup(userInfoEndpoint)) {

                // When
                UserInfo userInfo = lookup.lookup(token);

                // Then
                Assert.assertNotNull(userInfo);
                Assert.assertEquals(userInfo.getSub(), "test");
                Assert.assertEquals(userInfo.getPermissions(), List.of("api.read"));
                Assert.assertEquals(userInfo.getAttributes(), Map.of());
                Assert.assertEquals(userInfo.getRoles(), List.of("USER"));
            }
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenJwtWithExtraClaims_whenCallingUserInfo_thenAllClaimsReturned(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);

        try (Server server = builder.build()) {
            server.start();

            Key privateKey = keyServer.getPrivateKey(keyId);
            String token = Jwts.builder()
                    .subject("test-user")
                    .claim("preferred_name", "Alice Example")
                    .claim("roles", List.of("ADMIN", "USER"))
                    .claim("permissions", List.of("api.read", "api.write"))
                    .claim("attributes", Map.of("department", "Engineering", "location", "London"))
                    .header().keyId(keyId).and()
                    .signWith(privateKey)
                    .compact();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            try (UserInfoLookup lookup = new RemoteUserInfoLookup(userInfoEndpoint)) {

                // When
                UserInfo userInfo = lookup.lookup(token);

                // Then
                Assert.assertEquals(userInfo.getSub(), "test-user");
                Assert.assertEquals(userInfo.getPreferredName(), "Alice Example");
                Assert.assertEquals(userInfo.getRoles(), List.of("ADMIN", "USER"));
                Assert.assertEquals(userInfo.getPermissions(), List.of("api.read", "api.write"));
                Assert.assertEquals(userInfo.getAttributes(),
                                    Map.of("department", "Engineering", "location", "London"));
            }
        }
    }
    @Test
    public void givenMissingToken_whenCallingUserInfo_thenUnauthorized() throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);

        try (Server server = builder.build()) {
            server.start();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            try (UserInfoLookup lookup = new RemoteUserInfoLookup(userInfoEndpoint)) {
                // When & Then
                try {
                    lookup.lookup(null);
                    Assert.fail("Expected an exception due to missing token");
                } catch (UserInfoLookupException ex) {
                    Assert.assertTrue(ex.getMessage().contains("bearerToken must be provided"));
                }
            }
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenInvalidEndpoint_whenCallingUserInfo_thenNotFound(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);

        try (Server server = builder.build()) {
            server.start();

            Key privateKey = keyServer.getPrivateKey(keyId);
            String token = Jwts.builder()
                    .subject("test")
                    .header().keyId(keyId).and()
                    .signWith(privateKey)
                    .compact();

            // Point to a non-existent path
            String invalidEndpoint = keyServer.getBaseUri() + "not-a-valid-path";
            try (UserInfoLookup lookup = new RemoteUserInfoLookup(invalidEndpoint)) {
                // When & Then
                try {
                    lookup.lookup(token);
                    Assert.fail("Expected an exception due to 404 Not Found");
                } catch (UserInfoLookupException ex) {
                    Assert.assertTrue(ex.getMessage().contains("Endpoint " + invalidEndpoint + " not found"));
                }
            }
        }
    }

    @Test(expectedExceptions = UserInfoLookupException.class)
    public void givenUnauthorizedResponse_whenCallingUserInfo_thenThrowsUserInfoLookupException() throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);

        try (Server server = builder.build()) {
            server.start();

            // Make an invalid token (missing signature)
            String badToken = "Bearer this.is.not.a.valid.token";

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            try (UserInfoLookup lookup = new RemoteUserInfoLookup(userInfoEndpoint)) {
                // When & Then
                try {
                    lookup.lookup(badToken);
                    Assert.fail("Expected UserInfoLookupException due to 401/403 response");
                } catch (UserInfoLookupException ex) {
                    Assert.assertTrue(ex.getMessage().contains("Unauthorized when calling userinfo endpoint"));
                    throw ex;
                }
            }
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenServerError_whenCallingUserInfo_thenException(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);

        try (Server server = builder.build()) {
            server.start();

            Key privateKey = keyServer.getPrivateKey(keyId);
            String token = Jwts.builder()
                    // triggers server-side error
                    .subject("force-error")
                    .header().keyId(keyId).and()
                    .signWith(privateKey)
                    .compact();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            try (UserInfoLookup lookup = new RemoteUserInfoLookup(userInfoEndpoint)) {
                // When & Then
                try {
                    lookup.lookup(token);
                    Assert.fail("Expected an exception due to 500 Internal Server Error");
                } catch (UserInfoLookupException ex) {
                    Assert.assertTrue(ex.getMessage().contains("500"));
                }
            }
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenMissingKeyID_whenCallingUserInfo_thenException(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);

        try (Server server = builder.build()) {
            server.start();

            Key privateKey = keyServer.getPrivateKey(keyId);
            String token = Jwts.builder()
                    .subject("test")
                    .signWith(privateKey)
                    .compact();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            try (UserInfoLookup lookup = new RemoteUserInfoLookup(userInfoEndpoint)) {

                // When & Then
                try {
                    lookup.lookup(token);
                    Assert.fail(
                            "Expected an exception due to Unauthorized when calling userinfo endpoint (status 403)");
                } catch (UserInfoLookupException ex) {
                    Assert.assertTrue(ex.getMessage().contains("403"));
                }
            }
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenUnsignedToken_whenCallingUserInfo_thenException(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);

        try (Server server = builder.build()) {
            server.start();

            String token = Jwts.builder()
                    .header().keyId(keyId).and()
                    .subject("test")
                    .compact();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            try (UserInfoLookup lookup = new RemoteUserInfoLookup(userInfoEndpoint)) {
                // When & Then
                try {
                    lookup.lookup(token);
                    Assert.fail(
                            "Expected an exception due to Unauthorized when calling userinfo endpoint (status 401)");
                } catch (UserInfoLookupException ex) {
                    Assert.assertTrue(ex.getMessage().contains("403"));
                }
            }
        }
    }


    @Test
    public void givenUnreachableEndpoint_whenCallingUserInfo_thenIOExceptionWrapped() throws IOException {
        String badEndpoint = "http://localhost:9999/userinfo"; // no server running
        try (UserInfoLookup lookup = new RemoteUserInfoLookup(badEndpoint)) {
            try {
                lookup.lookup("dummy-token");
                Assert.fail("Expected UserInfoLookupException");
            } catch (UserInfoLookupException ex) {
                Assert.assertTrue(ex.getMessage().contains("I/O error"));
            }
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Illegal character.*")
    public void givenInvalidEndpointUrl_whenCreatingLookup_thenIllegalArgument() throws IOException {
        String invalidEndpoint = "ht!tp://bad-url"; // invalid URI
        new RemoteUserInfoLookup(invalidEndpoint);
    }
}
