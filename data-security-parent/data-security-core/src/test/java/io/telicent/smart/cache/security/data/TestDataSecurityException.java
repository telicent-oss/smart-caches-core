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
package io.telicent.smart.cache.security.data;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDataSecurityException {

    @Test
    public void givenMessage_whenCreating_thenMessageAvailable() {
        final DataSecurityException ex = new DataSecurityException("test error");

        Assert.assertEquals(ex.getMessage(), "test error");
        Assert.assertNull(ex.getCause());
    }

    @Test
    public void givenNullMessage_whenCreating_thenNullMessage() {
        final DataSecurityException ex = new DataSecurityException(null);

        Assert.assertNull(ex.getMessage());
    }

    @Test
    public void givenMessageAndCause_whenCreating_thenBothAvailable() {
        final Throwable cause = new RuntimeException("root cause");
        final DataSecurityException ex = new DataSecurityException("wrapping error", cause);

        Assert.assertEquals(ex.getMessage(), "wrapping error");
        Assert.assertSame(ex.getCause(), cause);
    }

    @Test
    public void givenNullCause_whenCreatingWithCauseConstructor_thenNullCause() {
        final DataSecurityException ex = new DataSecurityException("message only", null);

        Assert.assertEquals(ex.getMessage(), "message only");
        Assert.assertNull(ex.getCause());
    }
}
