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
 * Options pertaining to creating backup trackers used to keep backup state in sync between cooperating microservices
 */
public class BackupTrackerOptions {

    /**
     * The default backups topic
     */
    public static final String DEFAULT_BACKUPS_TOPIC = "backups";
    /**
     * Environment variable used to configure the backup topic
     */
    public static final String BACKUP_TOPIC = "BACKUP_TOPIC";

    @Option(name = {
            "--backup-bootstrap-server", "--backup-bootstrap-servers"
    }, title = "BackupBootstrapServers", description = "Provides a comma separated list of bootstrap servers to use for creating the initial connection to Kafka.  For commands that connect to Kafka anyway this option is unnecessary provided the Kafka source is configured via the --bootstrap-servers option, however for commands that don't require a Kafka connection normally this option is required for the Backup Tracker to work correctly.")
    private String backupBootstrapServers = Configurator.get(KafkaConfiguration.BOOTSTRAP_SERVERS);

    @Option(name = "--backup-topic", description = "Specifies a Kafka topic used to sync backup state between cooperating microservices")
    @NotBlank
    private String backupTopic = Configurator.get(new String[] { BACKUP_TOPIC }, DEFAULT_BACKUPS_TOPIC);

    @Option(name = "--no-singleton", arity = 0, hidden = true, description = "Disables use of singleton tracker registration which is useful when test commands are running in the same process")
    private boolean disableSingleton = true;

    /**
     * Gets the primary backup tracker
     *
     * @param bootstrapServers Kafka Bootstrap Servers
     * @param kafkaOptions     Kafka Options
     * @param application      Application ID
     * @return Primary backup tracker
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
     * Gets the secondary backup tracker
     *
     * @param bootstrapServers Kafka Bootstrap Servers
     * @param kafkaOptions     Kafka options
     * @param application      Application ID
     * @param listeners        Backup State transition listeners
     * @return Secondary backup tracker
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
