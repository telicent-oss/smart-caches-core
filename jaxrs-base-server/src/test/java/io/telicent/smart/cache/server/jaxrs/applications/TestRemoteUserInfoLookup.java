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
import io.telicent.smart.cache.server.jaxrs.resources.JwksResource;
import io.telicent.smart.cache.server.jaxrs.resources.UserInfoResource;
import io.telicent.smart.cache.server.jaxrs.utils.RandomPortProvider;
import io.telicent.smart.caches.configuration.auth.RemoteUserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfo;
import io.telicent.smart.caches.configuration.auth.UserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfoLookupException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
        JwksResource.reset();
    }

    @Override
    protected ServerBuilder buildServer() {
        return ServerBuilder.create().application(MockApplicationWithAuth.class)
                // Use a different port for each test just in case one test is slow to teardown the server
                .port(PORT.newPort()).displayName("Test");
    }

    @DataProvider(name = "keyIds")
    public Object[][] keyIds() {
        return this.keyServer.getKeyIds();
    }

    @Test(dataProvider = "keyIds")
    public void givenServerWithAuthConfigured_whenUsingRemoteUserInfoLookup_thenUserInfoReturned(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        //configureAuthentication();

        try (Server server = builder.build()) {
            server.start();
            UserInfoResource.setKeyServer(keyServer);

            Key privateKey = keyServer.getPrivateKey(keyId);
            String token = Jwts.builder()
                    .subject("test")
                    .header().keyId(keyId).and()
                    .signWith(privateKey)
                    .compact();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            UserInfoLookup lookup = new RemoteUserInfoLookup();

            // When
            UserInfo userInfo = lookup.lookup(userInfoEndpoint, token);

            // Then
            Assert.assertNotNull(userInfo);
            Assert.assertEquals(userInfo.getSub(), "test");
            Assert.assertEquals(userInfo.getPermissions(), List.of("api.read"));
            Assert.assertEquals(userInfo.getAttributes(), Map.of());
            Assert.assertEquals(userInfo.getRoles(), List.of("USER"));
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenJwtWithExtraClaims_whenCallingUserInfo_thenAllClaimsReturned(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        //configureAuthentication();

        try (Server server = builder.build()) {
            server.start();
            UserInfoResource.setKeyServer(keyServer);

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
            UserInfoLookup lookup = new RemoteUserInfoLookup();

            // When
            UserInfo userInfo = lookup.lookup(userInfoEndpoint, token);

            // Then
            Assert.assertEquals(userInfo.getSub(), "test-user");
            Assert.assertEquals(userInfo.getPreferredName(), "Alice Example");
            Assert.assertEquals(userInfo.getRoles(), List.of("ADMIN", "USER"));
            Assert.assertEquals(userInfo.getPermissions(), List.of("api.read", "api.write"));
            Assert.assertEquals(userInfo.getAttributes(), Map.of("department", "Engineering", "location", "London"));
        }
    }
    @Test(dataProvider = "keyIds")
    public void givenMissingToken_whenCallingUserInfo_thenUnauthorized(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        //configureAuthentication();

        try (Server server = builder.build()) {
            server.start();
            UserInfoResource.setKeyServer(keyServer);

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            UserInfoLookup lookup = new RemoteUserInfoLookup();

            // When & Then
            try {
                lookup.lookup(userInfoEndpoint, null);
                Assert.fail("Expected an exception due to missing token");
            } catch (UserInfoLookupException ex) {
                System.out.println(ex.getMessage());
                Assert.assertTrue(ex.getMessage().contains("bearerToken must be provided"));
            }
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenInvalidEndpoint_whenCallingUserInfo_thenNotFound(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        //configureAuthentication();

        try (Server server = builder.build()) {
            server.start();
            UserInfoResource.setKeyServer(keyServer);

            Key privateKey = keyServer.getPrivateKey(keyId);
            String token = Jwts.builder()
                    .subject("test")
                    .header().keyId(keyId).and()
                    .signWith(privateKey)
                    .compact();

            // Point to a non-existent path
            String invalidEndpoint = keyServer.getBaseUri() + "not-a-valid-path";
            UserInfoLookup lookup = new RemoteUserInfoLookup();

            // When & Then
            try {
                lookup.lookup(invalidEndpoint, token);
                Assert.fail("Expected an exception due to 404 Not Found");
            } catch (UserInfoLookupException ex) {
                System.out.println(ex.getMessage());
                Assert.assertTrue(ex.getMessage().contains("Endpoint " + invalidEndpoint + " not found"));
            }
        }
    }

    @Test(dataProvider = "keyIds", expectedExceptions = UserInfoLookupException.class)
    public void givenUnauthorizedResponse_whenCallingUserInfo_thenThrowsUserInfoLookupException(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        //configureAuthentication();

        try (Server server = builder.build()) {
            server.start();
            UserInfoResource.setKeyServer(keyServer);

            // Make an invalid token (missing signature)
            String badToken = "Bearer this.is.not.a.valid.token";

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            UserInfoLookup lookup = new RemoteUserInfoLookup();

            // When & Then
            try {
                lookup.lookup(userInfoEndpoint, badToken);
                Assert.fail("Expected UserInfoLookupException due to 401/403 response");
            } catch (UserInfoLookupException ex) {
                Assert.assertTrue(ex.getMessage().contains("Unauthorized when calling userinfo endpoint"));
                throw ex; // satisfy expectedExceptions
            }
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenServerError_whenCallingUserInfo_thenException(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        //configureAuthentication();

        try (Server server = builder.build()) {
            server.start();
            UserInfoResource.setKeyServer(keyServer);

            Key privateKey = keyServer.getPrivateKey(keyId);
            String token = Jwts.builder()
                    .subject("force-error") // trigger server-side error
                    .header().keyId(keyId).and()
                    .signWith(privateKey)
                    .compact();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            UserInfoLookup lookup = new RemoteUserInfoLookup();

            // When & Then
            try {
                lookup.lookup(userInfoEndpoint, token);
                Assert.fail("Expected an exception due to 500 Internal Server Error");
            } catch (UserInfoLookupException ex) {
                System.out.println(ex.getMessage());
                Assert.assertTrue(ex.getMessage().contains("500"));
            }
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenMissingKeyID_whenCallingUserInfo_thenException(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        //configureAuthentication();

        try (Server server = builder.build()) {
            server.start();
            UserInfoResource.setKeyServer(keyServer);

            Key privateKey = keyServer.getPrivateKey(keyId);
            String token = Jwts.builder()
                    .subject("test") // trigger server-side error
                    .signWith(privateKey)
                    .compact();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            UserInfoLookup lookup = new RemoteUserInfoLookup();

            // When & Then
            try {
                lookup.lookup(userInfoEndpoint, token);
                Assert.fail("Expected an exception due to Unauthorized when calling userinfo endpoint (status 401)");
            } catch (UserInfoLookupException ex) {
                System.out.println(ex.getMessage());
                Assert.assertTrue(ex.getMessage().contains("401"));
            }
        }
    }

    @Test(dataProvider = "keyIds")
    public void givenUnsignedToken_whenCallingUserInfo_thenException(String keyId) throws Exception {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        //configureAuthentication();

        try (Server server = builder.build()) {
            server.start();
            UserInfoResource.setKeyServer(keyServer);

            String token = Jwts.builder()
                    .subject("test") // trigger server-side error
                    .compact();

            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
            UserInfoLookup lookup = new RemoteUserInfoLookup();

            // When & Then
            try {
                lookup.lookup(userInfoEndpoint, token);
                Assert.fail("Expected an exception due to Unauthorized when calling userinfo endpoint (status 401)");
            } catch (UserInfoLookupException ex) {
                System.out.println(ex.getMessage());
                Assert.assertTrue(ex.getMessage().contains("401"));
            }
        }
    }


    @Test
    public void givenUnreachableEndpoint_whenCallingUserInfo_thenIOExceptionWrapped() {
        String badEndpoint = "http://localhost:9999/userinfo"; // no server running
        UserInfoLookup lookup = new RemoteUserInfoLookup();

        try {
            lookup.lookup(badEndpoint, "dummy-token");
            Assert.fail("Expected UserInfoLookupException");
        } catch (UserInfoLookupException ex) {
            Assert.assertTrue(ex.getMessage().contains("I/O error"));
        }
    }

    @Test
    public void givenInvalidEndpointUrl_whenCallingUserInfo_thenIllegalArgumentExceptionWrapped() {
        String invalidEndpoint = "ht!tp://bad-url"; // invalid URI
        UserInfoLookup lookup = new RemoteUserInfoLookup();

        try {
            lookup.lookup(invalidEndpoint, "dummy-token");
            Assert.fail("Expected UserInfoLookupException");
        } catch (UserInfoLookupException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid userInfoEndpoint URL"));
        }
    }
}
