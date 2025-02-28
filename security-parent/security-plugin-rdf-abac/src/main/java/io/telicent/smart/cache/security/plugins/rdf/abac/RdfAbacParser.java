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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.jena.abac.AE;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.jena.abac.core.AttributesStoreRemote;
import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.attributes.AttributesParser;
import io.telicent.smart.cache.security.attributes.MalformedAttributesException;
import io.telicent.smart.cache.security.labels.MalformedLabelsException;
import io.telicent.smart.cache.security.labels.SecurityLabels;
import io.telicent.smart.cache.security.labels.SecurityLabelsParser;
import io.telicent.smart.cache.security.labels.SecurityLabelsValidator;
import io.telicent.smart.cache.security.plugins.SecurityPlugin;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RdfAbacParser implements SecurityLabelsParser, AttributesParser,
        SecurityLabelsValidator {
    static final ObjectMapper JSON = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public UserAttributes<AttributeValueSet> parseAttributes(byte[] rawEntitlements) throws
            MalformedAttributesException {
        try {
            Map<String, Object> parsed = (Map<String, Object>) JSON.readValue(rawEntitlements, Map.class);
            Object rawAttributes = parsed.get(AttributesStoreRemote.jAttributes);
            if (rawAttributes == null) {
                return new RdfAbacEntitlements(rawEntitlements, AttributeValueSet.EMPTY);
            } else if (rawAttributes instanceof List<?> attributes) {
                return new RdfAbacEntitlements(rawEntitlements, AttributeValueSet.of(
                        attributes.stream()
                                  .filter(Objects::nonNull)
                                  .map(a -> a instanceof String ? (String) a : a.toString())
                                  .map(AE::parseAttrValue)
                                  .toList()));
            } else {
                throw new MalformedAttributesException(
                        "JSON Object contained " + AttributesStoreRemote.jAttributes + " value which was not an array of strings");
            }
        } catch (MalformedAttributesException e) {
            throw e;
        } catch (Throwable e) {
            throw new MalformedAttributesException("Failed to parse user attributes", e);
        }
    }

    @Override
    public SecurityLabels<List<AttributeExpr>> parseSecurityLabels(byte[] rawLabels) throws MalformedLabelsException {
        Short prefix = SecurityPlugin.decodeSchemaPrefix(rawLabels);
        if (prefix != null && prefix != RdfAbacPlugin.SCHEMA) {
            throw new MalformedLabelsException(
                    "Labels declares Schema ID " + prefix + " which does not match expected Schema ID " + RdfAbacPlugin.SCHEMA);
        }

        try {
            // Convert byte sequence into a string ignoring schema prefix if present
            // TODO Add parsing cache so frequently seen labels resolve to same reference which then allows for more effective caching in RdfAbacAuthorizer
            String labelStr = getLabelsString(rawLabels, prefix);
            return new RdfAbacLabels(rawLabels, AE.parseExprList(labelStr));
        } catch (Throwable e) {
            throw new MalformedLabelsException("Failed to parse security labels", e);
        }
    }

    private static String getLabelsString(byte[] rawLabels, Short prefix) {
        String labelStr;
        if (prefix != null) {
            labelStr = new String(rawLabels, 4, rawLabels.length - 4, StandardCharsets.UTF_8);
        } else {
            labelStr = new String(rawLabels, StandardCharsets.UTF_8);
        }
        return labelStr;
    }

    @Override
    public boolean validate(byte[] rawLabels) {
        Short prefix = SecurityPlugin.decodeSchemaPrefix(rawLabels);
        if (prefix != null && prefix != RdfAbacPlugin.SCHEMA) {
            return false;
        }
        try {
            AE.parseExprList(getLabelsString(rawLabels, prefix));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
