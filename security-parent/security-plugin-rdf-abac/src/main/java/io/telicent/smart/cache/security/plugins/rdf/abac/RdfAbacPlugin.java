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

import com.apicatalog.jsonld.StringUtils;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.core.*;
import io.telicent.jena.abac.labels.Labels;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.observability.LibraryVersion;
import io.telicent.smart.cache.security.attributes.UserAttributes;
import io.telicent.smart.cache.security.labels.*;
import io.telicent.smart.cache.security.plugins.rdf.abac.utils.DefaultingLabelsStore;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import io.telicent.smart.cache.security.Authorizer;
import io.telicent.smart.cache.security.attributes.AttributesParser;
import io.telicent.smart.cache.security.attributes.AttributesProvider;
import io.telicent.smart.cache.security.identity.DefaultIdentityProvider;
import io.telicent.smart.cache.security.identity.IdentityProvider;
import io.telicent.smart.cache.security.plugins.SecurityPlugin;
import io.telicent.smart.cache.security.plugins.failsafe.FailSafeAuthorizer;
import io.telicent.smart.cache.security.plugins.failsafe.RawPrimitive;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * The RDF ABAC Security plugin
 */
public class RdfAbacPlugin implements SecurityPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdfAbacPlugin.class);

    static {
        // RDF-ABAC relies heavily on Apache Jena so make sure it is initialized up front
        JenaSystem.init();
    }

    private static final RdfAbacParser PARSER = new RdfAbacParser();

    private final AttributesStore attributesStore;
    private final int evaluationCacheSize;

    /**
     * Creates a new RDF-ABAC security plugin
     */
    public RdfAbacPlugin() {
        if (StringUtils.isNotBlank(Configurator.get(AuthConstants.ENV_USERINFO_URL))) {
            this.attributesStore = new AttributesStoreAuthServer(null);
        } else {
            String attributesUrl = Configurator.get(AuthConstants.ENV_USER_ATTRIBUTES_URL);
            String hierarchyUrl = Configurator.get(new String[] { AuthConstants.ENV_ATTRIBUTE_HIERARCHY_URL },
                                                   AuthConstants.calculateHierarchyLookupUrl(attributesUrl));
            if (StringUtils.isBlank(attributesUrl)) {
                this.attributesStore = new AttributesStoreLocal();
            } else {
                this.attributesStore = new AttributesStoreRemote(attributesUrl, hierarchyUrl);
            }
        }

        this.evaluationCacheSize =
                Configurator.get(new String[] { RdfAbac.ENV_LABEL_EVALUATION_CACHE_SIZE }, Integer::parseInt,
                                 RdfAbac.DEFAULT_EVALUATION_CACHE_SIZE);
        logPluginInfo();
    }

    private void logPluginInfo() {
        LOGGER.info("RDF-ABAC Plugin Version {}", LibraryVersion.get("security-plugin-rdf-abac"));
        LOGGER.info("Label Evaluation Cache size is {}", this.evaluationCacheSize);
        LOGGER.info("Label Parser Configuration is {}", PARSER);
    }

    /**
     * Package only constructor for testing purposes, allows injecting an arbitrary attributes store
     *
     * @param attributesStore Attributes store
     */
    RdfAbacPlugin(AttributesStore attributesStore) {
        LOGGER.warn(
                "RdfAbacPlugin instantiated using development only constructor.  If you see this message in a production environment cease operating the system immediately and raise a support request.");
        this.attributesStore = Objects.requireNonNull(attributesStore, "Attributes Store cannot be null");
        this.evaluationCacheSize = RdfAbac.DEFAULT_EVALUATION_CACHE_SIZE;
        logPluginInfo();
    }


    @Override
    public short defaultSchema() {
        return RdfAbac.SCHEMA;
    }

    @Override
    public boolean supportsSchema(short schema) {
        return schema == RdfAbac.SCHEMA;
    }

    @Override
    public IdentityProvider identityProvider() {
        return DefaultIdentityProvider.INSTANCE;
    }

    @Override
    public AttributesParser entitlementsParser() {
        return PARSER;
    }

    @Override
    public AttributesProvider attributesProvider() {
        return context -> {
            AttributeValueSet attributes = this.attributesStore.attributes(context.username());
            if (attributes == null) {
                return new RdfAbacAttributes(AttributeValueSet.EMPTY);
            }

            return new RdfAbacAttributes(attributes);
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
    public SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel, Graph labelsGraph) {
        if (labelsGraph == null || labelsGraph.isEmpty()) {
            Short prefix = SecurityPlugin.decodeSchemaPrefix(defaultLabel);
            if (prefix == null || prefix == RdfAbac.SCHEMA) {
                return new DefaultLabelApplicator(PARSER.parseSecurityLabels(defaultLabel));
            } else {
                return new DefaultLabelApplicator(new RawPrimitive(prefix, defaultLabel));
            }
        }
        // TODO Ideally LabelsStore interface needs to change signature to make plugin implementation cleaner
        return new RdfAbacApplicator(PARSER,
                                     new DefaultingLabelsStore(Labels.createLabelsStoreMem(labelsGraph), defaultLabel));
    }

    @Override
    public Authorizer prepareAuthorizer(UserAttributes<?> userAttributes) {
        if (userAttributes == null) {
            return FailSafeAuthorizer.INSTANCE;
        } else if (userAttributes.decodedAttributes() instanceof AttributeValueSet attributes) {
            CxtABAC context = CxtABAC.context(attributes, this.attributesStore, DatasetGraphFactory.empty());
            return new RdfAbacAuthorizer(context, Caffeine.newBuilder().maximumSize(this.evaluationCacheSize).build());
        } else {
            // Entitlements in wrong format falls back to refusing access
            return FailSafeAuthorizer.INSTANCE;
        }
    }

}
