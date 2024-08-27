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
package io.telicent.smart.cache.cli.commands.debug;

import com.github.rvesse.airline.parser.ParseResult;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import io.telicent.smart.cache.server.jaxrs.utils.RandomPortProvider;
import io.telicent.smart.cache.sources.kafka.FlakyKafkaTest;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DockerTestDebugCliFakeReporterCommand extends AbstractDockerDebugCliTests {

    private final Client client = ClientBuilder.newClient();
    private static final RandomPortProvider RANDOM_PORT = new RandomPortProvider(11111);

    @AfterClass
    @Override
    public void teardown() {
        super.teardown();

        client.close();
    }

    protected static void verifyFakeReporter(Future<?> task) throws InterruptedException {
        Thread.sleep(250);
        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful(),
                          "Parsing failed:\n" + StringUtils.join(
                                  result.getErrors().stream().map(Throwable::getMessage).toArray(),
                                  "\n"));

        Thread.sleep(5000);
        task.cancel(true);
        Thread.sleep(250);

        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenFakeReporter_whenRunning_thenHealthProbeServerIsAvailable_andFakeReporterIsRunning() throws
            InterruptedException {
        // Given and When
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> task = executor.submit(() -> DebugCli.main(new String[] {
                "fake-reporter",
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--live-reporter-topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--live-reporter-interval",
                "1",
                "--app-name",
                "test",
                "--app-id",
                UUID.randomUUID().toString(),
                "--component-type",
                "mapper",
                "--input-name",
                "input",
                "--input-type",
                "topic",
                "--output-name",
                "output",
                "--output-type",
                "topic",
                "--error-interval",
                "1",
                "--error-chance",
                "1.0",
                "--health-probe-port",
                Integer.toString(RANDOM_PORT.newPort())
        }));

        // Then
        Thread.sleep(250);
        WebTarget target = client.target("http://localhost:" + RANDOM_PORT.getPort() + "/healthz");
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        HealthStatus status = builder.get(HealthStatus.class);
        Assert.assertTrue(status.isHealthy());

        // And
        verifyFakeReporter(task);
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenFakeReporterWithNoErrorChanceAndUnreadyReason_whenRunning_thenHealthProbeServerReportsUnhealthy_andFakeReporterRuns() throws
            InterruptedException {
        // Given and When
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> task = executor.submit(() -> DebugCli.main(new String[] {
                "fake-reporter",
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--live-reporter-topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--live-reporter-interval",
                "1",
                // Leaving most options as their defaults
                "--output-name",
                "output",
                "--output-type",
                "topic",
                "--error-interval",
                "1",
                "--error-chance",
                "0.0",
                "--health-probe-port",
                Integer.toString(RANDOM_PORT.newPort()),
                "--readiness-reason",
                "Unhealthy"
        }));

        // Then
        Thread.sleep(250);
        WebTarget target = client.target("http://localhost:" + RANDOM_PORT.getPort() + "/healthz");
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.get();
        Assert.assertEquals(response.getStatus(), Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        HealthStatus status = response.readEntity(HealthStatus.class);
        Assert.assertFalse(status.isHealthy());
        Assert.assertTrue(status.reasons().stream().anyMatch(r -> StringUtils.containsIgnoreCase(r, "Unhealthy")));

        // And
        verifyFakeReporter(task);
    }

}
