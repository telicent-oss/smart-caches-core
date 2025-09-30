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

public class TestActionTrackerOptions extends AbstractOptionsTests {

    @Test
    public void givenNoExternalConfig_whenCreatingActionTrackerOptions_thenDefaultsUsed() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        ActionTrackerOptions options = new ActionTrackerOptions();

        // Then
        Assert.assertNull(options.actionBootstrapServers);
        Assert.assertEquals(options.actionTopic, ActionTrackerOptions.DEFAULT_ACTIONS_TOPIC);
    }

    @Test
    public void givenExternalConfig_whenCreatingActionTrackerOptions_thenExternalConfigUsedAsDefaults() {
        // Given
        Properties properties = new Properties();
        properties.put(CliEnvironmentVariables.ACTION_BOOTSTRAP_SERVERS, "localhost:9092");
        properties.put(CliEnvironmentVariables.ACTION_TOPIC, "custom");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        ActionTrackerOptions options = new ActionTrackerOptions();

        // Then
        Assert.assertEquals(options.actionBootstrapServers, "localhost:9092");
        Assert.assertEquals(options.actionTopic, "custom");
    }

    @Test
    public void givenPartialExternalConfig_whenCreatingActionTrackerOptions_thenAvailableExternalConfigUsedAsDefaults() {
        // Given
        Properties properties = new Properties();
        properties.put(KafkaConfiguration.BOOTSTRAP_SERVERS, "localhost:9092");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        ActionTrackerOptions options = new ActionTrackerOptions();

        // Then
        Assert.assertEquals(options.actionBootstrapServers, "localhost:9092");
        Assert.assertEquals(options.actionTopic, ActionTrackerOptions.DEFAULT_ACTIONS_TOPIC);
    }
}
