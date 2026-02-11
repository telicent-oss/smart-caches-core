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

import com.github.benmanes.caffeine.cache.Cache;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.jena.abac.core.CxtABAC;
import io.telicent.smart.cache.security.data.Authorizer;
import io.telicent.smart.cache.security.data.labels.SecurityLabels;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class RdfAbacAuthorizer implements Authorizer {
    @NonNull
    private final CxtABAC context;

    @NonNull
    private final Cache<AttributeExpr, Boolean> evaluationCache;

    /**
     * Gets the user attributes for this authorizer
     * @return User Attributes
     */
    AttributeValueSet userAttributes() {
        return this.context.requestAttributes();
    }

    @Override
    public boolean canRead(SecurityLabels<?> labels) {
        return evaluateRdfAbacLabels(labels);
    }

    /**
     * Evaluates RDF-ABAC label expressions
     *
     * @param labels Security Labels
     * @return True if the labels permit access, false if no access or not supported labels schema
     */
    protected boolean evaluateRdfAbacLabels(SecurityLabels<?> labels) {
        try {
            if (labels.decodedLabels() instanceof List<?> list) {
                List<AttributeExpr> expressions = list.stream()
                                                      .filter(Objects::nonNull)
                                                      .filter(e -> e instanceof AttributeExpr)
                                                      .map(e -> (AttributeExpr) e)
                                                      .toList();
                if (expressions.size() != list.size()) {
                    return FORBIDDEN;
                }
                return expressions.stream()
                                  .allMatch(e -> evaluationCache.get(e, x -> x.eval(this.context).getBoolean()));
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
    public void close() {
        // Proactively clean up the evaluation cache
        this.evaluationCache.invalidateAll();
    }
}
