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
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.AcknowledgingListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.LoggingListener;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.store.global.GlobalDistributionLifecycleStoreMemory;
import io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTracker;
import io.telicent.smart.cache.distribution.lifecycle.tracker.TrackerState;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;

public class DockerTestDistributionLifecycleConfiguration {

    public static final String APP_ID = "test";
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

}
