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
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import org.apache.kafka.common.errors.RecordTooLargeException;

import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

/**
 * A retry handler for sinks used as DLQ
 * <p>
 * If an event cannot be sent as-is to the DLQ and produces a {@link RecordTooLargeException} then retries with the
 * value removed, any other errors are not retried.  Events sent to the DLQ will have headers pointing to their input
 * event anyway and thus an operator diagnosing a problem can find the offending input event even if they can't see the
 * value directly in the DLQ.
 * </p>
 */
public class DlqRetryHandler implements KafkaRetryHandler {

    /**
     * Gets the record too large exception, which is the only exception we retry
     *
     * @param e Exception
     * @return {@link RecordTooLargeException} if present, {@code null} otherwise
     */
    private RecordTooLargeException getRecordTooLarge(Exception e) {
        // Note that since send() is happening as an async task we may get an ExecutionException here which we want
        // to unwrap and test the exception cause
        // In general we find any RecordTooLargeException that is in the exception stack
        if (e instanceof RecordTooLargeException tooLarge) {
            return tooLarge;
        } else if (e instanceof ExecutionException execException && execException.getCause() instanceof RecordTooLargeException innerTooLarge) {
            return innerTooLarge;
        }

        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof RecordTooLargeException causeTooLarge) {
                return causeTooLarge;
            }
            cause = cause.getCause();
        }

        return null;
    }

    @Override
    public boolean isRetryable(Exception e) {
        // This only retries RecordTooLargeException, this implies the event was at/near the maximum permitted size
        // so if we retry with fewer DLQ headers added we may succeed in sending the event to the DLQ upon retry
        return getRecordTooLarge(e) != null;
    }

    @Override
    public int maxRetries() {
        return 1;
    }

    @Override
    public <TKey, TValue> Event<TKey, TValue> prepareEventForRetry(Event<TKey, TValue> event, Exception e) {
        if (event.value() != null) {
            RecordTooLargeException tooLarge = getRecordTooLarge(e);
            if (tooLarge == null) {
                return null;
            }
            return event.replaceValue((TValue) null)
                        .addHeaders(Stream.of(
                                new Header(TelicentHeaders.DEAD_LETTER_RETRY_REASON,
                                           "Event value removed due to " + tooLarge.getMessage())));
        }
        // If the event already didn't have a value then can't remove that so return null which indicates no retry
        // possible
        return null;
    }
}
