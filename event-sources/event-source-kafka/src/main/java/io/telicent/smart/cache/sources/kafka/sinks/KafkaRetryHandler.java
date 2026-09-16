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

/**
 * Interface used by {@link KafkaSink} to decide when some events can be retried even though Kafka sending reported them
 * as failed.
 * <p>
 * This interface is designed to allow applications to automatically retry sending events, possibly after modification,
 * when certain errors occur.  For example the {@link DlqRetryHandler} is a concrete implementation that retries when a
 * {@link org.apache.kafka.common.errors.RecordTooLargeException} occurs by stripping the event value.
 * </p>
 *
 * @see DlqRetryHandler
 */
public interface KafkaRetryHandler {

    /**
     * Gets whether the given exception represents an error we may be able to recover from if we retry sending the event
     * that caused it
     *
     * @param e Exception
     * @return True if retryable, false otherwise
     */
    boolean isRetryable(Exception e);

    /**
     * Gets the maximum number of retries (for exceptions that are reported as retryable by
     * {@link #isRetryable(Exception)}) that should be attempted before treating the event as failing to send.
     * <p>
     * The default implementation in this interface returns {@code 3}
     * </p>
     *
     * @return Maximum retries
     */
    default int maxRetries() {
        return 3;
    }

    /**
     * Prepares an event for retry, returning a potentially modified event
     * <p>
     * For example a retry handler that is retrying an event that was reported as too large might modify the event to
     * make it smaller before retrying.  If the retry handler cannot make the event smaller it can either return the
     * event unmodified and hope that a further retry will succeed, or it can return {@code null} to abort further
     * retries.
     * </p>
     * <p>
     * The retry handler receives the same exception that was previously checked against {@link #isRetryable(Exception)}
     * so can add that additional exception information to the event if it wishes to.
     * </p>
     *
     * @param event    Event
     * @param e        Exception that necessitated the retry, this is the same exception that would previously have been
     *                 passed to {@link #isRetryable(Exception)} to determine if a retry was possible.
     * @param <TKey>   Key Type
     * @param <TValue> Value Type
     * @return Event to retry with, or {@code null} if no further retries are possible
     */
    <TKey, TValue> Event<TKey, TValue> prepareEventForRetry(Event<TKey, TValue> event, Exception e);
}
