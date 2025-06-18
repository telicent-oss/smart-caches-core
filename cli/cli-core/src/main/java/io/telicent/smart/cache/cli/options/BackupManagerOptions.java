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

    @Option(name = "--backup-topic", description = "Specifies a Kafka topic used to sync backup state between cooperating microservices")
    @NotBlank
    private String backupTopic = Configurator.get(new String[] { "BACKUP_TOPIC" }, "backups");

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
                                                       .bootstrapServers(bootstrapServers)
                                                       .topic(this.backupTopic)
                                                       .producerConfig(kafkaOptions.getAdditionalProperties())
                                                       .keySerializer(UUIDSerializer.class)
                                                       .valueSerializer(BackupTransitionSerializer.class)
                                                       .build())
                                        .build();
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
    public BackupManager getSecondary(String bootstrapServers, KafkaOptions kafkaOptions, String application,
                                      List<BiConsumer<BackupManagerState, BackupManagerState>> listeners) {
        return KafkaSecondaryBackupManager.builder()
                                          .application(application)
                                          .listeners(listeners)
                                          .eventSource(KafkaEventSource.<UUID, BackupTransition>create()
                                                                       .bootstrapServers(StringUtils.isNotBlank(
                                                                               bootstrapServers) ? bootstrapServers :
                                                                                         kafkaOptions.bootstrapServers)
                                                                       .consumerConfig(
                                                                               kafkaOptions.getAdditionalProperties())
                                                                       .topic(this.backupTopic)
                                                                       .consumerGroup(kafkaOptions.getConsumerGroup())
                                                                       .readPolicy(KafkaReadPolicies.fromEarliest())
                                                                       .commitOnProcessed()
                                                                       .keyDeserializer(UUIDDeserializer.class)
                                                                       .valueDeserializer(
                                                                               BackupTransitionDeserializer.class)
                                                                       .build())
                                          .build();
    }
}
