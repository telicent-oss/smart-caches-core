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
package io.telicent.smart.cache.security;

import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractExceptionTests<T extends RuntimeException> {

    protected abstract T create(String message);

    protected abstract T create(String message, Throwable cause);

    @Test
    public void givenNullMessage_whenCreating_thenEmptyException() {
        // Given and When
        T ex = create(null);

        // Then
        Assert.assertNotNull(ex);
        Assert.assertNull(ex.getMessage());
    }

    @Test
    public void givenMessage_whenCreating_thenMessageAvailable() {
        // Given
        String message = "This is a test error";

        // When
        T ex = create(message);

        // Then
        Assert.assertEquals(ex.getMessage(), message);
    }

    @Test
    public void givenMessageAndCause_whenCreating_thenBothAvailable() {
        // Given
        String message = "Something went wrong";
        Throwable cause = new RuntimeException("failed");

        // When
        T ex = create(message, cause);

        // Then
        Assert.assertEquals(ex.getMessage(), message);
        Assert.assertSame(ex.getCause(), cause);
    }
}
