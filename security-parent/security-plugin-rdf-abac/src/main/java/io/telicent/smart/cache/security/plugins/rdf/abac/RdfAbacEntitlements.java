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
import io.telicent.smart.cache.security.AbstractSecurityPrimitive;
import io.telicent.smart.cache.security.entitlements.Entitlements;

import java.util.Objects;

public class RdfAbacEntitlements extends AbstractSecurityPrimitive implements Entitlements<AttributeValueSet> {
    private final AttributeValueSet attributes;

    RdfAbacEntitlements(byte[] encoded, AttributeValueSet attributes) {
        super(RdfAbacPlugin.SCHEMA, encoded);
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    public AttributeValueSet decodedEntitlements() {
        return this.attributes;
    }
}
