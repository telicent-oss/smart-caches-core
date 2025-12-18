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
package io.telicent.smart.cache.sources;

import lombok.Data;
import org.apache.commons.lang3.Strings;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TestHeaders {

    public static final byte[] BYTES_2468 = { 2, 4, 6, 8 };
    public static final byte[] BYTES_1357 = { 1, 3, 5, 7 };

    @Test
    public void givenEmptyHeader_whenInspecting_thenEmpty() {
        // Given
        Header header = new Header(null, null);

        // When and Then
        Assert.assertNull(header.key());
        Assert.assertNull(header.value());
    }

    @Test
    public void givenBlankHeader_whenInspecting_thenBlank() {
        // Given
        Header header = new Header("", "");

        // When and Then
        Assert.assertEquals(header.key(), "");
        Assert.assertEquals(header.value(), "");
    }

    @Test
    public void givenHeader_whenInspecting_thenOk() {
        // Given
        String textPlain = "text/plain";
        Header header = new Header("Content-Type", textPlain);

        // When and Then
        Assert.assertEquals(header.key(), "Content-Type");
        Assert.assertEquals(header.value(), textPlain);
        Assert.assertEquals(header.rawValue(), textPlain.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(header.toString(), "Content-Type: text/plain");
    }

    @Test
    public void givenHeaderWithoutValue_whenInspecting_thenOk() {
        // Given
        Header header = new Header("Content-Type", null);

        // When and Then
        Assert.assertEquals(header.key(), "Content-Type");
        Assert.assertNull(header.value());
        Assert.assertNull(header.rawValue());
        Assert.assertEquals(header.toString(), "Content-Type: null");
    }

    @Test
    public void givenRawHeader_whenInspecting_thenOk() {
        // Given
        byte[] value = { 1, 2, 3, 4 };
        RawHeader header = new RawHeader("test", value);

        // When and Then
        Assert.assertEquals(header.key(), "test");
        Assert.assertEquals(header.rawValue(), value);
        String toString = header.toString();
        Assert.assertTrue(Strings.CI.contains(toString, "4 bytes"));
    }

    @Test
    public void givenRawHeaderWithoutValue_whenInspecting_thenOk() {
        // Given
        RawHeader header = new RawHeader("test", null);

        // When and Then
        Assert.assertEquals(header.key(), "test");
        Assert.assertNull(header.rawValue());
        Assert.assertNull(header.value());
        String toString = header.toString();
        Assert.assertTrue(Strings.CI.contains(toString, "0 bytes"));
    }

    @DataProvider(name = "nonHeaders")
    public Object[][] nonHeaders() {
        return new Object[][] {
                { null },
                { new Object() },
                { Map.of("key", "test", "value", BYTES_2468)}
        };
    }

    @Test(dataProvider = "nonHeaders")
    public void givenHeader_whenComparingToNonHeaders_thenFalse(Object other) {
        // Given
        Header header = new Header("test", "value");

        // When and Then
        Assert.assertNotEquals(header, other);
        Assert.assertNotEquals(other, header);
    }

    @Test(dataProvider = "nonHeaders")
    public void givenRawHeader_whenComparingToNonHeaders_thenFalse(Object other) {
        // Given
        RawHeader header = new RawHeader("test", BYTES_2468);

        // When and Then
        Assert.assertNotEquals(header, other);
        Assert.assertNotEquals(other, header);
    }

    @DataProvider(name = "headers")
    public Object[][] headers() {
        return new Object[][] {
                // Basic Headers
                { new Header("test", "value"), new Header("test", "value"), true},
                { new Header("test", "value"), new Header("test", "another value"), false},
                { new Header("a", "value"), new Header("b", "value"), false },
                // Raw Headers
                { new RawHeader("test", BYTES_2468), new RawHeader("test", BYTES_2468), true},
                { new RawHeader("test", BYTES_1357), new RawHeader("test", BYTES_2468), false},
                { new RawHeader("a", BYTES_2468), new RawHeader("b", BYTES_2468), false},
                { new RawHeader("test", BYTES_2468), new RawHeader("test", null), false},
                // Mixed Header Implementations
                { new RawHeader("test", "test".getBytes(StandardCharsets.UTF_8)), new Header("test", "test"), true}
        };
    }

    @Test(dataProvider = "headers")
    public void givenTwoHeaders_whenCheckingEqualityAndHashCodes_thenAsExpected(EventHeader a, EventHeader b, boolean expectEquality) {
        // Given and When
        boolean equals1 = a.equals(b);
        boolean equals2 = b.equals(a);
        int hash1 = a.hashCode();
        int hash2 = b.hashCode();

        // Then
        if (expectEquality) {
            Assert.assertTrue(equals1);
            Assert.assertTrue(equals2);
            Assert.assertEquals(hash1, hash2);
        } else {
            Assert.assertFalse(equals1);
            Assert.assertFalse(equals2);
            Assert.assertNotEquals(hash1, hash2);
        }
    }
}
