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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestEnvelope {

    private static final ObjectMapper JSON = new ObjectMapper();

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
        Map<String, Object> body = Map.of("title", "Test", "flag", false, "number", 123, "values", List.of("a", "b", "c"));
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
}
