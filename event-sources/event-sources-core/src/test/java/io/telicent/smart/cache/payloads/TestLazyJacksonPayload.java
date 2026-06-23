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

import java.nio.charset.StandardCharsets;

public class TestLazyJacksonPayload {

    private static final class NoObjectMapper extends LazyJacksonPayload<ActualBody> {
        NoObjectMapper() {
            super(null, ActualBody.class, new byte[0]);
        }
    }

    private static final class NoClass extends LazyJacksonPayload<ActualBody> {
        NoClass() {
            super(new ObjectMapper(), null, new byte[0]);
        }
    }

    private static final class LazyBody extends LazyJacksonPayload<ActualBody> {
        LazyBody(byte[] rawData) {
            super(new ObjectMapper(), ActualBody.class, rawData);
        }

        LazyBody(ActualBody value) {
            super(value);
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoObjectMapper_whenCreatingLazyPayload_thenNPE() {
        // Given, When and Then
        new NoObjectMapper();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoClass_whenCreatingLazyPayload_thenNPE() {
        // Given, When and Then
        new NoClass();
    }

    @Test
    public void givenEmptyData_whenCreatingLazyPayload_thenOk_andDeserializationFails() {
        // Given
        byte[] data = new byte[0];

        // When
        LazyBody lazy = new LazyBody(data);

        // Then
        Assert.assertFalse(lazy.isReady());
        Assert.assertFalse(lazy.hasError());
        Assert.assertTrue(lazy.hasRawData());

        // And
        Assert.assertThrows(LazyPayloadException.class, lazy::getValue);
        Assert.assertFalse(lazy.isReady());
        Assert.assertTrue(lazy.hasError());
        Assert.assertThrows(LazyPayloadException.class, lazy::getValue);
    }

    @Test
    public void givenValidData_whenCreatingLazyPayload_thenOk_andDeserializationSucceeds() {
        // Given
        byte[] data = """
                {
                  "title": "Lazy Test",
                  "flag": true,
                  "number": 456,
                  "values": [ "foo", "bar" ]
                }
                """.getBytes(StandardCharsets.UTF_8);

        // When
        LazyBody lazy = new LazyBody(data);

        // Then
        Assert.assertFalse(lazy.isReady());
        Assert.assertFalse(lazy.hasError());
        Assert.assertTrue(lazy.hasRawData());

        // And
        ActualBody actual = lazy.getValue();
        Assert.assertTrue(lazy.isReady());
        Assert.assertFalse(lazy.hasError());
        Assert.assertSame(actual, lazy.getValue());
        Assert.assertEquals(actual.getTitle(), "Lazy Test");
        Assert.assertTrue(actual.isFlag());
        Assert.assertEquals(actual.getNumber(), 456);

    }

    @Test
    public void givenValidPayload_whenCreatingLazyPayload_thenOk() {
        // Given
        ActualBody body = new ActualBody("Test", false, 789, null);

        // When
        LazyBody lazy = new LazyBody(body);

        // Then
        Assert.assertTrue(lazy.isReady());
        Assert.assertFalse(lazy.hasRawData());
        Assert.assertFalse(lazy.hasError());
    }
}
