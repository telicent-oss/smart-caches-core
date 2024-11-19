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

@AllArgsConstructor
public class RdfAbacAuthorizer implements Authorizer<List<AttributeExpr>> {
    @NonNull
    private final CxtABAC context;

    // TODO Add attribute expression evaluation cache

    @Override
    public boolean canAccess(SecurityLabels<List<AttributeExpr>> labels) {
        try {
            return labels.decodedLabels().stream().allMatch(e -> e.eval(this.context).getBoolean());
        } catch (Throwable e) {
            // Any error is treated as no access
            return false;
        }
    }
}
