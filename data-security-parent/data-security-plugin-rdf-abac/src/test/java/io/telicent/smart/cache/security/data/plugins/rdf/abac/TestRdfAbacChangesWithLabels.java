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

import io.telicent.jena.abac.ABAC;
import io.telicent.jena.abac.SysABAC;
import io.telicent.jena.abac.attributes.syntax.AEX;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.core.VocabAuthz;
import io.telicent.jena.abac.labels.Labels;
import io.telicent.smart.cache.security.data.labels.SecurityLabels;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class TestRdfAbacChangesWithLabels {

    private static final Node S = NodeFactory.createURI("http://s");
    private static final Node P = NodeFactory.createURI("http://p");
    private static final Node O = NodeFactory.createURI("http://o");
    private static final Node GRAPH = NodeFactory.createURI("http://graph");

    private DatasetGraphABAC abac;
    private SecurityLabels<?> securityLabel;

    @BeforeMethod
    public void setUp() {
        abac = ABAC.authzDataset(DatasetGraphFactory.createTxnMem(),
                AEX.strALLOW, Labels.createLabelsStoreMem(), SysABAC.denyLabel, new AttributesStoreLocal());
        securityLabel = mock(SecurityLabels.class);
        when(securityLabel.toString()).thenReturn("clearance=S");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNonAbacDataset_whenConstructing_thenIllegalArgumentException() {
        new RdfAbacChangesWithLabels(DatasetGraphFactory.createTxnMem(), securityLabel);
    }

    @Test
    public void givenAbacDataset_whenConstructingWithoutDistributionId_thenNoException() {
        new RdfAbacChangesWithLabels(abac, securityLabel);
    }

    @Test
    public void givenAbacDataset_whenConstructingWithDistributionId_thenNoException() {
        new RdfAbacChangesWithLabels(abac, securityLabel, "http://distribution/1");
    }

    @Test
    public void givenLabel_whenAdding_thenTripleStoredWithLabel() {
        RdfAbacChangesWithLabels changes = new RdfAbacChangesWithLabels(abac, securityLabel);
        changes.txnBegin();
        changes.add(GRAPH, S, P, O);
        changes.txnCommit();

        Assert.assertTrue(abac.contains(GRAPH, S, P, O));
    }

    @Test
    public void givenNullLabel_whenAdding_thenTripleStoredWithoutLabel() {
        RdfAbacChangesWithLabels changes = new RdfAbacChangesWithLabels(abac, null);
        changes.txnBegin();
        changes.add(GRAPH, S, P, O);
        changes.txnCommit();

        Assert.assertTrue(abac.contains(GRAPH, S, P, O));
    }

    @Test
    public void givenDistributionId_whenAdding_thenTripleStoredInDistributionGraph() {
        String distributionId = "http://distribution/1";
        Node distGraph = NodeFactory.createURI(distributionId);

        RdfAbacChangesWithLabels changes = new RdfAbacChangesWithLabels(abac, securityLabel, distributionId);
        changes.txnBegin();
        changes.add(GRAPH, S, P, O);
        changes.txnCommit();

        Assert.assertTrue(abac.contains(distGraph, S, P, O));
        Assert.assertFalse(abac.contains(GRAPH, S, P, O));
    }

    @Test
    public void givenLabelsGraphQuad_whenAdding_thenNotStoredAsDataTriple() {
        RdfAbacChangesWithLabels changes = new RdfAbacChangesWithLabels(abac, securityLabel);
        changes.txnBegin();
        changes.add(VocabAuthz.graphForLabels, S, P, O);
        changes.txnCommit();

        Assert.assertFalse(abac.contains(VocabAuthz.graphForLabels, S, P, O));
    }

    @Test
    public void givenAddedTriple_whenDeleting_thenTripleRemoved() {
        RdfAbacChangesWithLabels add = new RdfAbacChangesWithLabels(abac, securityLabel);
        add.txnBegin();
        add.add(GRAPH, S, P, O);
        add.txnCommit();
        Assert.assertTrue(abac.contains(GRAPH, S, P, O));

        RdfAbacChangesWithLabels delete = new RdfAbacChangesWithLabels(abac, securityLabel);
        delete.txnBegin();
        delete.delete(GRAPH, S, P, O);
        delete.txnCommit();
        Assert.assertFalse(abac.contains(GRAPH, S, P, O));
    }

    @Test
    public void givenLabelsGraphQuad_whenDeleting_thenNotTreatedAsDataDelete() {
        RdfAbacChangesWithLabels changes = new RdfAbacChangesWithLabels(abac, securityLabel);
        changes.txnBegin();
        changes.delete(VocabAuthz.graphForLabels, S, P, O);
        changes.txnCommit();
        // No exception — labels-graph deletes are tracked internally, not as data
    }

    @Test
    public void givenTransactionBeginCommit_whenCommitting_thenChangesVisible() {
        RdfAbacChangesWithLabels changes = new RdfAbacChangesWithLabels(abac, securityLabel);
        changes.txnBegin();
        changes.add(GRAPH, S, P, O);
        changes.txnCommit();
        Assert.assertTrue(abac.contains(GRAPH, S, P, O));
    }

    @Test
    public void givenTransactionBeginAbort_whenAborting_thenChangesNotVisible() {
        RdfAbacChangesWithLabels changes = new RdfAbacChangesWithLabels(abac, securityLabel);
        changes.txnBegin();
        changes.add(GRAPH, S, P, O);
        changes.txnAbort();
        Assert.assertFalse(abac.contains(GRAPH, S, P, O));
    }
}
