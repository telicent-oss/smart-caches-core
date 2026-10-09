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
package io.telicent.smart.cache.security.data.labels;

import io.telicent.smart.cache.security.data.DataSecurityException;
import io.telicent.smart.cache.storage.labels.LabelsStore;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link DatasetGraph} whose quads can carry security labels, independent of any specific implementation that
 * provides the labelling.
 * <p>
 * This allows code that needs to write labelled data to do so without any dependency upon a
 * specific security implementation.  Plugins supply instances via
 * {@link io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin#prepareLabelledDataset(DatasetGraph)}.
 * </p>
 * <p>
 * Reads and writes of data, and transactions, use the normal {@link DatasetGraph} API.  Implementations
 * <strong>MUST</strong> ensure that label operations participate in the same transaction as the data operations.
 * </p>
 * <p>
 * Deleting data through the {@link DatasetGraph} API does not remove its labels, otherwise a data producer could strip
 * labels by deleting and re-adding data.  Labels are only removed by an explicit call to
 * {@link #removeLabels(Quad)}, which is reserved for administrative operations (e.g. Distribution Management).
 * </p>
 */
public interface DatasetGraphLabelled extends DatasetGraph {

    /**
     * Metric key for the number of label assignments attempted
     */
    String METRIC_LABEL_ADD_ATTEMPTS = "labelAddAttempts";
    /**
     * Metric key for the number of label writes avoided by duplicate detection
     */
    String METRIC_LABEL_CACHE_NO_OPS = "labelCacheNoOps";
    /**
     * Metric key for the number of label assignments written to storage
     */
    String METRIC_LABEL_WRITES = "labelWrites";

    /**
     * Gets the name of the graph used in incoming payloads to carry fine-grained labels, quads in this graph are label
     * data rather than user data so <strong>MUST NOT</strong> be written into this dataset as data
     *
     * @return Labels graph name
     */
    Node labelsGraphName();

    /**
     * Gets the parser for raw labels, e.g. the value of a {@code Security-Label} header, that is appropriate for this
     * labelled dataset
     *
     * @return Labels parser
     */
    SecurityLabelsParser labelsParser();

    /**
     * Applies the given labels to the given quads, which are expected to have already been written to this dataset
     *
     * @param quads  Quads to label
     * @param labels Labels to apply, as produced by {@link #labelsParser()}
     */
    void addLabels(Collection<Quad> quads, SecurityLabels<?> labels);

    /**
     * Applies a graph of fine-grained labels, i.e. a graph that describes which quads receive which labels
     *
     * @param labels Labels graph
     */
    void addLabelsGraph(Graph labels);

    /**
     * Removes the labels associated with the given quad
     * <p>
     * This only removes the labels, it does not remove the quad itself from the dataset.  It is intended for explicit
     * administrative operations that also remove the data, e.g. deleting a whole named graph, so that labels are not
     * left behind for data that no longer exists.  It <strong>MUST NOT</strong> be applied to incoming data - deletes
     * from a data producer must never remove labels as otherwise a producer could strip labels from data by deleting
     * and then re-adding it.  This method <strong>SHOULD</strong> be called inside a write transaction on this
     * dataset.
     * </p>
     *
     * @param quad Quad whose labels should be removed
     * @throws DataSecurityException Thrown if the labels cannot be removed
     */
    void removeLabels(Quad quad) throws DataSecurityException;

    /**
     * Gets the underlying labels store for this dataset, if it has one that can be exposed as a Smart Cache Storage
     * {@link LabelsStore}
     * <p>
     * This allows callers to interrogate the store for generic storage capabilities, e.g.
     * {@link io.telicent.smart.cache.storage.BackupRestoreCapable} or
     * {@link io.telicent.smart.cache.storage.CompactCapable}, in order to perform maintenance operations.  The store
     * remains owned by this dataset so callers <strong>MUST NOT</strong> close it.
     * </p>
     *
     * @return Labels store, or empty if the dataset has no labels store that is available as a {@link LabelsStore}
     */
    Optional<LabelsStore> labelsStore();

    /**
     * Gets the current values of any metrics the underlying labels store exposes, keyed by the {@code METRIC_*}
     * constants defined on this interface
     * <p>
     * The values are live counters so this should be called each time a current value is required.  A backend that does
     * not expose metrics reports none.
     * </p>
     *
     * @return Metrics, empty if none are available
     */
    default Map<String, Long> labelsMetrics() {
        return Map.of();
    }

}
