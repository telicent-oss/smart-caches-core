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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class DockerTestDebugCliReplayCommand extends AbstractDockerDebugCliTests {

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenEmptyCaptureDirectory_whenRunningReplayCommand_thenNothingIsReplayed() throws
            IOException {
        // Given
        File captureDir = Files.createTempDirectory("capture").toFile();

        // When
        DebugCli.main(new String[] {
                "replay",
                "--source-directory",
                captureDir.getAbsolutePath(),
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3"
        });

        // Then
        verifyReplayCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "all events have been exhausted"));
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenNonEmptyCaptureDirectory_whenRunningReplayCommand_thenEventsAreReplayed() throws IOException {
        // Given
        File captureDir = Files.createTempDirectory("capture").toFile();
        generateCapturedEvents(captureDir, new YamlFormat(), Collections.emptyList(), "Event %,d");

        // When
        DebugCli.main(new String[] {
                "replay",
                "--source-directory",
                captureDir.getAbsolutePath(),
                "--source-format",
                YamlFormat.NAME,
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3"
        });

        // Then
        verifyReplayCommandUsed();
        verifyReplayedEvents("Event %,d");
    }
}
