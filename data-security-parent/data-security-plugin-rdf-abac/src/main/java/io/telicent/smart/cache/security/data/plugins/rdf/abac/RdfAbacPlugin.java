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
import io.telicent.jena.abac.core.CxtABAC;
import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.fuseki.FMod_ABAC;
import io.telicent.jena.abac.fuseki.ServerABAC;
import io.telicent.jena.abac.labels.node.LabelToNodeGenerator;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.observability.LibraryVersion;
import io.telicent.smart.cache.security.data.DataAccessAuthorizer;
import io.telicent.smart.cache.security.data.distribution.DistributionLifecycleFilters;
import io.telicent.smart.cache.security.data.distribution.DistributionLifecycleStateFile;
import io.telicent.smart.cache.security.data.labels.*;
import io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin;
import io.telicent.smart.cache.security.data.plugins.failsafe.FailSafeAuthorizer;
import io.telicent.smart.cache.security.data.plugins.rdf.abac.distribution.RdfAbacDistributionLifecycleFilters;
import io.telicent.smart.cache.security.data.requests.RequestContext;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.kafka.common.FusekiSink;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.sparql.core.DatasetGraph;
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
                Configurator.get(new String[]{RdfAbac.ENV_LABEL_EVALUATION_CACHE_SIZE}, Integer::parseInt,
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
    public SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel, DatasetGraph datasetGraph) {
        if (datasetGraph instanceof DatasetGraphABAC datasetGraphABAC) {
            return new RdfAbacApplicator(PARSER, datasetGraphABAC.labelsStore());
        } else {
            return new DefaultLabelApplicator(PARSER.parseSecurityLabels(defaultLabel));
        }
    }

    @Override
    public DataAccessAuthorizer prepareAuthorizer(RequestContext context) {
        if (context == null) {
            return FailSafeAuthorizer.INSTANCE;
        } else {
            if (context.userInfo() != null) {
                final AttributeValueSet attributes = toAttributeValueSet(context.userInfo().getAttributes());
                CxtABAC abacContext =
                        CxtABAC.context(attributes, RdfAbac::getClassificationHierarchy, DatasetGraphFactory.empty());
                return new RdfAbacAuthorizer(abacContext,
                        Caffeine.newBuilder().maximumSize(this.evaluationCacheSize).build());
            } else {
                return FailSafeAuthorizer.INSTANCE;
            }
        }
    }

    @Override
    public Optional<SecurityLabelsBackup> prepareLabelsBackup() {
        return Optional.of(new RdfAbacLabelsBackup());
    }

    @Override
    public Optional<SecurityLabelsRestore> prepareLabelsRestore() {
        return Optional.of(new RdfAbacLabelsRestore());
    }

    @Override
    public Optional<SecurityLabelsCompact> prepareLabelsCompact() {
        return Optional.of(new RdfAbacLabelsCompact());
    }

    @Override
    public Optional<SecurityLabelsRemover> prepareLabelsRemover() {
        return Optional.of(new RdfAbacLabelsRemover());
    }

    @Override
    public Optional<FusekiModule> prepareLabelsModule() {
        return Optional.of(new FMod_ABAC());
    }

    @Override
    public Optional<FusekiSink<?>> prepareFusekiSink(DatasetGraph datasetGraph, boolean routeToNamedGraphs, DistributionLifecycleStateFile lifecycleStateFile) {
        if (datasetGraph instanceof DatasetGraphABAC datasetGraphABAC) {
            return Optional.of(new RdfAbacSink(datasetGraphABAC, routeToNamedGraphs, lifecycleStateFile));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public LabelToNode prepareLabelToNode() {
        return LabelToNodeGenerator.generate();
    }

    @Override
    public Optional<DistributionLifecycleFilters> prepareDistributionLifecycleFilters() {
        return Optional.of(new RdfAbacDistributionLifecycleFilters());
    }

    @Override
    public Set<Operation> getReadOperations() {
        return Set.of(ServerABAC.Vocab.operationGetLabels,
                ServerABAC.Vocab.operationGSPRLabels,
                ServerABAC.Vocab.operationQueryLabels);
    }

    @Override
    public Set<Operation> getReadWriteOperations() {
        return Set.of(ServerABAC.Vocab.operationUploadABAC);
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
