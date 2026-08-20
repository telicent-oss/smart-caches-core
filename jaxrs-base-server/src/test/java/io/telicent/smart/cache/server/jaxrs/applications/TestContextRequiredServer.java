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

import io.telicent.smart.cache.server.jaxrs.init.TestInit;
import io.telicent.smart.cache.server.jaxrs.utils.RandomPortProvider;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static io.telicent.smart.cache.server.jaxrs.applications.TestServer.verifyResponse;

// java:S1130 - throws declaration is conventional on test method signatures
@SuppressWarnings("java:S1130")
public class TestContextRequiredServer extends AbstractAppEntrypoint {

    private static final RandomPortProvider PORT = new RandomPortProvider(7878);

    private final Client client = ClientBuilder.newClient().register(ProblemCustomReaderWriter.class);

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    @BeforeMethod
    public void setup() {
        TestInit.reset();
    }

    @AfterMethod
    public void cleanup() {
        this.attributes.clear();
    }

    @AfterClass
    public void teardown() {
        this.client.close();
    }

    @Override
    protected ServerBuilder buildServer() {
        return ServerBuilder.create()
                            .application(ContextRequiredApplication.class)
                            // Use a different port for each test just in case one test is slow to teardown the server
                            .port(PORT.newPort())
                            .displayName("Context Required")
                            .withContextAttributes(this.attributes);
    }

    private WebTarget forServer(Server server, String path) {
        return this.client.target(server.getBaseUri()).path(path);
    }

    @DataProvider(name = "noContextRequiredPaths")
    public Object[][] noContextRequiredPaths() {
        return new Object[][] {
                {"/none"},
                {"/version-info"}
        };
    }

    @DataProvider(name = "contextRequiredPaths")
    public Object[][] contextRequiredPaths() {
        return new Object[][] {
                { "/basic" },
                { "/additional"},
                { "/extended/extended"},
                { "/extended/basic"},
                { "/extended/additional"}
        };
    }

    @Test(dataProvider = "noContextRequiredPaths")
    public void givenNoAttributes_whenAccessingResourcesThatDoNotRequireContext_thenOk(String path) throws
            IOException {
        // Given
        ServerBuilder builder = buildServer();
        try (Server server = builder.build()) {
            server.start();

            // When
            WebTarget target = forServer(server, path);
            Invocation.Builder invocation = target.request(MediaType.WILDCARD);

            // Then
            verifyResponse(invocation.get(), Response.Status.OK);
            server.shutdownNow();
        }
    }

    @Test(dataProvider = "contextRequiredPaths")
    public void givenNoAttributes_whenAccessingResourcesThatRequireContext_thenServiceUnavailable(String path) throws
            IOException {
        // Given
        ServerBuilder builder = buildServer();
        try (Server server = builder.build()) {
            server.start();

            // When
            WebTarget target = forServer(server, path);
            Invocation.Builder invocation = target.request(MediaType.WILDCARD);

            // Then
            verifyResponse(invocation.get(), Response.Status.SERVICE_UNAVAILABLE);
            server.shutdownNow();
        }
    }

    @DataProvider(name = "valid")
    private Object[][] validConfigurations() {
        return new Object[][] {
                { "/none", Collections.emptyMap() },
                { "/basic", Map.of("a", new Object()) },
                { "/additional", Map.of("a", new Object(), "map", new HashMap<>()) },
                { "/extended/extended", Map.of("a", "test", "extended", 12345L)},
                { "/extended/extended", Map.of("a", "test", "extended", BigDecimal.valueOf(1.23e4))}
        };
    }

    @DataProvider(name = "invalid")
    private Object[][] invalidConfigurations() {
        return new Object[][] {
                { "/basic", Collections.emptyMap() },
                { "/additional", Map.of("a", new Object(), "map", new ArrayList<>()) },
                { "/additional", Map.of("a", new Object(), "map", new LinkedHashMap<>()) },
                { "/extended/extended", Map.of("a", "test", "extended", 1.23e4)}
        };
    }

    @Test(dataProvider = "valid")
    public void givenValidAttributes_whenAccessingResourcesThatRequireContext_thenOk(String path,
                                                                                     Map<String, Object> attributes) throws
            IOException {
        // Given
        this.attributes.putAll(attributes);
        ServerBuilder builder = buildServer();
        try (Server server = builder.build()) {
            server.start();

            // When
            WebTarget target = forServer(server, path);
            Invocation.Builder invocation = target.request(MediaType.WILDCARD);

            // Then
            verifyResponse(invocation.get(), Response.Status.OK);
            server.shutdownNow();
        }
    }

    @Test(dataProvider = "invalid")
    public void givenInvalidAttributes_whenAccessingResourcesThatRequireContext_thenServiceUnavailable(String path,
                                                                                     Map<String, Object> attributes) throws
            IOException {
        // Given
        this.attributes.putAll(attributes);
        ServerBuilder builder = buildServer();
        try (Server server = builder.build()) {
            server.start();

            // When
            WebTarget target = forServer(server, path);
            Invocation.Builder invocation = target.request(MediaType.WILDCARD);

            // Then
            verifyResponse(invocation.get(), Response.Status.SERVICE_UNAVAILABLE);
            server.shutdownNow();
        }
    }
}
