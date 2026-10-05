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
package io.telicent.smart.cache.sources;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestDistributionKeyStrategy {

    private static final String DISTRIBUTION_ID = "https://telicent.io/datasets/acled#2026-08-release";

    @Test
    public void givenDefaultStrategy_whenGeneratingKey_thenKeyIsTheDistributionId() {
        // Given, When
        String key = DistributionKeyStrategy.DISTRIBUTION_ID.key(DISTRIBUTION_ID);

        // Then
        Assert.assertEquals(key, DISTRIBUTION_ID);
    }

    @Test
    public void givenDefaultStrategy_whenGeneratingKeysRepeatedly_thenKeysAreStable() {
        // Given, When and Then - stability is what gives us per-distribution partition affinity
        Assert.assertEquals(DistributionKeyStrategy.DISTRIBUTION_ID.key(DISTRIBUTION_ID),
                            DistributionKeyStrategy.DISTRIBUTION_ID.key(DISTRIBUTION_ID));
    }

    @Test
    public void givenCompositeStrategy_whenGeneratingKey_thenKeyIsUniqueButDecodesBack() {
        // Given, When
        String first = DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID.key(DISTRIBUTION_ID);
        String second = DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID.key(DISTRIBUTION_ID);

        // Then - uniqueness is what keeps log compaction viable
        Assert.assertNotEquals(first, second);
        Assert.assertTrue(first.startsWith(DISTRIBUTION_ID + DistributionIds.KEY_SEPARATOR));
        Assert.assertEquals(DistributionIds.fromKeyString(first), DISTRIBUTION_ID);
        Assert.assertEquals(DistributionIds.fromKeyString(second), DISTRIBUTION_ID);
    }

    @DataProvider(name = "blankDistributionIds")
    private Object[][] blankDistributionIds() {
        return new Object[][] { { null }, { "" }, { "   " } };
    }

    @Test(dataProvider = "blankDistributionIds")
    public void givenNoDistributionId_whenGeneratingKey_thenNull(String distributionId) {
        // Given, When and Then
        for (DistributionKeyStrategy strategy : DistributionKeyStrategy.values()) {
            Assert.assertNull(strategy.key(distributionId),
                              strategy + " must not generate a key when there is no Distribution ID");
        }
    }

    @DataProvider(name = "configValues")
    private Object[][] configValues() {
        return new Object[][] {
                { "distribution-id", DistributionKeyStrategy.DISTRIBUTION_ID },
                { "DISTRIBUTION-ID", DistributionKeyStrategy.DISTRIBUTION_ID },
                { "DISTRIBUTION_ID", DistributionKeyStrategy.DISTRIBUTION_ID },
                { "  distribution-id  ", DistributionKeyStrategy.DISTRIBUTION_ID },
                { "distribution-id-and-uuid", DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID },
                { "Distribution-Id-And-Uuid", DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID },
                { "DISTRIBUTION_ID_AND_UUID", DistributionKeyStrategy.DISTRIBUTION_ID_AND_UUID },
                // Blank falls back to the default rather than erroring
                { null, DistributionKeyStrategy.DEFAULT },
                { "", DistributionKeyStrategy.DEFAULT },
                { "   ", DistributionKeyStrategy.DEFAULT }
        };
    }

    @Test(dataProvider = "configValues")
    public void givenConfigValue_whenParsing_thenExpectedStrategy(String value, DistributionKeyStrategy expected) {
        // Given, When and Then
        Assert.assertEquals(DistributionKeyStrategy.parse(value), expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenUnrecognisedConfigValue_whenParsing_thenIllegalArgument() {
        // Given, When and Then
        DistributionKeyStrategy.parse("no-such-strategy");
    }

    @Test
    public void givenStrategies_whenRoundTrippingConfigValues_thenParsesBack() {
        // Given, When and Then
        for (DistributionKeyStrategy strategy : DistributionKeyStrategy.values()) {
            Assert.assertEquals(DistributionKeyStrategy.parse(strategy.configValue()), strategy);
        }
    }

    @Test
    public void givenDefault_whenChecking_thenIsPlainDistributionId() {
        // Given, When and Then - the platform default must remain the plain Distribution ID
        Assert.assertEquals(DistributionKeyStrategy.DEFAULT, DistributionKeyStrategy.DISTRIBUTION_ID);
    }
}
