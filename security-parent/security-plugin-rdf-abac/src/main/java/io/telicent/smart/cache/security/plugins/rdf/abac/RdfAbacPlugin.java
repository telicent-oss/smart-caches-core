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

import com.apicatalog.jsonld.StringUtils;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.core.AttributesStore;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.jena.abac.core.AttributesStoreRemote;
import io.telicent.jena.abac.core.CxtABAC;
import io.telicent.jena.abac.labels.Labels;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.security.labels.*;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import io.telicent.smart.cache.security.Authorizer;
import io.telicent.smart.cache.security.entitlements.Entitlements;
import io.telicent.smart.cache.security.entitlements.EntitlementsParser;
import io.telicent.smart.cache.security.entitlements.EntitlementsProvider;
import io.telicent.smart.cache.security.entitlements.MalformedEntitlementsException;
import io.telicent.smart.cache.security.identity.DefaultIdentityProvider;
import io.telicent.smart.cache.security.identity.IdentityProvider;
import io.telicent.smart.cache.security.plugins.SecurityPlugin;
import io.telicent.smart.cache.security.plugins.failsafe.FailSafeAuthorizer;
import io.telicent.smart.cache.security.plugins.failsafe.RawPrimitive;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * The RDF ABAC Security plugin
 */
public class RdfAbacPlugin implements SecurityPlugin {

    /**
     * Telicent's original RDF-ABAC labels schema is grandfathered in as Schema ID 0
     */
    public static final short SCHEMA = 0;

    static {
        // RDF-ABAC relies heavily on Apache Jena so make sure it is initialized up front
        JenaSystem.init();
    }

    private static final RdfAbacParser PARSER = new RdfAbacParser();

    private final AttributesStore attributesStore;

    public RdfAbacPlugin() {
        String attributesUrl = Configurator.get(AuthConstants.ENV_USER_ATTRIBUTES_URL);
        String hierarchyUrl = Configurator.get(new String[] { AuthConstants.ENV_ATTRIBUTE_HIERARCHY_URL },
                                               AuthConstants.calculateHierarchyLookupUrl(attributesUrl));

        if (StringUtils.isBlank(attributesUrl)) {
            this.attributesStore = new AttributesStoreLocal();
        } else {
            this.attributesStore = new AttributesStoreRemote(attributesUrl, hierarchyUrl);
        }
    }

    /**
     * Package only constructor for testing purposes, allows injecting an arbitrary attributes store
     *
     * @param attributesStore Attributes store
     */
    RdfAbacPlugin(AttributesStore attributesStore) {
        // TODO Should probably log a warning in case someone uses this in a testing environment
        this.attributesStore = Objects.requireNonNull(attributesStore, "Attributes Store cannot be null");
    }


    @Override
    public short defaultSchema() {
        return SCHEMA;
    }

    @Override
    public boolean supportsSchema(short schema) {
        return schema == SCHEMA;
    }

    @Override
    public IdentityProvider identityProvider() {
        return DefaultIdentityProvider.INSTANCE;
    }

    @Override
    public EntitlementsParser entitlementsParser() {
        return PARSER;
    }

    @Override
    public EntitlementsProvider entitlementsProvider() {
        return context -> {
            AttributeValueSet attributes = this.attributesStore.attributes(context.username());
            if (attributes == null) {
                return new RdfAbacEntitlements(new byte[0], AttributeValueSet.EMPTY);
            }

            // TODO Could create a lazy Entitlements implementation so we don't need to generate the encoded form up front?
            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            List<String> attrList = new ArrayList<>();
            attributes.attributeValues(v -> attrList.add(v.asString()));
            json.put("attributes", attrList);
            try {
                byte[] encoded = RdfAbacParser.JSON.writeValueAsBytes(json);
                return new RdfAbacEntitlements(encoded, attributes);
            } catch (Throwable e) {
                throw new MalformedEntitlementsException("Failed to encode user attributes to JSON", e);
            }
        };
    }

    @Override
    public SecurityLabelsParser labelsParser() {
        return PARSER;
    }

    @Override
    public SecurityLabelsValidator labelsValidator() {
        return PARSER;
    }

    @Override
    public SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel,
                                                            Graph labelsGraph) throws MalformedLabelsException {
        if (labelsGraph == null || labelsGraph.isEmpty()) {
            Short prefix = SecurityPlugin.decodeSchemaPrefix(defaultLabel);
            if (prefix == null || prefix == SCHEMA) {
                return new DefaultLabelApplicator(PARSER.parseSecurityLabels(defaultLabel));
            } else {
                return new DefaultLabelApplicator(new RawPrimitive(prefix, defaultLabel));
            }
        }
        // TODO Need to pull DefaultingLabelsStore from SC-Search into rdf-abac-core as right now defaultLabel won't be honoured
        // TODO Ideally LabelsStore interface needs to change signature to make plugin implementation cleaner
        return new RdfAbacApplicator(Labels.createLabelsStoreMem(labelsGraph));
    }

    @Override
    public Authorizer prepareAuthorizer(Entitlements<?> entitlements) {
        if (entitlements == null) {
            return FailSafeAuthorizer.INSTANCE;
        } else if (entitlements.decodedEntitlements() instanceof AttributeValueSet attributes) {
            CxtABAC context = CxtABAC.context(attributes, this.attributesStore, DatasetGraphFactory.empty());
            return new RdfAbacAuthorizer(context);
        } else {
            // Entitlements in wrong format falls back to refusing access
            return FailSafeAuthorizer.INSTANCE;
        }
    }

}
