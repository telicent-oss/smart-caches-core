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
