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
package io.telicent.smart.cache.distribution.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestHexGenerator {

    // SHA-256 of empty string, verified via `echo -n "" | sha256sum`
    private static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @DataProvider
    public Object[][] knownHashes() {
        return new Object[][] {
                { "", EMPTY_SHA256 },
                // echo -n "hello" | sha256sum
                { "hello", "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824" },
                // echo -n "Hello, World!" | sha256sum
                { "Hello, World!", "dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f" },
        };
    }

    @Test(dataProvider = "knownHashes")
    public void sha256Hex_knownInput_returnsExpectedHash(String input, String expectedHash) {
        Assert.assertEquals(HexGenerator.sha256Hex(input), expectedHash);
    }

    @Test
    public void sha256Hex_outputIs64LowercaseHexChars() {
        final String result = HexGenerator.sha256Hex("any input");
        Assert.assertEquals(result.length(), 64);
        Assert.assertTrue(result.matches("[0-9a-f]+"), "Output should be lowercase hex");
    }

    @Test
    public void sha256Hex_sameInputProducesSameOutput() {
        final String input = "deterministic";
        Assert.assertEquals(HexGenerator.sha256Hex(input), HexGenerator.sha256Hex(input));
    }

    @Test
    public void sha256Hex_differentInputsProduceDifferentHashes() {
        Assert.assertNotEquals(HexGenerator.sha256Hex("foo"), HexGenerator.sha256Hex("bar"));
    }

    @Test
    public void sha256Hex_caseSensitiveInput() {
        Assert.assertNotEquals(HexGenerator.sha256Hex("Hello"), HexGenerator.sha256Hex("hello"));
    }

    @Test
    public void sha256Hex_unicodeInput_handledAsUtf8() {
        // Verify Unicode input doesn't throw and produces a valid 64-char hex string
        final String result = HexGenerator.sha256Hex("café");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.length(), 64);
    }
}
