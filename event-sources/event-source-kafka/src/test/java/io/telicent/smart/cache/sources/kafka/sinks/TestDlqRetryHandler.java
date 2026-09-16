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
package io.telicent.smart.cache.sources.kafka.sinks;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.common.errors.RecordBatchTooLargeException;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class TestDlqRetryHandler {

    protected static final RecordTooLargeException RECORD_TOO_LARGE_EXCEPTION = new RecordTooLargeException("Test");

    private final DlqRetryHandler retryHandler = new DlqRetryHandler();

    @Test
    public void givenRetryHandler_whenQueryingMaxRetries_thenOneReturned() {
        // Given, When and Then
        Assert.assertEquals(retryHandler.maxRetries(), 1);
    }

    @DataProvider(name = "exceptions")
    private Object[][] exceptions() {
        return new Object[][] {
                { RECORD_TOO_LARGE_EXCEPTION, true },
                { new ExecutionException(RECORD_TOO_LARGE_EXCEPTION), true },
                { new ExecutionException(new IllegalStateException()), false },
                { new IllegalArgumentException(), false },
                { new RecordBatchTooLargeException(), false },
                { new IllegalStateException(new IllegalArgumentException(RECORD_TOO_LARGE_EXCEPTION)), true }
        };
    }

    @Test(dataProvider = "exceptions")
    public void givenRetryHandler_whenIsRetryable_thenAsExpected(Exception e, boolean shouldBeRetryable) {
        // Given, When and Then
        Assert.assertEquals(retryHandler.isRetryable(e), shouldBeRetryable);
    }

    @Test
    public void givenRetryHandler_whenPreparingForRetry_thenValueStripped_andRetryReasonHeaderAdded() {
        // Given
        Event<Integer, String> event = new SimpleEvent<>(Collections.emptyList(), 1, "Test");

        // When
        Event<Integer, String> prepared = retryHandler.prepareEventForRetry(event, RECORD_TOO_LARGE_EXCEPTION);

        // Then
        Assert.assertNotEquals(event, prepared);
        Assert.assertNull(prepared.value());

        // And
        Assert.assertNotNull(prepared.lastRawHeader(TelicentHeaders.DEAD_LETTER_RETRY_REASON));
    }

    @Test
    public void givenRetryHandler_whenPreparingTombstoneEvent_thenNullReturned() {
        // Given
        Event<Integer, String> tombstone = new SimpleEvent<>(Collections.emptyList(), 1, null);

        // When and Then
        Assert.assertNull(retryHandler.prepareEventForRetry(tombstone, RECORD_TOO_LARGE_EXCEPTION));
    }

    @Test
    public void givenRetryHandler_whenPreparingWithWrongExceptionType_thenNullReturned() {
        // Given
        Event<Integer, String> event = new SimpleEvent<>(Collections.emptyList(), 1, "Test");

        // When and Then
        Assert.assertNull(retryHandler.prepareEventForRetry(event, new NullPointerException()));
    }
}
