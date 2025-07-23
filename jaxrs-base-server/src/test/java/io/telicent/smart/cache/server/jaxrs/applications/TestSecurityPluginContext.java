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

import io.telicent.servlet.auth.jwt.JwtHttpConstants;
import io.telicent.servlet.auth.jwt.configuration.ConfigurationParameters;
import io.telicent.servlet.auth.jwt.verification.TestKeyUtils;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.server.jaxrs.init.MockAuthInit;
import io.telicent.smart.cache.server.jaxrs.utils.RandomPortProvider;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestSecurityPluginContext {
    private static final RandomPortProvider PORT = new RandomPortProvider(34543);

    private File secretKey;
    private final Client client = ClientBuilder.newClient();
    private final MockKeyServer keyServer = new MockKeyServer(11223);

    private WebTarget forServer(Server server, String path) {
        return this.client.target(server.getBaseUri()).path(path);
    }

    @BeforeClass
    public void setup() throws IOException {
        // Make the secret key available in a temporary file as we need that for our tests
        this.secretKey = TestKeyUtils.saveKeyToFile(Base64.getEncoder().encode(MockAuthInit.SIGNING_KEY.getEncoded()));

        this.keyServer.start();
    }

    @BeforeMethod
    public void preTest() {
        configureAuthentication();
    }

    @AfterMethod
    public void postTest() {
        Configurator.reset();
    }


    @AfterClass
    public void teardown() {
        this.keyServer.stop();
        this.client.close();
        Configurator.reset();
    }

    private void configureAuthentication() {
        Properties properties = new Properties();
        properties.put(ConfigurationParameters.PARAM_SECRET_KEY, this.secretKey.getAbsolutePath());
        Configurator.setSingleSource(new PropertiesSource(properties));
    }


    private static String verifyStringResponse(Invocation.Builder invocation) {
        // Given and When
        Response response = invocation.get();

        // Then
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        String body = response.readEntity(String.class);
        Assert.assertNotNull(body);
        Assert.assertFalse(StringUtils.isBlank(body));
        return body;
    }

    private static Invocation.Builder invokeForUser(WebTarget target, String user) {
        return target.request(MediaType.TEXT_PLAIN)
                     .header(JwtHttpConstants.HEADER_AUTHORIZATION,
                             String.format("%s %s", JwtHttpConstants.AUTH_SCHEME_BEARER,
                                           MockAuthInit.createToken(user)));
    }

    private static Server createServer() {
        return ServerBuilder.create()
                            .port(PORT.newPort())
                            .application(MockApplicationWithAuth.class)
                            .withListener(MockAuthInit.class)
                            .displayName("Test")
                            .build();
    }

    @DataProvider(name = "usernames")
    private Object[][] usernames() {
        return new Object[][] {
                { "test" },
                { "test@telicent.io" },
                { UUID.randomUUID().toString() }
        };
    }

    @Test(dataProvider = "usernames")
    public void givenServer_whenEchoingUsernameDirectly_thenUsernameIsEchoed(String username) throws IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/username/direct");
            Invocation.Builder invocation = invokeForUser(target, username);

            // Then
            String actual = verifyStringResponse(invocation);
            Assert.assertEquals(actual, username);
        }
    }

    @Test(dataProvider = "usernames")
    public void givenServer_whenEchoingUsernameViaPlugin_thenUsernameIsEchoed(String username) throws IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/username/plugin");
            Invocation.Builder invocation = invokeForUser(target, username);

            // Then
            String actual = verifyStringResponse(invocation);
            Assert.assertEquals(actual, username);
        }
    }

    @DataProvider(name = "headers")
    private Object[][] headers() {
        return new Object[][] {
                { "Test", List.of("foo") },
                { "Test", List.of("foo", "bar") },
                { "Test", List.of() }
        };
    }

    @Test(dataProvider = "headers")
    public void givenServer_whenEchoingHeaderDirect_thenHeaderValuesAreEchoed(String header,
                                                                              List<String> values) throws
            IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/headers/" + header + "/direct");
            Invocation.Builder invocation = invokeWithHeaders(target, header, values);

            // Then
            verifyHeadersEchoed(values, invocation);
        }
    }

    private static void verifyHeadersEchoed(List<String> values, Invocation.Builder invocation) {
        Response response = invocation.get();
        if (values.isEmpty()) {
            Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
        } else {
            Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
            List<String> actual =
                    new ArrayList<>(List.of(StringUtils.split(response.readEntity(String.class), "\n")));
            Assert.assertEquals(actual, values);
        }
    }

    private static Invocation.Builder invokeWithHeaders(WebTarget target, String header, List<String> values) {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(JwtHttpConstants.HEADER_AUTHORIZATION,
                    String.format("%s %s", JwtHttpConstants.AUTH_SCHEME_BEARER,
                                  MockAuthInit.createToken("test")));
        if (!values.isEmpty()) {
            headers.put(header, values.stream().map(v -> (Object) v).toList());
        }
        Invocation.Builder invocation = target.request(MediaType.TEXT_PLAIN).headers(headers);
        return invocation;
    }

    @Test(dataProvider = "headers")
    public void givenServer_whenEchoingHeaderViaPlugin_thenHeaderValuesAreEchoed(String header,
                                                                                 List<String> values) throws
            IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/headers/" + header + "/plugin");
            Invocation.Builder invocation = invokeWithHeaders(target, header, values);

            // Then
            verifyHeadersEchoed(values, invocation);
        }
    }

    @DataProvider(name = "methods")
    private Object[][] httpMethods() {
        return new Object[][] {
                { HttpMethod.GET },
                { HttpMethod.POST },
                { HttpMethod.DELETE },
        };
    }

    @Test(dataProvider = "methods")
    public void givenServer_whenEchoingMethodDirectly_thenMethodIsEchoed(String method) throws
            IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/method/direct");
            Invocation.Builder invocation = invokeForUser(target,"test");

            // Then
            verifyMethodEchoed(method, invocation);
        }
    }

    @Test(dataProvider = "methods")
    public void givenServer_whenEchoingMethodViaPlugin_thenMethodIsEchoed(String method) throws
            IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/method/plugin");
            Invocation.Builder invocation = invokeForUser(target,"test");

            // Then
            verifyMethodEchoed(method, invocation);
        }
    }

    private static void verifyMethodEchoed(String method, Invocation.Builder invocation) {
        Response response = null;
        if (method.equals(HttpMethod.GET)) {
            response = invocation.get();
        } else if (method.equals(HttpMethod.POST)) {
            response = invocation.post(Entity.entity("foo", MediaType.TEXT_PLAIN));
        } else if (method.equals(HttpMethod.DELETE)) {
            response = invocation.delete();
        } else {
            Assert.fail("Unsupported HTTP method: " + method);
        }
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        String actual = response.readEntity(String.class);
        Assert.assertEquals(actual, method);
    }

    @Test
    public void givenServer_whenEchoingUriDirectly_thenUriIsEchoed() throws
            IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/uri/direct");
            Invocation.Builder invocation = invokeForUser(target,"test");

            // Then
            String actual = verifyStringResponse(invocation);
            Assert.assertEquals(actual, server.getBaseUri() + "security/uri/direct");
        }
    }

    @Test
    public void givenServer_whenEchoingUriViaPlugin_thenUriIsEchoed() throws
            IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/uri/plugin");
            Invocation.Builder invocation = invokeForUser(target,"test");

            // Then
            String actual = verifyStringResponse(invocation);
            Assert.assertEquals(actual, server.getBaseUri() + "security/uri/plugin");
        }
    }

    @DataProvider(name = "paths")
    private Object[][] paths() {
        return new Object[][] {
                { "test", "test"},
                { "a%20test", "a test"},
                { "a%2Fencoded%2Fslash", "a/encoded/slash"},
        };
    }

    @Test(dataProvider = "paths")
    public void givenServer_whenEchoingPathDirectly_thenPathIsEchoed(String item, String expected) throws
            IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/path/" + item + "/direct");
            Invocation.Builder invocation = invokeForUser(target,"test");

            // Then
            String actual = verifyStringResponse(invocation);
            Assert.assertEquals(actual, "security/path/" + expected + "/direct");
        }
    }

    @Test(dataProvider = "paths")
    public void givenServer_whenEchoingPathViaPlugin_thenPathIsEchoed(String item, String expected) throws
            IOException {
        // Given
        try (Server server = createServer()) {
            server.start();

            // When
            WebTarget target = forServer(server, "/security/path/" + item + "/plugin");
            Invocation.Builder invocation = invokeForUser(target,"test");

            // Then
            String actual = verifyStringResponse(invocation);
            Assert.assertEquals(actual, "security/path/" + expected + "/plugin");
        }
    }

}
