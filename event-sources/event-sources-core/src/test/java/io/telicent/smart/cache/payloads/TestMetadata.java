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

import java.time.Instant;
import java.util.Date;

public class TestMetadata {

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoParameters_whenBuilding_thenNPE() {
        // Given, When and Then
        Metadata.create().build();
    }

    @Test
    public void givenValidParameters_whenBuilding_thenOk() {
        // Given and When
        Metadata metadata = Metadata.create()
                                    .generatedAt(Date.from(Instant.now()))
                                    .generatedBy("tests")
                                    .generatorVersion("1.0.0")
                                    .documentFormat("test/v1")
                                    .build();

        // Then
        Assert.assertEquals(metadata.getGeneratedBy(), "tests");
        Assert.assertNotNull(metadata.getGeneratedAt());
        Assert.assertEquals(metadata.getGeneratorVersion(), "1.0.0");
        Assert.assertEquals(metadata.getDocumentFormat(), "test/v1");
    }
}
