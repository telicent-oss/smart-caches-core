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

import com.github.benmanes.caffeine.cache.Caffeine;
import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeValue;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.jena.abac.core.*;
import io.telicent.jena.abac.labels.Labels;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.observability.LibraryVersion;
import io.telicent.smart.cache.security.data.labels.DefaultLabelApplicator;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsApplicator;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsParser;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsValidator;
import io.telicent.smart.cache.security.data.plugins.rdf.abac.utils.DefaultingLabelsStore;
import io.telicent.smart.cache.security.data.requests.RequestContext;
import io.telicent.smart.cache.security.data.Authorizer;
import io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin;
import io.telicent.smart.cache.security.data.plugins.failsafe.FailSafeAuthorizer;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The RDF ABAC Security plugin
 */
public class RdfAbacPlugin implements DataSecurityPlugin {

    static final Logger LOGGER = LoggerFactory.getLogger(RdfAbacPlugin.class);

    static {
        // RDF-ABAC relies heavily on Apache Jena so make sure it is initialized up front
        JenaSystem.init();
    }

    private static final RdfAbacParser PARSER = new RdfAbacParser();

    @Getter
    private final int evaluationCacheSize;

    /**
     * Creates a new RDF-ABAC security plugin
     */
    public RdfAbacPlugin() {
        this.evaluationCacheSize =
                Configurator.get(new String[] { RdfAbac.ENV_LABEL_EVALUATION_CACHE_SIZE }, Integer::parseInt,
                                 RdfAbac.DEFAULT_EVALUATION_CACHE_SIZE);
        logPluginInfo();
    }

    /**
     * Logs diagnostic information about the security plugin
     */
    private void logPluginInfo() {
        LOGGER.info("RDF-ABAC Plugin Version {}", LibraryVersion.get("data-security-plugin-rdf-abac"));
        LOGGER.info("Label Evaluation Cache size is {}", this.evaluationCacheSize);
        LOGGER.info("Label Parser Configuration is {}", PARSER);
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
            return new DefaultLabelApplicator(PARSER.parseSecurityLabels(defaultLabel));
        }
        // TODO Ideally LabelsStore interface needs to change signature to make plugin implementation cleaner
        return new RdfAbacApplicator(PARSER,
                                     new DefaultingLabelsStore(Labels.createLabelsStoreMem(labelsGraph), defaultLabel));
    }

    @Override
    public Authorizer prepareAuthorizer(RequestContext context) {
        if (context == null) {
            return FailSafeAuthorizer.INSTANCE;
        } else {
            // TODO Compute attributes from UserInfo here
            AttributeValueSet attributes;
            if (context.userInfo() != null) {
                attributes = toAttributeValueSet(context.userInfo().getAttributes());
            } else {
                attributes = AttributeValueSet.EMPTY;
            }
            CxtABAC abacContext =
                    CxtABAC.context(attributes, RdfAbac::getClassificationHierarchy, DatasetGraphFactory.empty());
            return new RdfAbacAuthorizer(abacContext,
                                         Caffeine.newBuilder().maximumSize(this.evaluationCacheSize).build());
        }
    }

    @Override
    public void close() {
        // No-op
    }

    /**
     * Converts users raw attributes into RDF-ABAC compatible attributes
     *
     * @param attributes Raw attributes
     * @return RDF-ABAC Attribute Value set
     */
    private AttributeValueSet toAttributeValueSet(Map<String, Object> attributes) {
        List<AttributeValue> attrs = new ArrayList<>();
        convertMapToAttributes(attrs, "", attributes);
        return AttributeValueSet.of(attrs);
    }

    private static void convertMapToAttributes(List<AttributeValue> attrs, String prefix, Map<String, Object> map) {
        if (MapUtils.isEmpty(map)) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            convertValue(attrs, !prefix.isEmpty() ? prefix + "." + entry.getKey() : entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static void convertValue(List<AttributeValue> attrs, String key, Object value) {
        // TODO Once we upgrade to JDK 21+ can simplify this into a switch statement
        if (value instanceof String strValue) {
            attrs.add(AttributeValue.of(key, ValueTerm.value(strValue)));
        } else if (value instanceof Number numberValue) {
            attrs.add(AttributeValue.of(key, ValueTerm.value(numberValue.toString())));
        } else if (value instanceof Boolean boolValue) {
            attrs.add(AttributeValue.of(key, ValueTerm.value(boolValue)));
        } else if (value instanceof Map<?, ?> map) {
            Map<String, Object> values = (Map<String, Object>) map;
            convertMapToAttributes(attrs, key, values);
        } else if (value instanceof Collection<?> collection) {
            Collection<Object> values = (Collection<Object>) collection;
            for (Object v : values) {
                convertValue(attrs, key, v);
            }
        } else {
            LOGGER.warn("Unsupported value type for attribute {} ignored: {}", key, value.getClass());
        }
    }

}
