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

import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeValue;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.smart.cache.security.plugins.AbstractSecurityPluginTests;
import io.telicent.smart.cache.security.plugins.SecurityPlugin;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.DataProvider;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestRdfAbacPlugin extends AbstractSecurityPluginTests {

    @Override
    protected SecurityPlugin getPlugin() {
        // We inject a custom AttributesStore for test purposes
        AttributesStoreLocal attributesStore = new AttributesStoreLocal();
        AttributeValueSet testAttributes = AttributeValueSet.of(
                List.of(AttributeValue.of("clearance", ValueTerm.value("S")),
                        AttributeValue.of("org", ValueTerm.value("Telicent"))));
        attributesStore.put("test", testAttributes);
        return new RdfAbacPlugin(attributesStore);
    }

    public static byte[] labelBytes(String label) {
        return label.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] prefixedLabelBytes(String label) {
        byte[] prefix = SecurityPlugin.encodeSchemaPrefix(RdfAbac.SCHEMA);
        byte[] labelBytes = labelBytes(label);

        byte[] prefixedLabel = new byte[prefix.length + labelBytes.length];
        ArrayUtils.arraycopy(prefix, 0, prefixedLabel, 0, prefix.length);
        ArrayUtils.arraycopy(labelBytes, 0, prefixedLabel, prefix.length, labelBytes.length);
        return prefixedLabel;
    }

    @DataProvider(name = "validLabels")
    @Override
    protected Object[][] validLabels() {
        return new Object[][] {
                { labelBytes("") },
                { labelBytes("clearance=S") },
                { prefixedLabelBytes("clearance=S") },
                { labelBytes("clearance=S && (org=foo || org=bar)") },
                { prefixedLabelBytes("clearance=S && (org=foo || org=bar)") },
                { labelBytes("clearance") },
                { labelBytes("clearance=S,(org=foo || org=bar)") },
        };
    }

    @DataProvider(name = "invalidLabels")
    @Override
    protected Object[][] invalidLabels() {
        return new Object[][] {
                { labelBytes("clearance=") },
                { labelBytes("white space=bad") },
                };
    }

    @DataProvider(name = "accessibleLabels")
    @Override
    protected Object[][] accessibleLabels() {
        return new Object[][] {
                { labelBytes("*") },
                { labelBytes("") },
                { labelBytes("clearance=S && org=Telicent") }
        };
    }

    @DataProvider(name = "forbiddenLabels")
    @Override
    protected Object[][] forbiddenLabels() {
        return new Object[][] {
                { labelBytes("clearance=TS") }
        };
    }
}
