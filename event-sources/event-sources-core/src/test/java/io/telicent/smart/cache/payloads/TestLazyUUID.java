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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TestLazyUUID {

    private static byte[] raw(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void givenValidUuidBytes_whenCreatingLazyUuid_thenNotDeserializedUntilValueAccessed() {
        // Given
        final UUID expected = UUID.randomUUID();
        final LazyUUID lazy = LazyUUID.of(raw(expected.toString()));

        // When
        Assert.assertFalse(lazy.isReady());
        Assert.assertTrue(lazy.hasRawData());
        Assert.assertEquals(lazy.sizeInBytes(), expected.toString().length());

        // Then
        Assert.assertEquals(lazy.getValue(), expected);
        Assert.assertTrue(lazy.isReady());
        Assert.assertFalse(lazy.hasError());
        // Raw data is discarded once we hold the deserialised value
        Assert.assertFalse(lazy.hasRawData());
    }

    @Test
    public void givenMalformedBytes_whenAccessingValue_thenErrorIsCachedAndRawDataRetained() {
        // Given
        final LazyUUID lazy = LazyUUID.of(raw("not-a-uuid"));

        // When
        Assert.assertThrows(LazyPayloadException.class, lazy::getValue);

        // Then
        Assert.assertTrue(lazy.hasError());
        Assert.assertFalse(lazy.isReady());
        Assert.assertTrue(lazy.hasRawData(), "Raw data must be retained so the key can be written to a DLQ");
        Assert.assertEquals(lazy.getRawData(), raw("not-a-uuid"));
        // Repeated access rethrows the same cached error
        Assert.assertThrows(LazyPayloadException.class, lazy::getValue);
    }

    @Test
    public void givenEmptyBytes_whenAccessingValue_thenError() {
        // Given
        final LazyUUID lazy = LazyUUID.of(new byte[0]);

        // When and Then
        Assert.assertThrows(LazyPayloadException.class, lazy::getValue);
    }

    @Test
    public void givenMalformedBytes_whenGettingValueOrNull_thenNull() {
        // Given
        final LazyUUID lazy = LazyUUID.of(raw("garbage"));

        // When and Then
        Assert.assertNull(lazy.getValueOrNull());
    }

    @Test
    public void givenUuidValue_whenCreatingLazyUuid_thenReadyImmediately() {
        // Given
        final UUID expected = UUID.randomUUID();

        // When
        final LazyUUID lazy = LazyUUID.of(expected);

        // Then
        Assert.assertTrue(lazy.isReady());
        Assert.assertFalse(lazy.hasRawData());
        Assert.assertEquals(lazy.sizeInBytes(), LazyPayload.UNKNOWN_SIZE);
        Assert.assertEquals(lazy.getValue(), expected);
    }

    @Test
    public void givenRandom_whenCreatingLazyUuid_thenDistinctValues() {
        // Given, When and Then
        Assert.assertNotEquals(LazyUUID.random(), LazyUUID.random());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullRawData_whenCreatingLazyUuid_thenNPE() {
        // Given, When and Then
        LazyUUID.of((byte[]) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullValue_whenCreatingLazyUuid_thenNPE() {
        // Given, When and Then
        LazyUUID.of((UUID) null);
    }

    @Test
    public void givenNullCharset_whenCreatingLazyUuid_thenDefaultCharsetUsed() {
        // Given
        final UUID expected = UUID.randomUUID();

        // When
        final LazyUUID lazy = LazyUUID.of(raw(expected.toString()), null);

        // Then
        Assert.assertEquals(lazy.getValue(), expected);
    }

    @Test
    public void givenNonDefaultCharset_whenAccessingValue_thenDecodedWithThatCharset() {
        // Given
        final UUID expected = UUID.randomUUID();
        final byte[] rawData = expected.toString().getBytes(StandardCharsets.UTF_16);

        // When
        final LazyUUID lazy = LazyUUID.of(rawData, StandardCharsets.UTF_16);

        // Then
        Assert.assertEquals(lazy.getValue(), expected);
        // And decoding those same bytes as UTF-8 does not yield a valid UUID
        Assert.assertNull(LazyUUID.of(rawData).getValueOrNull());
    }

    @Test
    public void givenSameUuid_whenComparingLazyAndPopulated_thenEqual() {
        // Given
        final UUID value = UUID.randomUUID();
        final LazyUUID lazy = LazyUUID.of(raw(value.toString()));
        final LazyUUID populated = LazyUUID.of(value);

        // When and Then
        Assert.assertEquals(lazy, populated);
        Assert.assertEquals(lazy.hashCode(), populated.hashCode());
    }

    @Test
    public void givenDifferentValues_whenComparing_thenNotEqual() {
        // Given
        final LazyUUID lazy = LazyUUID.random();

        // When and Then
        Assert.assertNotEquals(lazy, LazyUUID.random());
        Assert.assertNotEquals(lazy, LazyUUID.of(raw("not-a-uuid")));
        Assert.assertNotEquals(lazy, null);
    }

    @Test
    public void givenOneValidAndOneMalformed_whenComparing_thenNotEqual_andNoNPE() {
        // Given
        final LazyUUID valid = LazyUUID.of(raw(UUID.randomUUID().toString()));
        Assert.assertNotNull(valid.getValue());
        Assert.assertFalse(valid.hasRawData());
        final LazyUUID malformed = LazyUUID.of(raw("not-a-uuid"));
        Assert.assertNull(malformed.getValueOrNull());
        Assert.assertTrue(malformed.hasRawData());

        // When and Then
        Assert.assertNotEquals(valid, malformed);
        Assert.assertNotEquals(malformed, valid);
        Assert.assertNotEquals(valid.hashCode(), malformed.hashCode());
    }

    @Test
    public void givenSameMalformedData_whenComparing_thenEqual() {
        // Given
        final LazyUUID a = LazyUUID.of(raw("not-a-uuid"));
        final LazyUUID b = LazyUUID.of(raw("not-a-uuid"));

        // When and Then
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertNotEquals(a, LazyUUID.of(raw("also-not-a-uuid")));
    }

    @Test
    public void givenValidUuid_whenConvertingToString_thenCanonicalForm() {
        // Given
        final UUID value = UUID.randomUUID();

        // When and Then
        Assert.assertEquals(LazyUUID.of(raw(value.toString())).toString(), value.toString());
    }

    @Test
    public void givenMalformedData_whenConvertingToString_thenDescribesTheBadKey() {
        // Given
        final LazyUUID lazy = LazyUUID.of(raw("not-a-uuid"));

        // When
        final String actual = lazy.toString();

        // Then
        Assert.assertTrue(actual.contains("malformed"), actual);
        Assert.assertTrue(actual.contains("not-a-uuid"), actual);
    }

    @Test
    public void givenOversizedMalformedData_whenConvertingToString_thenTruncated() {
        // Given
        final LazyUUID lazy = LazyUUID.of(raw("x".repeat(1024)));

        // When
        final String actual = lazy.toString();

        // Then
        Assert.assertTrue(actual.length() < 128, "Expected the raw data to be truncated but got " + actual.length());
        Assert.assertTrue(actual.endsWith("...>"), actual);
    }
}
