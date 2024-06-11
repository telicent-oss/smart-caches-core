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
import com.github.rvesse.airline.parser.ParseResult;
import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.cli.commands.projection.debug.Capture;
import io.telicent.smart.cache.cli.commands.projection.debug.Dump;
import io.telicent.smart.cache.cli.commands.projection.debug.RdfDump;
import io.telicent.smart.cache.cli.options.LiveReporterOptions;
import io.telicent.smart.cache.live.LiveReporter;
import io.telicent.smart.cache.live.model.LiveHeartbeat;
import io.telicent.smart.cache.live.serializers.LiveHeartbeatDeserializer;
import io.telicent.smart.cache.projectors.driver.ProjectorDriver;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.file.FileEventFormatProvider;
import io.telicent.smart.cache.sources.file.FileEventFormats;
import io.telicent.smart.cache.sources.file.FileEventSource;
import io.telicent.smart.cache.sources.file.rdf.RdfFormat;
import io.telicent.smart.cache.sources.file.text.PlainTextFormat;
import io.telicent.smart.cache.sources.file.yaml.YamlFormat;
import io.telicent.smart.cache.sources.kafka.FlakyKafkaTest;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import io.telicent.smart.cache.sources.offsets.file.AbstractJacksonOffsetStore;
import io.telicent.smart.cache.sources.offsets.file.YamlOffsetStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.telicent.smart.cache.cli.commands.debug.TestLogUtil.enableSpecificLogging;

public class DockerTestDebugCli extends AbstractCommandTests {

    private final KafkaTestCluster kafka = new KafkaTestCluster();

    private void setUpLogging() {
        enableSpecificLogging(KafkaEventSource.class, Level.DEBUG);
        enableSpecificLogging(LiveReporterOptions.class, Level.WARN);
        enableSpecificLogging(LiveReporter.class, Level.INFO);
        enableSpecificLogging(ProjectorDriver.class, Level.INFO);
        enableSpecificLogging(AbstractJacksonOffsetStore.class, Level.DEBUG);
    }

    @BeforeClass
    @Override
    public void setup() {
        // Uncomment for easier debugging in IDE
        //SmartCacheCommandTester.TEE_TO_ORIGINAL_STREAMS = true;
        super.setup();

        this.kafka.setup();
        setUpLogging();
    }

