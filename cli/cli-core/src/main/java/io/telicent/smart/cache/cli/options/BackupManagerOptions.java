/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.cache.cli.options;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.NotBlank;
import io.telicent.smart.cache.backups.BackupManager;
import io.telicent.smart.cache.backups.BackupManagerState;
import io.telicent.smart.cache.backups.kafka.*;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Options pertaining to creating backup managers used to keep backup state in sync between cooperating microservices
 */
public class BackupManagerOptions {

    /**
     * The default backups topic
     */
    public static final String DEFAULT_BACKUPS_TOPIC = "backups";

    @Option(name = {
            "--backup-bootstrap-server", "--backup-bootstrap-servers"
    }, title = "BackupBootstrapServers", description = "Provides a comma separated list of bootstrap servers to use for creating the initial connection to Kafka.  For commands that connect to Kafka anyway this option is unnecessary provided the Kafka source is configured via the --bootstrap-servers option, however for commands that don't require a Kafka connection normally this option is required for the Backup Manager to work correctly.")
    private String backupBootstrapServers = Configurator.get(KafkaOptions.BOOTSTRAP_SERVERS);

    @Option(name = "--backup-topic", description = "Specifies a Kafka topic used to sync backup state between cooperating microservices")
    @NotBlank
    private String backupTopic = Configurator.get(new String[] { "BACKUP_TOPIC" }, DEFAULT_BACKUPS_TOPIC);

    /**
     * Gets the primary backup manager
     *
     * @param bootstrapServers Kafka Bootstrap Servers
     * @param kafkaOptions     Kafka Options
     * @param application      Application ID
     * @return Primary backup manager
     */
    public BackupManager getPrimary(String bootstrapServers, KafkaConfigurationOptions kafkaOptions,
                                    String application) {
        return KafkaPrimaryBackupManager.builder()
                                        .application(application)
                                        .sink(KafkaSink.<UUID, BackupTransition>create()
                                                       .bootstrapServers(selectBootstrapServers(bootstrapServers,
                                                                                                this.backupBootstrapServers))
                                                       .topic(this.backupTopic)
                                                       .producerConfig(kafkaOptions.getAdditionalProperties())
                                                       .keySerializer(UUIDSerializer.class)
                                                       .valueSerializer(BackupTransitionSerializer.class)
                                                       // We want to ensure that any secondary gets informed of
                                                       // transitions ASAP therefore we disable async send and linger
                                                       // so any sent transitions will be sent synchronously
                                                       // This also helps to prevent losing events during a shutdown
                                                       .noAsync()
                                                       .noLinger()
                                                       .build())
                                        .build();
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
     * Gets the secondary backup manager
     *
     * @param bootstrapServers Kafka Bootstrap Servers
     * @param kafkaOptions     Kafka options
     * @param application      Application ID
     * @param listeners        Backup State transition listeners
     * @return Secondary backup manager
     */
    public BackupManager getSecondary(String bootstrapServers, String consumerGroup, KafkaConfigurationOptions kafkaOptions,
                                      String application,
                                      List<BiConsumer<BackupManagerState, BackupManagerState>> listeners) {
        return KafkaSecondaryBackupManager.builder()
                                          .application(application)
                                          .listeners(listeners)
                                          .eventSource(KafkaEventSource.<UUID, BackupTransition>create()
                                                                       .bootstrapServers(
                                                                               selectBootstrapServers(bootstrapServers,
                                                                                                      this.backupBootstrapServers))
                                                                       .consumerConfig(
                                                                               kafkaOptions.getAdditionalProperties())
                                                                       .topic(this.backupTopic)
                                                                       .consumerGroup(consumerGroup)
                                                                       .readPolicy(KafkaReadPolicies.fromEarliest())
                                                                       .commitOnProcessed()
                                                                       .keyDeserializer(UUIDDeserializer.class)
                                                                       .valueDeserializer(
                                                                               BackupTransitionDeserializer.class)
                                                                       .build())
                                          .build();
    }
}
