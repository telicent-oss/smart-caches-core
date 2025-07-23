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
package io.telicent.smart.cache.security.plugins.rdf.abac;

import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.smart.cache.security.labels.MalformedLabelsException;
import io.telicent.smart.cache.security.labels.SecurityLabels;
import io.telicent.smart.cache.security.labels.SecurityLabelsParser;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestRdfAbacParser {

    private final RdfAbacPlugin plugin = new RdfAbacPlugin();

    @Test
    public void givenAbacParser_whenParsingSameLabelManyTimes_thenSameParsedLabelsEachTime() throws
            MalformedLabelsException {
        // Given
        SecurityLabelsParser parser = plugin.labelsParser();
        byte[] rawLabels = "clearance=S&&nationality=GBR".getBytes(StandardCharsets.UTF_8);

        // When
        SecurityLabels<?> parsed = parser.parseSecurityLabels(rawLabels);

        for (int i = 0; i < 10_000; i++) {
            // Then
            SecurityLabels<?> parsedAgain = parser.parseSecurityLabels(rawLabels);
            Assert.assertNotSame(parsed, parsedAgain);
            Assert.assertSame(parsed.decodedLabels(), parsedAgain.decodedLabels());
        }
    }

    @Test
    public void givenAbacParser_whenParsingUniqueLabels_thenParsedLabelIsUnique() throws MalformedLabelsException {
        // Given
        SecurityLabelsParser parser = plugin.labelsParser();
        Set<AttributeExpr> parsedExpressions = new HashSet<>();

        // When
        for (int i = 0; i < 10_000; i++) {
            byte[] uniqueLabel = ("username=user" + i).getBytes(StandardCharsets.UTF_8);
            SecurityLabels<?> parsed = parser.parseSecurityLabels(uniqueLabel);

            // Then
            if (parsed.decodedLabels() instanceof List<?> rawExprList) {
                Assert.assertFalse(rawExprList.isEmpty());
                Assert.assertTrue(
                        rawExprList.stream().map(e -> (AttributeExpr) e).allMatch(parsedExpressions::add));
            }
        }
    }
}
