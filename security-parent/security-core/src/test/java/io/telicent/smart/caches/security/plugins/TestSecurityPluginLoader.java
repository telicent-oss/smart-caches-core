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
package io.telicent.smart.caches.security.plugins;

import io.telicent.smart.caches.security.plugins.failsafe.FailSafePlugin;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSecurityPluginLoader {

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
}
