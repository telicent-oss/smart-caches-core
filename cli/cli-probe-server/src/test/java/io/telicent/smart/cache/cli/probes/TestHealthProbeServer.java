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
package io.telicent.smart.cache.cli.probes;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.telicent.smart.cache.cli.probes.resources.ReadinessResource;
import io.telicent.smart.cache.server.jaxrs.applications.AbstractAppEntrypoint;
import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import io.telicent.smart.cache.server.jaxrs.model.VersionInfo;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TestHealthProbeServer {

    private final static Random random = new Random();
    private final static AtomicInteger TEST_PORT = new AtomicInteger(1234 + random.nextInt(50));
    private final Client client = ClientBuilder.newClient();
    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractAppEntrypoint.class);
    private final TestLogger readinessLogger = TestLoggerFactory.getTestLogger(ReadinessResource.class);
    private HealthProbeServer server;

    @AfterMethod
    public void testCleanup() {
        this.server.shutdown();
        this.logger.clearAll();
        this.readinessLogger.clearAll();
    }

    @AfterClass
    public void teardown() {
        this.client.close();
    }

    @Test
    public void givenNoDisplayName_whenStartingProbeServer_thenFails() {
        // Given
        this.server = new HealthProbeServer(null, TEST_PORT.incrementAndGet(), null);

        // When
        server.run();

        // Then
        verifyErrorsLogged();
    }

    @Test
    public void givenBadPort_whenStartingProbeServer_thenFails() {
        // Given
        this.server = new HealthProbeServer("Test", -100, null);

        // When
        server.run();

        // Then
        verifyErrorsLogged();
    }

    private void verifyErrorsLogged() {
        Assert.assertNotEquals(logger.getAllLoggingEvents().stream().filter(m -> m.getLevel() == Level.ERROR).count(),
                               0);
    }

    private void verifyNoErrorsLogged() {
        Assert.assertEquals(logger.getAllLoggingEvents().stream().filter(m -> m.getLevel() == Level.ERROR).count(), 0);
    }

    private void verifyReadinessWarningLogged() {
        Assert.assertTrue(readinessLogger.getAllLoggingEvents()
                                         .stream()
                                         .filter(m -> m.getLevel() == Level.WARN)
                                         .anyMatch(m -> StringUtils.containsIgnoreCase(m.getFormattedMessage(),
                                                                                       "unhealthy")));
    }

    private void verifyLiveness(HealthProbeServer entrypoint) {
        // Given
        WebTarget target = client.target(entrypoint.getLivenessUrl());
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);

        // When
        VersionInfo versionInfo = builder.get(VersionInfo.class);

        // Then
        Assert.assertNotNull(versionInfo);
    }

    private HealthStatus verifyReadiness(HealthProbeServer entrypoint, boolean expectedReadiness) {
        // Given
        WebTarget target = client.target(entrypoint.getReadinessUrl());
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);

        // When
        Response response = builder.get();

        // Then
        HealthStatus status = response.readEntity(HealthStatus.class);
        Assert.assertEquals(status.isHealthy(), expectedReadiness);
        if (!expectedReadiness) {
            Assert.assertFalse(status.reasons().isEmpty(), "When reporting unready then reasons should be non-empty");
        }
        return status;
    }

    @Test
    public void givenPartialConfiguration_whenRunningProbeServer_thenSuccess_andServerRespondsToRequests() {
        // Given
        this.server = new HealthProbeServer("Test", TEST_PORT.incrementAndGet(), null);

        // When
        server.run();

        // Then
        verifyNoErrorsLogged();

        // And
        verifyLiveness(server);
        verifyReadiness(server, false);
    }

    @Test
    public void givenAlwaysHealthyConfiguration_whenRunningProbeServer_thenSuccess_andServerRespondsToRequests() {
        // Given
        this.server = new HealthProbeServer("Test", TEST_PORT.incrementAndGet(),
                                            () -> HealthStatus.builder()
                                                              .healthy(true)
                                                              .build());

        // When
        server.run();

        // Then
        verifyNoErrorsLogged();

        // And
        verifyLiveness(server);
        verifyReadiness(server, true);
    }

    @Test
    public void givenAlwaysUnhealthyConfiguration_whenRunningProbeServer_thenSuccess_andServerRespondsToRequests() {
        // Given
        this.server = new HealthProbeServer("Test", TEST_PORT.incrementAndGet(),
                                            () -> HealthStatus.builder()
                                                              .healthy(false)
                                                              .reasons(List.of("Something is wrong"))
                                                              .build());

        // When
        server.run();

        // Then
        verifyNoErrorsLogged();

        // And
        verifyLiveness(server);
        verifyReadiness(server, false);
        verifyReadinessWarningLogged();
    }

    @Test
    public void givenAlwaysNullReadinessCheck_whenRunningProbeServer_thenSuccess_andServerRespondsToRequests() {
        // Given
        this.server = new HealthProbeServer("Test", TEST_PORT.incrementAndGet(),
                                            () -> null);

        // When
        server.run();

        // Then
        verifyNoErrorsLogged();

        // And
        verifyLiveness(server);
        verifyReadiness(server, false);
        verifyReadinessWarningLogged();
    }

    @Test
    public void givenErrorThrowingReadinessCheck_whenRunningProbeServer_thenSuccess_andServerRespondsToRequests() {
        // Given
        this.server = new HealthProbeServer("Test", TEST_PORT.incrementAndGet(),
                                            () -> {throw new RuntimeException("Failed");});

        // When
        server.run();

        // Then
        verifyNoErrorsLogged();

        // And
        verifyLiveness(server);
        verifyReadiness(server, false);
        verifyReadinessWarningLogged();
    }

    @Test
    public void givenIntermittentlyHealthyConfiguration_whenRunningProbeServer_thenSuccess_andServerRespondsToRequests() {
        // Given
        AtomicInteger counter = new AtomicInteger(0);
        this.server = new HealthProbeServer("Test", TEST_PORT.incrementAndGet(), () -> {
            int value = counter.incrementAndGet();
            boolean healthy = value % 2 == 0;
            return HealthStatus.builder()
                               .healthy(healthy)
                               .reasons(healthy ? Collections.emptyList() : List.of("Currently unhealthy"))
                               .build();
        });

        // When
        server.run();

        // Then
        verifyNoErrorsLogged();

        // And
        verifyLiveness(server);
        verifyReadiness(server, false);
        verifyReadiness(server, true);
        verifyReadiness(server, false);
        verifyReadinessWarningLogged();
    }
}
