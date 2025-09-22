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
package io.telicent.smart.cache.projectors.sinks.events.splitter;

import org.apache.commons.codec.digest.PureJavaCrc32;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Locale;

public class TestChunkIntegrityHelper {

    private static final class CustomHashHelper extends AbstractChunkIntegrityHelper {
        public CustomHashHelper(String hashAlgorithm) {
            super(new PureJavaCrc32(), ChunkIntegrityHelper.CHECKSUM_ALGORITHM_CRC_32, hashAlgorithm);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Unable to initialize.*")
    public void givenBadAlgorithm_whenCreatingHelper_thenFails() {
        // Given, When and Then
        new CustomHashHelper("none");
    }

    @Test
    public void givenGoodAlgorithm_whenCreatingHelper_thenSuccess() {
        // Given
        String algo = "SHA512";

        // When
        CustomHashHelper helper = new CustomHashHelper(algo);

        // Then
        Assert.assertEquals(helper.hashAlgorithm(), algo.toLowerCase(Locale.ROOT));
    }
}
