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
import io.telicent.smart.cache.live.LiveErrorReporter;
import io.telicent.smart.cache.live.LiveReporter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class TestLiveReporterOptions extends AbstractOptionsTests {

    @Test
    public void givenNoExternalConfig_whenCreatingLiveReporterOptions_thenDefaultsUsed() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When
        LiveReporterOptions options = new LiveReporterOptions();

        // Then
        verifyDefaults(options);
    }

    private static void verifyDefaults(LiveReporterOptions options) {
        Assert.assertTrue(options.enableLiveReporter);
        Assert.assertNull(options.liveBootstrapServers);
        Assert.assertEquals(options.liveReportTopic, LiveReporter.DEFAULT_LIVE_TOPIC);
        Assert.assertEquals(options.liveErrorTopic, LiveErrorReporter.DEFAULT_LIVE_TOPIC);
        Assert.assertEquals(options.liveReportPeriod, LiveReporter.DEFAULT_REPORTING_PERIOD_SECONDS);
    }

    @Test
    public void givenExternalConfig_whenCreatingLiveReporterOptions_thenExternalConfigUsedAsDefaults() {
        // Given
        Properties properties = new Properties();
        properties.put(CliEnvironmentVariables.ENABLE_LIVE_REPORTER, "false");
        properties.put(CliEnvironmentVariables.LIVE_REPORTER_TOPIC, "heartbeats");
        properties.put(CliEnvironmentVariables.LIVE_ERROR_TOPIC, "errors");
        properties.put(CliEnvironmentVariables.LIVE_REPORTER_INTERVAL, "60");
        properties.put(CliEnvironmentVariables.LIVE_BOOTSTRAP_SERVERS, "localhost:9092");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        LiveReporterOptions options = new LiveReporterOptions();

        // Then
        Assert.assertFalse(options.enableLiveReporter);
        Assert.assertEquals(options.liveBootstrapServers, "localhost:9092");
        Assert.assertEquals(options.liveReportTopic, "heartbeats");
        Assert.assertEquals(options.liveErrorTopic, "errors");
        Assert.assertEquals(options.liveReportPeriod, 60);
    }

    @Test
    public void givenMalformedExternalConfig_whenCreatingLiveReporterOptions_thenDefaultsUsed() {
        // Given
        Properties properties = new Properties();
        properties.put(CliEnvironmentVariables.LIVE_REPORTER_TOPIC, "");
        properties.put(CliEnvironmentVariables.LIVE_ERROR_TOPIC, "");
        properties.put(CliEnvironmentVariables.LIVE_REPORTER_INTERVAL, "t7");
        properties.put(CliEnvironmentVariables.LIVE_BOOTSTRAP_SERVERS, "");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        LiveReporterOptions options = new LiveReporterOptions();

        // Then
        verifyDefaults(options);
    }

    @Test
    public void givenMalformedExternalBooleanConfig_whenCreatingLiveReporterOptions_thenDisablesFeature() {
        // Given
        Properties properties = new Properties();
        properties.put(CliEnvironmentVariables.ENABLE_LIVE_REPORTER, "foo");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        LiveReporterOptions options = new LiveReporterOptions();

        // Then
        Assert.assertFalse(options.enableLiveReporter);
    }
}
