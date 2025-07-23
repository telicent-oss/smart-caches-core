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

import ch.qos.logback.classic.Level;
import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.live.LiveReporter;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.kafka.*;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static io.telicent.smart.cache.cli.commands.debug.TestLogUtil.enableSpecificLogging;
import static org.apache.commons.lang3.Strings.CS;

public class DockerTestDebugCliMutualTlsKafka extends AbstractCommandTests {

    private static final String CLIENT_PROPERTIES_FILE = new File("test-certs/client.properties").getAbsolutePath();
    private final MutualTlsKafkaTestCluster kafka = new MutualTlsKafkaTestCluster();

    @BeforeClass
    @Override
    public void setup() {
        if (CS.contains(System.getProperty("os.name"), "Windows")) {
            throw new SkipException(
                    "These tests cannot run on Windows because the SSL certificates generator script assumes a Posix compatible OS");
        }

        this.kafka.setup();

        // Uncomment for easier debugging in IDE
        //SmartCacheCommandTester.TEE_TO_ORIGINAL_STREAMS = true;
        super.setup();
        setupLogging();
    }

    private void setupLogging() {
        enableSpecificLogging(KafkaEventSource.class, Level.DEBUG);
        enableSpecificLogging(LiveReporter.class, Level.INFO);
    }

    private void teardownLogging() {
        enableSpecificLogging(KafkaEventSource.class, Level.OFF);
        enableSpecificLogging(LiveReporter.class, Level.OFF);
    }


    @AfterMethod
    @Override
    public void testCleanup() throws InterruptedException {
        super.testCleanup();
        this.kafka.resetTestTopic();
    }

    @AfterClass
    @Override
    public void teardown() {
        this.kafka.teardown();
        teardownLogging();
        super.teardown();
    }

    private void generateKafkaEvents(String format) {
        generateKafkaEvents(Collections.emptyList(), format);
    }

    private void generateKafkaEvents(Collection<EventHeader> headers, String format) {
        try (KafkaSink<String, String> sink = KafkaSink.<String, String>create()
                                                       .keySerializer(StringSerializer.class)
                                                       .valueSerializer(StringSerializer.class)
                                                       .bootstrapServers(this.kafka.getBootstrapServers())
                                                       .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                       .producerConfig(this.kafka.getClientProperties())
                                                       .lingerMs(5)
                                                       .build()) {
            for (int i = 1; i <= 1_000; i++) {
                sink.send(new SimpleEvent<>(headers, Integer.toString(i),
                                            String.format(format, i)));
            }
        }
    }


    private void runDumpCommand(String dump, String... extraArgs) {
        List<String> args = new ArrayList<>(List.of(dump,
                                                    "--bootstrap-servers",
                                                    this.kafka.getBootstrapServers(),
                                                    "--topic",
                                                    KafkaTestCluster.DEFAULT_TOPIC,
                                                    "--kafka-properties",
                                                    CLIENT_PROPERTIES_FILE,
                                                    "--max-stalls",
                                                    "1",
                                                    "--poll-timeout",
                                                    "5",
                                                    "--read-policy",
                                                    "BEGINNING",
                                                    "--no-live-reporter",
                                                    "--no-health-probes"));
        if (extraArgs != null && extraArgs.length > 0) {
            args.addAll(Arrays.asList(extraArgs));
        }
        DebugCli.main(args.toArray(new String[0]));

    }

    @Test
    public void givenEmptyTopic_whenDumpingEvents_thenNothingDumped() {
        // Given and When
        runDumpCommand("dump");

        // Then
        AbstractDockerDebugCliTests.verifyDumpCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "Currently no new events available"));
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenNonEmptyTopic_whenDumpingEvents_thenEventsAreDumped() {
        // Given
        generateKafkaEvents("Event %,d");

        // When
        runDumpCommand("dump");

        // Then
        AbstractDockerDebugCliTests.verifyDumpCommandUsed();
        AbstractDockerDebugCliTests.verifyEvents("Event %,d");
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "Currently no new events available"));
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenNonEmptyTopic_whenDumpingRdfEvents_thenEventsAreDumped() {
        // Given
        generateKafkaEvents("<http://subject> <http://predicate> \"%d\" .");

        // When
        runDumpCommand("rdf-dump");

        // Then
        AbstractDockerDebugCliTests.verifyRdfDumpCommandUsed();
        AbstractDockerDebugCliTests.verifyEvents("\"%d\"");
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenNonEmptyTopic_whenDumpingRdfEvents_thenEventsAreDumped_andLiveReporterHeartbeatsAreGenerated() {
        // Given
        generateKafkaEvents("<http://subject> <http://predicate> \"%d\" .");

        // When
        runDumpCommand("rdf-dump", "--live-reporter");

        // Then
        AbstractDockerDebugCliTests.verifyRdfDumpCommandUsed();
        AbstractDockerDebugCliTests.verifyEvents("\"%d\"");

        // And
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "LiveReporter"));
    }
}
