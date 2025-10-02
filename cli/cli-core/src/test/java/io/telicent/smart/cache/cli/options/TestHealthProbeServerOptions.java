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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class TestHealthProbeServerOptions extends AbstractOptionsTests {

    @Test
    public void givenNoExternalConfig_whenCreatingHealthProbeServerOptions_thenDefaultsUsed() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        HealthProbeServerOptions options = new HealthProbeServerOptions();

        // Then
        Assert.assertTrue(options.enableHealthProbeServer);
        Assert.assertEquals(options.healthProbePort, HealthProbeServerOptions.DEFAULT_PORT);
    }

    @Test
    public void givenExternalConfig_whenCreatingHealthProbeServerOptions_thenExternalConfigUsedAsDefaults() {
        // Given
        Properties properties = new Properties();
        properties.put(CliEnvironmentVariables.ENABLE_HEALTH_PROBES, "false");
        properties.put(CliEnvironmentVariables.HEALTH_PROBES_PORT, "12345");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        HealthProbeServerOptions options = new HealthProbeServerOptions();

        // Then
        Assert.assertFalse(options.enableHealthProbeServer);
        Assert.assertEquals(options.healthProbePort, 12345);
    }
}
