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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CliEnvironmentVariables {
    /**
     * Environment variable that when set to {@code true} increases log level to {@code DEBUG}
     * <p>
     * Equivalent to specifying the {@code --verbose}/{@code --debug} option to a CLI command
     * </p>
     * <p>
     * See also {@link #TRACE} and {@link #QUIET}, if multiple environment variables are specified then the most verbose
     * logging level applies.
     * </p>
     * <p>
     * See also {@link LoggingOptions}.
     * </p>
     */
    public static final String DEBUG = "DEBUG";
    /**
     * Alternative environment variable for {@link #DEBUG} for naming alignment with both permitted CLI options
     * <p>
     * See also {@link LoggingOptions}.
     * </p>
     */
    public static final String VERBOSE = "VERBOSE";
    /**
     * Environment variable that when set to {@code true} increases log level to {@code TRACE}
     * <p>
     * Equivalent to specifying the {@code --trace} option to a CLI command
     * </p>
     * <p>
     * See also {@link #DEBUG} and {@link #QUIET}, if multiple environment variables are specified then the most verbose
     * logging level applies.
     * </p>
     * <p>
     * See also {@link LoggingOptions}.
     * </p>
     */
    public static final String TRACE = "TRACE";
    /**
     * Environment variable that when set to {@code true} decreases log level to {@code WARN}
     * <p>
     * Equivalent to specifying the {@code --quiet} option to a CLI command
     * </p>
     * <p>
     * See also {@link #TRACE} and {@link #DEBUG}, if multiple environment variables are specified then the most verbose
     * logging level applies.
     * </p>
     * <p>
     * See also {@link LoggingOptions}.
     * </p>
     */
    public static final String QUIET = "QUIET";
    /**
     * Environment variable that controls whether runtime information is printed during CLI command startup, value may
     * be {@code true} or {@code false}
     * <p>
     * Equivalent to specifying the {@code --runtime-info}/{@code --no-runtime-info} option to a CLI command
     * </p>
     * <p>
     * See also {@link LoggingOptions}.
     * </p>
     */
    public static final String ENABLE_RUNTIME_INFO = "ENABLE_RUNTIME_INFO";
    /**
     * Environment variable that controls the interval in minutes at which memory information is reported during the
     * life of the CLI command.
     * <p>
     * This only has an effect if {@link #ENABLE_RUNTIME_INFO} is not set to {@code false} and this variable is set to a
     * positive non-zero value.
     * </p>
     * <p>
     * Equivalent to specifying the {@code --memory-info-interval} option to a CLI command.
     * </p>
     * <p>
     * See also {@link LoggingOptions}.
     * </p>
     */
    public static final String MEMORY_INFO_INTERVAL = "MEMORY_INFO_INTERVAL";
    /**
     * Environment variable that controls the lag reporting interval in seconds that is used for
     * {@link io.telicent.smart.cache.sources.kafka.KafkaEventSource} instances
     * <p>
     * Equivalent to specifying the {@code --lag-report-interval} option to a CLI command
     * </p>
     * <p>
     * See also {@link KafkaOptions}.
     * </p>
     */
    public static final String LAG_REPORT_INTERVAL = "LAG_REPORT_INTERVAL";
    /**
     * Environment variable that controls the default
     * {@link org.apache.kafka.clients.consumer.ConsumerConfig#MAX_POLL_RECORDS_CONFIG} that is set on
     * {@link io.telicent.smart.cache.sources.kafka.KafkaEventSource} instances
     * <p>
     * Equivalent to specifying the {@code --kafka-max-poll-records} option to a CLI command
     * </p>
     * <p>
     * See also {@link KafkaOptions}.
     * </p>
     */
    public static final String MAX_POLL_RECORDS = "MAX_POLL_RECORDS";
    /**
     * Environment variable used to configure the action topic which is used to synchronise action state across
     * applications.
     * <p>
     * See also {@link ActionTrackerOptions}.
     * </p>
     */
    public static final String ACTION_TOPIC = "ACTION_TOPIC";
    /**
     * Environment variable used to configure the distribution lifecycle topic which is used to manage distribution
     * lifecycle across the platform.
     * <p>
     * See also {@link DistributionLifecycleTrackerOptions}.
     * </p>
     */
    public static final String DISTRIBUTION_LIFECYCLE_TOPIC = "DISTRIBUTION_LIFECYCLE_TOPIC";
    /**
     * Environment variable used to configure the distribution lifecycle DLQ topic which is used to forward malformed
     * distribution lifecycle events to.
     * <p>
     * See also {@link DistributionLifecycleTrackerOptions}.
     * </p>
     */
    public static final String DISTRIBUTION_LIFECYCLE_DLQ_TOPIC = "DISTRIBUTION_LIFECYCLE_DLQ_TOPIC";
    /**
     * Environment variable that controls the port upon which the health probes server runs if enabled
     * <p>
     * Equivalent to specifying the {@code --health-probe-port} option to a CLI command
     * </p>
     * <p>
     * See also {@link #ENABLE_HEALTH_PROBES}.
     * </p>
     * <p>
     * See also {@link HealthProbeServerOptions}.
     * </p>
     */
    protected static final String HEALTH_PROBES_PORT = "HEALTH_PROBES_PORT";
    /**
     * Environment variable that controls whether a health probe server is run as a part of a CLI command, may be
     * {@code true} or {@code false}
     * <p>
     * Equivalent to specifying the {@code --health-probes} or {@code --no-health-probes} option to a CLI command
     * </p>
     * <p>
     * See also {@link HealthProbeServerOptions}.
     * </p>
     */
    protected static final String ENABLE_HEALTH_PROBES = "ENABLE_HEALTH_PROBES";
    /**
     * Environment variable that controls whether output key compaction is enabled, may be {@code true} or
     * {@code false}
     * <p>
     * Equivalent to specifying the {@code --compact-keys} or {@code --no-compact-keys} option to a CLI command
     * </p>
     * <p>
     * See also {@link CompactOptions}.
     * </p>
     */
    protected static final String ENABLE_COMPACT_KEYS = "ENABLE_COMPACT_KEYS";
    /**
     * Environment variable that controls whether output value compaction is enabled, may be {@code true} or
     * {@code false}
     * <p>
     * Equivalent to specifying the {@code --compact-values} or {@code --no-compact-values} option to a CLI command
     * </p>
     * <p>
     * See also {@link CompactOptions}.
     * </p>
     */
    protected static final String ENABLE_COMPACT_VALUES = "ENABLE_COMPACT_VALUES";
    /**
     * Environment variable that specifies the bootstrap servers for the Kafka cluster to which Actions information
     * goes.  May be used as an alternative to
     * {@link io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration#BOOTSTRAP_SERVERS} if actions should go to
     * a different Kafka cluster than is used for normal application functionality, or if the application wouldn't
     * normally use Kafka.
     * <p>
     * Equivalent to specifying the {@code --action-bootstrap-servers} option to a CLI command
     * </p>
     * <p>
     * See also {@link ActionTrackerOptions}.
     * </p>
     */
    protected static final String ACTION_BOOTSTRAP_SERVERS = "ACTION_BOOTSTRAP_SERVERS";
    /**
     * Environment variable that specifies the bootstrap servers for the Kafka cluster that manages Distribution
     * Lifecycle events.  May be used as an alternative to
     * {@link io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration#BOOTSTRAP_SERVERS} if distribution
     * lifecycle should be managed by a different Kafka cluster than is used for normal application functionality, or if
     * the application wouldn't normally use Kafka.
     * <p>
     * Equivalent to specifying the {@code --dist-lifecycle-bootstrap-servers} option to a CLI command
     * </p>
     * <p>
     * See also {@link DistributionLifecycleTrackerOptions}.
     * </p>
     */
    protected static final String DISTRIBUTION_LIFECYCLE_BOOTSTRAP_SERVERS = "DISTRIBUTION_LIFECYCLE_BOOTSTRAP_SERVERS";

    /**
     * Environment variable that specifies the read policy that controls how events are read from Kafka for a
     * {@link io.telicent.smart.cache.sources.kafka.KafkaEventSource}
     * <p>
     * Equivalent to specifying the {@code --read-policy} option to a CLI command
     * </p>
     * <p>
     * See also {@link KafkaOptions}.
     * </p>
     */
    protected static final String KAFKA_READ_POLICY = "KAFKA_READ_POLICY";
    /**
     * Environment variable that specifies an external offset file for the application
     * <p>
     * Equivalent to specifying the {@code --offsets-file} option to a CLI command
     * </p>
     * <p>
     * See also {@link OffsetStoreOptions}.
     * </p>
     */
    protected static final String OFFSETS_FILE = "OFFSETS_FILE";
}
