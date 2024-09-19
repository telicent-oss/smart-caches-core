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
package io.telicent.smart.cache.cli.commands.options;

import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import org.apache.kafka.common.config.SaslConfigs;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class TestKafkaOptions extends AbstractCommandTests {

    public static final String TEST_BOOTSTRAP_SERVERS = "test:9092";

    private static void verifyPropertyExists(Properties properties, String key) {
        Assert.assertTrue(properties.containsKey(key), "Missing property " + key);
    }

    private static void verifyPropertyValue(Properties properties, String key, String bar) {
        verifyPropertyExists(properties, key);
        Assert.assertEquals(properties.get(key), bar);
    }

    @Test
    public void givenMinimalKafkaArguments_whenParsing_thenNoAdditionalProperties() {
        // Given
        String[] args = new String[] {
                "--bootstrap-servers", TEST_BOOTSTRAP_SERVERS, "--topic", KafkaTestCluster.DEFAULT_TOPIC
        };

        // When
        SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        Properties properties = getKafkaProperties();
        Assert.assertTrue(properties.isEmpty());
    }

    private static Properties getKafkaProperties() {
        return ((KafkaOptionsCommand) SmartCacheCommandTester.getLastParseResult()
                                                             .getCommand()).kafkaOptions.getAdditionalProperties();
    }

    @Test
    public void givenKafkaUserCredentials_whenParsing_thenAdditionalPropertiesGenerated() {
        // Given
        String[] args = new String[] {
                "--bootstrap-servers",
                TEST_BOOTSTRAP_SERVERS,
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--kafka-user",
                "test",
                "--kafka-password",
                "test"
        };

        // When
        SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        Properties properties = getKafkaProperties();
        Assert.assertFalse(properties.isEmpty());
        verifyPropertyExists(properties, SaslConfigs.SASL_JAAS_CONFIG);
    }

    @Test
    public void givenKafkaUserCredentialsAndLoginType_whenParsing_thenAdditionalPropertiesGenerated() {
        // Given
        String[] args = new String[] {
                "--bootstrap-servers",
                TEST_BOOTSTRAP_SERVERS,
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--kafka-user",
                "test",
                "--kafka-password",
                "test",
                "--kafka-login-type",
                "SCRAM-SHA-256"
        };

        // When
        SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        Properties properties = getKafkaProperties();
        Assert.assertFalse(properties.isEmpty());
        verifyPropertyExists(properties, SaslConfigs.SASL_JAAS_CONFIG);
        Assert.assertEquals(properties.getProperty(SaslConfigs.SASL_MECHANISM), "SCRAM-SHA-256");
    }

    @Test
    public void givenKafkaExtraProperty_whenParsing_thenAdditionalPropertiesGenerated() {
        // Given
        String[] args = new String[] {
                "--bootstrap-servers",
                TEST_BOOTSTRAP_SERVERS,
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--kafka-property",
                "foo",
                "bar"
        };

        // When
        SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        Properties properties = getKafkaProperties();
        Assert.assertFalse(properties.isEmpty());
        verifyPropertyValue(properties, "foo", "bar");
    }

    @Test
    public void givenKafkaExtraProperties_whenParsing_thenAdditionalPropertiesGenerated() {
        // Given
        String[] args = new String[] {
                "--bootstrap-servers",
                TEST_BOOTSTRAP_SERVERS,
                "--topic",
                KafkaTestCluster.DEFAULT_TOPIC,
                "--kafka-property",
                "foo",
                "bar",
                "--kafka-property",
                "key=value",
                "--kafka-property",
                "another",
                "test"
        };

        // When
        SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        Properties properties = getKafkaProperties();
        Assert.assertFalse(properties.isEmpty());
        verifyPropertyValue(properties, "foo", "bar");
        verifyPropertyValue(properties, "key", "value");
        verifyPropertyValue(properties, "another", "test");
    }

    @Test
    public void givenKafkaPropertiesFile_whenParsing_thenAdditionalPropertiesAreLoaded() throws IOException {
        // Given
        Properties original = new Properties();
        original.put("foo", "bar");
        original.put("key", "value");
        original.put("another", "test");
        File temp = Files.createTempFile("kafka", ".properties").toFile();
        try {
            try (FileOutputStream output = new FileOutputStream(temp)) {
                original.store(output, null);
            }
            Assert.assertNotEquals(temp.length(), 0L);
            String[] args = new String[] {
                    "--bootstrap-servers",
                    TEST_BOOTSTRAP_SERVERS,
                    "--topic",
                    KafkaTestCluster.DEFAULT_TOPIC,
                    "--kafka-properties", temp.getAbsolutePath()
            };

            // When
            SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

            // Then
            Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
            Properties properties = getKafkaProperties();
            Assert.assertFalse(properties.isEmpty());
            verifyPropertyValue(properties, "foo", "bar");
            verifyPropertyValue(properties, "key", "value");
            verifyPropertyValue(properties, "another", "test");
        } finally {
            temp.delete();
        }
    }
}
