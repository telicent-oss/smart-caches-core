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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.telicent.smart.caches.configuration.auth.RemoteUserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfo;
import io.telicent.smart.caches.configuration.auth.UserInfoLookup;
import io.telicent.smart.caches.configuration.auth.UserInfoLookupException;
import org.apache.http.util.EntityUtils;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpOverrideForwardedRequest;
import org.mockserver.model.HttpRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URI;
import java.net.http.HttpClient;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TestRemoteUserInfoLookupWithAuth {

    String baseUri;

    private static ClientAndServer mockServer;
    private static PrivateKey privateKey;

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
        //TODO
        // returns null!
        //Assert.assertEquals(userInfo.getPreferredName(), "Alice Example");
        Assert.assertEquals(userInfo.getRoles(), List.of("ADMIN", "USER"));
        Assert.assertEquals(userInfo.getPermissions(), List.of("api.read", "api.write"));
        Assert.assertEquals(userInfo.getAttributes(), Map.of("department", "Engineering", "location", "London"));

    }
//    @Test(dataProvider = "keyIds")
//    public void givenMissingToken_whenCallingUserInfo_thenUnauthorized(String keyId) throws Exception {
//        // Given
//        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
//        //configureAuthentication();
//
//        try (Server server = builder.build()) {
//            server.start();
//            UserInfoResource.setKeyServer(keyServer);
//
//            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
//            UserInfoLookup lookup = new RemoteUserInfoLookup();
//
//            // When & Then
//            try {
//                lookup.lookup(userInfoEndpoint, null);
//                Assert.fail("Expected an exception due to missing token");
//            } catch (UserInfoLookupException ex) {
//                System.out.println(ex.getMessage());
//                Assert.assertTrue(ex.getMessage().contains("bearerToken must be provided"));
//            }
//        }
//    }
//
//    @Test(dataProvider = "keyIds")
//    public void givenInvalidEndpoint_whenCallingUserInfo_thenNotFound(String keyId) throws Exception {
//        // Given
//        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
//        //configureAuthentication();
//
//        try (Server server = builder.build()) {
//            server.start();
//            UserInfoResource.setKeyServer(keyServer);
//
//            Key privateKey = keyServer.getPrivateKey(keyId);
//            String token = Jwts.builder()
//                    .subject("test")
//                    .header().keyId(keyId).and()
//                    .signWith(privateKey)
//                    .compact();
//
//            // Point to a non-existent path
//            String invalidEndpoint = keyServer.getBaseUri() + "not-a-valid-path";
//            UserInfoLookup lookup = new RemoteUserInfoLookup();
//
//            // When & Then
//            try {
//                lookup.lookup(invalidEndpoint, token);
//                Assert.fail("Expected an exception due to 404 Not Found");
//            } catch (UserInfoLookupException ex) {
//                System.out.println(ex.getMessage());
//                Assert.assertTrue(ex.getMessage().contains("Endpoint " + invalidEndpoint + " not found"));
//            }
//        }
//    }
//
//    @Test(dataProvider = "keyIds", expectedExceptions = UserInfoLookupException.class)
//    public void givenUnauthorizedResponse_whenCallingUserInfo_thenThrowsUserInfoLookupException(String keyId) throws Exception {
//        // Given
//        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
//        //configureAuthentication();
//
//        try (Server server = builder.build()) {
//            server.start();
//            UserInfoResource.setKeyServer(keyServer);
//
//            // Make an invalid token (missing signature)
//            String badToken = "Bearer this.is.not.a.valid.token";
//
//            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
//            UserInfoLookup lookup = new RemoteUserInfoLookup();
//
//            // When & Then
//            try {
//                lookup.lookup(userInfoEndpoint, badToken);
//                Assert.fail("Expected UserInfoLookupException due to 401/403 response");
//            } catch (UserInfoLookupException ex) {
//                Assert.assertTrue(ex.getMessage().contains("Unauthorized when calling userinfo endpoint"));
//                throw ex; // satisfy expectedExceptions
//            }
//        }
//    }
//
//    @Test(dataProvider = "keyIds")
//    public void givenServerError_whenCallingUserInfo_thenException(String keyId) throws Exception {
//        // Given
//        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
//        //configureAuthentication();
//
//        try (Server server = builder.build()) {
//            server.start();
//            UserInfoResource.setKeyServer(keyServer);
//
//            Key privateKey = keyServer.getPrivateKey(keyId);
//            String token = Jwts.builder()
//                    .subject("force-error") // trigger server-side error
//                    .header().keyId(keyId).and()
//                    .signWith(privateKey)
//                    .compact();
//
//            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
//            UserInfoLookup lookup = new RemoteUserInfoLookup();
//
//            // When & Then
//            try {
//                lookup.lookup(userInfoEndpoint, token);
//                Assert.fail("Expected an exception due to 500 Internal Server Error");
//            } catch (UserInfoLookupException ex) {
//                System.out.println(ex.getMessage());
//                Assert.assertTrue(ex.getMessage().contains("500"));
//            }
//        }
//    }

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
