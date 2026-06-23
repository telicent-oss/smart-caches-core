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

import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.NullSource;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class TestDistributionLifecycleTrackerOptions extends AbstractOptionsTests {

    @Test
    public void givenNoExternalConfig_whenCreatingDistributionLifecycleTrackerOptions_thenDefaultsUsed() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        DistributionLifecycleTrackerOptions options = new DistributionLifecycleTrackerOptions();

        // Then
        Assert.assertNull(options.distLifecycleBootstrapServers);
        Assert.assertEquals(options.distLifecycleTopic, DistributionLifecycleTrackerOptions.DEFAULT_LIFECYCLE_TOPIC);
        Assert.assertEquals(options.distLifecycleDlqTopic,
                            DistributionLifecycleTrackerOptions.DEFAULT_LIFECYCLE_DLQ_TOPIC);
    }

    @Test
    public void givenExternalConfig_whenCreatingDistributionLifecycleTrackerOptions_thenExternalConfigUsedAsDefaults() {
        // Given
        Properties properties = new Properties();
        properties.put(CliEnvironmentVariables.DISTRIBUTION_LIFECYCLE_BOOTSTRAP_SERVERS, "localhost:9092");
        properties.put(CliEnvironmentVariables.DISTRIBUTION_LIFECYCLE_TOPIC, "custom");
        properties.put(CliEnvironmentVariables.DISTRIBUTION_LIFECYCLE_DLQ_TOPIC, "dlq");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        DistributionLifecycleTrackerOptions options = new DistributionLifecycleTrackerOptions();

        // Then
        Assert.assertEquals(options.distLifecycleBootstrapServers, "localhost:9092");
        Assert.assertEquals(options.distLifecycleTopic, "custom");
        Assert.assertEquals(options.distLifecycleDlqTopic, "dlq");
    }

    @Test
    public void givenPartialExternalConfig_whenCreatingDistributionLifecycleTrackerOptions_thenAvailableExternalConfigUsedAsDefaults() {
        // Given
        Properties properties = new Properties();
        properties.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        DistributionLifecycleTrackerOptions options = new DistributionLifecycleTrackerOptions();

        // Then
        Assert.assertEquals(options.distLifecycleBootstrapServers, "localhost:9092");
        Assert.assertEquals(options.distLifecycleTopic, DistributionLifecycleTrackerOptions.DEFAULT_LIFECYCLE_TOPIC);
        Assert.assertEquals(options.distLifecycleDlqTopic, DistributionLifecycleTrackerOptions.DEFAULT_LIFECYCLE_DLQ_TOPIC);
    }
}
