package io.telicent.smart.cache.security.plugins.rdf.abac.utils;

import io.telicent.jena.abac.labels.LabelsStore;
import lombok.Generated;
import lombok.experimental.Delegate;
import org.apache.jena.graph.Triple;
import io.telicent.jena.abac.labels.Label;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * A decorator for {@link LabelsStore} that returns fallback default labels
 */
@Generated
public class DefaultingLabelsStore implements LabelsStore {
    private final @Delegate LabelsStore store;
    private final List<Label> defaultLabels;

    /**
     * Creates a new labels store with fallback default labels
     *
     * @param labelStore    Actual labels store
     * @param defaultLabels Fallback default labels
     */
    public DefaultingLabelsStore(final LabelsStore labelStore, final byte[] defaultLabels) {
        Objects.requireNonNull(labelStore);
        if (isBlank(defaultLabels)) {
            throw new IllegalArgumentException("Default Labels cannot be blank");
        }
        this.store = labelStore;
        this.defaultLabels = List.of(new Label(defaultLabels, StandardCharsets.UTF_8));
    }

    private boolean isBlank(byte[] defaultLabels) {
        if (defaultLabels == null) {
            return true;
        }
        for (byte defaultLabel : defaultLabels) {
            if (defaultLabel != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Label> labelsForTriples(final Triple triple) {
        final List<Label> ls = this.store.labelsForTriples(triple);
        return ls.isEmpty() ? this.defaultLabels : ls;
    }
}

