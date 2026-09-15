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
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.errors.RecordTooLargeException;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

/**
 * A retry handler for sinks used as DLQ
 */
public class DlqRetryHandler implements KafkaRetryHandler {

    /**
     * The DLQ headers that our services typically apply to events going to DLQ, these are listed in order from most
     * important to least important.  When retrying we will gradually strip one header at a time until we have no
     * headers left to strip
     */
    //@formatter:off
    private static final List<String> DLQ_HEADERS =
            List.of(TelicentHeaders.DEAD_LETTER_REASON,
                    TelicentHeaders.DEAD_LETTER_SOURCE_TOPIC,
                    TelicentHeaders.DEAD_LETTER_SOURCE_PARTITION,
                    TelicentHeaders.DEAD_LETTER_SOURCE_OFFSET,
                    TelicentHeaders.DEAD_LETTER_EXCEPTION_CLASS);
    //@formatter:on

    @Override
    public boolean isRetryable(Exception e) {
        // Note that since the send() is happening as an async task we may get an ExecutionException here which we want
        // to  unwrap and test the exception cause
        if (e instanceof ExecutionException execException && execException.getCause() instanceof RecordTooLargeException) {
            return true;
        }

        // This only retries RecordTooLargeException, this implies the event was at/near the maximum permitted size
        // so if we retry with fewer DLQ headers added we may succeed in sending the event to the DLQ upon retry
        return e instanceof RecordTooLargeException;
    }

    @Override
    public int maxRetries() {
        return DLQ_HEADERS.size();
    }

    @Override
    public <TKey, TValue> Event<TKey, TValue> prepareEventForRetry(Event<TKey, TValue> event) {
        // Find the least important DLQ header on the event and remove it
        String headerToRemove = IntStream.iterate(DLQ_HEADERS.size() - 1, i -> i >= 0, i -> i - 1)
                                         .mapToObj(DLQ_HEADERS::get)
                                         .filter(header -> StringUtils.isNotBlank(event.lastHeader(header)))
                                         .findFirst()
                                         .orElse(null);

        // If no DLQ headers left to remove no further retry possible
        if (headerToRemove == null) {
            return null;
        }
        // Otherwise strip that DLQ header from the event and retry
        return event.replaceHeaders(event.headers().filter(h -> !Objects.equals(h.key(), headerToRemove)));
    }
}
