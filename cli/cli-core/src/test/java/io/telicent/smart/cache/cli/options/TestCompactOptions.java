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

public class TestCompactOptions extends AbstractOptionsTests {

    @Test
    public void givenNoExternalConfig_whenCreatingCompactOptions_thenDefaultsUsed() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        CompactOptions options = new CompactOptions();

        // Then
        Assert.assertTrue(options.compactKeys);
        Assert.assertFalse(options.compactValues);
    }

    @Test
    public void givenExternalConfig_whenCreatingCompactOptions_thenExternalConfigUsedAsDefaults() {
        // Given
        Properties properties = new Properties();
        properties.put(CliEnvironmentVariables.ENABLE_COMPACT_KEYS, "false");
        properties.put(CliEnvironmentVariables.ENABLE_COMPACT_VALUES, "true");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        CompactOptions options = new CompactOptions();

        // Then
        Assert.assertFalse(options.compactKeys);
        Assert.assertTrue(options.compactValues);
    }
}