    @AfterMethod
    @Override
    public void testCleanup() throws InterruptedException {
        super.testCleanup();

        this.kafka.resetTestTopic();
        this.kafka.resetTopic(LiveReporter.DEFAULT_LIVE_TOPIC);

        // Occasionally we can get random test errors because the test topic(s) don't get recreated in time which can
        // lead to incomplete test data being generated in the subsequent test.  A short sleep makes that unlikely to
        // occur.
        Thread.sleep(500);
    }
    private void teardownLogging() {
        enableSpecificLogging(KafkaEventSource.class, Level.OFF);
        enableSpecificLogging(LiveReporterOptions.class, Level.OFF);
        enableSpecificLogging(LiveReporter.class, Level.OFF);
        enableSpecificLogging(ProjectorDriver.class, Level.OFF);
        enableSpecificLogging(AbstractJacksonOffsetStore.class, Level.OFF);
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

    private void generateKafkaEvents(Collection<Header> headers, String format) {
        try (KafkaSink<String, String> sink = KafkaSink.<String, String>create()
                                                       .keySerializer(StringSerializer.class)
                                                       .valueSerializer(StringSerializer.class)
                                                       .bootstrapServers(this.kafka.getBootstrapServers())
                                                       .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                       .lingerMs(5)
                                                       .build()) {
            for (int i = 1; i <= 1_000; i++) {
                sink.send(new SimpleEvent<>(headers, Integer.toString(i), String.format(format, i)));
            }
        }
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_dump_01() {
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
                "BEGINNING"
        });

        verifyDumpCommandUsed();

        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "Currently no new events available"));
        Assert.assertFalse(StringUtils.contains(stdErr, "live heartbeats are not being reported anywhere"));
        Assert.assertTrue(StringUtils.contains(stdErr, "Background Live Reporter thread started"));
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_dump_02() {
        // Add some sample events to Kafka
        generateKafkaEvents("Event %,d");

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
                "BEGINNING"
        });

        verifyDumpCommandUsed();

        verifyEvents("Event %,d");
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "Currently no new events available"));
        Assert.assertFalse(StringUtils.contains(stdErr, "live heartbeats are not being reported anywhere"));
        Assert.assertTrue(StringUtils.contains(stdErr, "Background Live Reporter thread started"));
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_dump_03() {
        // No input events, source directory that does not contain any events
        // As no Kafka server configured Live Heartbeats won't go to Kafka
        DebugCli.main(new String[] {
                "dump",
                "--source-directory",
                "target",
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        verifyDumpCommandUsed();

        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "all events have been exhausted"));
        Assert.assertTrue(StringUtils.contains(stdErr, "live heartbeats are not being reported anywhere"));
        Assert.assertTrue(StringUtils.contains(stdErr, "Background Live Reporter thread started"));

        verifyHeartbeats(false);
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_dump_04() {
        // No input events, source directory that does not contain any events
        // BUT we explicitly configure the live reporter bootstrap servers so should still get some heartbeats to Kafka
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
                "BEGINNING"
        });

        verifyDumpCommandUsed();

        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "all events have been exhausted"));
        Assert.assertFalse(StringUtils.contains(stdErr, "live heartbeats are not being reported anywhere"));
        Assert.assertTrue(StringUtils.contains(stdErr, "Background Live Reporter thread started"));

        verifyHeartbeats(true);
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenOffsetsFile_whenDumpingTopic_thenOffsetsAreStoredInFile() throws IOException {
        // Given
        generateKafkaEvents("Event %,d");
        File offsetsFile = File.createTempFile("offsets", ".test");

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
                KafkaEventSource.externalOffsetStoreKey(KafkaTestCluster.DEFAULT_TOPIC, 0, "dump")), 10L);
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "no persistent offsets"));
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
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
        Assert.assertNull(
                store.loadOffset(KafkaEventSource.externalOffsetStoreKey(KafkaTestCluster.DEFAULT_TOPIC, 0, "dump")));
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "failed to store offsets"));
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_capture_01() throws IOException {
        File captureDir = Files.createTempDirectory("capture").toFile();
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
    public void debug_capture_02() throws IOException {
        // Add some sample events to Kafka
        generateKafkaEvents("Event %,d");

        File captureDir = Files.createTempDirectory("capture").toFile();
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
    public void debug_capture_03() throws IOException {
        // Add some sample events to Kafka
        generateKafkaEvents("Event %,d");

        File captureDir = Files.createTempDirectory("capture").toFile();
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
    public void debug_capture_04() throws IOException {
        // Add some sample events to Kafka
        generateKafkaEvents("<http://subject> <http://predicate> \"%d\" .");

        File captureDir = Files.createTempDirectory("capture").toFile();
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

    private void verifyHeartbeats(boolean kafkaHeartbeatsExpected) {
        //@formatter:off
        KafkaEventSource<Bytes, LiveHeartbeat> source =
                KafkaEventSource.<Bytes, LiveHeartbeat>create().bootstrapServers(this.kafka.getBootstrapServers())
                                .topic(LiveReporter.DEFAULT_LIVE_TOPIC)
                                .keyDeserializer(BytesDeserializer.class)
                                .valueDeserializer(LiveHeartbeatDeserializer.class)
                                .consumerGroup("debug-cli-tests")
                                .readPolicy(KafkaReadPolicies.fromBeginning())
                                .build();
        //@formatter:on
        try {
            if (kafkaHeartbeatsExpected) {
                // Make sure that some heartbeats were emitted
                Assert.assertNotEquals(source.remaining(), 0L);
            } else {
                // No heartbeats expected
                Assert.assertNull(source.remaining());
            }
        } finally {
            source.close();
        }
    }

    public static void verifyEvents(String format) {
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        for (int i = 1; i <= 1_000; i++) {
            boolean eventFound = StringUtils.contains(stdOut, String.format(format, i));
            if (!eventFound) {
                SmartCacheCommandTester.printToOriginalStdOut(
                        "Missing expected event, command standard error is displayed below:");
                SmartCacheCommandTester.printToOriginalStdOut(SmartCacheCommandTester.getLastStdErr());
                SmartCacheCommandTester.printToOriginalStdOut("\n\n");
            }
            Assert.assertTrue(eventFound, "Missing event " + i);
        }
    }

    private static void verifyCapturedEvents(File captureDir, String captureFormat, String format) {
        FileEventFormatProvider provider = FileEventFormats.get(captureFormat);
        Assert.assertNotNull(provider);

        FileEventSource<Bytes, String> source =
                provider.createSource(new BytesDeserializer(), new StringDeserializer(), captureDir);
        for (int i = 1; i <= 1_000; i++) {
            Event<Bytes, String> event = source.poll(Duration.ofSeconds(1));
            Assert.assertNotNull(event, "Missing event " + i);
            Assert.assertTrue(Objects.equals(event.value(), String.format(format, i)), "Wrong event " + i);
        }
        Assert.assertNull(source.poll(Duration.ofSeconds(1)));
        Assert.assertTrue(source.isExhausted());
        source.close();
    }


    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_rdf_dump_01() {
        // Add some sample events to Kafka
        generateKafkaEvents("<http://subject> <http://predicate> \"%d\" .");

        DebugCli.main(new String[] {
                "rdf-dump",
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        verifyRdfDumpCommandUsed();

        verifyEvents("\"%d\"");
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_rdf_dump_02() {
        // Add some sample events to Kafka
        generateKafkaEvents("<http://subject> <http://predicate> \"%d\" .");

        DebugCli.main(new String[] {
                "rdf-dump",
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
                "--output-language",
                "no-such-language"
        });

        verifyRdfDumpCommandUsed();

        verifyEvents("\"%d\"");
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_rdf_dump_03() {
        // Add some sample events to Kafka
        generateKafkaEvents(List.of(new Header(HttpNames.hContentType, WebContent.ctPatch.getContentTypeStr())),
                            "A <http://subject> <http://predicate> \"%d\" .");

        DebugCli.main(new String[] {
                "rdf-dump",
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        verifyRdfDumpCommandUsed();

        verifyEvents("\"%d\"");
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void givenMalformedRdfEvents_whenDumpingRdf_thenMalformedEventsAreNoted_andKafkaOffsetsAreUpdated() {
        // Given
        // Add some sample malformed events to Kafka
        generateKafkaEvents("<http://malformed> \"%d\" .");
        String consumerGroup = "no-head-of-line-blocking";

        // When
        DebugCli.main(new String[] {
                "rdf-dump",
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
                "--group",
                consumerGroup
        });

        // Then
        verifyRdfDumpCommandUsed();
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "Ignored malformed RDF event"));
        Assert.assertEquals(StringUtils.countMatches(stdErr, "Ignored malformed RDF event"), 1_000);

        // And
        KafkaEventSource<Bytes, Bytes> source = KafkaEventSource.<Bytes, Bytes>create()
                                                                .bootstrapServers(this.kafka.getBootstrapServers())
                                                                .consumerGroup(consumerGroup)
                                                                .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                                .keyDeserializer(BytesDeserializer.class)
                                                                .valueDeserializer(BytesDeserializer.class)
                                                                .build();
        try {
            Assert.assertNull(source.poll(Duration.ofSeconds(1)),
                              "Consumer Group should be at end of topic despite malformed RDF events");
        } finally {
            source.close();
        }
    }

    public static void printStdErrIfFailedUnexpectedly() {
        if (SmartCacheCommandTester.getLastExitStatus() != 0) {
            SmartCacheCommandTester.printToOriginalStdOut(
                    "Command exited with " + SmartCacheCommandTester.getLastExitStatus() + " when 0 was expected, standard error is displayed below:");
            SmartCacheCommandTester.printToOriginalStdOut(SmartCacheCommandTester.getLastStdErr());
            SmartCacheCommandTester.printToOriginalStdOut("\n\n");
        }
    }

    public static void verifyDumpCommandUsed() {
        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        SmartCacheCommand command = result.getCommand();
        Assert.assertNotNull(command);
        Assert.assertTrue(command instanceof Dump);

        printStdErrIfFailedUnexpectedly();
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }

    public static void verifyRdfDumpCommandUsed() {
        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        SmartCacheCommand command = result.getCommand();
        Assert.assertNotNull(command);
        Assert.assertTrue(command instanceof RdfDump);

        printStdErrIfFailedUnexpectedly();
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }

    public static void verifyCaptureCommandUsed() {
        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        SmartCacheCommand command = result.getCommand();
        Assert.assertNotNull(command);
        Assert.assertTrue(command instanceof Capture);

        printStdErrIfFailedUnexpectedly();
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_fake_reporter_01() throws InterruptedException {
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
                "1.0"
        }));

        verifyFakeReporter(task);
    }

    @Test(retryAnalyzer = FlakyKafkaTest.class)
    public void debug_fake_reporter_02() throws InterruptedException {
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
                "0.0"
        }));

        verifyFakeReporter(task);
    }

    private static void verifyFakeReporter(Future<?> task) throws InterruptedException {
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

}
