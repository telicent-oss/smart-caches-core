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
import io.telicent.smart.cache.cli.options.KafkaOptions;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import org.apache.kafka.common.config.SaslConfigs;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Set;

public class TestKafkaOptions extends AbstractCommandTests {

    public static final String TEST_BOOTSTRAP_SERVERS = "test:9092";

    private static void verifyPropertyExists(Properties properties, String key) {
        Assert.assertTrue(properties.containsKey(key), "Missing property " + key);
    }

    private static void verifyPropertyValue(Properties properties, String key, String bar) {
        verifyPropertyExists(properties, key);
        Assert.assertEquals(properties.get(key), bar);
    }

    @AfterMethod
    @Override
    public void testCleanup() {
        super.testCleanup();
        Configurator.reset();
    }

    @Test
    public void givenSingleTopicInConfig_whenParsing_thenSingleTopic() {
        // Given
        Properties properties = new Properties();
        properties.put(KafkaConfiguration.TOPIC, KafkaTestCluster.DEFAULT_TOPIC);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        String[] args = new String[] {
                "--bootstrap-server", TEST_BOOTSTRAP_SERVERS
        };
        SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        Assert.assertEquals(getParsedKafkaOptions().topics, Set.of(KafkaTestCluster.DEFAULT_TOPIC));
    }

    @Test
    public void givenMultipleTopicsInConfig_whenParsing_thenMultipleTopics() {
        // Given
        Properties properties = new Properties();
        properties.put(KafkaConfiguration.TOPIC, KafkaTestCluster.DEFAULT_TOPIC + ",other,,another");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        String[] args = new String[] {
                "--bootstrap-server", TEST_BOOTSTRAP_SERVERS
        };
        SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        Assert.assertEquals(getParsedKafkaOptions().topics, Set.of(KafkaTestCluster.DEFAULT_TOPIC, "other", "another"));
    }

    @Test
    public void givenTopicsInOptionsAndConfig_whenParsing_thenMultipleTopics() {
        // Given
        Properties properties = new Properties();
        properties.put(KafkaConfiguration.TOPIC, KafkaTestCluster.DEFAULT_TOPIC);
        Configurator.setSingleSource(new PropertiesSource(properties));
        String[] args = new String[] {
                "--bootstrap-servers", TEST_BOOTSTRAP_SERVERS, "--topic", "other"
        };

        // When
        SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        Assert.assertEquals(getParsedKafkaOptions().topics, Set.of(KafkaTestCluster.DEFAULT_TOPIC, "other"));
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
        Properties properties = getParsedKafkaProperties();
        Assert.assertTrue(properties.isEmpty());
    }

    private static Properties getParsedKafkaProperties() {
        return getParsedKafkaOptions().getAdditionalProperties();
    }

    private static KafkaOptions getParsedKafkaOptions() {
        Assert.assertNotNull(SmartCacheCommandTester.getLastParseResult(), "Should have successfully parsed options");
        Assert.assertNotNull(SmartCacheCommandTester.getLastParseResult().getCommand(),
                             "Should have successfully parsed options and created a command");
        return ((KafkaOptionsCommand) SmartCacheCommandTester.getLastParseResult().getCommand()).kafkaOptions;
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
        Properties properties = getParsedKafkaProperties();
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
        Properties properties = getParsedKafkaProperties();
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
        Properties properties = getParsedKafkaProperties();
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
        Properties properties = getParsedKafkaProperties();
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
                    "--kafka-properties",
                    temp.getAbsolutePath()
            };

            // When
            SmartCacheCommand.runAsSingleCommand(KafkaOptionsCommand.class, args);

            // Then
            Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
            Properties properties = getParsedKafkaProperties();
            Assert.assertFalse(properties.isEmpty());
            verifyPropertyValue(properties, "foo", "bar");
            verifyPropertyValue(properties, "key", "value");
            verifyPropertyValue(properties, "another", "test");
        } finally {
            temp.delete();
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenExternal_throwException() {
        // given
        // when
        // then
        KafkaOptions.ReadPolicy.EXTERNAL.toReadPolicy();
    }
}
