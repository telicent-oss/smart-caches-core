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
package io.telicent.smart.cache.security.data.plugins.rdf.abac;

import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.smart.cache.security.data.AbstractSecurityPrimitive;
import io.telicent.smart.cache.security.data.labels.SecurityLabels;

import java.util.List;
import java.util.Objects;

public class RdfAbacLabels extends AbstractSecurityPrimitive implements SecurityLabels<List<AttributeExpr>> {

    private final List<AttributeExpr> expressions;

    RdfAbacLabels(byte[] encoded, List<AttributeExpr> expressions) {
        super(encoded);
        this.expressions = Objects.requireNonNull(expressions);
    }

    @Override
    public List<AttributeExpr> decodedLabels() {
        return this.expressions;
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.expressions.size(); i++) {
            sb.append(this.expressions.get(i).toString());
            if (i < this.expressions.size() - 1) {
                sb.append(" && ");
            }
        }
        return sb.toString();
    }
}
