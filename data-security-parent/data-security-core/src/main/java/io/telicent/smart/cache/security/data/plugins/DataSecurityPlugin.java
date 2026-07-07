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
package io.telicent.smart.cache.security.data.plugins;

import io.telicent.smart.cache.security.data.DataAccessAuthorizer;
import io.telicent.smart.cache.security.data.distribution.DistributionLifecycleFilters;
import io.telicent.smart.cache.security.data.labels.*;
import io.telicent.smart.cache.security.data.plugins.failsafe.FailSafeAuthorizer;
import io.telicent.smart.cache.security.data.requests.RequestContext;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.kafka.common.FusekiSink;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.core.DatasetGraph;

import java.util.Optional;
import java.util.Set;

/**
 * Interface for data security plugins, primarily this provides access to the various interfaces since a plugin may wish
 * to compose itself from multiple components and/or reuse some standard components
 */
public interface DataSecurityPlugin {

    /**
     * Gets the labels parser
     *
     * @return Labels parser
     */
    SecurityLabelsParser labelsParser();

    /**
     * Gets the labels validator
     *
     * @return Labels validator
     */
    SecurityLabelsValidator labelsValidator();

    /**
     * Prepares a labels applicator
     *
     * @param defaultLabel The default label to apply if no more specific label applies
     * @param datasetGraph  The fine-grained security-labelled dataset graph
     * @return Labels applicator
     * @throws MalformedLabelsException Thrown if the provided default label is invalid or not supported by this plugin
     */
    SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel, DatasetGraph datasetGraph);

    /**
     * Prepares an authorizer based on the given request context
     * <p>
     * The returned instance is scoped to the lifetime of a single user request so implementors should take that into
     * account when implementing their authorizer, see {@link DataAccessAuthorizer#canRead(SecurityLabels)} Javadoc for more
     * details.
     * </p>
     * <p>
     * In the event that a {@code null} context is provided, or there's insufficient user information to make
     * authorization decisions then a plugin <strong>MUST</strong> return an authorizer that rejects all authorization
     * requests e.g. {@link FailSafeAuthorizer#INSTANCE}.
     * </p>
     *
     * @param context Request context
     * @return Authorizer
     */
    DataAccessAuthorizer prepareAuthorizer(RequestContext context);

    /**
     * Prepares a labels backup implementation for backing up security labels associated with a dataset
     *
     * @return Labels backup
     */
    default Optional<SecurityLabelsBackup> prepareLabelsBackup() {
        return Optional.empty();
    }

    /**
     * Prepares a labels restore implementation for restoring security labels from a previously created backup
     *
     * @return Labels restore
     */
    default Optional<SecurityLabelsRestore> prepareLabelsRestore() {
        return Optional.empty();
    };

    /**
     * Prepares a labels compaction implementation for compacting the security labels store, removing stale or orphaned
     * label entries
     *
     * @return Labels compact
     */
    default Optional<SecurityLabelsCompact> prepareLabelsCompact() {
        return Optional.empty();
    };

    /**
     * Prepares a labels remover implementation for removing security labels associated with specific quads
     *
     * @return Labels remover
     */
    default Optional<SecurityLabelsRemover> prepareLabelsRemover() {
        return Optional.empty();
    };

    /**
     * Prepares a Fuseki module that integrates security labels processing into the Fuseki server lifecycle
     *
     * @return Fuseki module
     */
    default Optional<FusekiModule> prepareLabelsModule() {
        return Optional.empty();
    };

    /**
     * Prepares an optional Fuseki sink for consuming incoming data events into the labelled dataset
     *
     * @param datasetGraph        the dataset graph that will receive the incoming data
     * @param routeToNamedGraphs  whether incoming data should be routed to named graphs rather than the default graph
     * @return an {@link Optional} containing the Fuseki sink if this plugin supports one, or empty if not applicable
     */
    default Optional<FusekiSink<?>> prepareFusekiSink(DatasetGraph datasetGraph, boolean routeToNamedGraphs) {
        return Optional.empty();
    }

    /**
     * Prepares the label-to-node mapping strategy used during RDF parsing to associate blank node labels with graph
     * nodes
     *
     * @return Label-to-node mapping
     */
    default LabelToNode prepareLabelToNode() {
        return SyntaxLabels.createLabelToNode();
    }

    /**
     * Prepares distribution lifecycle filters for managing dataset lifecycle events during data distribution
     *
     * @return Distribution lifecycle filters
     */
    default Optional<DistributionLifecycleFilters> prepareDistributionLifecycleFilters() {
        return Optional.empty();
    };

    /**
     * Gets the set of Fuseki operations that are treated as read-only for the purposes of access control
     *
     * @return Read operations
     */
    default Set<Operation> getReadOperations() {
        return Set.of();
    };

    /**
     * Gets the set of Fuseki operations that require both read and write access for the purposes of access control
     *
     * @return Read-write operations
     */
    default Set<Operation> getReadWriteOperations() {
        return Set.of();
    };

    /**
     * Closes the plugin releasing any resources it may be holding
     */
    void close();
}
