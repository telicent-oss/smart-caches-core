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
package io.telicent.smart.caches.security.plugins.rdf.abac;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.jena.abac.AE;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.jena.abac.core.AttributesStoreRemote;
import io.telicent.smart.caches.security.entitlements.Entitlements;
import io.telicent.smart.caches.security.entitlements.EntitlementsParser;
import io.telicent.smart.caches.security.entitlements.MalformedEntitlementsException;
import io.telicent.smart.caches.security.labels.MalformedLabelsException;
import io.telicent.smart.caches.security.labels.SecurityLabels;
import io.telicent.smart.caches.security.labels.SecurityLabelsParser;
import io.telicent.smart.caches.security.labels.SecurityLabelsValidator;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RdfAbacParser implements SecurityLabelsParser<List<AttributeExpr>>, EntitlementsParser<AttributeValueSet>,
        SecurityLabelsValidator {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public Entitlements<AttributeValueSet> parseEntitlements(byte[] rawEntitlements) throws
            MalformedEntitlementsException {
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
                throw new MalformedEntitlementsException(
                        "JSON Object contained " + AttributesStoreRemote.jAttributes + " value which was not an array of strings");
            }
        } catch (MalformedEntitlementsException e) {
            throw e;
        } catch (Throwable e) {
            throw new MalformedEntitlementsException("Failed to parse user attributes", e);
        }
    }

    @Override
    public SecurityLabels<List<AttributeExpr>> parseSecurityLabels(byte[] rawLabels) throws MalformedLabelsException {
        try {
            return new RdfAbacLabels(rawLabels, AE.parseExprList(new String(rawLabels, StandardCharsets.UTF_8)));
        } catch (Throwable e) {
            throw new MalformedLabelsException("Failed to parse security labels", e);
        }
    }

    @Override
    public boolean validate(byte[] rawLabels) {
        try {
            AE.parseExprList(new String(rawLabels, StandardCharsets.UTF_8));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
