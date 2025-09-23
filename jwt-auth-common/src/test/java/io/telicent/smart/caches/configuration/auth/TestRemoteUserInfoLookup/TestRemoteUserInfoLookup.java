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
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.URI;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.mockserver.integration.ClientAndServer;

public class TestRemoteUserInfoLookup {

    protected ClientAndServer mockServer;
    private static final Map<String, Object> NOT_ACKNOWLEDGED = Map.of("acknowledged", false);
    int port;
    String baseUri;
    private static final ObjectMapper MAPPER = new ObjectMapper();


    @BeforeClass
    public void setup() throws Exception {

        this.mockServer = ClientAndServer.startClientAndServer();
        ConfigurationProperties.disableSystemOut(true);
        this.port = mockServer.getPort();
        this.baseUri = "http://localhost:" + port;

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String keyId = "test-key-1";
        mockServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/userinfo")
                )
                .respond(
                        HttpResponse.response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"sub\":\"test\",\"roles\":[\"USER\"],\"permissions\":[\"api.read\"],\"attributes\":{}}")
                );

    }


//    @BeforeMethod
//    public void testSetup() {
//        DataResource.reset();
//        TestInit.reset();
//        //Configurator.reset();
//    }


//    @AfterMethod
//    public void postMethod() {
//        //this.mockServer.reset();
//    }

    @AfterClass
    public void teardown() {
        //this.client.close();
        //Configurator.reset();

        this.mockServer.reset();
//        AwsElbKeyUrlRegistry.reset();
    }

    private HttpResponse notAcknowledgedResponse() throws JsonProcessingException {
        return response(NOT_ACKNOWLEDGED);
    }

    protected HttpResponse response(Map<String, Object> response) throws JsonProcessingException {
        return HttpResponse.response()
                .withHeader("X-elastic-product", "Elasticsearch")
                .withHeader("Content-Type", "application/json")
                .withStatusCode(200)
                .withBody(MAPPER.writeValueAsString(response));
    }

    private String createAwsToken(String keyId, KeyPair keyPair, String username) {
        return Jwts.builder()
                .header()
                .keyId(keyId)
                .and()
                .subject(username)
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)))
                .signWith(keyPair.getPrivate())
                .compact();
    }


    @Test
    public void givenServerWithAuthConfigured_whenUsingRemoteUserInfoLookup_thenUserInfoReturned() throws Exception {
        // Given
        //ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        //configureAuthentication();

        //TODO
        // what is this server, do I kick this out in favour of clientAndServer?
        try (ClientAndServer server = ClientAndServer.startClientAndServer()) {
            //UserInfoResource.setKeyServer(keyServer);

            //TODO
            // is this working?
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            gen.initialize(256);
            KeyPair keyPair = gen.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            String keyId = "my-test-key-1";
            Map<String, PublicKey> publicKeys = Map.of(keyId, publicKey);
            PublicKey pub = publicKeys.get(keyId);
            String token = Jwts.builder()
                    .subject("test")
                    .claim("roles", List.of("USER"))
                    .header().keyId(keyId).and()
                    .signWith(privateKey)
                    .compact();
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(pub)
                    .build()
                    .parseSignedClaims(token);


            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:1080/userinfo"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();


            //TODO
            // I need to create a mock server that will return the userinfo object given token
            String userInfoEndpoint = baseUri + "/userinfo";
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

//    @Test(dataProvider = "keyIds")
//    public void givenJwtWithExtraClaims_whenCallingUserInfo_thenAllClaimsReturned(String keyId) throws Exception {
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
//                    .subject("test-user")
//                    .claim("preferred_name", "Alice Example")
//                    .claim("roles", List.of("ADMIN", "USER"))
//                    .claim("permissions", List.of("api.read", "api.write"))
//                    .claim("attributes", Map.of("department", "Engineering", "location", "London"))
//                    .header().keyId(keyId).and()
//                    .signWith(privateKey)
//                    .compact();
//
//            String userInfoEndpoint = keyServer.getBaseUri() + "userinfo";
//            UserInfoLookup lookup = new RemoteUserInfoLookup();
//
//            // When
//            UserInfo userInfo = lookup.lookup(userInfoEndpoint, token);
//
//            // Then
//            Assert.assertEquals(userInfo.getSub(), "test-user");
//            Assert.assertEquals(userInfo.getPreferredName(), "Alice Example");
//            Assert.assertEquals(userInfo.getRoles(), List.of("ADMIN", "USER"));
//            Assert.assertEquals(userInfo.getPermissions(), List.of("api.read", "api.write"));
//            Assert.assertEquals(userInfo.getAttributes(), Map.of("department", "Engineering", "location", "London"));
//        }
//    }
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
