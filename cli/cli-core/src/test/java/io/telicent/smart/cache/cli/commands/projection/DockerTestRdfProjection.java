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
package io.telicent.smart.cache.cli.commands.projection;

import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.jena.riot.WebContent;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class DockerTestRdfProjection extends AbstractCommandTests {

    private static final int TEST_DATA_SIZE = 10;

    /**
     * Intentionally protected so we can extend this test class and run it against different test clusters
     */
    protected KafkaTestCluster kafka = new BasicKafkaTestCluster();

    @BeforeClass
    @Override
    public void setup() {
        this.kafka.setup();
        generateKafkaEvents(List.of(new Header(TelicentHeaders.CONTENT_TYPE, WebContent.contentTypeNTriples)));

        super.setup();
    }

    @AfterMethod
    @Override
    public void testCleanup() {
        super.testCleanup();
    }

    @AfterClass
    @Override
    public void teardown() {
        this.kafka.teardown();

        super.teardown();
    }

    private void generateKafkaEvents(Collection<EventHeader> headers) {
        try (KafkaSink<String, String> sink = KafkaSink.<String, String>create()
                                                       .keySerializer(StringSerializer.class)
                                                       .valueSerializer(StringSerializer.class)
                                                       .bootstrapServers(this.kafka.getBootstrapServers())
                                                       .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                       .producerConfig(this.kafka.getClientProperties())
                                                       .lingerMs(5)
                                                       .build()) {
            for (int i = 1; i <= TEST_DATA_SIZE; i++) {
                sink.send(new SimpleEvent<>(headers, Integer.toString(i),
                                            String.format("<https://example/s> <https://example/p> \"%,d\".", i)));
            }
        }
    }

    protected void runCommand(Class<? extends SmartCacheCommand> commandClass, List<String> extraArgs) throws
            IOException {
        List<String> args = new ArrayList<>();
        //@formatter:off
        CollectionUtils.addAll(args,
                               "--bootstrap-servers",
                               this.kafka.getBootstrapServers(),
                               "--topic",
                               KafkaTestCluster.DEFAULT_TOPIC,
                               "--max-stalls",
                               "1",
                               "--poll-timeout",
                               "5",
                               "--read-policy",
                               "BEGINNING");

        // If there are Kafka Properties needed pass those in via a temporary file
        Properties properties = this.kafka.getClientProperties();
        File configFile = null;
        if (!properties.isEmpty()) {
            configFile = Files.createTempFile("kafka", ".properties").toFile();
            try (FileOutputStream output = new FileOutputStream(configFile)) {
                properties.store(output, null);
            }
            CollectionUtils.addAll(args,"--kafka-properties", configFile.getAbsolutePath());
        }
        CollectionUtils.addAll(args, extraArgs);

        SmartCacheCommand.runAsSingleCommand(commandClass, args.toArray(new String[0]));

        if (configFile != null) {
            configFile.delete();
        }
    }


    @Test
    public void givenRdfCommand_whenProjecting_thenOk()throws IOException {
        // Given
        // Data generated once in setup()

        // When
        runCommand(RdfCommand.class, Collections.emptyList());

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
    }

    @Test
    public void givenRdfCommand_whenProjectingWithCapture_thenCaptured() throws IOException {
        // Given
        // Data generated once in setup()
        File captureDir = Files.createTempDirectory("capture").toFile();

        // When
        runCommand(RdfCommand.class, List.of("--capture-directory", captureDir.getAbsolutePath()));

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        Assert.assertEquals(captureDir.listFiles().length, TEST_DATA_SIZE);
    }
}
