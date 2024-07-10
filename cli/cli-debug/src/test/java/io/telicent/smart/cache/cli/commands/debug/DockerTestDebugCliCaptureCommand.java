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
import io.telicent.smart.cache.sources.file.rdf.RdfFormat;
import io.telicent.smart.cache.sources.file.text.PlainTextFormat;
import io.telicent.smart.cache.sources.file.yaml.YamlFormat;
import io.telicent.smart.cache.sources.kafka.FlakyKafkaTest;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DockerTestDebugCliCaptureCommand extends AbstractDockerDebugCliTests {

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenCaptureDirectoryAndEmptyInput_whenRunningCaptureCommand_thenNothingIsCaptured() throws
            IOException {
        // Given
        File captureDir = Files.createTempDirectory("capture").toFile();

        // When
        DebugCli.main(new String[] {
                "capture",
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING",
                "--capture-dir",
                captureDir.getAbsolutePath()
        });

        // Then
        verifyCaptureCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        // Expecting a DEBUG statement from KafkaEventSource
        Assert.assertTrue(StringUtils.contains(stdErr, "Currently no new events available"));
        // Expecting NOT to receive a WARN statement from LiveReporterOptions
        Assert.assertFalse(StringUtils.contains(stdErr, "live heartbeats are not being reported anywhere"));
        // Expecting an INFO statement from LiveReporter
        Assert.assertTrue(StringUtils.contains(stdErr, "Background Live Reporter thread started"));
        // Expect no captured events
        File[] files = captureDir.listFiles();
        Assert.assertNotNull(files);
        Assert.assertEquals(files.length, 0, "Expected no events to be captured");
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenInputsAndCaptureDirectory_whenRunningCaptureCommand_thenEventsAreCaptured() throws IOException {
        // Given
        generateKafkaEvents("Event %,d");
        File captureDir = Files.createTempDirectory("capture").toFile();

        // When
        DebugCli.main(new String[] {
                "capture",
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING",
                "--capture-dir",
                captureDir.getAbsolutePath()
        });

        // Then
        verifyCaptureCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        // Expecting a DEBUG statement from KafkaEventSource
        Assert.assertTrue(StringUtils.contains(stdErr, "Currently no new events available"));
        // Expecting NOT to receive a WARN statement from LiveReporterOptions
        Assert.assertFalse(StringUtils.contains(stdErr, "live heartbeats are not being reported anywhere"));
        // Expecting an INFO statement from LiveReporter
        Assert.assertTrue(StringUtils.contains(stdErr, "Background Live Reporter thread started"));
        // Verify capture
        verifyCapturedEvents(captureDir, YamlFormat.NAME, "Event %,d");
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenInputsAndCaptureDirectoryAndCaptureFormat_whenRunningCaptureCommand_thenEventsAreCapturedInCorrectFormat() throws
            IOException {
        // Given
        generateKafkaEvents("Event %,d");
        File captureDir = Files.createTempDirectory("capture").toFile();

        // When
        DebugCli.main(new String[] {
                "capture",
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING",
                "--capture-dir",
                captureDir.getAbsolutePath(),
                "--capture-format",
                PlainTextFormat.NAME
        });

        // Then
        verifyCaptureCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        // Expecting a DEBUG statement from KafkaEventSource
        Assert.assertTrue(StringUtils.contains(stdErr, "Currently no new events available"));
        // Expecting NOT to receive a WARN statement from LiveReporterOptions
        Assert.assertFalse(StringUtils.contains(stdErr, "live heartbeats are not being reported anywhere"));
        // Expecting an INFO statement from LiveReporter
        Assert.assertTrue(StringUtils.contains(stdErr, "Background Live Reporter thread started"));
        // Verify capture
        verifyCapturedEvents(captureDir, PlainTextFormat.NAME, "Event %,d");
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenInputsAndCaptureDirectoryAndRdfFormat_whenRunningCaptureCommand_thenEventsAreCapturedInRdfFormat() throws IOException {
        // Given
        generateKafkaEvents("<http://subject> <http://predicate> \"%d\" .");
        File captureDir = Files.createTempDirectory("capture").toFile();

        // When
        DebugCli.main(new String[] {
                "capture",
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING",
                "--capture-dir",
                captureDir.getAbsolutePath(),
                "--capture-format",
                RdfFormat.NAME
        });

        // Then
        verifyCaptureCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        // Expecting a DEBUG statement from KafkaEventSource
        Assert.assertTrue(StringUtils.contains(stdErr, "Currently no new events available"));
        // Expecting NOT to receive a WARN statement from LiveReporterOptions
        Assert.assertFalse(StringUtils.contains(stdErr, "live heartbeats are not being reported anywhere"));
        // Expecting an INFO statement from LiveReporter
        Assert.assertTrue(StringUtils.contains(stdErr, "Background Live Reporter thread started"));
        // Verify capture
        verifyCapturedEvents(captureDir, RdfFormat.NAME, "<http://subject> <http://predicate> \"%d\" .");
    }
}
