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
import io.telicent.smart.cache.cli.options.KafkaOptions;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import org.apache.jena.vocabulary.XSD;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.telicent.smart.cache.cli.commands.debug.DebugCliIT.DEFAULT_TIMEOUT;

public class DebugCliKafkaIT extends AbstractCliKafkaIT {

    @Test
    public void givenKafkaEvents_whenDumpingAndCapturingRdfAsExternalCommand_thenRdfIsDumped_andCaptureCanBeDumped() throws
            IOException {
        // Given
        generateKafkaEvents(Collections.emptyList(), "<https://subject> <https://predicate> \"%d\" .");
        File captureDir = Files.createTempDirectory("capture-target").toFile();
        Map<String, String> env = new HashMap<>();
        env.put("PROJECT_VERSION", SmartCacheCommandTester.detectProjectVersion());

        // When
        Process process =
                SmartCacheCommandTester.runAsExternalCommand(DEBUG_SCRIPT.getAbsolutePath(), env, new String[] {
                        "rdf-dump",
                        "--bootstrap-servers", this.kafka.getBootstrapServers(),
                        "--topic",
                        KafkaTestCluster.DEFAULT_TOPIC,
                        "--kafka-properties",
                        this.propertiesFile.getAbsolutePath(),
                        "--max-stalls",
                        "1",
                        "--poll-timeout",
                        "5",
                        "--read-policy",
                        "BEGINNING",
                        "--capture-dir",
                        captureDir.getAbsolutePath()
                });
        SmartCacheCommandTester.waitForExternalCommand(process, DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        // Then
        verifySuccessfulCommandCompletion();
        TestDebugCli.verifyEventsDumped("\"%d\"");

        // And
        verifyCaptureCanBeDumped(captureDir);
    }

    @Test
    public void givenKafkaEvents_whenDumpingAndCapturingRdfAsExternalCommandConfiguredViaEnvironmentVariables_thenRdfIsDumped_andCaptureCanBeDumped() throws
            IOException {
        // Given
        generateKafkaEvents(Collections.emptyList(), "<https://subject> <https://predicate> \"%d\" .");
        File captureDir = Files.createTempDirectory("capture-target").toFile();
        Map<String, String> env = new HashMap<>();
        env.put("PROJECT_VERSION", SmartCacheCommandTester.detectProjectVersion());
        env.put(KafkaOptions.BOOTSTRAP_SERVERS, this.kafka.getBootstrapServers());
        env.put(KafkaOptions.TOPIC, KafkaTestCluster.DEFAULT_TOPIC);
        env.put(KafkaOptions.KAFKA_PROPERTIES, this.propertiesFile.getAbsolutePath());

        // When
        Process process =
                SmartCacheCommandTester.runAsExternalCommand(DEBUG_SCRIPT.getAbsolutePath(), env, new String[] {
                        "rdf-dump",
                        "--max-stalls",
                        "1",
                        "--poll-timeout",
                        "5",
                        "--read-policy",
                        "BEGINNING",
                        "--capture-dir",
                        captureDir.getAbsolutePath()
                });
        SmartCacheCommandTester.waitForExternalCommand(process, DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        // Then
        verifySuccessfulCommandCompletion();
        TestDebugCli.verifyEventsDumped("\"%d\"");

        // And
        verifyCaptureCanBeDumped(captureDir);
    }

    private void verifyCaptureCanBeDumped(File captureDir) throws IOException {
        SmartCacheCommandTester.resetTestState();
        Map<String, String> env = new HashMap<>();
        env.put("PROJECT_VERSION", SmartCacheCommandTester.detectProjectVersion());
        Process process = SmartCacheCommandTester.runAsExternalCommand(DEBUG_SCRIPT.getAbsolutePath(), env, new String[] {
                "dump",
                "--source-dir",
                captureDir.getAbsolutePath(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });
        SmartCacheCommandTester.waitForExternalCommand(process, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        verifySuccessfulCommandCompletion();
        TestDebugCli.verifyEventsDumped("\"%d\"");
    }

    @Test
    public void givenCaptureDirectory_whenReplayingToKafkaAsExternalCommand_thenRdfCanBeDumpedFromKafka() throws
            IOException {
        // Given
        File sourceDir = Files.createTempDirectory("dump-events-input").toFile();
        TestDebugCli.generateSampleEvents(sourceDir, "<https://subject> <https://predicate> \"%d\"^^<" + XSD.integer.getURI() + "> .");
        Map<String, String> env = new HashMap<>();
        env.put("PROJECT_VERSION", SmartCacheCommandTester.detectProjectVersion());

        // When
        Process process =
                SmartCacheCommandTester.runAsExternalCommand(DEBUG_SCRIPT.getAbsolutePath(), env, new String[] {
                        "replay",
                        "--source-dir",
                        sourceDir.getAbsolutePath(),
                        "--max-stalls",
                        "1",
                        "--poll-timeout",
                        "3",
                        "--bootstrap-servers",
                        this.kafka.getBootstrapServers(),
                        "--topic",
                        KafkaTestCluster.DEFAULT_TOPIC,
                        "--kafka-properties",
                        this.propertiesFile.getAbsolutePath(),
                });
        SmartCacheCommandTester.waitForExternalCommand(process, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        verifySuccessfulCommandCompletion();

        // Then
        SmartCacheCommandTester.resetTestState();
        process = SmartCacheCommandTester.runAsExternalCommand(DEBUG_SCRIPT.getAbsolutePath(), env, new String[] {
                "rdf-dump",
                "--bootstrap-server",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--kafka-properties",
                this.propertiesFile.getAbsolutePath(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "5",
                "--read-policy",
                "BEGINNING"
        });
        SmartCacheCommandTester.waitForExternalCommand(process, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        verifySuccessfulCommandCompletion();
        TestDebugCli.verifyEventsDumped("%d");
    }

    private void verifySuccessfulCommandCompletion() {
        dumpStdErrIfFailed();
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }
}
