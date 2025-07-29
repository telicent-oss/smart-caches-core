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
import io.telicent.smart.cache.cli.commands.projection.debug.Dump;
import io.telicent.smart.cache.sources.kafka.FlakyKafkaTest;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.offsets.file.YamlOffsetStore;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.Strings.CS;

public class DockerTestDebugCliDumpCommand extends AbstractDockerDebugCliTests {

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenEmptyInput_whenRunningDumpCommand_thenNothingIsDumped() {
        // Given
        // No inputs

        // When
        DebugCli.main(new String[] {
                "dump",
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
                "--no-health-probes"
        });

        // Then
        verifyDumpCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "Currently no new events available"));
        Assert.assertFalse(CS.contains(stdErr, "live heartbeats are not being reported anywhere"));
        Assert.assertTrue(CS.contains(stdErr, "Background Live Reporter thread started"));
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenInputs_whenRunningDumpCommand_thenEventsAreDumped() {
        // Given
        generateKafkaEvents("Event %,d");

        // When
        DebugCli.main(new String[] {
                "dump",
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
                "--no-health-probes"
        });

        // Then
        verifyDumpCommandUsed();
        verifyEvents("Event %,d");
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "Currently no new events available"));
        Assert.assertFalse(CS.contains(stdErr, "live heartbeats are not being reported anywhere"));
        Assert.assertTrue(CS.contains(stdErr, "Background Live Reporter thread started"));
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenNoInputsAndNonKafkaSource_whenRunningDumpCommand_thenNothingIsDumped_andNoLiveHeartbeats() {
        // Given
        // No input events, source directory that does not contain any events

        // When
        DebugCli.main(new String[] {
                "dump",
                "--source-directory",
                "target",
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING",
                "--no-health-probes"
        });

        // Then
        verifyDumpCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "all events have been exhausted"));

        // And
        Assert.assertTrue(CS.contains(stdErr, "live heartbeats are not being reported anywhere"));
        Assert.assertTrue(CS.contains(stdErr, "Background Live Reporter thread started"));
        verifyHeartbeats(false);
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenNoInputsAndLiveHeartbeatsEnabled_whenRunningDumpCommand_thenNothingIsDumped_andLiveHeartbeatsAreProduced() {
        // Given
        // No input events, source directory that does not contain any events

        // When
        DebugCli.main(new String[] {
                "dump",
                "--source-directory",
                "target",
                "--live-bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING",
                "--no-health-probes"
        });

        // Then
        verifyDumpCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "all events have been exhausted"));

        // And
        Assert.assertFalse(CS.contains(stdErr, "live heartbeats are not being reported anywhere"));
        Assert.assertTrue(CS.contains(stdErr, "Background Live Reporter thread started"));
        verifyHeartbeats(true);
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenOffsetsFile_whenDumpingTopic_thenOffsetsAreStoredInFile() throws IOException {
        // Given
        generateKafkaEvents("Event %,d");
        File offsetsFile = File.createTempFile("offsets", ".yml");

        // When
        DebugCli.main(new String[] {
                "dump",
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
                "--no-live-reporter",
                "--no-health-probes",
                // Specify our offsets file
                "--offsets-file",
                offsetsFile.getAbsolutePath(),
                // Read at most 10 events so that we'll have a predictable offset written to our offsets file
                "--limit",
                "10"
        });

        // Then
        verifyDumpCommandUsed();
        YamlOffsetStore store = new YamlOffsetStore(offsetsFile);
        Assert.assertEquals(store.<Long>loadOffset(
                KafkaEventSource.externalOffsetStoreKey(KafkaTestCluster.DEFAULT_TOPIC, 0, "smart-cache-debug-dump")), 10L);
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "no persistent offsets"));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenUnusableOffsetsFile_whenDumpingTopic_thenOffsetsAreNotStored() throws IOException {
        // Given
        generateKafkaEvents("Event %,d");
        File offsetsFile = new File("/no/such/path/to/unusable/offsets.file");

        // When
        DebugCli.main(new String[] {
                "dump",
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
                "--no-live-reporter",
                "--no-health-probes",
                // Specify our offsets file
                "--offsets-file",
                offsetsFile.getAbsolutePath(),
                // Read at most 10 events so that we'll have a predictable offset written to our offsets file
                "--limit",
                "10"
        });

        // Then
        verifyCommandUsed(Dump.class);
        Assert.assertEquals(1, SmartCacheCommandTester.getLastExitStatus());
        YamlOffsetStore store = new YamlOffsetStore(offsetsFile);
        Assert.assertNull(
                store.loadOffset(KafkaEventSource.externalOffsetStoreKey(KafkaTestCluster.DEFAULT_TOPIC, 0, "dump")));
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "failed to store offsets"));
    }
}
