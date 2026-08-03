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
import io.telicent.smart.cache.configuration.sources.NullSource;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.AcknowledgingListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.LoggingListener;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Properties;

public class TestDistributionLifecycleConfiguration {

    @BeforeClass
    public void setup() {
        Configurator.reset();
    }

    @AfterMethod
    public void cleanup() {
        Configurator.reset();
    }

    @AfterClass
    public void teardown() {
        Configurator.reset();
    }

    @Test
    public void givenFeatureDisabled_whenCreatingComponents_thenNullReturned() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, false);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        Assert.assertNull(DistributionLifecycleConfiguration.createStateStore("test"));
        Assert.assertNull(
                DistributionLifecycleConfiguration.createAcknowledgingListener(null, "test", "1.2.3", null, null));
        Assert.assertNull(
                DistributionLifecycleConfiguration.createTracker(null, "test", null, 1, Collections.emptyList()));
    }

    @Test
    public void givenNoConfiguration_whenCheckingDistributionLifecycleConfig_thenDisabled() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When and Then
        Assert.assertFalse(DistributionLifecycleConfiguration.isEnabled());
        Assert.assertNull(DistributionLifecycleConfiguration.createStateStore("test"));
        Assert.assertNull(DistributionLifecycleConfiguration.resolveStateFile());
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveListenerThreads(),
                            DistributionLifecycleConfiguration.DEFAULT_LISTENER_THREADS);
        Assert.assertNull(
                DistributionLifecycleConfiguration.createTracker(KafkaConfiguration.builder().build(), "test", null, 1,
                                                                 Collections.emptyList()));
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*Missing configuration.*")
    public void givenPartialConfiguration_whenCheckingDistributionLifecycleConfig_thenEnabled_andNoStoreCanBeCreated() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, "true");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        Assert.assertTrue(DistributionLifecycleConfiguration.isEnabled());

        // And
        DistributionLifecycleConfiguration.createStateStore("test");
    }

    @Test
    public void givenFullConfiguration_whenCheckingDistributionLifecycleConfig_thenEnabled_andComponentsCanBeCreated() throws
            IOException {
        // Given
        File stateFile = Files.createTempFile("distribution-lifecycle-state", ".json").toFile();
        stateFile.delete();
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, "true");
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_STATE_FILE,
                       stateFile.getAbsolutePath());
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_LISTENER_THREADS, 4);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        Assert.assertTrue(DistributionLifecycleConfiguration.isEnabled());
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveStateFile(), stateFile);
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveListenerThreads(), 4);

        // And
        try (DistributionLifecycleStateStore store = DistributionLifecycleConfiguration.createStateStore(
                "test")) {
            Assert.assertTrue(store.activeEvents().isEmpty());

            try (AcknowledgingListener listener = DistributionLifecycleConfiguration.createAcknowledgingListener(
                    KafkaConfiguration.builder().bootstrapServers("localhost:9092").outputTopic("tests").build(),
                    "test",
                    "1.2.3", store, new LoggingListener())) {
                Assert.assertNotNull(listener);
            }
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*state file unreadable.*")
    public void givenBadConfiguration_whenCheckingDistributionLifecycleConfig_thenEnabled_andStoreFailsToCreate() throws
            IOException {
        // Given
        File stateFile = Files.createTempFile("distribution-lifecycle-state", ".json").toFile();
        try (FileWriter writer = new FileWriter(stateFile)) {
            writer.write("junk");
        }
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, "true");
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_STATE_FILE,
                       stateFile.getAbsolutePath());
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_LISTENER_THREADS, 4);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        Assert.assertTrue(DistributionLifecycleConfiguration.isEnabled());
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveStateFile(), stateFile);
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveListenerThreads(), 4);

        // And
        DistributionLifecycleConfiguration.createStateStore("test");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Kafka Configuration.*")
    public void givenNullKafkaConfiguration_whenCreatingListener_thenNPE() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, true);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        DistributionLifecycleConfiguration.createAcknowledgingListener(null, "test", "1.2.3", null, null);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Invalid configuration for output.*")
    public void givenEmptyKafkaConfiguration_whenCreatingListener_thenIllegalState() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, true);
        Configurator.setSingleSource(new PropertiesSource(properties));
        KafkaConfiguration kafkaConfiguration = KafkaConfiguration.builder().build();

        // When and Then
        DistributionLifecycleConfiguration.createAcknowledgingListener(kafkaConfiguration, "test", "1.2.3", null, null);
    }
}
