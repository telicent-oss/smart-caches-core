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
package io.telicent.smart.cache.distribution.lifecycle.config;

import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.Util;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.AcknowledgingListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.DistributionLifecycleListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.LoggingListener;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.store.global.GlobalDistributionLifecycleStoreMemory;
import io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTracker;
import io.telicent.smart.cache.distribution.lifecycle.tracker.TrackerState;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.LazyUUID;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeSerializer;
import io.telicent.smart.cache.sources.kafka.serializers.LazyUUIDSerializer;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

// java:S3577 - test support class, not a test class - no tests to run
@SuppressWarnings("java:S3577")
public class DockerTestDistributionLifecycleConfiguration {

    public static final String APP_ID = "test";
    public static final String APP_VERSION = "1.2.3";
    /**
     * Distribution used by the restart tests whose events are published, read and committed before the restart
     */
    public static final String RESTART_DISTRIBUTION = "restart-distro";
    /**
     * Distribution used by the restart tests whose event is only published after the restart
     */
    public static final String SENTINEL_DISTRIBUTION = "sentinel-distro";
    private final KafkaTestCluster kafka = new BasicKafkaTestCluster();
    private final AtomicInteger consumerId = new AtomicInteger(0);
    private File stateFile;

    @BeforeClass
    public void setup() {
        this.kafka.setup();
        this.kafka.createTopic(DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_TOPIC);
        this.kafka.createTopic(DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_DLQ_TOPIC);
        Configurator.reset();
    }

    @BeforeMethod
    public void setupState() throws IOException {
        this.stateFile = Files.createTempFile("state", ".json").toFile();
        this.stateFile.delete();
    }

    @AfterMethod
    public void cleanup() {
        // NB - Reset the topics so that events, and the consumer offsets for them, published by one test don't leak
        //      into another test
        this.kafka.resetTopic(DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_TOPIC);
        this.kafka.resetTopic(DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_DLQ_TOPIC);
        this.stateFile.delete();
        Configurator.reset();
    }

    @AfterClass
    public void teardown() {
        this.kafka.teardown();
        Configurator.reset();
    }

