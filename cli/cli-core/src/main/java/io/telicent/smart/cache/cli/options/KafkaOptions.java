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
package io.telicent.smart.cache.cli.options;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.AllowedEnumValues;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import com.github.rvesse.airline.annotations.restrictions.ranges.IntegerRange;
import com.github.rvesse.airline.annotations.restrictions.ranges.LongRange;
import com.github.rvesse.airline.model.CommandMetadata;
import io.telicent.smart.cache.cli.restrictions.RequiredForSource;
import io.telicent.smart.cache.cli.restrictions.SourceRequired;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.sources.kafka.KafkaSecurity;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * Options related to configuring a Kafka Event source
 */
public class KafkaOptions {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOptions.class);

    /**
     * Environment variable used to specify Kafka bootstrap servers
     */
    public static final String BOOTSTRAP_SERVERS = "BOOTSTRAP_SERVERS";
    /**
     * Environment variable used to specify Kafka topic
     */
    public static final String TOPIC = "TOPIC";
    /**
     * Environment variable used to specify Kafka input topic, may be used interchangeably with {@link #TOPIC}
     */
    public static final String INPUT_TOPIC = "INPUT_TOPIC";
    /**
     * Environment variable used to specify Kafka output dead letter topic, where events with processing errors will be
     * written.
     */
    public static final String DLQ_TOPIC = "DLQ_TOPIC";
    /**
     * Environment variable used to specify Kafka output dead letter topic, where events with processing errors will be
     * written.
     *
     * @deprecated Use corrected constant {@link #DLQ_TOPIC}
     */
    @Deprecated(forRemoval = true)
    public static final String DQL_TOPIC = DLQ_TOPIC;
    /**
     * Environment variable used to specify Kafka consumer group
     */
    public static final String CONSUMER_GROUP = "CONSUMER_GROUP";

    /**
     * Environment variable used to specify Kafka username
     */
    public static final String KAFKA_USERNAME = "KAFKA_USER";
    /**
     * Environment variable used to specify Kafka password
     */
    public static final String KAFKA_PASSWORD = "KAFKA_PASSWORD";
    /**
     * Default consumer group used if a more specific one is not set or automatically determined based upon the command
     * being run
     */
    public static final String DEFAULT_CONSUMER_GROUP = "smart-cache";
    private static final String LOGIN_PLAIN = "PLAIN";
    private static final String LOGIN_SCRAM_SHA_256 = "SCRAM-SHA-256";
    private static final String LOGIN_SCRAM_SHA_512 = "SCRAM-SHA-512";

    @AirlineModule
    private CommandMetadata command;

    /**
     * Kafka bootstrap servers
     */
    @Option(name = {
            "--bootstrap-server",
            "--bootstrap-servers"
    }, title = "BootstrapServers", description = "Provides a comma separated list of bootstrap servers to use for creating the initial connection to Kafka.")
    @SourceRequired(name = "Kafka", unlessEnvironment = BOOTSTRAP_SERVERS)
    public String bootstrapServers = Configurator.get(BOOTSTRAP_SERVERS);

    /**
     * Kafka input topic
     */
    @Option(name = {
            "-t",
            "--topic"
    }, title = "KafkaTopic", description = "Provides the name of the Kafka topic(s) to read events from.  May be specified multiple times to supply multiple topics, when multiple topics are specified then they must all contain events that can be deserialized by this command otherwise errors will occur!")
    @RequiredForSource(sourceName = "Kafka", unlessEnvironment = { TOPIC, INPUT_TOPIC })
    public Set<String> topics = Configurator.get(new String[] { TOPIC, INPUT_TOPIC }, t -> {
        Set<String> ts = new LinkedHashSet<>();
        ts.add(t);
        return ts;
    }, new LinkedHashSet<>());

    /**
     * Kafka DLQ Topic
     */
    @Option(name = {
            "-dlq",
            "--dlq-topic"
    }, title = "KafkaDlqTopic", description = "Provides the name of a Kafka dead letter topic, where events with processing errors will be written.")
    public String dlqTopic = Configurator.get(DLQ_TOPIC);

    /**
     * Kafka consumer group
     */
    @Option(name = {
            "-g",
            "--group"
    }, title = "KafkaConsumerGroup", description = "Provides the name of the Kafka Consumer Group to use.  If not set defaults to the name of the command being invoked, or smart-cache if that is unknown.")
    private String group = null;

    /**
     * Lag reporting interval
     */
    @Option(name = "--lag-report-interval", title = "LagReportIntervalSeconds", description = "Specifies how often in seconds the lag for the Kafka topic partitions being read should be calculated and reported.  As calculating lag can be an expensive operation it should not be done too often.  Default is 30 seconds.")
    @LongRange(min = 5)
    private long lagReportInterval = 30;

    @Option(name = "--kafka-max-poll-records", title = "KafkaMaxPollRecords", description = "Specifies the maximum number of records to retrieve from Kafka in a single poll request.  Defaults to 1,000.")
    @IntegerRange(min = 100)
    private int maxPollRecords = 1000;

    /**
     * Gets the maximum number of records to poll from Kafka in a single request
     *
     * @return Maximum records to poll
     */
    public int getMaxPollRecords() {
        return this.maxPollRecords;
    }

    /**
     * Gets the additional configuration properties to pass to Kafka
     *
     * @return Configuration properties
     */
    public Properties getAdditionalProperties() {
        Properties properties = new Properties();

        // If a Username and Password are provided then configure Kafka properties for login based on those
        if (StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password)) {
            properties.put(SaslConfigs.SASL_MECHANISM, this.loginType);
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                           Objects.equals(this.loginType, LOGIN_PLAIN) ? SecurityProtocol.SASL_PLAINTEXT.name :
                           SecurityProtocol.SASL_SSL.name);
            properties.put(SaslConfigs.SASL_JAAS_CONFIG, Objects.equals(this.loginType, LOGIN_PLAIN) ?
                                                         KafkaSecurity.plainLogin(this.username, this.password) :
                                                         KafkaSecurity.scramLogin(this.username, this.password));
            LOGGER.info("Configured Kafka properties for SASL {} authentication", this.loginType);
        }

        // Load in any command line provided properties
        for (int i = 0; i <= this.extraConfiguration.size() - 2; i += 2) {
            properties.put(this.extraConfiguration.get(i), this.extraConfiguration.get(i + 1));
        }

        // Load in the properties file (if specified)
        if (this.propertiesFile != null) {
            try (FileInputStream input = new FileInputStream(this.propertiesFile)) {
                properties.load(input);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to read user supplied Kafka properties file %s",
                                                         this.propertiesFile.getAbsolutePath()));
            }
        }

        LOGGER.info("Gathered/generated {} Kafka properties based on supplied options", properties.size());

        return properties;
    }

    @Option(name = {
            "--kafka-user", "--kafka-username"
    }, title = "KafkaUser", description = "Specifies the username used to connect to Kafka.  May also be specified via the KAFKA_USER environment variable.")
    private String username = Configurator.get(KAFKA_USERNAME);

    @Option(name = "--kafka-password", title = "KafkaPassword", description = "Specifies the password used to connect to Kafka.  Generally it is better to use the KAFKA_PASSWORD environment variable to supply this instead of supplying it directly at the command line.")
    private String password = Configurator.get(KAFKA_PASSWORD);

    @Option(name = "--kafka-login-type", title = "LoginType", description = "Specifies the Kafka Login Type to use in conjunction with the --kafka-user and --kafka-password arguments, if you use an alternative Kafka authentication mechanism then use --kafka-properties to supply a suitably configured properties file instead.")
    @AllowedRawValues(allowedValues = { LOGIN_PLAIN, LOGIN_SCRAM_SHA_256, LOGIN_SCRAM_SHA_512 })
    private String loginType = LOGIN_PLAIN;

    @Option(name = "--kafka-properties", title = "KafkaPropertiesFile", description = "Specifies a properties file containing Kafka configuration properties to use with Kafka.")
    @com.github.rvesse.airline.annotations.restrictions.File(mustExist = true)
    private File propertiesFile = Configurator.get("KAFKA_PROPERTIES", File::new, null);

    @Option(name = "--kafka-property", title = "KafkaProperty", arity = 2, description = "Specifies a Kafka configuration property to use with Kafka.  These are loaded prior to any properties from a file specified via the --kafka-properties option.")
    private List<String> extraConfiguration = new ArrayList<>();

    /**
     * Gets the consumer group to use to take advantage of Kafka's Consumer Group features.
     * <p>
     * This may be provided explicitly by the user, either via the command line {@code -g/--group} option or the
     * {@value #CONSUMER_GROUP} environment variable.  If not provided explicitly then a suitable default will be
     * automatically selected.  This default will be the name of the command being invoked, or if not used in the
     * context of a command then defaults to {@value #DEFAULT_CONSUMER_GROUP}.
     * </p>
     *
     * @return Consumer Group to use
     */
    public String getConsumerGroup() {
        if (this.group != null) {
            return this.group;
        }

        String envValue = Configurator.get(new String[] { CONSUMER_GROUP }, null);
        if (envValue != null) {
            return envValue;
        }

        if (this.command != null) {
            return this.command.getName();
        }

        return DEFAULT_CONSUMER_GROUP;
    }

    /**
     * Gets the configured lag reporting interval
     *
     * @return Lag reporting interval
     */
    public Duration getLagReportInterval() {
        return Duration.ofSeconds(this.lagReportInterval);
    }

    /**
     * Possible read policies
     */
    public enum ReadPolicy {
        /**
         * Read from beginning
         */
        BEGINNING,
        /**
         * Read from end
         */
        END,
        /**
         * Read from earliest
         */
        EARLIEST,
        /**
         * Read from latest
         */
        LATEST;

        /**
         * Converts into a Kafka Read Policy
         *
         * @param <TKey>   Key Type
         * @param <TValue> Value Type
         * @return Read policy
         */
        public <TKey, TValue> KafkaReadPolicy<TKey, TValue> toReadPolicy() {
            return switch (this) {
                case EARLIEST -> KafkaReadPolicies.fromEarliest();
                case LATEST -> KafkaReadPolicies.fromLatest();
                case END -> KafkaReadPolicies.fromEnd();
                default -> KafkaReadPolicies.fromBeginning();
            };
        }
    }

    /**
     * Kafka read policy
     */
    @Option(name = "--read-policy", title = "KafkaReadPolicy", description = "Specifies how events should be read from Kafka.  Defaults to EARLIEST i.e. automatically read from the earliest available events that have yet to be consumed by the specified Consumer Group.")
    @AllowedEnumValues(ReadPolicy.class)
    public ReadPolicy readPolicy = ReadPolicy.EARLIEST;
}
