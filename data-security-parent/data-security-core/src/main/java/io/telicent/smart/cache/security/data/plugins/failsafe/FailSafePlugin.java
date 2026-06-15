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
package io.telicent.smart.cache.security.data.plugins.failsafe;

import io.telicent.smart.cache.security.data.DataAccessAuthorizer;
import io.telicent.smart.cache.security.data.distribution.DistributionLifecycleFilters;
import io.telicent.smart.cache.security.data.labels.*;
import io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin;
import io.telicent.smart.cache.security.data.requests.RequestContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.graph.Triple;
import org.apache.jena.kafka.common.FusekiSink;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

import java.util.Optional;
import java.util.Set;

/**
 * A fallback plugin that is used if the system detects multiple plugins and doesn't know which should be used, when
 * this plugin is used then all labels are considered invalid and all access requests are rejected.
 * <p>
 * In normal operation this <strong>SHOULD</strong> never get used, only if the system is misconfigured will it be used
 * to put the system into a fail-safe mode.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FailSafePlugin implements DataSecurityPlugin {

    /**
     * Singleton instance of the fail-safe plugin
     */
    public static final FailSafePlugin INSTANCE = new FailSafePlugin();

    /**
     * Error message produced when trying to interact with any part of the API involving security labels
     */
    public static final String MALFORMED_LABELS_FAILSAFE_MESSAGE =
            "Operating in fail-safe mode, all labels are considered malformed as we could not load a Security Plugin";

    @Override
    public SecurityLabelsParser labelsParser() {
        return rawLabels -> {
            throw new MalformedLabelsException(MALFORMED_LABELS_FAILSAFE_MESSAGE);
        };
    }

    @Override
    public SecurityLabelsValidator labelsValidator() {
        return l -> false;
    }

    @Override
    public SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel, DatasetGraph datasetGraph) {
        return new SecurityLabelsApplicator() {
            @Override
            public SecurityLabels<?> labelForTriple(Triple triple) {
                return new RawPrimitive(defaultLabel);
            }

            @Override
            public SecurityLabels<?> labelForQuad(Quad quad) {
                return new RawPrimitive(defaultLabel);
            }

            @Override
            public void close() {
                // No-op
            }
        };
    }

    @Override
    public DataAccessAuthorizer prepareAuthorizer(RequestContext context) {
        return FailSafeAuthorizer.INSTANCE;
    }

    @Override
    public Optional<SecurityLabelsBackup> prepareLabelsBackup() {
        return Optional.empty();
    }

    @Override
    public Optional<SecurityLabelsRestore> prepareLabelsRestore() {
        return Optional.empty();
    }

    @Override
    public Optional<SecurityLabelsCompact> prepareLabelsCompact() {
        return Optional.empty();
    }

    @Override
    public Optional<SecurityLabelsRemover> prepareLabelsRemover() {
        return Optional.empty();
    }

    @Override
    public Optional<FusekiModule> prepareLabelsModule() {
        return Optional.empty();
    }

    @Override
    public Optional<FusekiSink<?>> prepareFusekiSink(DatasetGraph datasetGraph, boolean routeToNamedGraphs) {
        return Optional.empty();
    }

    @Override
    public LabelToNode prepareLabelToNode() {
        return SyntaxLabels.createLabelToNode();
    }

    @Override
    public Optional<DistributionLifecycleFilters> prepareDistributionLifecycleFilters() {
        return Optional.empty();
    }

    @Override
    public Set<Operation> getReadOperations() {
        return Set.of();
    }

    @Override
    public Set<Operation> getReadWriteOperations() {
        return Set.of();
    }

    @Override
    public void close() {
        // No-op
    }

}
