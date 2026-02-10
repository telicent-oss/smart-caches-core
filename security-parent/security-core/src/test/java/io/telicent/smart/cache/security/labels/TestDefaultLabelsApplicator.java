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
package io.telicent.smart.cache.security.labels;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDefaultLabelsApplicator {

    @Test
    public void givenDefaultLabelApplicator_whenApplying_thenDefaultAlwaysReturned() {
        // Given
        SecurityLabels<?> defaultLabel = Mockito.mock(SecurityLabels.class);
        try (SecurityLabelsApplicator applicator = new DefaultLabelApplicator(defaultLabel)) {
            // When and Then
            for (int i = 1; i <= 1000; i++) {
                Triple t = Triple.create(NodeFactory.createURI("https://example.org/subjects/" + i),
                                         NodeFactory.createURI("https://example.org/predicate"),
                                         NodeFactory.createLiteralDT(Integer.toString(i),
                                                                     XSDDatatype.XSDinteger));
                SecurityLabels<?> applied = applicator.labelForTriple(t);
                Assert.assertSame(applied, defaultLabel);
            }
        }
    }
}
