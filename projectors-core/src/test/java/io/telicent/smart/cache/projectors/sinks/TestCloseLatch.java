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
package io.telicent.smart.cache.projectors.sinks;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestCloseLatch {

    @Test
    public void givenCloseLatch_whenOpeningAndClosing_thenIndicatesClosed() {
        // Given
        CloseLatch latch = new CloseLatch();

        // When
        Assert.assertTrue(latch.isClosed());
        latch.open();
        Assert.assertFalse(latch.isClosed());
        Assert.assertTrue(latch.close());

        // Then
        Assert.assertTrue(latch.isClosed());
    }

    @Test
    public void givenCloseLatch_whenOpeningMoreThanOnce_thenSingleCloseStillIndicatesOpen() {
        // Given
        CloseLatch latch = new CloseLatch();

        // When
        latch.open();
        latch.open();
        Assert.assertFalse(latch.isClosed());

        // Then
        Assert.assertFalse(latch.close());
        Assert.assertFalse(latch.isClosed());
    }

    @DataProvider(name = "interactions")
    public static Object[][] interactions() {
        return new Object[][] {
                { 3, 3 },
                { 1, 2 },
                { 10, 4 },
                { 8, 11 },
                { 0, 1 },
                { 1, 0}
        };
    }

    @Test(dataProvider = "interactions")
    public void givenCloseLatch_whenOpeningAndClosingMoreThanOnce_thenCorrectClosedStateReported(int opens, int closes) {
        // Given
        CloseLatch latch = new CloseLatch();

        // When
        for (int i = 1; i <= opens; i++) {
            latch.open();
        }
        for (int i = 1; i <= closes; i++) {
            latch.close();
        }

        // Then
        Assert.assertEquals(latch.isClosed(), closes >= opens);
    }
}
