/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.cache.server.jaxrs.applications;

import io.telicent.servlet.auth.jwt.JwtHttpConstants;
import io.telicent.servlet.auth.jwt.configuration.ConfigurationParameters;
import io.telicent.servlet.auth.jwt.verification.TestKeyUtils;
import io.telicent.servlet.auth.jwt.verifier.aws.AwsElbKeyUrlRegistry;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.server.jaxrs.init.JwtAuthInitializer;
import io.telicent.smart.cache.server.jaxrs.init.MockAuthInit;
import io.telicent.smart.cache.server.jaxrs.init.TestInit;
import io.telicent.smart.cache.server.jaxrs.model.Problem;
import io.telicent.smart.cache.server.jaxrs.resources.DataResource;
import io.telicent.smart.cache.server.jaxrs.resources.JwksResource;
import io.telicent.smart.cache.server.jaxrs.utils.RandomPortProvider;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.Strings;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TestServerRoleAuthorization extends AbstractAppEntrypoint {
    private static final RandomPortProvider PORT = new RandomPortProvider(22554);
    private static final Map<String, Object> USER_ROLE = Map.of("roles", List.of("USER"));
    private static final Map<String, Object> USER_AND_ADMIN_ROLES = Map.of("roles", List.of("USER", "ADMIN"));

    private final Client client = ClientBuilder.newClient();

    private File secretKey;

    private final MockKeyServer keyServer = new MockKeyServer(55667);

    @BeforeClass
    public void setup() throws Exception {
        // Make the secret key available in a temporary file as we need that for our tests
        this.secretKey = TestKeyUtils.saveKeyToFile(Base64.getEncoder().encode(MockAuthInit.SIGNING_KEY.getEncoded()));

        // Start the Mock Key Server
        this.keyServer.start();
    }


    @BeforeMethod
    public void testSetup() {
        DataResource.reset();
        TestInit.reset();
        Configurator.reset();
    }

    @AfterClass
    public void teardown() {
        this.client.close();
        Configurator.reset();

        // Clean up temporary keys
        this.secretKey.delete();
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

    private WebTarget forServer(Server server, String path) {
        return this.client.target(server.getBaseUri()).path(path);
    }

    private Properties configureAuthentication() {
        Properties properties = new Properties();
        properties.put(ConfigurationParameters.PARAM_SECRET_KEY, this.secretKey.getAbsolutePath());
        properties.put(ConfigurationParameters.PARAM_ROLES_CLAIM, "roles");
        Configurator.setSingleSource(new PropertiesSource(properties));
        return properties;
    }

    private void verifyAuthenticationRequired(Server server, String authHeaderValue) {
        // When
        WebTarget target = forServer(server, "/healthz");
        Invocation.Builder invocation = target.request(MediaType.APPLICATION_JSON);

        // Then
        // Properly configured auth but no credentials so 403 Forbidden
        TestServer.verifyError(invocation, SyncInvoker::get, Response.Status.FORBIDDEN.getStatusCode());

        // And
        // After adding credentials should get a success response
        invocation = invocation.header(JwtHttpConstants.HEADER_AUTHORIZATION, authHeaderValue);
        TestServer.verifyResponse(invocation.get(), Response.Status.NO_CONTENT);
    }

    @DataProvider(name = "keyIds")
    public Object[][] keyIds() {
        return this.keyServer.getKeyIds();
    }

    @Test
    public void givenServerWithAuthConfigured_whenMakingRequests_thenAuthenticationIsRequired_andAuthenticatedRequestSucceed() throws
            IOException {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        configureAuthentication();
        try (Server server = builder.build()) {
            server.start();

            // When and Then
            verifyAuthenticationRequired(server, String.format("%s %s", JwtHttpConstants.AUTH_SCHEME_BEARER,
                                                               MockAuthInit.createToken("test")));


            server.shutdownNow();
        }
    }

    @Test
    public void givenServerWithRolesConfigured_whenMakingRequestWithNoRoles_thenUnauthorized() throws IOException {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        configureAuthentication();
        try (Server server = builder.build()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/data/test");
            Invocation.Builder invocation = prepareRequest(target, MockAuthInit.createToken("test"));
            try (Response response = invocation.delete()) {
                // Then
                verifyUnauthorized(response, "require roles");
            }

            server.shutdownNow();
        }
    }

    private static Invocation.Builder prepareRequest(WebTarget target, String jwt) {
        return target.request(MediaType.APPLICATION_JSON)
                     .header(JwtHttpConstants.HEADER_AUTHORIZATION,
                             String.format("%s %s", JwtHttpConstants.AUTH_SCHEME_BEARER,
                                           jwt));
    }

    @Test
    public void givenServerWithRolesConfigured_whenMakingRequestWithInsufficientRoles_thenUnauthorized() throws
            IOException {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        configureAuthentication();
        try (Server server = builder.build()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/data/test");
            Invocation.Builder invocation = prepareRequest(target, MockAuthInit.createToken("test", USER_ROLE));
            try (Response response = invocation.delete()) {
                // Then
                verifyUnauthorized(response, "require roles");
            }

            server.shutdownNow();
        }
    }

    private static void verifyUnauthorized(Response response, String expectedErrorDetail) {
        Assert.assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());
        Problem problem = response.readEntity(Problem.class);
        Assert.assertTrue(Strings.CI.contains(problem.getDetail(), expectedErrorDetail));
    }

    @Test
    public void givenServerWithRolesConfigured_whenMakingRequestWithSufficientRoles_thenAuthorized() throws
            IOException {
        // Given
        ServerBuilder builder = buildServer().withListener(JwtAuthInitializer.class);
        configureAuthentication();
        try (Server server = builder.build()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/data/test");
            Invocation.Builder invocation = prepareRequest(target, MockAuthInit.createToken("test",
                                                                                            USER_AND_ADMIN_ROLES));
            try (Response response = invocation.delete()) {
                // Then
                verifyUnauthorized(response, "require roles");
            }

            server.shutdownNow();
        }
    }
}
