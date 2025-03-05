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

import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

public class AbstractCliKafkaIT extends AbstractCommandTests {
    protected static final File DEBUG_SCRIPT = new File("debug.sh");
    protected KafkaTestCluster kafka = new BasicKafkaTestCluster();

    protected File propertiesFile;

    @BeforeClass
    @Override
    public void setup() {
        this.kafka.setup();
        try {
            this.propertiesFile = Files.createTempFile("kafka",".properties").toFile();
            try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
                this.kafka.getClientProperties().store(fos, null);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        super.setup();
    }

    @AfterMethod
    @Override
    public void testCleanup() throws InterruptedException {
        super.testCleanup();

        // Reset topic and wait briefly for Kafka to clean up
        this.kafka.resetTestTopic();
    }

    @AfterClass
    @Override
    public void teardown() {
        super.teardown();
        this.kafka.teardown();
    }

    protected void generateKafkaEvents(Collection<Header> headers, String format) {
        try (KafkaSink<String, String> sink = KafkaSink.<String, String>create()
                                                       .keySerializer(StringSerializer.class)
                                                       .valueSerializer(StringSerializer.class)
                                                       .bootstrapServers(this.kafka.getBootstrapServers())
                                                       .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                       .producerConfig(this.kafka.getClientProperties())
                                                       .lingerMs(5)
                                                       .build()) {
            for (int i = 1; i <= 1_000; i++) {
                sink.send(new SimpleEvent<>(headers, Integer.toString(i), String.format(format, i)));
            }
        }
    }

    protected void dumpStdErrIfFailed() {
        if (SmartCacheCommandTester.getLastExitStatus() != 0) {
            String stdErr = SmartCacheCommandTester.getLastStdErr();
            SmartCacheCommandTester.printToOriginalStdOut("Command Failed, error output below:");
            SmartCacheCommandTester.printToOriginalStdOut(stdErr);
        }
    }
}
