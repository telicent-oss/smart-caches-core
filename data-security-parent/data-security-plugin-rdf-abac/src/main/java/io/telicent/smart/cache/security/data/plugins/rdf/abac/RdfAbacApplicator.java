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

import io.telicent.jena.abac.labels.Label;
import io.telicent.jena.abac.labels.LabelsStore;
import io.telicent.smart.cache.security.data.labels.SecurityLabels;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsApplicator;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphTxn;

import java.util.Objects;

/**
 * A labels applicator that uses an RDF-ABAC {@link LabelsStore} to decide what label applies to a given quad
 */
public class RdfAbacApplicator implements SecurityLabelsApplicator {
    private final RdfAbacParser parser;
    private final LabelsStore labelsStore;
    private final GraphTxn labelsGraph = GraphFactory.createTxnGraph();


    public RdfAbacApplicator(RdfAbacParser parser, LabelsStore labelsStore) {
        this.parser = Objects.requireNonNull(parser);
        this.labelsStore = Objects.requireNonNull(labelsStore);
    }

    @Override
    public SecurityLabels<?> labelForTriple(Triple triple) {
        final Quad quad = Quad.create(Quad.defaultGraphIRI, triple);
        return labelForQuad(quad);
    }

    @Override
    public SecurityLabels<?> labelForQuad(Quad quad) {
        // LabelStore returns Label record which holds byte sequences plus charsets
        // For the encoded representation we simply copy the raw bytes for each label
        final Label label = this.labelsStore.labelForQuad(quad);
        if (label == null) {
            return null;
        }
        return this.parser.parseSecurityLabels(label.data()); // FIXME the Label includes the charset but this is being ignored here
    }

    @Override
    public void close() {
        // LabelsStore is owned by DatasetGraphABAC and must not be closed here
    }

}
