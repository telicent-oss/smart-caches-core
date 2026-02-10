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
package io.telicent.smart.cache.security.plugins.failsafe;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestRawPrimitive {

    @Test(invocationCount = 10)
    public void givenRandomBytes_whenUsingRawPrimitive_thenPreservedAsIs() {
        // Given
        byte[] random = RandomUtils.insecure().randomBytes(100);

        // When
        RawPrimitive raw = new RawPrimitive(random);

        // Then
        Assert.assertEquals(raw.encoded(), random);
        verifyStoredBytes(random, raw.decodedAttributes());
        verifyStoredBytes(random, raw.decodedLabels());
        Assert.assertNotNull(raw.toDebugString());
    }

    private static void verifyStoredBytes(byte[] random, RawBytes stored) {
        Assert.assertEquals(stored.data(), random);
    }

    @Test(invocationCount = 10)
    public void givenRandomBytes_whenUsingRawPrimitive_thenToStringIndicatesEncodedLength() {
        // Given
        int length = RandomUtils.insecure().randomInt(10, 100);
        byte[] random = RandomUtils.insecure().randomBytes(length);

        // When
        RawPrimitive raw = new RawPrimitive(random);

        // Then
        String toString = raw.toString();
        Assert.assertTrue(Strings.CS.contains(toString, Integer.toString(length)));
    }
}
