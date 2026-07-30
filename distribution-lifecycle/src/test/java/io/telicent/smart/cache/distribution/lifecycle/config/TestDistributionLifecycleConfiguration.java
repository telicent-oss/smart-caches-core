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
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class TestDistributionLifecycleConfiguration {

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
    public void givenNoConfiguration_whenCheckingDistributionLifecycleConfig_thenDisabled() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When and Then
        Assert.assertFalse(DistributionLifecycleConfiguration.isEnabled());
        Assert.assertNull(DistributionLifecycleConfiguration.createDistributionLifecycleStateStore("test"));
        Assert.assertNull(DistributionLifecycleConfiguration.resolveStateFile());
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveListenerThreads(),
                            DistributionLifecycleConfiguration.DEFAULT_LISTENER_THREADS);
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
        DistributionLifecycleConfiguration.createDistributionLifecycleStateStore("test");
    }

    @Test
    public void givenFullConfiguration_whenCheckingDistributionLifecycleConfig_thenEnabled_andStoreCanBeCreated() throws
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
        try (DistributionLifecycleStateStore store = DistributionLifecycleConfiguration.createDistributionLifecycleStateStore(
                "test")) {
            Assert.assertTrue(store.activeEvents().isEmpty());
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
        DistributionLifecycleConfiguration.createDistributionLifecycleStateStore("test");
    }
}
