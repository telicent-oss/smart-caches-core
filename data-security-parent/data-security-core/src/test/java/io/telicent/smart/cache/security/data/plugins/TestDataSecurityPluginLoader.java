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
package io.telicent.smart.cache.security.data.plugins;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// java:S8924 - qualified Mockito calls are preferred over static imports here
@SuppressWarnings("java:S8924")
public class TestDataSecurityPluginLoader {

    @BeforeClass
    public void setup() {
        DataSecurityPluginLoader.reset();
    }

    @AfterMethod
    public void teardown() {
        DataSecurityPluginLoader.reset();
    }

    @Test
    public void givenNoPluginRegistrations_whenLoading_thenError_andSubsequentLoadErrors() {
        // Given and When
        Assert.assertFalse(DataSecurityPluginLoader.isLoaded());
        try {
            DataSecurityPluginLoader.load();
            Assert.fail("Plugin loading should have failed");
        } catch (Error e) {
            // Then
            Assert.assertNotNull(e);
        }

        // And
        Assert.assertTrue(DataSecurityPluginLoader.isLoaded());
        Assert.assertTrue(DataSecurityPluginLoader.isFailSafeMode());
        try {
            DataSecurityPluginLoader.load();
            Assert.fail("Plugin loading should have failed");
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void givenTooManyPluginRegistrations_whenLoading_thenError() {
        // Given
        List<DataSecurityPlugin> plugins = new ArrayList<>();
        plugins.add(mock(DataSecurityPlugin.class));
        plugins.add(mock(DataSecurityPlugin.class));
        ServiceLoader<DataSecurityPlugin> loader = (ServiceLoader<DataSecurityPlugin>) Mockito.mock(ServiceLoader.class);
        when(loader.iterator()).thenReturn(plugins.iterator());
        try (MockedStatic<ServiceLoader> mock = Mockito.mockStatic(ServiceLoader.class)) {
            mock.when(() -> ServiceLoader.load(DataSecurityPlugin.class)).thenReturn(loader);

            // When
            Error error = Assert.expectThrows(Error.class, DataSecurityPluginLoader::load);

            // Then
            Assert.assertTrue(error.getMessage().contains("Multiple Telicent Security Plugins"));
            Assert.assertTrue(DataSecurityPluginLoader.isLoaded());
            Assert.assertTrue(DataSecurityPluginLoader.isFailSafeMode());
        }
    }

    @Test
    public void givenNotYetLoaded_whenCheckingIsLoaded_thenFalse() {
        Assert.assertFalse(DataSecurityPluginLoader.isLoaded());
        Assert.assertFalse(DataSecurityPluginLoader.isFailSafeMode());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void givenSuccessfulLoad_whenResetting_thenIsLoadedReturnsFalse() {
        // Given - load a plugin
        List<DataSecurityPlugin> plugins = new ArrayList<>();
        plugins.add(mock(DataSecurityPlugin.class));
        ServiceLoader<DataSecurityPlugin> loader = (ServiceLoader<DataSecurityPlugin>) Mockito.mock(ServiceLoader.class);
        when(loader.iterator()).thenReturn(plugins.iterator());
        try (MockedStatic<ServiceLoader> mockedStatic = Mockito.mockStatic(ServiceLoader.class)) {
            mockedStatic.when(() -> ServiceLoader.load(DataSecurityPlugin.class)).thenReturn(loader);
            DataSecurityPluginLoader.load();
        }
        Assert.assertTrue(DataSecurityPluginLoader.isLoaded());

        // When
        DataSecurityPluginLoader.reset();

        // Then
        Assert.assertFalse(DataSecurityPluginLoader.isLoaded());
        Assert.assertFalse(DataSecurityPluginLoader.isFailSafeMode());
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void givenOnePluginRegistrations_whenLoading_thenOk_andLoadedInstanceIsSingleton() {
        // Given
        List<DataSecurityPlugin> plugins = new ArrayList<>();
        DataSecurityPlugin plugin = mock(DataSecurityPlugin.class);
        plugins.add(plugin);
        ServiceLoader<DataSecurityPlugin> loader = (ServiceLoader<DataSecurityPlugin>) Mockito.mock(ServiceLoader.class);
        when(loader.iterator()).thenReturn(plugins.iterator());
        try (MockedStatic<ServiceLoader> mock = Mockito.mockStatic(ServiceLoader.class)) {
            mock.when(() -> ServiceLoader.load(DataSecurityPlugin.class)).thenReturn(loader);

            // When
            DataSecurityPlugin loaded = DataSecurityPluginLoader.load();

            // Then
            Assert.assertTrue(DataSecurityPluginLoader.isLoaded());
            Assert.assertFalse(DataSecurityPluginLoader.isFailSafeMode());
            Assert.assertSame(loaded, plugin);

            // And
            DataSecurityPlugin loadedAgain = DataSecurityPluginLoader.load();
            Assert.assertSame(loadedAgain, loaded);
        }
    }

    @DataProvider(name = "concurrentLoadOutcomes")
    public Object[][] concurrentLoadOutcomes() {
        return new Object[][] { { false }, { true } };
    }

    @Test(dataProvider = "concurrentLoadOutcomes")
    @SuppressWarnings("unchecked")
    public void givenConcurrentLoad_whenAnotherCallerInitializes_thenReuseItsOutcome(boolean failSafe)
            throws Exception {
        DataSecurityPlugin plugin = mock(DataSecurityPlugin.class);
        ServiceLoader<DataSecurityPlugin> loader = mock(ServiceLoader.class);
        when(loader.iterator()).thenAnswer(invocation -> (failSafe ? List.<DataSecurityPlugin>of() : List.of(plugin)).iterator());

        // Hold the initialization lock until the competing caller has reached it with no plugin loaded.
        Field lockField = DataSecurityPluginLoader.class.getDeclaredField("LOCK");
        lockField.setAccessible(true);
        Object lock = lockField.get(null);
        FutureTask<DataSecurityPlugin> competingLoad = new FutureTask<>(DataSecurityPluginLoader::load);
        Thread caller = new Thread(competingLoad, "concurrent-plugin-loader");
        caller.setDaemon(true);
        try (MockedStatic<ServiceLoader> mockedStatic = Mockito.mockStatic(ServiceLoader.class)) {
            mockedStatic.when(() -> ServiceLoader.load(DataSecurityPlugin.class)).thenReturn(loader);
            try {
                synchronized (lock) {
                    caller.start();
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                    while (caller.getState() != Thread.State.BLOCKED && System.nanoTime() < deadline) {
                        Thread.onSpinWait();
                    }
                    Assert.assertEquals(caller.getState(), Thread.State.BLOCKED,
                                        "Competing caller must wait for plugin initialization");
                    if (failSafe) {
                        Assert.expectThrows(Error.class, DataSecurityPluginLoader::load);
                    } else {
                        Assert.assertSame(DataSecurityPluginLoader.load(), plugin);
                    }
                }
                if (failSafe) {
                    ExecutionException error = Assert.expectThrows(ExecutionException.class,
                            () -> competingLoad.get(5, TimeUnit.SECONDS));
                    Assert.assertTrue(error.getCause() instanceof Error);
                    Assert.assertTrue(error.getCause().getMessage().contains("operating in fail-safe mode"));
                    Assert.assertTrue(DataSecurityPluginLoader.isFailSafeMode());
                } else {
                    Assert.assertSame(competingLoad.get(5, TimeUnit.SECONDS), plugin);
                    Assert.assertFalse(DataSecurityPluginLoader.isFailSafeMode());
                }
                mockedStatic.verify(() -> ServiceLoader.load(DataSecurityPlugin.class), Mockito.times(1));
            } finally {
                caller.join(TimeUnit.SECONDS.toMillis(5));
            }
        }
    }

}
