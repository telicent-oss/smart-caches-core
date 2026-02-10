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

import ch.qos.logback.core.testUtil.RandomUtil;
import io.telicent.smart.cache.security.Authorizer;
import io.telicent.smart.cache.security.attributes.AttributesProvider;
import io.telicent.smart.cache.security.attributes.MalformedAttributesException;
import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.labels.SecurityLabels;
import io.telicent.smart.cache.security.labels.SecurityLabelsApplicator;
import io.telicent.smart.cache.security.plugins.AbstractSecurityPluginTests;
import io.telicent.smart.cache.security.plugins.SecurityPlugin;
import io.telicent.smart.cache.security.requests.RequestContext;
import org.apache.commons.lang3.RandomUtils;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
    protected UserAttributes<?> getTestAttributes() {
        return new RawPrimitive(new byte[0]);
    }

    @Test
    public void givenFailSafePlugin_whenAuthorizing_thenForbidden() {
        // Given
        UserAttributes<?> attributes = Mockito.mock(UserAttributes.class);
        SecurityLabels<?> labels = Mockito.mock(SecurityLabels.class);
        try (Authorizer authorizer = this.plugin.prepareAuthorizer(attributes)) {
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

    @Test(expectedExceptions = MalformedAttributesException.class)
    public void givenFailSafePlugin_whenObtainingUserAttributes_thenMalformed() {
        // Given
        AttributesProvider provider = this.plugin.attributesProvider();
        RequestContext context = Mockito.mock(RequestContext.class);

        // When and Then
        provider.attributesForUser(context);
    }

    @Test(expectedExceptions = MalformedAttributesException.class)
    public void givenFailSafePlugin_whenParsingAttributes_thenMalformed() {
        // Given, When and Then
        this.plugin.attributesParser().parseAttributes(RandomUtils.insecure().randomBytes(500));
    }
}
