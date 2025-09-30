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

import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.sources.offsets.OffsetStore;
import io.telicent.smart.cache.sources.offsets.file.JsonOffsetStore;
import io.telicent.smart.cache.sources.offsets.file.YamlOffsetStore;
import org.apache.jena.atlas.lib.FileOps;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Properties;

public class TestOffsetStoreOptions extends AbstractCommandTests {

    @Override
    @BeforeClass
    public void setup() {
        super.setup();
        Configurator.reset();
    }

    @Override
    @AfterMethod
    public void testCleanup() {
        super.testCleanup();
        Configurator.reset();
    }

    @Override
    @AfterClass
    public void teardown() {
        super.teardown();
        Configurator.reset();
        FileOps.deleteSilent("example.json");
        FileOps.deleteSilent("example.yml");
        FileOps.deleteSilent("example.yaml");
    }

    @Test
    public void givenNoArguments_defaultToNull() {
        // Given
        String[] args = new String[] {};

        // When
        SmartCacheCommand.runAsSingleCommand(OffsetStoreOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        OffsetStore offsetStore = getOffsetStore();
        Assert.assertNull(offsetStore);
    }


    @Test
    public void givenYamlFileArgument_returnCorrect() {
        // Given
        String[] args = new String[] {
                "--offsets-file", "example.yml"
        };


        // When
        SmartCacheCommand.runAsSingleCommand(OffsetStoreOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        OffsetStore offsetStore = getOffsetStore();
        Assert.assertNotNull(offsetStore);
        Assert.assertTrue(offsetStore instanceof YamlOffsetStore);
    }

    @Test
    public void givenAlternateYamlFileArgument_returnCorrect() {
        // Given
        String[] args = new String[] {
                "--offsets-file", "example.yaml"
        };

        // When
        SmartCacheCommand.runAsSingleCommand(OffsetStoreOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        OffsetStore offsetStore = getOffsetStore();
        Assert.assertNotNull(offsetStore);
        Assert.assertTrue(offsetStore instanceof YamlOffsetStore);
    }

    @Test
    public void givenJsonFileArgument_returnCorrect() {
        // Given
        String[] args = new String[] {
                "--offsets-file", "example.json"
        };

        // When
        SmartCacheCommand.runAsSingleCommand(OffsetStoreOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        OffsetStore offsetStore = getOffsetStore();
        Assert.assertNotNull(offsetStore);
        Assert.assertTrue(offsetStore instanceof JsonOffsetStore);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "File extension not supported:.*")
    public void givenUnrecognisedFileArgument_returnError() {
        // Given
        String[] args = new String[] {
                "--offsets-file", "example.txt"
        };

        // When
        SmartCacheCommand.runAsSingleCommand(OffsetStoreOptionsCommand.class, args);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        OffsetStore offsetStore = getOffsetStore();
        Assert.assertNull(offsetStore);
    }

    private static OffsetStore getOffsetStore() {
        Assert.assertNotNull(SmartCacheCommandTester.getLastParseResult());
        return ((OffsetStoreOptionsCommand) SmartCacheCommandTester.getLastParseResult()
                                                                   .getCommand()).offsetOptions.getOffsetStore();
    }

    @Test
    public void givenExternalConfig_whenCreatingOffsetStoreOptions_thenExternalConfigUsedAsDefaults() {
        // Given
        Properties properties = new Properties();
        properties.put(CliEnvironmentVariables.OFFSETS_FILE, "target/offsets.yaml");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        OffsetStoreOptions options = new OffsetStoreOptions();
        OffsetStore store = options.getOffsetStore();

        // Then
        Assert.assertTrue(store instanceof YamlOffsetStore);
    }
}
