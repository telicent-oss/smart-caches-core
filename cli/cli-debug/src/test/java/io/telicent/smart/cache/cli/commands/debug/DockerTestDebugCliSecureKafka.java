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
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.kafka.FlakyKafkaTest;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.SecureKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.telicent.smart.cache.cli.commands.debug.TestLogUtil.enableSpecificLogging;

public class DockerTestDebugCliSecureKafka extends AbstractCommandTests {

    private final SecureKafkaTestCluster kafka = new SecureKafkaTestCluster();

    @BeforeClass
    @Override
    public void setup() {
        // Uncomment for easier debugging in IDE
        //SmartCacheCommandTester.TEE_TO_ORIGINAL_STREAMS = true;
        super.setup();
        setupLogging();
        this.kafka.setup();
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
        this.kafka.resetTopic(LiveReporter.DEFAULT_LIVE_TOPIC);

        // Occasionally we can get random test errors because the test topic(s) don't get recreated in time which can
        // lead to incomplete test data being generated in the subsequent test.  A short sleep makes that unlikely to
        // occur.
        Thread.sleep(500);
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
                                                       .producerConfig(this.kafka.getClientProperties(
                                                               this.kafka.getAdminUsername(),
                                                               this.kafka.getAdminPassword()))
                                                       .lingerMs(5)
                                                       .build()) {
            for (int i = 1; i <= 1_000; i++) {
                sink.send(new SimpleEvent<>(headers, Integer.toString(i),
                                            String.format(format, i)));
            }
        }
    }

    @Test
    public void debug_dump_01() {
        DebugCli.main(new String[] {
                "dump",
                "--bootstrap-servers",
                this.kafka.getBootstrapServers(),
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--kafka-user",
                this.kafka.getAdminUsername(),
                "--kafka-password",
                this.kafka.getAdminPassword(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        DockerTestDebugCli.verifyDumpCommandUsed();

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
                "--kafka-user",
                this.kafka.getAdminUsername(),
                "--kafka-password",
                this.kafka.getAdminPassword(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        DockerTestDebugCli.verifyDumpCommandUsed();

        DockerTestDebugCli.verifyEvents("Event %,d");
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "Currently no new events available"));
        Assert.assertFalse(StringUtils.contains(stdErr, "live heartbeats are not being reported anywhere"));
        Assert.assertTrue(StringUtils.contains(stdErr, "Background Live Reporter thread started"));
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
                "--kafka-user",
                this.kafka.getAdminUsername(),
                "--kafka-password",
                this.kafka.getAdminPassword(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        DockerTestDebugCli.verifyRdfDumpCommandUsed();

        DockerTestDebugCli.verifyEvents("\"%d\"");
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
                "--kafka-user",
                this.kafka.getAdminUsername(),
                "--kafka-password",
                this.kafka.getAdminPassword(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING",
                "--output-language",
                "no-such-language"
        });

        DockerTestDebugCli.verifyRdfDumpCommandUsed();

        DockerTestDebugCli.verifyEvents("\"%d\"");
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
                "--kafka-user",
                this.kafka.getAdminUsername(),
                "--kafka-password",
                this.kafka.getAdminPassword(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        DockerTestDebugCli.verifyRdfDumpCommandUsed();

        DockerTestDebugCli.verifyEvents("\"%d\"");
    }
}
