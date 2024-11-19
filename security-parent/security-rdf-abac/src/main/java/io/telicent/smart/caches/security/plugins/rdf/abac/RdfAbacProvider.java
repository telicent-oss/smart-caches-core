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

import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.jena.abac.core.CxtABAC;
import io.telicent.smart.caches.security.AuthorizationProvider;
import io.telicent.smart.caches.security.Authorizer;
import org.apache.jena.sparql.core.DatasetGraphFactory;

import java.util.List;

public class RdfAbacProvider implements AuthorizationProvider<AttributeValueSet, List<AttributeExpr>> {
    @Override
    public Authorizer<List<AttributeExpr>> prepare(AttributeValueSet entitlements) {
        // TODO Support for remote attributes store for hierarchies
        CxtABAC context = CxtABAC.context(entitlements, x -> null, DatasetGraphFactory.empty());
        return new RdfAbacAuthorizer(context);
    }
}
