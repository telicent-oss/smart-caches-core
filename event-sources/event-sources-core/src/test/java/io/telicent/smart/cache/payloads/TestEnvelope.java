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
package io.telicent.smart.cache.payloads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestEnvelope {

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoParameters_whenBuilding_thenNPE() {
        // Given, When and Then
        Envelope.create().build();
    }

    @Test
    public void givenValidParameters_whenBuilding_thenNPE() {
        // Given and When
        Envelope envelope = build(Map.of("test", true));

        // Then
        Assert.assertNotNull(envelope.getId());
        Assert.assertNotNull(envelope.getMetadata());
        Assert.assertNotNull(envelope.getBody());
    }

    private static Envelope build(Map<String, Object> body) {
        return Envelope.create()
                       .id(UUID.randomUUID())
                       .metadata(Metadata.create()
                                         .generatedAt(Date.from(Instant.now()))
                                         .generatedBy("tests")
                                         .generatorVersion("1.0.0")
                                         .documentFormat("tests/v1")
                                         .build())
                       .body(body)
                       .build();
    }

    @Test
    public void givenValidBody_whenConvertingToValueType_thenOk() {
        // Given
        Map<String, Object> body = Map.of("title", "Test", "flag", false, "number", 123);
        Envelope envelope = build(body);

        // When
        ActualBody actual = envelope.getBodyAs(ActualBody.class);

        // Then
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getTitle(), "Test");
        Assert.assertFalse(actual.isFlag());
        Assert.assertEquals(actual.getNumber(), 123);
        Assert.assertNull(actual.getValues());
    }

    @Test
    public void givenValidComplexBody_whenConvertingToValueType_thenOk() {
        // Given
        Map<String, Object> body =
                Map.of("title", "Test", "flag", false, "number", 123, "values", List.of("a", "b", "c"));
        Envelope envelope = build(body);

        // When
        ActualBody actual = envelope.getBodyAs(ActualBody.class);

        // Then
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getTitle(), "Test");
        Assert.assertFalse(actual.isFlag());
        Assert.assertEquals(actual.getNumber(), 123);
        Assert.assertEquals(actual.getValues(), List.of("a", "b", "c"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenValidBody_whenConvertingToWrongValueType_thenFails() {
        // Given
        Map<String, Object> body = Map.of("title", "Test", "flag", false, "number", 123);
        Envelope envelope = build(body);

        // When
        envelope.getBodyAs(RdfPayload.class);
    }

    @Test
    public void givenEnvelope_whenWritingToJson_thenDatesSerializedAsStrings() throws JsonProcessingException {
        // Given
        String textualDate = "2026-05-08T10:12:17.000+00:00";
        Date test = Date.from(Instant.parse(textualDate));
        Envelope envelope = Envelope.create()
                                    .id(UUID.randomUUID())
                                    .metadata(Metadata.create()
                                                      .generatedBy("tests")
                                                      .generatedAt(test)
                                                      .generatorVersion("1.2.3")
                                                      .documentFormat("tests/v1")
                                                      .build())
                                    .body(Map.of("date", test))
                                    .build();

        // When
        String json = Envelope.JSON.writeValueAsString(envelope);

        // Then
        Assert.assertTrue(json.contains(textualDate));
    }

    @Test
    public void givenInvalidData_whenUsingLazyEnvelope_thenFailsWhenValueAccessed() {
        // Given
        String data = """
                      {
                        "invalid": 
                      """;

        // When
        LazyEnvelope lazy = LazyEnvelope.of(data.getBytes(StandardCharsets.UTF_8));

        // Then
        Assert.assertFalse(lazy.isReady());
        Assert.assertFalse(lazy.hasError());
        Assert.assertThrows(LazyPayloadException.class, lazy::getValue);
        Assert.assertTrue(lazy.hasError());
    }

    @Test
    public void givenValidData_whenUsingLazyEnvelope_thenValueAccessible_andValueRoundTrips() throws
            JsonProcessingException {
        // Given
        Map<String, Object> body = Map.of("title", "Test", "flag", false, "number", 123);
        Envelope envelope = build(body);
        byte[] data = Envelope.JSON.writeValueAsBytes(envelope);

        // When
        LazyEnvelope lazy = LazyEnvelope.of(data);

        // Then
        Assert.assertFalse(lazy.isReady());
        Envelope parsed = lazy.getValue();
        Assert.assertTrue(lazy.isReady());
        Assert.assertSame(lazy.getValue(), parsed);

        // And
        Assert.assertEquals(parsed, envelope);
    }

    @Test
    public void givenValidData_whenHoldingInLazy_thenValueSame() {
        // Given
        Map<String, Object> body = Map.of("title", "Test", "flag", false, "number", 123);
        Envelope envelope = build(body);

        // When
        LazyEnvelope lazy = LazyEnvelope.of(envelope);

        // Then
        Assert.assertTrue(lazy.isReady());
        Assert.assertSame(lazy.getValue(), envelope);
    }
}
