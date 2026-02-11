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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.graph.Triple;

import java.util.List;
import java.util.Objects;

/**
 * A labels applicator that uses an RDF-ABAC {@link LabelsStore} to decide what label applies to a given quad
 */
public class RdfAbacApplicator implements SecurityLabelsApplicator {
    private final RdfAbacParser parser;
    private final LabelsStore labelsStore;

    public RdfAbacApplicator(RdfAbacParser parser, LabelsStore labelsStore) {
        this.parser = Objects.requireNonNull(parser);
        this.labelsStore = Objects.requireNonNull(labelsStore);
    }

    @Override
    public SecurityLabels<?> labelForTriple(Triple triple) {
        // LabelStore returns Label record which holds byte sequences plus charsets
        // For the encoded representation we simply copy the raw bytes for each label, inserting a comma between each to
        // create a list of label expressions
        List<Label> rawLabels = this.labelsStore.labelsForTriples(triple);
        int labelSize =
                rawLabels.stream().map(Label::data).map(d -> d.length).reduce(0, Integer::sum) + rawLabels.size() - 1;
        byte[] encoded = new byte[labelSize];
        int pos = 0;
        for (Label label : rawLabels) {
            byte[] rawLabel = label.data();
            ArrayUtils.arraycopy(rawLabel, 0, encoded, pos, rawLabel.length);
            pos += rawLabel.length;
            if (pos < encoded.length) {
                encoded[pos] = ',';
                pos++;
            }
        }
        return this.parser.parseSecurityLabels(encoded);
    }

    @Override
    public void close() {
        try {
            this.labelsStore.close();
        } catch (Exception e) {
            RdfAbacPlugin.LOGGER.debug("Failed to close labels store: {}", e.getMessage());
        }
    }
}
