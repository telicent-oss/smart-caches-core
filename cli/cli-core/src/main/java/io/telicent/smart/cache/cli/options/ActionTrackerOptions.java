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

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.NotBlank;
import io.telicent.smart.cache.actions.tracker.*;
import io.telicent.smart.cache.actions.tracker.listeners.ActionTransitionListener;
import io.telicent.smart.cache.actions.tracker.model.*;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;

import java.util.List;
import java.util.UUID;

/**
 * Options pertaining to creating action trackers used to keep action state in sync between cooperating microservices
 */
public class ActionTrackerOptions {

    /**
     * The default actions topic
     */
    public static final String DEFAULT_ACTIONS_TOPIC = "actions";
    /**
     * Environment variable used to configure the backup topic
     */
    public static final String ACTION_TOPIC = "ACTION_TOPIC";

    @Option(name = {
            "--action-bootstrap-server", "--action-bootstrap-servers"
    }, title = "ActionBootstrapServers", description = "Provides a comma separated list of bootstrap servers to use for creating the initial connection to Kafka.  For commands that connect to Kafka anyway this option is unnecessary provided the Kafka source is configured via the --bootstrap-servers option, however for commands that don't require a Kafka connection normally this option is required for the Action Tracker to work correctly.")
    private String backupBootstrapServers = Configurator.get(KafkaConfiguration.BOOTSTRAP_SERVERS);

    @Option(name = { "--action-topic", "--actions-topic" }, description = "Specifies a Kafka topic used to sync action state between cooperating microservices")
    @NotBlank
    private String backupTopic = Configurator.get(new String[] { ACTION_TOPIC }, DEFAULT_ACTIONS_TOPIC);

    @Option(name = "--no-singleton", arity = 0, hidden = true, description = "Disables use of singleton tracker registration which is useful when test commands are running in the same process")
    private boolean disableSingleton = true;

    /**
     * Gets the primary action tracker
     *
     * @param bootstrapServers Kafka Bootstrap Servers
     * @param kafkaOptions     Kafka Options
     * @param application      Application ID
     * @return Primary tracker
     */
    public ActionTracker getPrimary(String bootstrapServers, KafkaConfigurationOptions kafkaOptions,
                                    String application) {
        ActionTracker tracker = PrimaryActionTracker.builder()
                                                    .application(application)
                                                    .sink(KafkaSink.<UUID, ActionTransition>create()
                                                                        .bootstrapServers(
                                                                                selectBootstrapServers(bootstrapServers,
                                                                                                       this.backupBootstrapServers))
                                                                        .topic(this.backupTopic)
                                                                        .producerConfig(
                                                                                kafkaOptions.getAdditionalProperties())
                                                                        .keySerializer(UUIDSerializer.class)
                                                                        .valueSerializer(
                                                                                ActionTransitionSerializer.class)
                                                                        // We want to ensure that any secondary gets informed of
                                                                        // transitions ASAP therefore we disable async send and linger
                                                                        // so any sent transitions will be sent synchronously
                                                                        // This also helps to prevent losing events during a shutdown
                                                                        .noAsync()
                                                                        .noLinger()
                                                                        .build())
                                                    .build();
        if (!this.disableSingleton) {
            ActionTrackerRegistry.setInstance(tracker);
        }
        return tracker;
    }

    /**
     * Selects the first valid bootstrap servers value from the given values
     *
     * @param bootstrapServers Bootstrap server values
     * @return First valid value, or {@code null} if no valid value
     */
    private String selectBootstrapServers(String... bootstrapServers) {
        for (String bootstrapServer : bootstrapServers) {
            if (StringUtils.isNotBlank(bootstrapServer)) {
                return bootstrapServer;
            }
        }
        return null;
    }

    /**
     * Gets the secondary action tracker
     *
     * @param bootstrapServers Kafka Bootstrap Servers
     * @param kafkaOptions     Kafka options
     * @param application      Application ID
     * @param listeners        Action State transition listeners
     * @return Secondary tracker
     */
    public ActionTracker getSecondary(String bootstrapServers, String consumerGroup,
                                      KafkaConfigurationOptions kafkaOptions, String application,
                                      List<ActionTransitionListener> listeners) {
        ActionTracker tracker = SecondaryActionTracker.builder()
                                                      .application(application)
                                                      .listeners(listeners)
                                                      .eventSource(
                                                                   KafkaEventSource.<UUID, ActionTransition>create()
                                                                                   .bootstrapServers(
                                                                                           selectBootstrapServers(
                                                                                                   bootstrapServers,
                                                                                                   this.backupBootstrapServers))
                                                                                   .consumerConfig(
                                                                                           kafkaOptions.getAdditionalProperties())
                                                                                   .topic(this.backupTopic)
                                                                                   .consumerGroup(consumerGroup)
                                                                                   .readPolicy(
                                                                                           KafkaReadPolicies.fromEarliest())
                                                                                   .commitOnProcessed()
                                                                                   .keyDeserializer(
                                                                                           UUIDDeserializer.class)
                                                                                   .valueDeserializer(
                                                                                           ActionTransitionDeserializer.class)
                                                                                   .build())
                                                      .build();
        if (!this.disableSingleton) {
            ActionTrackerRegistry.setInstance(tracker);
        }
        return tracker;
    }
}
