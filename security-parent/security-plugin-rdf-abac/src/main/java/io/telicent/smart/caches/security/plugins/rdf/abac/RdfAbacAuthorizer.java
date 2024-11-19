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

import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.jena.abac.core.CxtABAC;
import io.telicent.smart.caches.security.Authorizer;
import io.telicent.smart.caches.security.labels.SecurityLabels;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class RdfAbacAuthorizer implements Authorizer {
    @NonNull
    private final CxtABAC context;

    @Override
    public boolean canAccess(SecurityLabels<?> labels) {
        try {
            // Can't make access decisions for labels not in our supported schema
            if (labels.schema() != RdfAbacPlugin.SCHEMA) {
                return FORBIDDEN;
            }

            if (labels.decodedLabels() instanceof List<?> list) {
                List<AttributeExpr> expressions =
                        list.stream()
                            .filter(Objects::nonNull)
                            .filter(e -> e instanceof AttributeExpr)
                            .map(e -> (AttributeExpr) e)
                            .toList();
                if (expressions.size() != list.size()) {
                    return FORBIDDEN;
                }
                // TODO Add attribute expression evaluation cache
                return expressions.stream().allMatch(e -> e.eval(this.context).getBoolean());
            } else {
                // Can't make access decisions if the labels have been decoded into a different data structure than we expect
                return FORBIDDEN;
            }
        } catch (Throwable e) {
            // Any error is treated as forbidden
            return FORBIDDEN;
        }
    }

    @Override
    public void close() throws Exception {
        // No-op, we just rely on the garbage collector cleaning up
    }
}
