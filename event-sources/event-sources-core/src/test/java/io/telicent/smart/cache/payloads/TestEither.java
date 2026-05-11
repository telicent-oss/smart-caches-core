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

public class TestEither {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNullValues_whenCreatingEither_thenIllegalArgument() {
        // Given, When and Then
        new Either<String, Integer>(null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenBothValues_whenCreatingEither_thenIllegalArgument() {
        // Given, When and Then
        new Either<>("test", 124);
    }

    @Test
    public void givenFirstValueType_whenCreatingEither_thenOk() {
        // Given
        String strValue = "test";
        Integer intValue = null;

        // When
        Either<String, Integer> either = new Either<>(strValue, intValue);

        // Then
        Assert.assertTrue(either.isA());
        Assert.assertEquals(either.getA(), strValue);
        Assert.assertNull(either.getB());
    }

    @Test
    public void givenSecondValueType_whenCreatingEither_thenOk() {
        // Given
        String strValue = null;
        Integer intValue = 4568;

        // When
        Either<String, Integer> either = new Either<>(strValue, intValue);

        // Then
        Assert.assertTrue(either.isB());
        Assert.assertEquals(either.getB(), intValue);
        Assert.assertNull(either.getA());
    }
}
