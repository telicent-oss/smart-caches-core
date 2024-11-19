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
package io.telicent.smart.caches.security.plugins;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

public class TestSchemaPrefixes {

    private static final Random RANDOM = new Random();

    @Test
    public void givenEmptyData_whenDecodingPrefix_thenNull() {
        // Given
        byte[] data = new byte[0];

        // When
        Short decoded = SecurityPlugin.decodeSchemaPrefix(data);

        // Then
        Assert.assertNull(decoded);
    }

    @Test(invocationCount = 1000)
    public void givenRandomSchemaIds_whenEncodingPrefix_thenDecodedCorrectly() {
        // Given
        short id = (short) RANDOM.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1);

        // When
        byte[] encoded = SecurityPlugin.encodeSchemaPrefix(id);

        // Then
        Short decoded = SecurityPlugin.decodeSchemaPrefix(encoded);
        Assert.assertNotNull(decoded);
        Assert.assertEquals(decoded.shortValue(), id);
    }
}
