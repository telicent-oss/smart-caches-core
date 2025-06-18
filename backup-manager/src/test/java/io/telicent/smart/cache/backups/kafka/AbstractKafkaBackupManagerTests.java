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
package io.telicent.smart.cache.backups.kafka;

import io.telicent.smart.cache.backups.BackupManager;
import io.telicent.smart.cache.backups.BackupManagerState;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AbstractKafkaBackupManagerTests {
    protected static final AtomicInteger GROUP_ID = new AtomicInteger(0);

    /**
     * The Kafka test cluster to use, protected so derived tests can change to a different implementation if desired
     */
    protected KafkaTestCluster kafka = new BasicKafkaTestCluster();

    private static void verifySecondaryState(BackupManager manager, BackupManagerState expected) {
        Awaitility.await()
                  .alias("Secondary State Sync")
                  .atMost(Duration.ofSeconds(10))
                  .pollDelay(Duration.ofSeconds(3))
                  .pollInterval(Duration.ofSeconds(1))
                  .until(() -> manager.getState() == expected);
    }

    @BeforeClass
    public void setup() {
        this.kafka.setup();
    }

    @AfterMethod
    public void cleanup() {
        this.kafka.resetTestTopic();
    }

    @AfterClass
    public void teardown() {
        this.kafka.teardown();
    }

    private Consumer<BackupManager> make(Consumer<BackupManager> consumer) {
        return consumer;
    }

    @DataProvider(name = "transitions")
    protected Object[][] transitions() {
        //@formatter:off
        return new Object[][] {
            {
                Collections.<Consumer<BackupManager>>emptyList(), BackupManagerState.STARTING
            },
            {
                List.of(make(BackupManager::startupComplete)), BackupManagerState.READY
            },
            {
                List.of(make(BackupManager::startupComplete),
                        make(BackupManager::startBackup)),
                BackupManagerState.BACKING_UP
            },
            {
                List.of(make(BackupManager::startupComplete),
                        make(BackupManager::startBackup),
                        make(BackupManager::finishBackup)),
                BackupManagerState.READY
            },
            {
                List.of(make(BackupManager::startupComplete),
                        make(BackupManager::startBackup),
                        make(BackupManager::finishBackup),
                        make(BackupManager::startRestore)),
                BackupManagerState.RESTORING
            },
            {
                List.of(make(BackupManager::startRestore),
                        make(BackupManager::finishRestore)),
                BackupManagerState.READY
            }
        };
        //@formatter:on
    }

    protected KafkaEventSource<UUID, BackupTransition> createSource(String groupId) {
        return KafkaEventSource.<UUID, BackupTransition>create()
                               .bootstrapServers(this.kafka.getBootstrapServers())
                               .consumerConfig(this.kafka.getClientProperties())
                               .topic(KafkaTestCluster.DEFAULT_TOPIC)
                               .consumerGroup(groupId)
                               .readPolicy(KafkaReadPolicies.fromEarliest())
                               .commitOnProcessed()
                               .keyDeserializer(UUIDDeserializer.class)
                               .valueDeserializer(BackupTransitionDeserializer.class)
                               .build();
    }

    @NotNull
    protected KafkaSink<UUID, BackupTransition> createSink() {
        return KafkaSink.<UUID, BackupTransition>create()
                        .bootstrapServers(this.kafka.getBootstrapServers())
                        .producerConfig(this.kafka.getClientProperties())
                        .topic(KafkaTestCluster.DEFAULT_TOPIC)
                        .keySerializer(UUIDSerializer.class)
                        .valueSerializer(BackupTransitionSerializer.class)
                        .noLinger()
                        .noAsync()
                        .build();
    }

    @DataProvider(name = "splitTransitions")
    protected Object[][] splitTransitions() {
        //@formatter:off
        return new Object[][] {
            {
                List.of(make(BackupManager::startupComplete),
                        make(BackupManager::startBackup)),
                List.of(make(BackupManager::finishBackup)),
                BackupManagerState.BACKING_UP,
                BackupManagerState.READY,
            },
            {
                List.of(make(BackupManager::startupComplete),
                        make(BackupManager::startBackup)),
                Collections.emptyList(),
                BackupManagerState.BACKING_UP,
                BackupManagerState.BACKING_UP,
            },
            {
                List.of(make(BackupManager::startupComplete),
                        make(BackupManager::startBackup)),
                List.of(make(BackupManager::finishBackup),
                        make(BackupManager::startRestore)),
                BackupManagerState.BACKING_UP,
                BackupManagerState.RESTORING,
            },
            {
                List.of(make(BackupManager::startupComplete),
                        make(BackupManager::startBackup),
                        make(BackupManager::finishBackup)),
                List.of(make(BackupManager::startRestore),
                        make(BackupManager::finishRestore)),
                BackupManagerState.READY,
                BackupManagerState.READY,
            },
        };
        //@formatter:on
    }

    protected void verifySecondaryManager(String application, KafkaEventSource<UUID, BackupTransition> source,
                                          List<Consumer<BackupManager>> transitions, BackupManager primary,
                                          BackupManagerState expectedState) {
        // Given
        try (BackupManager secondary = KafkaSecondaryBackupManager.builder()
                                                                  .application(application)
                                                                  .eventSource(source)
                                                                  .build()) {

            // When
            for (Consumer<BackupManager> transition : transitions) {
                transition.accept(primary);
            }

            // Then
            AbstractKafkaBackupManagerTests.verifySecondaryState(secondary, expectedState);
        }
    }
}
