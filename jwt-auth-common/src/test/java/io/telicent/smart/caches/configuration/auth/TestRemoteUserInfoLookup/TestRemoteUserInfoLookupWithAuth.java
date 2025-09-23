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

package io.telicent.smart.caches.configuration.auth.TestRemoteUserInfoLookup;

import io.jsonwebtoken.Jwts;
import io.telicent.smart.caches.configuration.auth.RemoteUserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfo;
import io.telicent.smart.caches.configuration.auth.UserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfoLookupException;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpOverrideForwardedRequest;
import org.mockserver.model.HttpRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

public class TestRemoteUserInfoLookupWithAuth {

    private static ClientAndServer mockServer;
    private static PrivateKey privateKey;
    private static final String userInfoEndpoint = "http://localhost:1080/userinfo";
    private static final String keyId = "test-key-id";

    @BeforeClass
    public void setup() throws Exception {

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        new LocalUserInfoHandler(publicKey).start(8081);
        mockServer = ClientAndServer.startClientAndServer(1080);

        new MockServerClient("localhost", 1080)
                .when(HttpRequest.request().withPath("/userinfo"))
                .forward(HttpOverrideForwardedRequest.forwardOverriddenRequest(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/userinfo")
                                .withHeader("Host", "localhost:8081")
                ));
    }

    @AfterClass
    public void teardown() {
        mockServer.stop();
    }

    @Test
    public void givenServerWithAuthConfigured_whenUsingRemoteUserInfoLookup_thenUserInfoReturned() throws Exception {
        String token = Jwts.builder()
                .subject("test")
                .header().keyId(keyId).and()
                .claim("roles", List.of("USER"))
                .claim("permissions", List.of("api.read"))
                .signWith(privateKey)
                .compact();

        UserInfoLookup lookup = new RemoteUserInfoLookup();
        UserInfo userInfo = lookup.lookup("http://localhost:1080/userinfo", token);

        Assert.assertNotNull(userInfo);
        Assert.assertEquals(userInfo.getSub(), "test");
        Assert.assertEquals(userInfo.getPermissions(), List.of("api.read"));
        Assert.assertEquals(userInfo.getAttributes(), Map.of());
        Assert.assertEquals(userInfo.getRoles(), List.of("USER"));
    }

    @Test
    public void givenJwtWithExtraClaims_whenCallingUserInfo_thenAllClaimsReturned() throws Exception {

        String token = Jwts.builder()
                    .subject("test-user")
                    .header().keyId(keyId).and()
                    .claim("preferred_name", "Alice Example")
                    .claim("roles", List.of("ADMIN", "USER"))
                    .claim("permissions", List.of("api.read", "api.write"))
                    .claim("attributes", Map.of("department", "Engineering", "location", "London"))
                    .signWith(privateKey)
                    .compact();

        UserInfoLookup lookup = new RemoteUserInfoLookup();
        UserInfo userInfo = lookup.lookup("http://localhost:1080/userinfo", token);

        // Then
        Assert.assertEquals(userInfo.getSub(), "test-user");
        Assert.assertEquals(userInfo.getPreferredName(), "Alice Example");
        Assert.assertEquals(userInfo.getRoles(), List.of("ADMIN", "USER"));
        Assert.assertEquals(userInfo.getPermissions(), List.of("api.read", "api.write"));
        Assert.assertEquals(userInfo.getAttributes(), Map.of("department", "Engineering", "location", "London"));

    }

    @Test
    public void givenMissingToken_whenCallingUserInfo_thenUnauthorized() throws Exception {
        // Given
        UserInfoLookup lookup = new RemoteUserInfoLookup();
        // When & Then
        try {
            lookup.lookup("http://localhost:1080/userinfo", null);
            Assert.fail("Expected an exception due to missing token");
        } catch (UserInfoLookupException ex) {
            Assert.assertTrue(ex.getMessage().contains("bearerToken must be provided"));
        }
    }

