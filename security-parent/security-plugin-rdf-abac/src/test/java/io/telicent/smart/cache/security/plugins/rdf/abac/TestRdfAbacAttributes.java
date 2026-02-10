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

import io.telicent.jena.abac.AttributeValueSet;
import io.telicent.jena.abac.attributes.AttributeValue;
import io.telicent.jena.abac.attributes.ValueTerm;
import io.telicent.smart.cache.security.attributes.MalformedAttributesException;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class TestRdfAbacAttributes {

    @Test(expectedExceptions = MalformedAttributesException.class)
    public void givenBadAttributeValueSet_whenEncoding_thenFails() {
        // Given
        ValueTerm value = mock(ValueTerm.class);
        when(value.asString()).thenThrow(new RuntimeException("Bad"));
        AttributeValueSet attributes = AttributeValueSet.of(AttributeValue.of("test", value));

        // When and Then
        RdfAbacAttributes abacAttributes = new RdfAbacAttributes(attributes);
        abacAttributes.encoded();
    }
}
