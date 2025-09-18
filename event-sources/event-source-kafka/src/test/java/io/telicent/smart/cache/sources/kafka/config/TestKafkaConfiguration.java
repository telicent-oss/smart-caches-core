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
package io.telicent.smart.cache.sources.kafka.config;

import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.NullSource;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class TestKafkaConfiguration {

    @AfterMethod
    private void cleanup() {
        Configurator.reset();
    }

    @Test
    public void givenNoConfiguration_whenObtainingKafkaConfig_thenObtained_andInvalidForUse() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        KafkaConfiguration config = KafkaConfiguration.fromConfig().build();

        // Then
        Assert.assertNotNull(config);

        // And
        Assert.assertFalse(config.isValidForInput());
        Assert.assertFalse(config.isValidForOutput());
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*for input.*")
    public void givenNoConfiguration_whenObtainingKafkaConfiguration_thenCannotObtainInputBuilder() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        KafkaConfiguration config = KafkaConfiguration.fromConfig().build();

        // Then
        config.inputBuilder(BytesDeserializer.class, BytesDeserializer.class);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*for output.*")
    public void givenNoConfiguration_whenObtainingKafkaConfiguration_thenCannotObtainOutputBuilder() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        KafkaConfiguration config = KafkaConfiguration.fromConfig().build();

        // Then
        config.outputBuilder(BytesSerializer.class, BytesSerializer.class);
    }

    @Test
    public void givenMinimalInputConfiguration_whenObtainingKafkaConfig_thenValidForInput_andNotValidForOutput() {
        // Given
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        props.put(KafkaConfiguration.INPUT_TOPIC, "input");
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.forInputFromConfig(null, "some-consumer");

        // Then
        Assert.assertTrue(config.isValidForInput());

        // And
        Assert.assertFalse(config.isValidForOutput());
    }

    @Test
    public void givenMinimalOutputConfiguration_whenObtainingKafkaConfig_thenValidForOutput_andNotValidForInput() {
        // Given
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        props.put(KafkaConfiguration.OUTPUT_TOPIC, "output");
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.forOutputFromConfig(null);

        // Then
        Assert.assertTrue(config.isValidForOutput());

        // And
        Assert.assertFalse(config.isValidForInput());
    }

    @Test
    public void givenMinimalConfiguration_whenObtainingKafkaConfig_thenValidForInputAndOutput() {
        // Given
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        props.put(KafkaConfiguration.INPUT_TOPIC, "input");
        props.put(KafkaConfiguration.OUTPUT_TOPIC, "output");
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.forInputOutputFromConfig(null, null, "some-consumer");

        // Then
        Assert.assertTrue(config.isValidForInput());
        config.inputBuilder(BytesDeserializer.class, BytesDeserializer.class);
        Assert.assertTrue(config.isValidForOutput());
        config.outputBuilder(BytesSerializer.class, BytesSerializer.class);
    }

    @Test
    public void givenBootstrapServer_whenObtainingKafkaConfigWithDefaults_thenValidForInputAndOutput() {
        // Given
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.forInputOutputFromConfig("input", "output", "some-consumer");

        // Then
        Assert.assertTrue(config.isValidForInput());
        Assert.assertTrue(config.isValidForOutput());
    }

    @Test
    public void givenConfigurationWithNonExistentPropertiesFile_whenObtainingKafkaConfig_thenNoPropertiesLoaded() {
        // Given
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        props.put(KafkaConfiguration.KAFKA_PROPERTIES_FILE, "/no/such/properties.txt");
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.fromConfig().build();

        // Then
        Assert.assertTrue(config.getClientProperties().isEmpty());
    }

    @Test
    public void givenConfigurationWithNonFilePropertiesFile_whenObtainingKafkaConfig_thenNoPropertiesLoaded() {
        // Given
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        props.put(KafkaConfiguration.KAFKA_PROPERTIES_FILE, new File(".").getAbsolutePath());
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.fromConfig().build();

        // Then
        Assert.assertTrue(config.getClientProperties().isEmpty());
    }

    @Test
    public void givenConfigurationWithNonReadablePropertiesFile_whenObtainingKafkaConfig_thenNoPropertiesLoaded() throws
            IOException {
        // Given
        File propertiesFile = Files.createTempFile("non-readable", ".properties").toFile();
        propertiesFile.setReadable(false, false);
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        props.put(KafkaConfiguration.KAFKA_PROPERTIES_FILE, propertiesFile.getAbsolutePath());
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.fromConfig().build();

        // Then
        Assert.assertTrue(config.getClientProperties().isEmpty());
    }

    private File tempProperties(Properties props) throws IOException {
        File temp = Files.createTempFile("temporary", ".properties").toFile();
        try (FileOutputStream outputStream = new FileOutputStream(temp)) {
            props.store(outputStream, "Generated by unit tests");
        }
        temp.deleteOnExit();
        return temp;
    }

    @Test
    public void givenConfigurationWithValidPropertiesFile_whenObtainingKafkaConfig_thenPropertiesLoaded() throws
            IOException {
        // Given
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
        File kafkaPropsFile = tempProperties(kafkaProps);
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        props.put(KafkaConfiguration.KAFKA_PROPERTIES_FILE, kafkaPropsFile.getAbsolutePath());
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.fromConfig().build();

        // Then
        Assert.assertFalse(config.getClientProperties().isEmpty());
        Assert.assertEquals(config.getClientProperties().getProperty(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG),
                            "false");
    }

    @DataProvider(name = "loginTypes")
    private Object[][] loginTypes() {
        return new Object[][] {
                { KafkaConfiguration.LOGIN_PLAIN },
                { KafkaConfiguration.LOGIN_SCRAM_SHA_256 },
                { KafkaConfiguration.LOGIN_SCRAM_SHA_512 },
        };
    }

    @Test(dataProvider = "loginTypes")
    public void givenConfigurationWithCredentials_whenObtainingKafkaConfig_thenConfiguredForAuthentication(String loginType) {
        // Given
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        props.put(KafkaConfiguration.KAFKA_LOGIN_TYPE, loginType);
        props.put(KafkaConfiguration.INPUT_TOPIC, "input");
        props.put(KafkaConfiguration.KAFKA_USERNAME, "user");
        props.put(KafkaConfiguration.KAFKA_PASSWORD, "pass");
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.forInputFromConfig(null, "consumer-group");
        config.inputBuilder(BytesDeserializer.class, BytesDeserializer.class);

        // Then
        Properties effective = config.effectiveProperties();
        Assert.assertFalse(effective.isEmpty());
        Assert.assertTrue(effective.containsKey(SaslConfigs.SASL_MECHANISM));
        Assert.assertTrue(effective.containsKey(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        Assert.assertTrue(effective.containsKey(SaslConfigs.SASL_JAAS_CONFIG));
    }

    @Test
    public void givenConfigurationWithUsernameOnly_whenObtainingKafkaConfig_thenNotConfiguredForAuthentication() {
        // Given
        Properties props = new Properties();
        props.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        props.put(KafkaConfiguration.INPUT_TOPIC, "input");
        props.put(KafkaConfiguration.KAFKA_USERNAME, "user");
        Configurator.setSingleSource(new PropertiesSource(props));

        // When
        KafkaConfiguration config = KafkaConfiguration.forInputFromConfig(null, "consumer-group");
        config.inputBuilder(BytesDeserializer.class, BytesDeserializer.class);

        // Then
        Properties effective = config.effectiveProperties();
        Assert.assertTrue(effective.isEmpty());
    }
}
