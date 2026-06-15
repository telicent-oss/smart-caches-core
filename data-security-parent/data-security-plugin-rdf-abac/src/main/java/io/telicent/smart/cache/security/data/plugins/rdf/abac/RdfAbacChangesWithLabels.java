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

import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.core.VocabAuthz;
import io.telicent.jena.abac.labels.Label;
import io.telicent.smart.cache.security.data.labels.SecurityLabels;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.kafka.utils.RDFChangesApplyExternalTransaction;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphTxn;

public class RdfAbacChangesWithLabels extends RDFChangesApplyExternalTransaction {

    //private final Label securityLabel;
    private final SecurityLabels<?> securityLabel;
    private final DatasetGraphABAC datasetABAC;
    //private final DatasetGraph datasetGraph;
    private final GraphTxn labelsGraph = GraphFactory.createTxnGraph();
    private final Node targetGraph;

    public RdfAbacChangesWithLabels(DatasetGraph dsgz,
                                     SecurityLabels<?> securityLabel) {
        this(dsgz, securityLabel, null);
    }

    public RdfAbacChangesWithLabels(DatasetGraph datasetGraph,
                                    SecurityLabels<?> securitylabel, String distributionId) {
        super(datasetGraph);
        if(datasetGraph instanceof  DatasetGraphABAC datasetGraphABAC) {
            this.securityLabel = securitylabel;
            this.datasetABAC = datasetGraphABAC;
            this.targetGraph = distributionId != null ? NodeFactory.createURI(distributionId) : null;
            this.labelsGraph.begin(TxnType.WRITE);
        } else {
            throw new IllegalArgumentException("Dataset graph must be an instance of DatasetGraphABAC");
        }

    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        if (VocabAuthz.graphForLabels.equals(g)) {
            // If quad is for labels graph just track that for now
            this.labelsGraph.add(s, p, o);
        } else {
            if (this.targetGraph != null) {
                g = targetGraph;
            } else if (g == null) {
                g = Quad.defaultGraphIRI;
            }
            super.add(g, s, p, o);

            // Apply specific security label if there is one, if not we're relying on the dataset default label applying
            // at read time
            if (securityLabel != null) {
                this.datasetABAC.labelsStore().add(g, s, p, o, Label.fromText(securityLabel.toString())); // FIXME - this is almost certainly wrong
            }
        }
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        if (VocabAuthz.graphForLabels.equals(g)) {
            // If quad is for labels graph just update the labels graph state
            this.labelsGraph.delete(s, p, o);
        } else {
            if (this.targetGraph != null) {
                g = targetGraph;
            }
            // Otherwise remove the quad
            // NB - While there is a remove() method on LabelsStore we intentionally don't use it because otherwise a
            //      malicious data producer could remove labels from data by creating a patch that deleted and then re-added
            //      triples
            super.delete(g, s, p, o);
        }
    }

    @Override
    public void txnBegin() {
        // Begin a new transaction first
        super.txnBegin();

        // Begin a transaction on the labels graph
        if (!this.labelsGraph.isInTransaction()) {
            this.labelsGraph.begin(TxnType.WRITE);
        }
    }

    @Override
    public void txnCommit() {
        // Commit and apply the labels graph first
        this.labelsGraph.commit();
        applyLabelsGraph();

        // Then apply the commit as normal
        super.txnCommit();
    }

    private void applyLabelsGraph() {
        if (!this.labelsGraph.isEmpty()) {
            this.datasetABAC.labelsStore().addGraph(this.labelsGraph);
        }
    }

    @Override
    public void txnAbort() {
        // Abort any changes to the labels graph first
        this.labelsGraph.abort();

        // Then apply the abort as normal
        super.txnAbort();
    }

    @Override
    public void finish() {
        // Upon finish apply the final state of the labels graph
        applyLabelsGraph();
        super.finish();
    }
}
