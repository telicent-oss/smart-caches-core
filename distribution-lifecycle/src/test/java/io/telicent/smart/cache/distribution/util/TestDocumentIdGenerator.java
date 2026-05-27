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
package io.telicent.smart.cache.distribution.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestDocumentIdGenerator {

    private static final String ENTITY_URI = "http://example.com/entity/123";
    private static final String DISTRIBUTION_ID = "dist-001";
    private static final String EXPECTED_PREFIX = "distribution-sha256:";

    // --- enabledCombined=false: pass-through ---

    @Test
    public void generateDocumentId_notEnabled_returnsEntityUri() {
        final String result = DocumentIdGenerator.generateDocumentId(ENTITY_URI, DISTRIBUTION_ID, false);
        Assert.assertEquals(result, ENTITY_URI);
    }

    @Test
    public void generateDocumentId_notEnabled_nullDistributionId_returnsEntityUri() {
        // distributionId is irrelevant when combined mode is off
        final String result = DocumentIdGenerator.generateDocumentId(ENTITY_URI, null, false);
        Assert.assertEquals(result, ENTITY_URI);
    }

    @Test
    public void generateDocumentId_notEnabled_blankDistributionId_returnsEntityUri() {
        final String result = DocumentIdGenerator.generateDocumentId(ENTITY_URI, "   ", false);
        Assert.assertEquals(result, ENTITY_URI);
    }

    // --- enabledCombined=true: combined ID generation ---

    @Test
    public void generateDocumentId_enabled_returnsPrefixedSha256() {
        final String result = DocumentIdGenerator.generateDocumentId(ENTITY_URI, DISTRIBUTION_ID, true);
        Assert.assertTrue(result.startsWith(EXPECTED_PREFIX),
                "Result should start with '" + EXPECTED_PREFIX + "' but was: " + result);
        // prefix + 64-char hex
        Assert.assertEquals(result.length(), EXPECTED_PREFIX.length() + 64);
    }

    @Test
    public void generateDocumentId_enabled_isDeterministic() {
        final String first = DocumentIdGenerator.generateDocumentId(ENTITY_URI, DISTRIBUTION_ID, true);
        final String second = DocumentIdGenerator.generateDocumentId(ENTITY_URI, DISTRIBUTION_ID, true);
        Assert.assertEquals(first, second);
    }

    @Test
    public void generateDocumentId_enabled_differentEntityUris_produceDifferentIds() {
        final String id1 = DocumentIdGenerator.generateDocumentId("http://example.com/entity/1", DISTRIBUTION_ID, true);
        final String id2 = DocumentIdGenerator.generateDocumentId("http://example.com/entity/2", DISTRIBUTION_ID, true);
        Assert.assertNotEquals(id1, id2);
    }

    @Test
    public void generateDocumentId_enabled_differentDistributionIds_produceDifferentIds() {
        final String id1 = DocumentIdGenerator.generateDocumentId(ENTITY_URI, "dist-001", true);
        final String id2 = DocumentIdGenerator.generateDocumentId(ENTITY_URI, "dist-002", true);
        Assert.assertNotEquals(id1, id2);
    }

    @Test
    public void generateDocumentId_enabled_distributionIdUrlEncoded() {
        // A distribution ID with special characters should not throw and should produce a valid result
        final String result = DocumentIdGenerator.generateDocumentId(ENTITY_URI, "dist/with spaces&special=chars", true);
        Assert.assertTrue(result.startsWith(EXPECTED_PREFIX));
        Assert.assertEquals(result.length(), EXPECTED_PREFIX.length() + 64);
    }

    // --- enabledCombined=true with missing/blank distributionId: error cases ---

    @DataProvider
    public Object[][] blankDistributionIds() {
        return new Object[][] {
                { null },
                { "" },
                { "   " },
        };
    }

    @Test(dataProvider = "blankDistributionIds",
          expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*Distribution-Id.*")
    public void generateDocumentId_enabled_blankDistributionId_throwsIllegalState(String distributionId) {
        DocumentIdGenerator.generateDocumentId(ENTITY_URI, distributionId, true);
    }

    @Test
    public void generateDocumentId_enabled_blankDistributionId_errorMessageIsCorrect() {
        try {
            DocumentIdGenerator.generateDocumentId(ENTITY_URI, null, true);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), DocumentIdGenerator.MISSING_DISTRIBUTION_ID_ERROR);
        }
    }
}
