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
import com.github.rvesse.airline.annotations.restrictions.ranges.IntegerRange;
import com.github.rvesse.airline.annotations.restrictions.ranges.LongRange;
import com.github.rvesse.airline.model.CommandMetadata;
import io.telicent.smart.cache.cli.restrictions.RequiredForSource;
import io.telicent.smart.cache.cli.restrictions.SourceRequired;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

/**
 * Options related to configuring a Kafka Event source
 */
public class KafkaOptions extends KafkaConfigurationOptions {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOptions.class);

    /**
     * Default consumer group used if a more specific one is not set or automatically determined based upon the command
     * being run
     */
    public static final String DEFAULT_CONSUMER_GROUP = "smart-cache";

    @AirlineModule
    private CommandMetadata command;

    /**
     * Kafka bootstrap servers
     */
    @Option(name = {
            "--bootstrap-server", "--bootstrap-servers"
    }, title = "BootstrapServers", description = "Provides a comma separated list of bootstrap servers to use for creating the initial connection to Kafka.")
    @SourceRequired(name = "Kafka", unlessEnvironment = KafkaConfiguration.BOOTSTRAP_SERVERS)
    public String bootstrapServers = Configurator.get(KafkaConfiguration.BOOTSTRAP_SERVERS);

    /**
     * Kafka input topic
     */
    @Option(name = {
            "-t", "--topic"
    }, title = "KafkaTopic", description = "Provides the name of the Kafka topic(s) to read events from.  May be specified multiple times to supply multiple topics, when multiple topics are specified then they must all contain events that can be deserialized by this command otherwise errors will occur!")
    @RequiredForSource(sourceName = "Kafka", unlessEnvironment = {
            KafkaConfiguration.TOPIC, KafkaConfiguration.INPUT_TOPIC
    })
    public Set<String> topics =
            Configurator.get(new String[] { KafkaConfiguration.TOPIC, KafkaConfiguration.INPUT_TOPIC }, t -> {
                Set<String> ts = new LinkedHashSet<>();
                if (t.contains(",")) {
                    // Multiple topics in the configuration value
                    ts.addAll(Arrays.asList(t.split(",")));
                    ts.removeIf(StringUtils::isBlank);
                } else {
                    // Single topic in the configuration value
                    ts.add(t);
                }
                return ts;
            }, new LinkedHashSet<>());

    /**
     * Kafka DLQ Topic
     */
    @Option(name = {
            "-dlq", "--dlq-topic"
    }, title = "KafkaDlqTopic", description = "Provides the name of a Kafka dead letter topic, where events with processing errors will be written.")
    public String dlqTopic = Configurator.get(KafkaConfiguration.DLQ_TOPIC);

    /**
     * Kafka consumer group
     */
    @Option(name = {
            "-g", "--group"
    }, title = "KafkaConsumerGroup", description = "Provides the name of the Kafka Consumer Group to use.  If not set defaults to the name of the command being invoked, or smart-cache if that is unknown.")
    private String group = Configurator.get(KafkaConfiguration.CONSUMER_GROUP);

    /**
     * Lag reporting interval
     */
    @Option(name = "--lag-report-interval", title = "LagReportIntervalSeconds", description = "Specifies how often in seconds the lag for the Kafka topic partitions being read should be calculated and reported.  As calculating lag can be an expensive operation it should not be done too often.  Default is 30 seconds.")
    @LongRange(min = 5)
    private long lagReportInterval =
            Configurator.get(CliEnvironmentVariables.LAG_REPORT_INTERVAL, Long::parseLong, 30L);

    @Option(name = "--kafka-max-poll-records", title = "KafkaMaxPollRecords", description = "Specifies the maximum number of records to retrieve from Kafka in a single poll request.  Defaults to 1,000.")
    @IntegerRange(min = 100)
    private int maxPollRecords = Configurator.get(CliEnvironmentVariables.MAX_POLL_RECORDS, Integer::parseInt, 1000);

    /**
     * Gets the maximum number of records to poll from Kafka in a single request
     *
     * @return Maximum records to poll
     */
    public int getMaxPollRecords() {
        return this.maxPollRecords;
    }

    /**
     * Gets the consumer group to use to take advantage of Kafka's Consumer Group features.
     * <p>
     * This may be provided explicitly by the user, either via the command line {@code -g/--group} option or the
     * {@value KafkaConfiguration#CONSUMER_GROUP} environment variable.  If not provided explicitly then a suitable
     * default will be automatically selected.  This default will be the name of the command being invoked, or if not
     * used in the context of a command then defaults to {@value #DEFAULT_CONSUMER_GROUP}.
     * </p>
     *
     * @return Consumer Group to use
     * @deprecated Use {@link #getConsumerGroup(String)} that allows the application to control the default value
     * explicitly
     */
    @Deprecated(since = "0.29.3")
    public String getConsumerGroup() {
        return getConsumerGroup(null);
    }

    /**
     * Gets the consumer group to use to take advantage of Kafka's Consumer Group features.
     * <p>
     * This may be provided explicitly by the user, either via the command line {@code -g/--group} option or the
     * {@value KafkaConfiguration#CONSUMER_GROUP} environment variable.  If not provided explicitly then the provided
     * default is used (the {@code defaultConsumerGroup} parameter) if present.  If that is not provided then a suitable
     * default will be automatically selected, this auto-selected default will be the name of the command being invoked.
     * Finally, if not used in the context of a command then defaults to {@value #DEFAULT_CONSUMER_GROUP}.
     * </p>
     *
     * @return Consumer Group to use
     */
    public String getConsumerGroup(String defaultConsumerGroup) {
        // Use explicitly configured group if present, otherwise use the application provided default if present
        if (StringUtils.isNotBlank(this.group)) {
            return this.group;
        } else if (StringUtils.isNotBlank(defaultConsumerGroup)) {
            return defaultConsumerGroup;
        }

        // If used in the context of a CLI Command find the name of that command and use that as the consumer group
        if (this.command != null) {
            return this.command.getName();
        }

        // Finally fallback to the generic default value
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
        LATEST,
        /**
         * Read from external source
         */
        EXTERNAL;

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
                case EXTERNAL -> getExternalReadPolicy();
                default -> KafkaReadPolicies.fromBeginning();
            };
        }
    }

    public static <TKey, TValue> KafkaReadPolicy<TKey, TValue> getExternalReadPolicy() {
        // NB - Expectation is that any command that wants to use an external read policy will not call toReadPolicy()
        //      and will instead manually configure the KafkaReadPolicy instance needed
        throw new IllegalArgumentException("External Read Policy is not supported");
    }

    /**
     * Kafka read policy
     */
    @Option(name = "--read-policy", title = "KafkaReadPolicy", description = "Specifies how events should be read from Kafka.  Defaults to EARLIEST i.e. automatically read from the earliest available events that have yet to be consumed by the specified Consumer Group.")
    @AllowedEnumValues(ReadPolicy.class)
    public ReadPolicy readPolicy =
            Configurator.get(CliEnvironmentVariables.KAFKA_READ_POLICY, ReadPolicy::valueOf, ReadPolicy.EARLIEST);
}
