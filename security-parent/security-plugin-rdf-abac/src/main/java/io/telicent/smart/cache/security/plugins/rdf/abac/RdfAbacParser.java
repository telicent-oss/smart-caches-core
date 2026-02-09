/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.cache.security.plugins.rdf.abac;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.telicent.jena.abac.AE;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.jena.abac.attributes.AttributeValue;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.jena.abac.core.AttributesStoreRemote;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.attributes.AttributesParser;
import io.telicent.smart.cache.security.attributes.MalformedAttributesException;
import io.telicent.smart.cache.security.labels.MalformedLabelsException;
import io.telicent.smart.cache.security.labels.SecurityLabels;
import io.telicent.smart.cache.security.labels.SecurityLabelsParser;
import io.telicent.smart.cache.security.labels.SecurityLabelsValidator;
import io.telicent.smart.cache.security.plugins.SecurityPlugin;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RdfAbacParser implements SecurityLabelsParser, AttributesParser, SecurityLabelsValidator {

    private final Cache<String, List<AttributeExpr>> labelParserCache;
    private final int cacheSize;
    private final Duration cacheDuration;

    /**
     * Creates a new parser
     */
    public RdfAbacParser() {
        this.cacheSize = Configurator.get(new String[] { RdfAbac.ENV_PARSER_CACHE_SIZE }, Integer::parseInt,
                                          RdfAbac.DEFAULT_PARSER_CACHE_SIZE);
        this.cacheDuration = Configurator.get(new String[] { RdfAbac.ENV_PARSER_CACHE_DURATION }, Duration::parse,
                                              Duration.ofMinutes(5));
        int initialCacheSize =
                Math.min(RdfAbac.DEFAULT_PARSER_CACHE_MIN_SIZE, Math.max(this.cacheSize, this.cacheSize / 10));
        this.labelParserCache = Caffeine.newBuilder()
                                        .expireAfterAccess(this.cacheDuration)
                                        .initialCapacity(initialCacheSize)
                                        .maximumSize(this.cacheSize)
                                        .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public UserAttributes<AttributeValueSet> parseAttributes(byte[] rawAttributes) {
        try {
            Map<String, Object> parsed = (Map<String, Object>) RdfAbac.JSON.readValue(rawAttributes, Map.class);
            if (parsed.isEmpty() || !parsed.containsKey(AttributesStoreRemote.jAttributes)) {
                return new RdfAbacAttributes(AttributeValueSet.EMPTY);
            } else {
                // Following code assumes a Telicent Auth Server User Info JSON response where attributes is one of the
                // keys which contains a map of user attributes
                parsed = (Map<String, Object>) parsed.get(AttributesStoreRemote.jAttributes);
                List<AttributeValue> attributes = new ArrayList<>();
                for (Map.Entry<String, Object> entry : parsed.entrySet()) {
                    if (entry.getValue() instanceof List<?> values) {
                        for (Object value : values) {
                            attributes.add(toAttribute(entry.getKey(), value));
                        }
                    } else {
                        attributes.add(toAttribute(entry));
                    }
                }
                return new RdfAbacAttributes(AttributeValueSet.of(attributes));
            }
        } catch (MalformedAttributesException e) {
            throw e;
        } catch (Throwable e) {
            throw new MalformedAttributesException("Failed to parse user attributes", e);
        }
    }

    private AttributeValue toAttribute(Map.Entry<String, Object> entry) {
        return toAttribute(entry.getKey(), entry.getValue());
    }

    private AttributeValue toAttribute(String name, Object value) {
        if (value == null) {
            throw new MalformedAttributesException("Unexpected null attribute value for '" + name + "'");
        } else if (value instanceof String strValue) {
            if (Objects.equals(strValue, "true")) {
                return AttributeValue.of(name, ValueTerm.TRUE);
            } else if (Objects.equals(strValue, "false")) {
                return AttributeValue.of(name, ValueTerm.FALSE);
            } else {
                return AttributeValue.of(name, ValueTerm.value(strValue));
            }
        } else if (value instanceof Boolean boolValue) {
            return AttributeValue.of(name, ValueTerm.value(boolValue));
        } else {
            return AttributeValue.of(name, ValueTerm.value(value.toString()));
        }
    }

    @Override
    public SecurityLabels<List<AttributeExpr>> parseSecurityLabels(byte[] rawLabels) {
        Short prefix = SecurityPlugin.decodeSchemaPrefix(rawLabels);
        if (prefix != null && prefix != RdfAbac.SCHEMA) {
            throw new MalformedLabelsException(
                    "Labels declares Schema ID " + prefix + " which does not match expected Schema ID " + RdfAbac.SCHEMA);
        }

        try {
            // Convert byte sequence into a string ignoring schema prefix if present
            String labelStr = getLabelsString(rawLabels, prefix);
            return new RdfAbacLabels(rawLabels, this.labelParserCache.get(labelStr, AE::parseExprList));
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
        if (prefix != null && prefix != RdfAbac.SCHEMA) {
            return false;
        }
        try {
            AE.parseExprList(getLabelsString(rawLabels, prefix));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "RdfAbacParser{cacheSize=" + this.cacheSize + ", cacheDuration=" + this.cacheDuration + "}";
    }
}