    @Test()
    public void givenInvalidEndpoint_whenCallingUserInfo_thenNotFound() throws Exception {
        // Given
        String token = Jwts.builder()
                    .subject("test")
                    .header().keyId(keyId).and()
                    .signWith(privateKey)
                    .compact();

        // Point to a non-existent path
        String invalidEndpoint = "http://localhost:1080/not-a-valid-path";
        UserInfoLookup lookup = new RemoteUserInfoLookup();

        // When & Then
        try {
            lookup.lookup(invalidEndpoint, token);
            Assert.fail("Expected an exception due to 404 Not Found");
        } catch (UserInfoLookupException ex) {
            Assert.assertTrue(ex.getMessage().contains("Endpoint " + invalidEndpoint + " not found"));
        }
    }

    @Test(expectedExceptions = UserInfoLookupException.class)
    public void givenUnauthorizedResponse_whenCallingUserInfo_thenThrowsUserInfoLookupException() throws Exception {
        // Given
        String badToken = "Bearer this.is.not.a.valid.token";
        UserInfoLookup lookup = new RemoteUserInfoLookup();
        // When & Then
        try {
            lookup.lookup(userInfoEndpoint, badToken);
            Assert.fail("Expected UserInfoLookupException due to 401/403 response");
        } catch (UserInfoLookupException ex) {
            Assert.assertTrue(ex.getMessage().contains("Unauthorized when calling userinfo endpoint"));
            throw ex;
        }
    }

    @Test
    public void givenMissingKeyIdHeader_whenCallingUserInfo_thenUnauthorizedReturned() throws Exception {
        // Given
        String token = Jwts.builder()
                .subject("test")
                .signWith(privateKey)
                .compact();

        UserInfoLookup lookup = new RemoteUserInfoLookup();
        // When & Then
        try {
            lookup.lookup(userInfoEndpoint, token);
            Assert.fail("Expected UserInfoLookupException");
        } catch (UserInfoLookupException ex) {
            Assert.assertTrue(
                    ex.getMessage().contains("Unauthorized when calling userinfo endpoint (status 401)"),
                    "Should be unauthorized"
                        );
        }
    }

    @Test
    public void givenServerError_whenCallingUserInfo_thenException() throws Exception {
        // Given
         String token = Jwts.builder()
                 .header().keyId(keyId).and()
                 .subject("force-error") // trigger server-side error
                 .signWith(privateKey)
                 .compact();

         UserInfoLookup lookup = new RemoteUserInfoLookup();

         // When & Then
        try {
            lookup.lookup(userInfoEndpoint, token);
            Assert.fail("Expected an exception due to 500 Internal Server Error");
        } catch (UserInfoLookupException ex) {
            Assert.assertTrue(ex.getMessage().contains("500"));
        }
    }

    @Test
    public void givenMissingUserInfoEndpoint_whenCallingUserInfo_thenException() throws Exception {
        // Given
        String token = Jwts.builder()
                .header().keyId(keyId).and()
                .subject("test") // trigger server-side error
                .signWith(privateKey)
                .compact();

        UserInfoLookup lookup = new RemoteUserInfoLookup();

        // When & Then
        try {
            lookup.lookup(null, token);
            Assert.fail("Expected an exception due to missing userinfo endpoint");
        } catch (UserInfoLookupException ex) {
            Assert.assertTrue(ex.getMessage().contains("userInfoEndpoint must be provided"));
        }
    }

    @Test
    public void givenMalformedJsonResponse_whenCallingUserInfo_thenParsingExceptionWrapped() throws Exception {
        // issue JWT with subject = "malformed-json" to trigger that path
        String token = Jwts.builder()
                .header().keyId(keyId).and()
                .subject("malformed-json")
                .signWith(privateKey)
                .compact();

        UserInfoLookup lookup = new RemoteUserInfoLookup();

        try {
            lookup.lookup(userInfoEndpoint, token);
            Assert.fail("Expected UserInfoLookupException");
        } catch (UserInfoLookupException ex) {
            Assert.assertTrue(ex.getMessage().contains("Failed to parse userinfo response"));
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
