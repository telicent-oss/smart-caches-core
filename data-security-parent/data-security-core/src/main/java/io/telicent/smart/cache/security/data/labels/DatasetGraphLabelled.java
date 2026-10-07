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
import java.util.Optional;

/**
 * A {@link DatasetGraph} whose quads can carry security labels, independent of the security plugin (and hence the
 * label storage) that provides the labelling
 * <p>
 * This allows code that needs to write labelled data, e.g. a Kafka sink, to do so without any dependency upon a
 * specific security implementation.  Plugins supply instances via
 * {@link io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin#prepareLabelledDataset(DatasetGraph)}.
 * </p>
 * <p>
 * Reads and writes of data, and transactions, use the normal {@link DatasetGraph} API.  Implementations
 * <strong>MUST</strong> ensure that label operations participate in the same transaction as the data operations.
 * </p>
 * <p>
 * There is deliberately no operation for removing labels.  Deleting data does not remove its labels, otherwise a data
 * producer could strip labels by deleting and re-adding data.
 * </p>
 */
public interface DatasetGraphLabelled extends DatasetGraph {

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
     * This is intended for explicit administrative removal of data, e.g. deleting a whole named graph, and
     * <strong>MUST NOT</strong> be used when applying incoming data.  Applying deletes from a data producer must never
     * remove labels as otherwise a producer could strip labels from data by deleting and then re-adding it.  This
     * method <strong>SHOULD</strong> be called inside a write transaction on this dataset.
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
}
