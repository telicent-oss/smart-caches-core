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

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDefaultLabelsApplicator {

    @Test
    public void givenNullDefaultLabel_whenApplying_thenNullLabelReturned() {
        // Given
        try (SecurityLabelsApplicator applicator = new DefaultLabelApplicator(null)) {
            // When and Then
            for (int i = 1; i <= 1000; i++) {
                final Triple t = Triple.create(NodeFactory.createURI("https://example.org/subjects/" + i),
                                               NodeFactory.createURI("https://example.org/predicate"),
                                               NodeFactory.createLiteralDT(Integer.toString(i),
                                                                           XSDDatatype.XSDinteger));
                final SecurityLabels<?> applied = applicator.labelForTriple(t);
                Assert.assertNull(applied);
            }
        }
    }

    @Test
    public void givenDefaultLabelApplicator_whenApplying_thenDefaultAlwaysReturned() {
        // Given
        final SecurityLabels<?> defaultLabel = Mockito.mock(SecurityLabels.class);
        try (SecurityLabelsApplicator applicator = new DefaultLabelApplicator(defaultLabel)) {
            // When and Then
            for (int i = 1; i <= 1000; i++) {
                final Triple t = Triple.create(NodeFactory.createURI("https://example.org/subjects/" + i),
                                         NodeFactory.createURI("https://example.org/predicate"),
                                         NodeFactory.createLiteralDT(Integer.toString(i),
                                                                     XSDDatatype.XSDinteger));
                final SecurityLabels<?> applied = applicator.labelForTriple(t);
                Assert.assertSame(applied, defaultLabel);
            }
        }
    }

    @Test
    public void givenDefaultLabelApplicator_whenApplyingToQuad_thenDefaultAlwaysReturned() {
        final SecurityLabels<?> defaultLabel = Mockito.mock(SecurityLabels.class);
        final Quad quad = Quad.create(Quad.defaultGraphIRI,
                NodeFactory.createURI("https://example.org/s"),
                NodeFactory.createURI("https://example.org/p"),
                NodeFactory.createURI("https://example.org/o"));

        try (SecurityLabelsApplicator applicator = new DefaultLabelApplicator(defaultLabel)) {
            final SecurityLabels<?> applied = applicator.labelForQuad(quad);
            Assert.assertSame(applied, defaultLabel);
        }
    }

    @Test
    public void givenDefaultLabelApplicator_whenClosingMultipleTimes_thenNoError() {
        final SecurityLabels<?> defaultLabel = Mockito.mock(SecurityLabels.class);
        final DefaultLabelApplicator applicator = new DefaultLabelApplicator(defaultLabel);
        applicator.close();
        applicator.close();
    }
}
