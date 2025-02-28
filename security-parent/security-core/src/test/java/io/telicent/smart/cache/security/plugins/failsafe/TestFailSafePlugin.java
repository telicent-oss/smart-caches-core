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
package io.telicent.smart.cache.security.plugins.failsafe;

import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.attributes.MalformedAttributesException;
import io.telicent.smart.cache.security.plugins.AbstractSecurityPluginTests;
import io.telicent.smart.cache.security.plugins.SecurityPlugin;
import org.testng.annotations.DataProvider;

public class TestFailSafePlugin extends AbstractSecurityPluginTests {
    @Override
    protected SecurityPlugin getPlugin() {
        return FailSafePlugin.INSTANCE;
    }

    @DataProvider(name = "validLabels")
    @Override
    protected Object[][] validLabels() {
        return new Object[0][];
    }

    @DataProvider(name = "invalidLabels")
    @Override
    protected Object[][] invalidLabels() {
        return new Object[][] {
                { new byte[0] },
                { new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9} },
                { new byte[1000]}
        };
    }

    @DataProvider(name = "accessibleLabels")
    @Override
    protected Object[][] accessibleLabels() {
        return new Object[0][];
    }

    @DataProvider(name = "forbiddenLabels")
    @Override
    protected Object[][] forbiddenLabels() {
        return new Object[0][];
    }

    @Override
    protected UserAttributes<?> getTestEntitlements() throws MalformedAttributesException {
        return new RawPrimitive(new byte[0]);
    }
}
