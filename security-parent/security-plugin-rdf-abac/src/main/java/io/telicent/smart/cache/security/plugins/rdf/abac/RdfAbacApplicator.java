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
package io.telicent.smart.cache.security.plugins.rdf.abac;

import io.telicent.jena.abac.AE;
import io.telicent.jena.abac.labels.LabelsStore;
import io.telicent.smart.cache.security.labels.SecurityLabels;
import io.telicent.smart.cache.security.labels.SecurityLabelsApplicator;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Triple;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class RdfAbacApplicator implements SecurityLabelsApplicator {

    private final LabelsStore labelsStore;

    public RdfAbacApplicator(LabelsStore labelsStore) {
        this.labelsStore = Objects.requireNonNull(labelsStore);
    }

    @Override
    public SecurityLabels<?> labelForTriple(Triple triple) {
        // TODO Need to evolve LabelsStore interface to just return byte[] sequences as this would avoid us having to
        //      convert the List<String> back to byte[]
        List<String> rawLabels = this.labelsStore.labelsForTriples(triple);
        return new RdfAbacLabels(StringUtils.join(rawLabels, ',').getBytes(StandardCharsets.UTF_8),
                                 rawLabels.stream().map(AE::parseExpr).toList());
    }

    @Override
    public void close() throws Exception {
        if (this.labelsStore instanceof Closeable) {
            ((Closeable) this.labelsStore).close();
        }
    }
}
