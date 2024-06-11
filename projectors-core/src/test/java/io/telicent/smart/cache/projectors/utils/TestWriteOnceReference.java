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
package io.telicent.smart.cache.projectors.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestWriteOnceReference {

    @Test
    public void givenWriteOnceReferenceWithInitialValue_whenAccessing_thenValueIsSet() {
        // Given
        WriteOnceReference<String> reference = new WriteOnceReference<>("test");

        // When
        String actual = reference.get();

        // Then
        Assert.assertTrue(reference.isSet());
        Assert.assertEquals(actual, "test");
    }

    @Test
    public void givenWriteOnceReference_whenSetting_thenValueIsSet() {
        // Given
        WriteOnceReference<String> reference = new WriteOnceReference<>();

        // When
        reference.set("test");

        // Then
        Assert.assertTrue(reference.isSet());
        Assert.assertEquals(reference.get(), "test");
    }

    @Test
    public void givenWriteOnceReference_thenNotSet() {
        // Given
        WriteOnceReference<String> reference = new WriteOnceReference<>();

        // Then
        Assert.assertFalse(reference.isSet());
        Assert.assertNull(reference.get());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenWriteOnceReference_whenSettingTwice_thenError() {
        // Given
        WriteOnceReference<String> reference = new WriteOnceReference<>();

        // When and Then
        reference.set("test");
        reference.set("other");
    }
}
