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

import io.jsonwebtoken.lang.Arrays;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.jena.abac.attributes.AttributeValue;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.jena.abac.attributes.syntax.AE_And;
import io.telicent.smart.cache.security.attributes.AttributesParser;
import io.telicent.smart.cache.security.attributes.MalformedAttributesException;
import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.labels.SecurityLabels;
import io.telicent.smart.cache.security.labels.SecurityLabelsParser;
import io.telicent.smart.cache.security.plugins.SecurityPlugin;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestRdfAbacParser {

    private final RdfAbacPlugin plugin = new RdfAbacPlugin();

    @Test
    public void givenAbacParser_whenParsingSameLabelManyTimes_thenSameParsedLabelsEachTime() {
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
    public void givenAbacParser_whenParsingUniqueLabels_thenParsedLabelIsUnique() {
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
                Assert.assertTrue(rawExprList.stream().map(e -> (AttributeExpr) e).allMatch(parsedExpressions::add));
            }
        }
    }

    @DataProvider(name = "attributes")
    private Object[][] attributes() {
        //@formatter:off
        return new Object[][] {
                { "{}", AttributeValueSet.EMPTY },
                {
                        """
                    {
                       "key": "value",
                       "other": 123
                    }
                    """, AttributeValueSet.EMPTY
                },
                {
                    """
                    {
                      "attributes": {
                        "email": "test@example.org",
                        "age": 42,
                        "name": "Mr T. Test",
                        "admin": false,
                        "user": "true"
                      }
                    }
                    """,
                    AttributeValueSet.of(List.of(AttributeValue.of("email", ValueTerm.value("test@example.org")),
                                                 AttributeValue.of("age", ValueTerm.value("42")),
                                                 AttributeValue.of("name", ValueTerm.value("Mr T. Test")),
                                                 AttributeValue.of("admin", ValueTerm.FALSE),
                                                 AttributeValue.of("user", ValueTerm.TRUE)))
                },
                {
                    """
                    {
                      "attributes": {
                        "email": "test@example.org",
                        "age": 42,
                        "name": "Mr T. Test",
                        "admin": "false",
                        "user": "true"
                      }
                    }
                    """,
                        AttributeValueSet.of(List.of(AttributeValue.of("email", ValueTerm.value("test@example.org")),
                                                     AttributeValue.of("age", ValueTerm.value("42")),
                                                     AttributeValue.of("name", ValueTerm.value("Mr T. Test")),
                                                     AttributeValue.of("admin", ValueTerm.FALSE),
                                                     AttributeValue.of("user", ValueTerm.TRUE)))
                },
                {
                    """
                    {
                      "attributes": {
                        "name": [
                          "Mr T. Test",
                          "Mr Timothy Test",
                          "Test"
                        ]
                      }
                    }
                    """,
                    AttributeValueSet.of(List.of(AttributeValue.of("name", ValueTerm.value("Mr T. Test")),
                                                 AttributeValue.of("name", ValueTerm.value("Mr Timothy Test")),
                                                 AttributeValue.of("name", ValueTerm.value("Test"))))
                }
        };
        //@formatter:on
    }

    @Test(dataProvider = "attributes")
    public void givenAbacParser_whenParsingAttributes_thenParsedAsExpected_andCanObtainEncodedForm(String rawJson, AttributeValueSet expected) {
        // Given
        AttributesParser parser = plugin.attributesParser();

        // When
        UserAttributes<?> attributes = parser.parseAttributes(rawJson.getBytes(StandardCharsets.UTF_8));

        // Then
        Assert.assertNotNull(attributes);
        Assert.assertNotNull(attributes.decodedAttributes());
        if (attributes.decodedAttributes() instanceof AttributeValueSet attrValueSet) {
            Assert.assertEquals(attrValueSet, expected);
        } else {
            Assert.fail("Parsed attributes were not an AttributeValueSet");
        }

        // And
        byte[] encoded = attributes.encoded();
        Assert.assertNotEquals(encoded.length, 0);
        byte[] encodedAgain = attributes.encoded();
        Assert.assertSame(encodedAgain, encoded);
    }

    @DataProvider(name = "badAttributes")
    private Object[][] badAttributes() {
        return new Object[][] {
                // Unterminated JSON object
                { "{" },
                // Unterminated JSON key
                {
                        """
                    {
                      "attributes
                    """
                },
                // Missing value
                {
                        """
                    {
                      "attributes":
                    }
                    """
                },
                // Wrong value type
                {
                        """
                    {
                      "attributes": []
                    }
                    """
                },
                // Null Attribute value
                {
                        """
                    {
                      "attributes": {
                        "name": null
                      }
                    }
                    """
                },
                };
    }

    @Test(dataProvider = "badAttributes", expectedExceptions = MalformedAttributesException.class)
    public void givenAbacParser_whenParsingBadAttributes_thenFailsToParse(String badJson) {
        // Given
        AttributesParser parser = plugin.attributesParser();

        // When and Then
        parser.parseAttributes(badJson.getBytes(StandardCharsets.UTF_8));
    }
}
