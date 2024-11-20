package io.telicent.smart.caches.security.plugins.rdf.abac;

import io.telicent.jena.abac.AE;
import io.telicent.jena.abac.attributes.AttributeExpr;
import io.telicent.jena.abac.labels.LabelsStore;
import io.telicent.smart.caches.security.labels.SecurityLabels;
import io.telicent.smart.caches.security.labels.SecurityLabelsApplicator;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Triple;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class RdfAbacApplicator implements SecurityLabelsApplicator<List<AttributeExpr>> {

    private final LabelsStore labelsStore;

    public RdfAbacApplicator(LabelsStore labelsStore) {
        this.labelsStore = Objects.requireNonNull(labelsStore);
    }

    @Override
    public SecurityLabels<List<AttributeExpr>> labelForTriple(Triple triple) {
        // TODO Need to evolve LabelsStore interface to just return byte[] sequences
        List<String> rawLabels = this.labelsStore.labelsForTriples(triple);
        return new RdfAbacLabels(StringUtils.join(rawLabels, ',').getBytes(StandardCharsets.UTF_8),
                                 rawLabels.stream().map(AE::parseExpr).toList());
    }
}
