package io.telicent.smart.cache.security.plugins.rdf.abac;

import io.telicent.jena.abac.labels.Label;
import io.telicent.jena.abac.labels.LabelsStore;
import io.telicent.smart.cache.security.labels.SecurityLabels;
import io.telicent.smart.cache.security.plugins.AbstractSecurityPluginTests;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class TestRdfAbacApplicator {

    private List<Byte> asList(byte[] bs) {
        List<Byte> list = new ArrayList<>(bs.length);
        for (byte b : bs) {
            list.add(b);
        }
        return list;
    }

    @Test
    public void givenLabelStoreWithMultipleLabelsForTriple_whenApplying_thenEncodedCombinesLabels() {
        // Given
        List<Label> labels = new ArrayList<>();
        Label a = Label.fromText("test");
        labels.add(a);
        Label b = Label.fromText("clearance=S");
        labels.add(b);
        LabelsStore store = mock(LabelsStore.class);
        when(store.labelsForTriples(any())).thenReturn(labels);

        // When
        try (RdfAbacApplicator applicator = new RdfAbacApplicator(new RdfAbacParser(), store)) {
            SecurityLabels<?> applied = applicator.labelForTriple(AbstractSecurityPluginTests.TEST_TRIPLE);
            for (Label l : labels) {
                Assert.assertNotEquals(
                        Collections.indexOfSubList(asList(applied.encoded()), asList(l.data())), -1);
            }
        }
    }

    @Test
    public void givenFailsOnCloseLabelsStore_whenApplying_thenNoError() throws Exception {
        // Given
        List<Label> labels = new ArrayList<>();
        Label a = Label.fromText("test");
        labels.add(a);
        LabelsStore store = mock(LabelsStore.class);
        when(store.labelsForTriples(any())).thenReturn(labels);
        doThrow(new RuntimeException("failed")).when(store).close();

        // When
        try (RdfAbacApplicator applicator = new RdfAbacApplicator(new RdfAbacParser(), store)) {
            SecurityLabels<?> applied = applicator.labelForTriple(AbstractSecurityPluginTests.TEST_TRIPLE);
            Assert.assertNotEquals(
                    Collections.indexOfSubList(asList(applied.encoded()), asList(a.data())), -1);
        }
    }
}
