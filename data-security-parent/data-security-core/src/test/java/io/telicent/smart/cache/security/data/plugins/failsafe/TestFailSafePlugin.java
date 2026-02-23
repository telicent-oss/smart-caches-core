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
package io.telicent.smart.cache.security.data.plugins.failsafe;

import io.telicent.smart.cache.security.data.DataAccessAuthorizer;
import io.telicent.smart.cache.security.data.labels.SecurityLabels;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsApplicator;
import io.telicent.smart.cache.security.data.plugins.AbstractDataSecurityPluginTests;
import io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin;
import io.telicent.smart.cache.security.data.requests.RequestContext;
import io.telicent.smart.caches.configuration.auth.UserInfo;
import org.apache.commons.lang3.RandomUtils;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestFailSafePlugin extends AbstractDataSecurityPluginTests {
    @Override
    protected DataSecurityPlugin getPlugin() {
        return FailSafePlugin.INSTANCE;
    }

    @Override
    protected UserInfo getTestUserInfo(String username) {
        return UserInfo.builder().sub(username).build();
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

    @Test
    public void givenFailSafePlugin_whenAuthorizing_thenForbidden() {
        // Given
        RequestContext context = Mockito.mock(RequestContext.class);
        SecurityLabels<?> labels = Mockito.mock(SecurityLabels.class);
        try (DataAccessAuthorizer authorizer = this.plugin.prepareAuthorizer(context)) {
            // When and Then
            Assert.assertFalse(authorizer.canRead(labels));
        }
    }

    @Test
    public void givenFailSafePlugin_whenApplyingLabels_thenDefaultLabelPreservedAsIs() {
        // Given
        byte[] defaultLabel = RandomUtils.insecure().randomBytes(50);

        // When
        try (SecurityLabelsApplicator applicator = this.plugin.prepareLabelsApplicator(defaultLabel, null)) {
            SecurityLabels<?> applied = applicator.labelForTriple(TEST_TRIPLE);

            // Then
            Assert.assertEquals(applied.encoded(), defaultLabel);
            Assert.assertTrue(applied.decodedLabels() instanceof RawBytes);
        }
    }

    @Test
    public void givenFailSafePlugin_whenClosing_thenNoOp() {
        // Given
        DataSecurityPlugin plugin = FailSafePlugin.INSTANCE;

        // When and Then
        plugin.close();
    }
}
