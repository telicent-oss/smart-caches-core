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

import ch.qos.logback.classic.Level;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.NullSource;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Properties;

public class TestLoggingOptions extends AbstractOptionsTests {

    @Test
    public void givenNoExternalConfig_whenCreatingLoggingOptions_thenDefaultsUsed() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        LoggingOptions options = new LoggingOptions();

        // Then
        Assert.assertNull(options.effectiveLevel());
        Assert.assertTrue(options.showRuntimeInfo);
    }

    @DataProvider(name = "loggingSingleVar")
    private Object[][] loggingSingleVar() {
        return new Object[][] {
                { CliEnvironmentVariables.DEBUG, Level.DEBUG },
                { CliEnvironmentVariables.TRACE, Level.TRACE },
                { CliEnvironmentVariables.QUIET, Level.WARN }
        };
    }

    @Test(dataProvider = "loggingSingleVar")
    public void givenSingleExternalConfigValue_whenCreatingLoggingOptions_thenEffectiveLevelAsExpected(String var,
                                                                                                       Level expected) {
        // Given
        Properties properties = new Properties();
        properties.put(var, "true");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        LoggingOptions options = new LoggingOptions();

        // Then
        Assert.assertEquals(options.effectiveLevel(), expected);
    }

    @DataProvider(name = "loggingMultiVar")
    private Object[][] loggingMultipleVariables() {
        return new Object[][] {
                { List.of(CliEnvironmentVariables.DEBUG, CliEnvironmentVariables.TRACE), Level.TRACE },
                { List.of(CliEnvironmentVariables.TRACE, CliEnvironmentVariables.QUIET), Level.TRACE },
                { List.of(CliEnvironmentVariables.DEBUG, CliEnvironmentVariables.QUIET), Level.DEBUG }
        };
    }

    @Test(dataProvider = "loggingMultiVar")
    public void givenMultipleExternalConfigValue_whenCreatingLoggingOptions_thenMostVerboseLevelSelected(List<String> vars,
                                                                                                         Level mostVerbose) {
        // Given
        Properties properties = new Properties();
        vars.forEach(var -> properties.put(var, "true"));
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        LoggingOptions options = new LoggingOptions();

        // Then
        Assert.assertEquals(options.effectiveLevel(), mostVerbose);
    }

    @Test
    public void givenExternalConfig_whenCreatingLoggingOptions_thenExternalConfigUsedAsDefault() {
        // Given
        Properties properties = new Properties();
        properties.put(CliEnvironmentVariables.DEBUG, "true");
        properties.put(CliEnvironmentVariables.SHOW_RUNTIME_INFO, "false");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        LoggingOptions options = new LoggingOptions();

        // Then
        Assert.assertEquals(options.effectiveLevel(), Level.DEBUG);
        Assert.assertFalse(options.showRuntimeInfo);
    }
}
