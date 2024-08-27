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

import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.cli.commands.projection.debug.Capture;
import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import io.telicent.smart.cache.server.jaxrs.utils.RandomPortProvider;
import io.telicent.smart.cache.sources.kafka.FlakyKafkaTest;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DockerTestDebugCliHealthProbes extends AbstractDockerDebugCliTests {

    private final Client client = ClientBuilder.newClient();
    private static final RandomPortProvider RANDOM_PORT = new RandomPortProvider(13333);

    @BeforeClass
    @Override
    public void setup() {
        //SmartCacheCommandTester.TEE_TO_ORIGINAL_STREAMS = true;
        super.setup();
    }

    @AfterClass
    @Override
    public void teardown() {
        super.teardown();
        client.close();
    }

    public void waitForCommandExit() {
        while (SmartCacheCommandTester.getLastExitStatus() == Integer.MIN_VALUE) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignored
            }
        }
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenCaptureDirectoryAndEmptyInput_whenRunningCaptureCommand_thenHealthProbesAreResponsive() throws
            IOException {
        // Given
        File captureDir = Files.createTempDirectory("capture").toFile();

        // When
        Future<?> future = null;
        ExecutorService executor = null;
        try {
            executor = Executors.newSingleThreadExecutor();
            future = executor.submit(() -> DebugCli.main(new String[] {
                    "capture",
                    "--bootstrap-servers",
                    this.kafka.getBootstrapServers(),
                    "--topic",
                    KafkaTestCluster.DEFAULT_TOPIC,
                    "--max-stalls",
                    "1",
                    "--poll-timeout",
                    "10",
                    "--read-policy",
                    "BEGINNING",
                    "--capture-dir",
                    captureDir.getAbsolutePath(),
                    "--health-probe-port",
                    Integer.toString(RANDOM_PORT.newPort())

            }));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore
            }

            // Then
            verifyCommandUsed(Capture.class);
            WebTarget target = client.target("http://localhost:" + RANDOM_PORT.getPort() + "/version-info");
            Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
            Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
            target = client.target("http://localhost:" + RANDOM_PORT.getPort() + "/healthz");
            HealthStatus status = target.request(MediaType.APPLICATION_JSON_TYPE).get(HealthStatus.class);
            Assert.assertTrue(status.isHealthy());

        } finally {
            if (future != null) {
                future.cancel(true);
            }
            if (executor != null) {
                executor.shutdownNow();
            }
            waitForCommandExit();
        }
    }
}
