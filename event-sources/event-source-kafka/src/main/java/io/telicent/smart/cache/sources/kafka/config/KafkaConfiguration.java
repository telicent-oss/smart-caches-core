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
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaSecurity;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * Represents Kafka Configuration and provides helper methods for building input {@link KafkaEventSource}'s and output
 * {@link KafkaSink}'s
 */
@Builder
@Getter
@ToString
public class KafkaConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfiguration.class);

    /**
     * Environment variable used to specify Kafka username
     */
    public static final String KAFKA_USERNAME = "KAFKA_USER";
    /**
     * Environment variable used to specify Kafka password
     */
    public static final String KAFKA_PASSWORD = "KAFKA_PASSWORD";
    /**
     * Environment variable used to specify desired Kafka login type
     */
    public static final String KAFKA_LOGIN_TYPE = "KAFKA_LOGIN_TYPE";
    /**
     * Environment variable used to specify Kafka bootstrap servers
     */
    public static final String BOOTSTRAP_SERVERS = "BOOTSTRAP_SERVERS";
    /**
     * Environment variable used to specify Kafka topic
     */
    public static final String TOPIC = "TOPIC";
    /**
     * Environment variable used to specify Kafka input topic, may be used interchangeably with {@link #TOPIC} where
     * only Kafka input is supported
     */
    public static final String INPUT_TOPIC = "INPUT_TOPIC";
    /**
     * Environment variable used to specify Kafka output topic, may be used interchangeably with {@link #TOPIC} where
     * only Kafka output is supported
     */
    public static final String OUTPUT_TOPIC = "OUTPUT_TOPIC";
    /**
     * Environment variable used to specify Kafka output dead letter topic, where events with processing errors will be
     * written.
     */
    public static final String DLQ_TOPIC = "DLQ_TOPIC";
    /**
     * Environment variable used to specify Kafka consumer group
     */
    public static final String CONSUMER_GROUP = "CONSUMER_GROUP";
    /**
     * Environment variable used to specify path to a Kafka properties file to supply additional Kafka configuration
     */
    public static final String KAFKA_CONFIG_FILE_PATH = "KAFKA_CONFIG_FILE_PATH";
    /**
     * Environment variable used to specify path to a Kafka properties file to supply additional Kafka configuration
     */
    public static final String KAFKA_PROPERTIES_FILE = "KAFKA_PROPERTIES_FILE";
    public static final String LOGIN_PLAIN = "PLAIN";
    public static final String LOGIN_SCRAM_SHA_256 = "SCRAM-SHA-256";
    public static final String LOGIN_SCRAM_SHA_512 = "SCRAM-SHA-512";

    private final String bootstrapServers, consumerGroup, inputTopic, outputTopic;
    @ToString.Exclude
    private final String username, password;
    @Builder.Default
    private final String loginType = LOGIN_PLAIN;
    @ToString.Exclude
    private final Properties clientProperties;

    /**
     * Gets whether this configuration is valid for input use cases
     *
     * @return True if valid for input, false otherwise
     */
    public boolean isValidForInput() {
        return StringUtils.isNoneBlank(this.bootstrapServers, this.consumerGroup, this.inputTopic);
    }

    /**
     * Gets whether this configuration is valid for output use cases
     *
     * @return True if valid for output, false otherwise
     */
    public boolean isValidForOutput() {
        return StringUtils.isNoneBlank(this.bootstrapServers, this.outputTopic);
    }

    /**
     * Gets a Kafka Event Source builder based on this configuration
     *
     * @param keyDeserializerClass   Key Deserializer class
     * @param valueDeserializerClass Value Deserializer class
     * @param <TKey>                 Key type
     * @param <TValue>               Value type
     * @return Kafka Event Source builder
     * @throws IllegalStateException Thrown if the configuration is invalid for input
     */
    public <TKey, TValue> KafkaEventSource.Builder<TKey, TValue> inputBuilder(
            Class<? extends Deserializer<TKey>> keyDeserializerClass,
            Class<? extends Deserializer<TValue>> valueDeserializerClass) {
        if (!isValidForInput()) {
            throw new IllegalStateException(
                    "Invalid configuration for input, at least Bootstrap Servers, Input topic and Consumer Group MUST be defined");
        }

        Properties finalProperties = effectiveProperties();

        return KafkaEventSource.<TKey, TValue>create()
                               .bootstrapServers(this.bootstrapServers)
                               .consumerGroup(this.consumerGroup)
                               .topic(this.inputTopic)
                               .consumerConfig(finalProperties)
                               .keyDeserializer(keyDeserializerClass)
                               .valueDeserializer(valueDeserializerClass);
    }

    /**
     * Prepares the Kafka properties based on this configuration object
     * <p>
     * This combines any properties supplied via an external properties file and/or supplied when this configuration
     * object was built, plus any authentication specific properties which are generated based upon the username,
     * password and login type configured (if any).
     * </p>
     *
     * @return Prepared properties
     */
    public Properties effectiveProperties() {
        Properties finalProperties = new Properties();
        finalProperties.putAll(this.clientProperties);
        addLoginProperties(finalProperties, this.loginType, this.username, this.password);
        return finalProperties;
    }

    /**
     * Creates a Kafka Sink builder based on this configuration
     *
     * @param keySerializerClass   Key Serializer class
     * @param valueSerializerClass Value Serializer class
     * @param <TKey>               Key type
     * @param <TValue>             Value type
     * @return Kafka Sink Builder
     * @throws IllegalStateException If the configuration is not valid for output
     */
    public <TKey, TValue> KafkaSink.KafkaSinkBuilder<TKey, TValue> outputBuilder(
            Class<? extends Serializer<TKey>> keySerializerClass,
            Class<? extends Serializer<TValue>> valueSerializerClass) {
        if (!isValidForOutput()) {
            throw new IllegalStateException(
                    "Invalid configuration for output, at least Bootstrap Servers and Output Topic MUST be defined");
        }

        Properties finalProperties = effectiveProperties();

        return KafkaSink.<TKey, TValue>create()
                        .bootstrapServers(this.bootstrapServers)
                        .topic(this.outputTopic)
                        .producerConfig(finalProperties)
                        .keySerializer(keySerializerClass)
                        .valueSerializer(valueSerializerClass);
    }

    /**
     * Adds Kafka login properties to the given properties assuming provided {@code username} and {@code password} are
     * not blank
     *
     * @param properties Properties to add to
     * @param loginType  Login type
     * @param username   Username
     * @param password   Password
     */
    public static void addLoginProperties(Properties properties, String loginType, String username, String password) {
        Objects.requireNonNull(properties, "Properties cannot be null");
        // If a Username and Password are provided then configure Kafka properties for login based on those
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            properties.put(SaslConfigs.SASL_MECHANISM, loginType);
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                           Objects.equals(loginType, KafkaConfiguration.LOGIN_PLAIN) ?
                           SecurityProtocol.SASL_PLAINTEXT.name : SecurityProtocol.SASL_SSL.name);
            properties.put(SaslConfigs.SASL_JAAS_CONFIG, Objects.equals(loginType, KafkaConfiguration.LOGIN_PLAIN) ?
                                                         KafkaSecurity.plainLogin(username, password) :
                                                         KafkaSecurity.scramLogin(username, password));
            LOGGER.info("Configured Kafka properties for SASL {} authentication", loginType);
        }
    }

    /**
     * Loads Kafka properties file as specified by configuration
     *
     * @return Kafka Properties file, may be empty if not configured
     */
    private static Properties getPropertiesFromConfig() {
        File propertiesFile =
                Configurator.get(new String[] { KAFKA_CONFIG_FILE_PATH, KAFKA_PROPERTIES_FILE }, File::new, null);
        Properties properties = new Properties();
        if (propertiesFile != null) {
            loadProperties(propertiesFile, properties);
        }
        return properties;
    }

    /**
     * Loads a Kafka properties file
     *
     * @param propertiesFile Properties File
     * @param properties     Properties
     */
    public static void loadProperties(File propertiesFile, Properties properties) {
        Objects.requireNonNull(propertiesFile, "propertiesFile cannot be null");
        if (!propertiesFile.exists() || !propertiesFile.isFile()) {
            LOGGER.error("Properties file {} does not exist or is not a file", propertiesFile.getAbsolutePath());
        } else {
            try (FileInputStream input = new FileInputStream(propertiesFile)) {
                properties.load(input);
                LOGGER.info("Loaded {} Kafka properties from configuration file {}", properties.size(), propertiesFile);
            } catch (IOException e) {
                LOGGER.error("Failed to read Kafka configuration file {}", propertiesFile.getAbsolutePath(), e);
            }
        }
    }

    /**
     * Gets a configuration builder with basic configuration pulled from the
     * {@link io.telicent.smart.cache.configuration.Configurator}
     *
     * @return Configuration builder
     */
    public static KafkaConfiguration.KafkaConfigurationBuilder fromConfig() {
        return KafkaConfiguration.builder()
                                 .bootstrapServers(Configurator.get(BOOTSTRAP_SERVERS))
                                 .clientProperties(getPropertiesFromConfig())
                                 .username(Configurator.get(KAFKA_USERNAME))
                                 .password(Configurator.get(KAFKA_PASSWORD))
                                 .loginType(Configurator.get(new String[] { KAFKA_LOGIN_TYPE }, LOGIN_PLAIN));
    }

    /**
     * Gets configuration for input
     *
     * @param defaultTopic         Default input topic if not specified by configuration
     * @param defaultConsumerGroup Default consumer group if not specified by configuration
     * @return Configuration
     */
    public static KafkaConfiguration forInputFromConfig(String defaultTopic, String defaultConsumerGroup) {
        return fromConfig().inputTopic(Configurator.get(new String[] { TOPIC, INPUT_TOPIC }, defaultTopic))
                           .consumerGroup(Configurator.get(new String[] { CONSUMER_GROUP }, defaultConsumerGroup))
                           .build();
    }

    /**
     * Gets configuration for output
     *
     * @param defaultTopic Default output topic if not specified by configuration
     * @return Configuration
     */
    public static KafkaConfiguration forOutputFromConfig(String defaultTopic) {
        return fromConfig().outputTopic(Configurator.get(new String[] { TOPIC, OUTPUT_TOPIC }, defaultTopic)).build();
    }

    /**
     * Gets configuration for input and output
     *
     * @param defaultInputTopic    Default input topic if not specified by configuration
     * @param defaultOutputTopic   Default output topic if not specified by configuration
     * @param defaultConsumerGroup Default consumer group if not specified by configuration
     * @return Configuration
     */
    public static KafkaConfiguration forInputOutputFromConfig(String defaultInputTopic, String defaultOutputTopic,
                                                              String defaultConsumerGroup) {
        return fromConfig().inputTopic(Configurator.get(new String[] { INPUT_TOPIC }, defaultInputTopic))
                           .outputTopic(Configurator.get(new String[] { OUTPUT_TOPIC }, defaultOutputTopic))
                           .consumerGroup(Configurator.get(new String[] { CONSUMER_GROUP }, defaultConsumerGroup))
                           .build();
    }
}
