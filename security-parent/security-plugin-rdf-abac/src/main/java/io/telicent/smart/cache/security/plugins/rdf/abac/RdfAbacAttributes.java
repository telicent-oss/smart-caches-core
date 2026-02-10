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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.jena.abac.core.AttributesStoreRemote;
import io.telicent.smart.cache.security.attributes.MalformedAttributesException;
import io.telicent.smart.cache.security.attributes.UserAttributes;
import lombok.NonNull;

import java.util.*;

/**
 * Wraps RDF-ABAC {@link AttributeValueSet} in the Security Plugin API
 */
public class RdfAbacAttributes implements UserAttributes<AttributeValueSet> {
    private final AttributeValueSet attributes;
    private byte[] encoded;

    /**
     * Creates new user attributes
     *
     * @param attributes RDF-ABAC Attribute value set
     */
    RdfAbacAttributes(AttributeValueSet attributes) {
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    public AttributeValueSet decodedAttributes() {
        return this.attributes;
    }

    @Override
    public byte[] encoded() {
        if (this.encoded == null) {
            Map<String, Object> json = new LinkedHashMap<>();
            Map<String, Object> attrs = new LinkedHashMap<>();
            try {
                this.attributes.attributes().forEach(a -> attrs.put(a.name(), asValue(this.attributes.get(a))));
                json.put(AttributesStoreRemote.jAttributes, attrs);
                this.encoded = RdfAbac.JSON.writeValueAsBytes(json);
            } catch (Throwable e) {
                throw new MalformedAttributesException("Failed to calculate encoded bytes for RDF-ABAC AttributeValueSet");
            }
        }
        return this.encoded;
    }

    private Object asValue(Collection<ValueTerm> valueTerms) {
        if (valueTerms.size() == 1) {
            return valueTerms.iterator().next().asString();
        } else {
            return valueTerms.stream().map(ValueTerm::asString).toList();
        }
    }
}
