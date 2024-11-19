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
import io.telicent.smart.caches.security.AuthorizationProvider;
import io.telicent.smart.caches.security.entitlements.EntitlementsParser;
import io.telicent.smart.caches.security.entitlements.EntitlementsProvider;
import io.telicent.smart.caches.security.identity.DefaultIdentityProvider;
import io.telicent.smart.caches.security.identity.IdentityProvider;
import io.telicent.smart.caches.security.labels.SecurityLabelsParser;
import io.telicent.smart.caches.security.labels.SecurityLabelsValidator;
import io.telicent.smart.caches.security.plugins.SecurityPlugin;
import org.apache.jena.sys.JenaSystem;

import java.util.List;

/**
 * The RDF ABAC Security plugin
 */
public class RdfAbacPlugin implements SecurityPlugin<AttributeValueSet, List<AttributeExpr>> {

    static {
        // RDF-ABAC relies heavily on Apache Jena so make sure it is initialized up front
        JenaSystem.init();
    }

    private static final RdfAbacParser PARSER = new RdfAbacParser();
    private static final RdfAbacProvider AUTHORIZATION_PROVIDER = new RdfAbacProvider();

    @Override
    public IdentityProvider identityProvider() {
        return DefaultIdentityProvider.INSTANCE;
    }

    @Override
    public EntitlementsParser<AttributeValueSet> entitlementsParser() {
        return PARSER;
    }

    @Override
    public EntitlementsProvider<AttributeValueSet> entitlementsProvider() {
        // TODO Use an AttributesStoreRemote instance here
        return null;
    }

    @Override
    public SecurityLabelsParser<List<AttributeExpr>> labelsParser() {
        return PARSER;
    }

    @Override
    public SecurityLabelsValidator labelsValidator() {
        return PARSER;
    }

    @Override
    public AuthorizationProvider<AttributeValueSet, List<AttributeExpr>> authorizationProvider() {
        return AUTHORIZATION_PROVIDER;
    }
}
