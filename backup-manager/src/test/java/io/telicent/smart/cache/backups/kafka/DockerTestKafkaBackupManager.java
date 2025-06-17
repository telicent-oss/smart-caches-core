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
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DockerTestKafkaBackupManager {

    private static final AtomicInteger GROUP_ID = new AtomicInteger(0);

    /**
     * The Kafka test cluster to use, protected so derived tests can change to a different implementation if desired
     */
    protected KafkaTestCluster kafka = new BasicKafkaTestCluster();

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
    private Object[][] transitions() {
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


    @Test(dataProvider = "transitions")
    public void givenKafkaBackupManagers_whenTransitioningStates_thenStateManagersAreSynced(
            List<Consumer<BackupManager>> transitions, BackupManagerState expectedFinalState) {
        // Given
        try (KafkaSink<UUID, BackupTransition> sink = KafkaSink.<UUID, BackupTransition>create()
                                                               .bootstrapServers(this.kafka.getBootstrapServers())
                                                               .producerConfig(this.kafka.getClientProperties())
                                                               .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                               .keySerializer(UUIDSerializer.class)
                                                               .valueSerializer(BackupTransitionSerializer.class)
                                                               .noLinger()
                                                               .noAsync()
                                                               .build()) {
            //@formatter:off
            KafkaEventSource<UUID, BackupTransition> source
                    = KafkaEventSource.<UUID, BackupTransition>create()
                                      .bootstrapServers(this.kafka.getBootstrapServers())
                                      .consumerConfig(this.kafka.getClientProperties())
                                      .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                      .consumerGroup("test-backup-manager" + GROUP_ID.incrementAndGet())
                                      .readPolicy(KafkaReadPolicies.fromEarliest())
                                      .commitOnProcessed()
                                      .keyDeserializer(UUIDDeserializer.class)
                                      .valueDeserializer(BackupTransitionDeserializer.class)
                                      .build();
            //@formatter:on

            BackupManager primary = KafkaPrimaryBackupManager.builder().application("test").sink(sink).build();
            BackupManager secondary =
                    KafkaSecondaryBackupManager.builder().application("test").eventSource(source).build();

            // When
            for (Consumer<BackupManager> transition : transitions) {
                transition.accept(primary);
            }

            // Then
            Assert.assertEquals(primary.getState(), expectedFinalState);
            Awaitility.await()
                      .alias("Secondary State Sync")
                      .atMost(Duration.ofSeconds(10))
                      .pollDelay(Duration.ofSeconds(3))
                      .pollInterval(Duration.ofSeconds(1))
                      .until(() -> secondary.getState() == expectedFinalState);
        }
    }

    @Test(dataProvider = "transitions")
    public void givenKafkaBackupManagersForDifferentApps_whenTransitioningStates_thenNotSynced(
            List<Consumer<BackupManager>> transitions, BackupManagerState expectedFinalState) {
        // Given
        try (KafkaSink<UUID, BackupTransition> sink = KafkaSink.<UUID, BackupTransition>create()
                                                               .bootstrapServers(this.kafka.getBootstrapServers())
                                                               .producerConfig(this.kafka.getClientProperties())
                                                               .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                               .keySerializer(UUIDSerializer.class)
                                                               .valueSerializer(BackupTransitionSerializer.class)
                                                               .noLinger()
                                                               .noAsync()
                                                               .build()) {
            //@formatter:off
            KafkaEventSource<UUID, BackupTransition> source
                    = KafkaEventSource.<UUID, BackupTransition>create()
                                      .bootstrapServers(this.kafka.getBootstrapServers())
                                      .consumerConfig(this.kafka.getClientProperties())
                                      .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                      .consumerGroup("test-backup-manager" + GROUP_ID.incrementAndGet())
                                      .readPolicy(KafkaReadPolicies.fromEarliest())
                                      .commitOnProcessed()
                                      .keyDeserializer(UUIDDeserializer.class)
                                      .valueDeserializer(BackupTransitionDeserializer.class)
                                      .build();
            //@formatter:on

            BackupManager primary = KafkaPrimaryBackupManager.builder().application("app-a").sink(sink).build();
            BackupManager secondary =
                    KafkaSecondaryBackupManager.builder().application("app-b").eventSource(source).build();

            // When
            for (Consumer<BackupManager> transition : transitions) {
                transition.accept(primary);
            }

            // Then
            Assert.assertEquals(primary.getState(), expectedFinalState);
            Awaitility.await()
                      .alias("Secondary State Not Synced")
                      .atMost(Duration.ofSeconds(10))
                      .pollDelay(Duration.ofSeconds(5))
                      .pollInterval(Duration.ofSeconds(1))
                      .until(() -> secondary.getState() == BackupManagerState.STARTING);
        }
    }

}