    @Test
    public void givenFullConfiguration_whenCreatingTracker_thenTrackerCreated() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, "true");
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_STATE_FILE,
                       stateFile.getAbsolutePath());
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_LISTENER_THREADS, 4);
        properties.put(KafkaConfiguration.BOOTSTRAP_SERVERS, this.kafka.getBootstrapServers());
        properties.put(KafkaConfiguration.CONSUMER_GROUP, "test-" + consumerId.incrementAndGet());
        properties.put(KafkaConfiguration.INPUT_TOPIC, DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_TOPIC);
        properties.put(KafkaConfiguration.OUTPUT_TOPIC, DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_TOPIC);
        properties.put(KafkaConfiguration.DLQ_TOPIC, DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_DLQ_TOPIC);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        try (DistributionLifecycleStateStore stateStore = DistributionLifecycleConfiguration.createStateStore(
                APP_ID)) {
            KafkaConfiguration kafkaConfig =
                    KafkaConfiguration.forInputOutputFromConfig(null, null, null, null);
            AcknowledgingListener listener =
                    DistributionLifecycleConfiguration.createAcknowledgingListener(kafkaConfig, APP_ID, "1.2.3",
                                                                                   stateStore, new LoggingListener());

            try (DistributionLifecycleTracker tracker = DistributionLifecycleConfiguration.createTracker(kafkaConfig,
                                                                                                         APP_ID,
                                                                                                         stateStore,
                                                                                                         DistributionLifecycleConfiguration.resolveListenerThreads(),
                                                                                                         List.of(listener))) {
                // Then
                verifyTracker(tracker, stateStore);
            }
        }
    }

    private static void verifyTracker(DistributionLifecycleTracker tracker,
                                      DistributionLifecycleStateStore stateStore) {
        Assert.assertNotNull(tracker);
        Assert.assertTrue(tracker.isRunning());
        Assert.assertEquals(tracker.getTrackerState(), TrackerState.RUNNING);
        Assert.assertSame(tracker.getStateStore(), stateStore);
    }

    @Test
    public void givenPartialConfiguration_whenCreatingTracker_thenTrackerCreated() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, "true");
        properties.put(KafkaConfiguration.BOOTSTRAP_SERVERS, this.kafka.getBootstrapServers());
        properties.put(KafkaConfiguration.CONSUMER_GROUP, "test-" + consumerId.incrementAndGet());
        properties.put(KafkaConfiguration.INPUT_TOPIC, DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_TOPIC);
        properties.put(KafkaConfiguration.OUTPUT_TOPIC, DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_TOPIC);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        try (DistributionLifecycleStateStore store = new GlobalDistributionLifecycleStoreMemory()) {
            KafkaConfiguration kafkaConfig =
                    KafkaConfiguration.forInputOutputFromConfig(null, null, null, null);

            try (DistributionLifecycleTracker tracker = DistributionLifecycleConfiguration.createTracker(kafkaConfig,
                                                                                                         APP_ID, store,
                                                                                                         DistributionLifecycleConfiguration.resolveListenerThreads(),
                                                                                                         List.of(new LoggingListener()))) {
                // Then
                verifyTracker(tracker, store);
            }
        }

    }

    /**
     * Configures a full distribution lifecycle configuration using the given consumer group, the consumer group is a
     * parameter as the restart tests <strong>MUST</strong> reuse the same consumer group across restarts in order to
     * exercise the previously committed offsets
     *
     * @param consumerGroup Consumer group
     */
    private void configureFullLifecycle(String consumerGroup) {
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, "true");
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_STATE_FILE,
                       this.stateFile.getAbsolutePath());
        properties.put(KafkaConfiguration.BOOTSTRAP_SERVERS, this.kafka.getBootstrapServers());
        properties.put(KafkaConfiguration.CONSUMER_GROUP, consumerGroup);
        properties.put(KafkaConfiguration.INPUT_TOPIC, DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_TOPIC);
        properties.put(KafkaConfiguration.OUTPUT_TOPIC, DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_TOPIC);
        properties.put(KafkaConfiguration.DLQ_TOPIC, DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_DLQ_TOPIC);
        Configurator.setSingleSource(new PropertiesSource(properties));
    }

    private Sink<Event<LazyUUID, LazyEnvelope>> createLifecycleSink(KafkaConfiguration kafkaConfig) {
        return kafkaConfig.outputBuilder(LazyUUIDSerializer.class, LazyEnvelopeSerializer.class)
                          .noAsync()
                          .noLinger()
                          .build();
    }

    private void sendLifecycleEvent(Sink<Event<LazyUUID, LazyEnvelope>> sink, String distributionId,
                                    DistributionLifecycleState from, DistributionLifecycleState to) {
        sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                             Util.action(UUID.randomUUID(), distributionId, from, to)));
    }

    /**
     * Publishes the lifecycle events used by the restart tests, then reads them with a tracker so that the applications
     * consumer offsets are advanced and committed, leaving a populated state store behind
     *
     * @param kafkaConfig Kafka configuration
     */
    private void populateAndCommit(KafkaConfiguration kafkaConfig) {
        try (Sink<Event<LazyUUID, LazyEnvelope>> sink = createLifecycleSink(kafkaConfig)) {
            sendLifecycleEvent(sink, RESTART_DISTRIBUTION, DistributionLifecycleState.Unregistered,
                               DistributionLifecycleState.Registered);
            sendLifecycleEvent(sink, RESTART_DISTRIBUTION, DistributionLifecycleState.Registered,
                               DistributionLifecycleState.Active);
        }

        CountingListener counter = new CountingListener();
        try (DistributionLifecycleStateStore stateStore = DistributionLifecycleConfiguration.createStateStore(APP_ID)) {
            try (AcknowledgingListener listener = DistributionLifecycleConfiguration.createAcknowledgingListener(
                    kafkaConfig, APP_ID, APP_VERSION, stateStore, counter);
                 DistributionLifecycleTracker tracker = DistributionLifecycleConfiguration.createTracker(kafkaConfig,
                                                                                                        APP_ID,
                                                                                                        stateStore, 1,
                                                                                                        List.of(listener))) {
                verifyTracker(tracker, stateStore);
                Util.verifyDistributionState(RESTART_DISTRIBUTION, stateStore, DistributionLifecycleState.Active);
                Util.awaitEquals("Initial run reads both lifecycle events",
                                 () -> counter.count(RESTART_DISTRIBUTION), 2);
            }
        }
    }

    @Test
    public void givenCommittedOffsets_whenStateStoreWipedAndTrackerRestarted_thenStateStoreRebuilt() {
        // Given
        configureFullLifecycle("test-" + this.consumerId.incrementAndGet());
        KafkaConfiguration kafkaConfig = KafkaConfiguration.forInputOutputFromConfig(null, null, null, null);
        populateAndCommit(kafkaConfig);

        // When - the state store is wiped but the consumer offsets, which point at the end of the topic, are not
        this.stateFile.delete();
        Assert.assertFalse(this.stateFile.exists(), "Failed to wipe the state store");

        // Then - the restarted application must re-read the previous lifecycle events and rebuild its state store
        CountingListener counter = new CountingListener();
        try (DistributionLifecycleStateStore stateStore = DistributionLifecycleConfiguration.createStateStore(APP_ID)) {
            Assert.assertTrue(stateStore.isEmpty(), "State store should be empty after being wiped");

            try (AcknowledgingListener listener = DistributionLifecycleConfiguration.createAcknowledgingListener(
                    kafkaConfig, APP_ID, APP_VERSION, stateStore, counter);
                 DistributionLifecycleTracker tracker = DistributionLifecycleConfiguration.createTracker(kafkaConfig,
                                                                                                        APP_ID,
                                                                                                        stateStore, 1,
                                                                                                        List.of(listener))) {
                verifyTracker(tracker, stateStore);
                Util.verifyDistributionState(RESTART_DISTRIBUTION, stateStore, DistributionLifecycleState.Active);
                Util.awaitEquals("Restarted run re-reads both lifecycle events",
                                 () -> counter.count(RESTART_DISTRIBUTION), 2);
                Assert.assertFalse(stateStore.isEmpty(), "State store should have been rebuilt from the topic");
            }
        }
    }

    @Test
    public void givenCommittedOffsets_whenTrackerRestartedWithIntactStateStore_thenEventsNotReRead() {
        // Given
        configureFullLifecycle("test-" + this.consumerId.incrementAndGet());
        KafkaConfiguration kafkaConfig = KafkaConfiguration.forInputOutputFromConfig(null, null, null, null);
        populateAndCommit(kafkaConfig);

        // When - the application restarts with its state store intact
        CountingListener counter = new CountingListener();
        try (DistributionLifecycleStateStore stateStore = DistributionLifecycleConfiguration.createStateStore(APP_ID)) {
            Assert.assertFalse(stateStore.isEmpty(), "State store should have survived the restart");

            try (AcknowledgingListener listener = DistributionLifecycleConfiguration.createAcknowledgingListener(
                    kafkaConfig, APP_ID, APP_VERSION, stateStore, counter);
                 DistributionLifecycleTracker tracker = DistributionLifecycleConfiguration.createTracker(kafkaConfig,
                                                                                                        APP_ID,
                                                                                                        stateStore, 1,
                                                                                                        List.of(listener))) {
                // Then - it resumes from its committed offsets, so only newly published events are seen
                // NB - The tracker is only RUNNING once it has caught up with the topic, so any re-read of the earlier
                //      events would already have been dispatched to our listener before the sentinel event below is
                //      published
                verifyTracker(tracker, stateStore);
                try (Sink<Event<LazyUUID, LazyEnvelope>> sink = createLifecycleSink(kafkaConfig)) {
                    sendLifecycleEvent(sink, SENTINEL_DISTRIBUTION, DistributionLifecycleState.Unregistered,
                                       DistributionLifecycleState.Registered);
                }
                Util.verifyDistributionState(SENTINEL_DISTRIBUTION, stateStore,
                                             DistributionLifecycleState.Registered);
                Util.awaitEquals("Restarted run reads the newly published event",
                                 () -> counter.count(SENTINEL_DISTRIBUTION), 1);
                Assert.assertEquals(counter.count(RESTART_DISTRIBUTION), 0,
                                    "Previously committed lifecycle events should not be re-read when the state store is intact");
            }
        }
    }

    /**
     * A listener that records the distributions whose lifecycle events it is given so tests can detect whether events
     * were re-read from the topic
     */
    private static final class CountingListener implements DistributionLifecycleListener {
        private final List<String> distributions = new CopyOnWriteArrayList<>();

        @Override
        public void accept(LifecycleAction action) {
            this.distributions.add(action.getDistributionId());
        }

        public int count(String distributionId) {
            return (int) this.distributions.stream().filter(d -> Objects.equals(d, distributionId)).count();
        }
    }
}
