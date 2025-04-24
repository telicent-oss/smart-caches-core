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
import io.telicent.smart.cache.cli.commands.projection.debug.Replay;
import io.telicent.smart.cache.cli.options.LiveReporterOptions;
import io.telicent.smart.cache.live.LiveReporter;
import io.telicent.smart.cache.live.model.LiveHeartbeat;
import io.telicent.smart.cache.live.serializers.LiveHeartbeatDeserializer;
import io.telicent.smart.cache.projectors.driver.ProjectorDriver;
import io.telicent.smart.cache.projectors.sinks.events.file.EventCapturingSink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.file.FileEventFormatProvider;
import io.telicent.smart.cache.sources.file.FileEventFormats;
import io.telicent.smart.cache.sources.file.FileEventSource;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import io.telicent.smart.cache.sources.offsets.file.AbstractJacksonOffsetStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static io.telicent.smart.cache.cli.commands.debug.TestLogUtil.enableSpecificLogging;

public class AbstractDockerDebugCliTests extends AbstractCommandTests {

    static {
        JenaSystem.init();
    }

    protected final KafkaTestCluster kafka = new BasicKafkaTestCluster();

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

    public void verifyReplayedEvents(String format) {
        KafkaEventSource<String, String> source = null;
        try {
            source = KafkaEventSource.<String, String>create()
                                     .bootstrapServers(
                                             this.kafka.getBootstrapServers())
                                     .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                     .consumerGroup("replay-verification")
                                     .readPolicy(KafkaReadPolicies.fromBeginning())
                                     .keyDeserializer(StringDeserializer.class)
                                     .valueDeserializer(StringDeserializer.class)
                                     .build();
            for (int i = 1; i <= 1_000; i++) {
                Event<String, String> event = source.poll(Duration.ofSeconds(3));
                Assert.assertNotNull(event, "Missing event " + i);
                Assert.assertTrue(StringUtils.contains(event.value(), String.format(format, i)),
                                  "Wrong value for event " + i);
            }
        } finally {
            if (source != null) {
                source.close();
            }
        }
    }

    protected static void verifyCapturedEvents(File captureDir, String captureFormat, String format) {
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

    public static void printStdErrIfFailedUnexpectedly() {
        if (SmartCacheCommandTester.getLastExitStatus() != 0) {
            SmartCacheCommandTester.printToOriginalStdOut(
                    "Command exited with " + SmartCacheCommandTester.getLastExitStatus() + " when 0 was expected, standard error is displayed below:");
            SmartCacheCommandTester.printToOriginalStdOut(SmartCacheCommandTester.getLastStdErr());
            SmartCacheCommandTester.printToOriginalStdOut("\n\n");
        }
    }

    public static void verifyDumpCommandUsed() {
        verifyCommandUsed(Dump.class);

        AbstractDockerDebugCliTests.printStdErrIfFailedUnexpectedly();
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }

    public static void verifyCommandUsed(Class<?> expectedCommandClass) {
        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        SmartCacheCommand command = result.getCommand();
        Assert.assertNotNull(command);
        Assert.assertEquals(command.getClass(), expectedCommandClass);
    }

    public static void verifyRdfDumpCommandUsed() {
        verifyCommandUsed(RdfDump.class);

        AbstractDockerDebugCliTests.printStdErrIfFailedUnexpectedly();
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }

    public static void verifyCaptureCommandUsed() {
        verifyCommandUsed(Capture.class);

        AbstractDockerDebugCliTests.printStdErrIfFailedUnexpectedly();
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }

    public static void verifyReplayCommandUsed() {
        verifyCommandUsed(Replay.class);

        AbstractDockerDebugCliTests.printStdErrIfFailedUnexpectedly();
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }

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
        this.kafka.setup();

        // Uncomment for easier debugging in IDE
        //SmartCacheCommandTester.TEE_TO_ORIGINAL_STREAMS = true;
        super.setup();

        setUpLogging();
    }

    @AfterMethod
    @Override
    public void testCleanup() throws InterruptedException {
        super.testCleanup();

        this.kafka.resetTestTopic();
        this.kafka.resetTopic(LiveReporter.DEFAULT_LIVE_TOPIC);
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

    protected void generateKafkaEvents(String format) {
        generateKafkaEvents(Collections.emptyList(), format);
    }

    protected void generateKafkaEvents(Collection<EventHeader> headers, String format) {
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

    protected void generateCapturedEvents(File captureDir, FileEventFormatProvider provider, Collection<EventHeader> headers,
                                          String format) {
        try (EventCapturingSink<String, String> sink = EventCapturingSink.<String, String>create()
                                                                         .directory(captureDir)
                                                                         .extension(provider.defaultFileExtension())
                                                                         .writer(provider.createWriter(
                                                                                 new StringSerializer(),
                                                                                 new StringSerializer()))
                                                                         .discard()
                                                                         .build()) {
            for (int i = 1; i <= 1_000; i++) {
                sink.send(new SimpleEvent<>(headers, Integer.toString(i), String.format(format, i)));
            }
        }
    }

    protected void verifyHeartbeats(boolean kafkaHeartbeatsExpected) {
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
}
