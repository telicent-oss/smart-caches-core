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

import io.telicent.smart.cache.backups.BackupTracker;
import io.telicent.smart.cache.backups.BackupTrackerState;
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

public class AbstractKafkaBackupTrackerTests {
    protected static final AtomicInteger GROUP_ID = new AtomicInteger(0);

    /**
     * The Kafka test cluster to use, protected so derived tests can change to a different implementation if desired
     */
    protected KafkaTestCluster kafka = new BasicKafkaTestCluster();

    private static void verifySecondaryState(BackupTracker tracker, BackupTrackerState expected) {
        Awaitility.await()
                  .alias("Secondary State Sync")
                  .atMost(Duration.ofSeconds(10))
                  .pollDelay(Duration.ofSeconds(3))
                  .pollInterval(Duration.ofSeconds(1))
                  .until(() -> tracker.getState() == expected);
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

    private Consumer<BackupTracker> make(Consumer<BackupTracker> consumer) {
        return consumer;
    }

    @DataProvider(name = "transitions")
    protected Object[][] transitions() {
        //@formatter:off
        return new Object[][] {
            {
                Collections.<Consumer<BackupTracker>>emptyList(), BackupTrackerState.STARTING
            },
            {
                List.of(make(BackupTracker::startupComplete)), BackupTrackerState.READY
            },
            {
                List.of(make(BackupTracker::startupComplete),
                        make(BackupTracker::startBackup)),
                BackupTrackerState.BACKING_UP
            },
            {
                List.of(make(BackupTracker::startupComplete),
                        make(BackupTracker::startBackup),
                        make(BackupTracker::finishBackup)),
                BackupTrackerState.READY
            },
            {
                List.of(make(BackupTracker::startupComplete),
                        make(BackupTracker::startBackup),
                        make(BackupTracker::finishBackup),
                        make(BackupTracker::startRestore)),
                BackupTrackerState.RESTORING
            },
            {
                List.of(make(BackupTracker::startRestore),
                        make(BackupTracker::finishRestore)),
                BackupTrackerState.READY
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
                List.of(make(BackupTracker::startupComplete),
                        make(BackupTracker::startBackup)),
                List.of(make(BackupTracker::finishBackup)),
                BackupTrackerState.BACKING_UP,
                BackupTrackerState.READY,
            },
            {
                List.of(make(BackupTracker::startupComplete),
                        make(BackupTracker::startBackup)),
                Collections.emptyList(),
                BackupTrackerState.BACKING_UP,
                BackupTrackerState.BACKING_UP,
            },
            {
                List.of(make(BackupTracker::startupComplete),
                        make(BackupTracker::startBackup)),
                List.of(make(BackupTracker::finishBackup),
                        make(BackupTracker::startRestore)),
                BackupTrackerState.BACKING_UP,
                BackupTrackerState.RESTORING,
            },
            {
                List.of(make(BackupTracker::startupComplete),
                        make(BackupTracker::startBackup),
                        make(BackupTracker::finishBackup)),
                List.of(make(BackupTracker::startRestore),
                        make(BackupTracker::finishRestore)),
                BackupTrackerState.READY,
                BackupTrackerState.READY,
            },
        };
        //@formatter:on
    }

    protected void verifySecondaryTracker(String application, KafkaEventSource<UUID, BackupTransition> source,
                                          List<Consumer<BackupTracker>> transitions, BackupTracker primary,
                                          BackupTrackerState expectedState) {
        // Given
        try (BackupTracker secondary = KafkaSecondaryBackupTracker.builder()
                                                                  .application(application)
                                                                  .eventSource(source)
                                                                  .build()) {

            // When
            for (Consumer<BackupTracker> transition : transitions) {
                transition.accept(primary);
            }

            // Then
            AbstractKafkaBackupTrackerTests.verifySecondaryState(secondary, expectedState);
        }
    }
}
