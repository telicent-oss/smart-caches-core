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
package io.telicent.smart.cache.security.plugins;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSecurityPluginLoader {

    @BeforeClass
    public void setup() {
        SecurityPluginLoader.reset();
    }

    @AfterMethod
    public void teardown() {
        SecurityPluginLoader.reset();
    }

    @Test
    public void givenNoPluginRegistrations_whenLoading_thenError_andSubsequentLoadErrors() {
        // Given and When
        try {
            SecurityPluginLoader.load();
            Assert.fail("Plugin loading should have failed");
        } catch (Error e) {
            // Then
            Assert.assertNotNull(e);
        }

        // And
        Assert.assertTrue(SecurityPluginLoader.isLoaded());
        Assert.assertTrue(SecurityPluginLoader.isFailSafeMode());
        try {
            SecurityPluginLoader.load();
            Assert.fail("Plugin loading should have failed");
        } catch (Error e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void givenTooManyPluginRegistrations_whenLoading_thenError() {
        // Given
        List<SecurityPlugin> plugins = new ArrayList<>();
        plugins.add(mock(SecurityPlugin.class));
        plugins.add(mock(SecurityPlugin.class));
        ServiceLoader<SecurityPlugin> loader = (ServiceLoader<SecurityPlugin>) Mockito.mock(ServiceLoader.class);
        when(loader.iterator()).thenReturn(plugins.iterator());
        try (MockedStatic<ServiceLoader> mock = Mockito.mockStatic(ServiceLoader.class)) {
            mock.when(() -> ServiceLoader.load(SecurityPlugin.class)).thenReturn(loader);

            // When
            try {
                SecurityPluginLoader.load();
            } catch (Error e) {
                // Then

                Assert.assertTrue(SecurityPluginLoader.isLoaded());
                Assert.assertTrue(SecurityPluginLoader.isFailSafeMode());
            }
        }
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void givenOnePluginRegistrations_whenLoading_thenOk_andLoadedInstanceIsSingleton() {
        // Given
        List<SecurityPlugin> plugins = new ArrayList<>();
        SecurityPlugin plugin = mock(SecurityPlugin.class);
        plugins.add(plugin);
        ServiceLoader<SecurityPlugin> loader = (ServiceLoader<SecurityPlugin>) Mockito.mock(ServiceLoader.class);
        when(loader.iterator()).thenReturn(plugins.iterator());
        try (MockedStatic<ServiceLoader> mock = Mockito.mockStatic(ServiceLoader.class)) {
            mock.when(() -> ServiceLoader.load(SecurityPlugin.class)).thenReturn(loader);

            // When
            SecurityPlugin loaded = SecurityPluginLoader.load();

            // Then
            Assert.assertTrue(SecurityPluginLoader.isLoaded());
            Assert.assertFalse(SecurityPluginLoader.isFailSafeMode());
            Assert.assertSame(loaded, plugin);

            // And
            SecurityPlugin loadedAgain = SecurityPluginLoader.load();
            Assert.assertSame(loadedAgain, loaded);
        }
    }
}
