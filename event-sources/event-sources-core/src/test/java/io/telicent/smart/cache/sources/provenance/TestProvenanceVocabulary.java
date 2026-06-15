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
package io.telicent.smart.cache.sources.provenance;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestProvenanceVocabulary {

    @Test
    public void givenRequestId_whenMintingActivityIri_thenWithinTelicentNamespaceAndContainsId() {
        // Given, When
        String iri = ProvenanceVocabulary.activityIri("req-1");

        // Then
        Assert.assertTrue(iri.startsWith(ProvenanceVocabulary.TELICENT_PROV_NS), "should be in Telicent prov namespace");
        Assert.assertTrue(iri.endsWith("req-1"), "should embed the request id");
    }

    @Test
    public void givenDistributionId_whenMintingDistributionIri_thenWithinTelicentNamespaceAndContainsId() {
        // Given, When
        String iri = ProvenanceVocabulary.distributionIri("dist-1");

        // Then
        Assert.assertTrue(iri.startsWith(ProvenanceVocabulary.TELICENT_PROV_NS));
        Assert.assertTrue(iri.endsWith("dist-1"));
    }

    @Test
    public void givenDistributionId_whenMintingProvenanceGraphIri_thenUsesGraphBase() {
        // Given, When
        String iri = ProvenanceVocabulary.provenanceGraphIri("dist-1");

        // Then
        Assert.assertEquals(iri, ProvenanceVocabulary.TELICENT_PROV_GRAPH_BASE + "dist-1");
    }

    @Test
    public void givenProvTerms_whenInspected_thenWithinProvNamespace() {
        // Given, When and Then
        Assert.assertTrue(ProvenanceVocabulary.PROV_ENTITY.startsWith(ProvenanceVocabulary.PROV_NS));
        Assert.assertTrue(ProvenanceVocabulary.PROV_ACTIVITY.startsWith(ProvenanceVocabulary.PROV_NS));
        Assert.assertTrue(ProvenanceVocabulary.PROV_WAS_GENERATED_BY.startsWith(ProvenanceVocabulary.PROV_NS));
        Assert.assertTrue(ProvenanceVocabulary.PROV_WAS_DERIVED_FROM.startsWith(ProvenanceVocabulary.PROV_NS));
    }

    @Test
    public void givenDcatTerms_whenInspected_thenWithinDcatNamespace() {
        // Given, When and Then
        Assert.assertTrue(ProvenanceVocabulary.DCAT_DISTRIBUTION.startsWith(ProvenanceVocabulary.DCAT_NS));
        Assert.assertTrue(ProvenanceVocabulary.DCAT_DATA_SERVICE.startsWith(ProvenanceVocabulary.DCAT_NS));
        Assert.assertTrue(ProvenanceVocabulary.DCAT_ACCESS_SERVICE.startsWith(ProvenanceVocabulary.DCAT_NS));
    }
}
