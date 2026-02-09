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
package io.telicent.smart.cache.cli.commands.backup;

import com.github.rvesse.airline.SingleCommand;
import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.cli.options.ActionTrackerOptions;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.apache.commons.lang3.Strings.CS;

public class DockerTestActionTracker extends AbstractCommandTests {

    /**
     * Intentionally protected so we can extend this test class and run it against different test clusters
     */
    protected KafkaTestCluster kafka = new BasicKafkaTestCluster();

    @BeforeClass
    @Override
    public void setup() {
        this.kafka.setup();
        this.kafka.resetTopic(ActionTrackerOptions.DEFAULT_ACTIONS_TOPIC);

        // Uncomment for easier debugging
        //SmartCacheCommandTester.TEE_TO_ORIGINAL_STREAMS = true;
        super.setup();
    }

    @AfterMethod
    @Override
    public void testCleanup() {
        super.testCleanup();

        this.kafka.resetTopic(ActionTrackerOptions.DEFAULT_ACTIONS_TOPIC);
    }

    @AfterClass
    @Override
    public void teardown() {
        this.kafka.teardown();

        super.teardown();
    }

    @Test
    public void givenCooperatingProcesses_whenPrimaryPerformsBackupRestore_thenSecondaryPausesOperations() throws
            ExecutionException, InterruptedException {
        // Given
        //@formatter:off
        List<String> args = List.of("--action-bootstrap-servers",
                                    this.kafka.getBootstrapServers(),
                                    "--action-topic",
                                    KafkaTestCluster.DEFAULT_TOPIC,
                                    "--no-singleton"
                                    // For debugging useful to turn the delays down
                                    /*, "--big-delay", "3", "--small-delay", "1"*/
        );
        //@formatter:on
        BackupPrimary primary = SingleCommand.singleCommand(BackupPrimary.class).parse(args);
        BackupSecondary secondary = SingleCommand.singleCommand(BackupSecondary.class).parse(args);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // When
            Future<Integer> primaryFuture = executor.submit(primary::run);
            Future<Integer> secondaryFuture = executor.submit(secondary::run);
            Assert.assertEquals(primaryFuture.get(), 0);
            Assert.assertEquals(secondaryFuture.get(), 0);

            // Then
            String stdOut = SmartCacheCommandTester.getLastStdOut();
            List<String> primaryLines = findOutputLines(stdOut, primary.tag());
            List<String> secondaryLines = findOutputLines(stdOut, secondary.tag());
            hasLine(primaryLines, "Finished backup");
            hasLine(primaryLines, "Finished restore");
            hasLine(secondaryLines, "Paused");
            hasLine(secondaryLines, "state READY");
            hasLine(secondaryLines, "state PROCESSING");
            Assert.assertTrue(CS.contains(secondaryLines.get(secondaryLines.size() - 2), "Working"));
        } finally {
            executor.shutdownNow();
        }
    }

    private static @NotNull List<String> findOutputLines(String stdOut, String primary) {
        return Arrays.stream(StringUtils.split(stdOut, '\n'))
                     .filter(line -> line.startsWith("[" + primary + "]"))
                     .toList();
    }

    @Test
    public void givenCooperatingProcesses_whenPrimaryPerformsBackupRestore_thenSecondaryPausesOperationsViaCircuitBreaker() throws
            ExecutionException, InterruptedException {
        // Given
        //@formatter:off
        List<String> args = List.of("--action-bootstrap-servers",
                                    this.kafka.getBootstrapServers(),
                                    "--action-topic",
                                    KafkaTestCluster.DEFAULT_TOPIC,
                                    "--no-singleton"
                                    // For debugging useful to turn the delays down
                /*, "--big-delay", "3", "--small-delay", "1"*/
        );
        //@formatter:on
        BackupPrimary primary = SingleCommand.singleCommand(BackupPrimary.class).parse(args);
        BackupCircuitBreaker secondary = SingleCommand.singleCommand(BackupCircuitBreaker.class).parse(args);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // When
            Future<Integer> primaryFuture = executor.submit(primary::run);
            Future<Integer> secondaryFuture = executor.submit(secondary::run);
            Assert.assertEquals(primaryFuture.get(), 0);
            Assert.assertEquals(secondaryFuture.get(), 0);

            // Then
            String stdOut = SmartCacheCommandTester.getLastStdOut();
            List<String> primaryLines = findOutputLines(stdOut, primary.tag());
            List<String> secondaryLines = findOutputLines(stdOut, secondary.tag());
            hasLine(primaryLines, "Finished backup");
            hasLine(primaryLines, "Finished restore");
            hasLine(secondaryLines, "CLOSED");
            hasLine(secondaryLines, "OPEN");
            hasLine(secondaryLines, "Final item count");
            String countLine = secondaryLines.stream().filter(line -> line.contains("Final item count")).findFirst().orElse(null);
            Assert.assertNotNull(countLine);
            int count = Integer.parseInt(countLine.substring(countLine.lastIndexOf(" ") + 1));
            Assert.assertTrue(count > 27, "Should have queued items such that final count is near 30");
        } finally {
            executor.shutdownNow();
        }
    }

    private static void hasLine(List<String> lines, String expected) {
        Assert.assertTrue(lines.stream().anyMatch(line -> CS.contains(line, expected)));
    }
}
