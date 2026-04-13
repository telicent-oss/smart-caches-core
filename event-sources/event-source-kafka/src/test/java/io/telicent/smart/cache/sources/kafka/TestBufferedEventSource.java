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
package io.telicent.smart.cache.sources.kafka;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.EventSourceException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collection;

public class TestBufferedEventSource {

    private static abstract class DummySource
            extends AbstractBufferedEventSource<KafkaEvent<Integer, String>, Integer, String> {
        @Override
        protected Event<Integer, String> decodeEvent(KafkaEvent<Integer, String> internalEvent) {
            return internalEvent;
        }

        @Override
        public Long remaining() {
            return 0L;
        }

        @Override
        public void processed(Collection<Event<?, ?>> processedEvents) {

        }
    }

    private static final class AlwaysEmpty
            extends DummySource {

        @Override
        protected boolean tryFillBuffer(Duration timeout) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignore
                return true;
            }
            return false;
        }
    }

    private static final class AlwaysErrors extends DummySource {

        @Override
        protected boolean tryFillBuffer(Duration timeout) {
            throw new EventSourceException("Failed");
        }
    }

    private static final class EventuallyNonEmpty
            extends DummySource {

        private int attemptCount = 0;
        private final int yieldAfterAttempts;

        private EventuallyNonEmpty(int yieldAfterAttempts) {
            this.yieldAfterAttempts = yieldAfterAttempts;
        }

        @Override
        protected boolean tryFillBuffer(Duration timeout) {
            this.attemptCount++;
            if (this.attemptCount > this.yieldAfterAttempts) {
                this.events.add(new KafkaEvent<>(new ConsumerRecord<>("test", 0, 0, 1, "test"), null));
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Ignore
                    return true;
                }
            }
            return false;
        }
    }

    @Test
    public void givenAlwaysEmptySource_whenPolling_thenWaitsForTimeoutBeforeReturningNull() {
        // Given
        EventSource<Integer, String> source = new AlwaysEmpty();

        // When
        long start = System.currentTimeMillis();
        Event<Integer, String> event = source.poll(Duration.ofSeconds(1));

        // Then
        Assert.assertNull(event);
        verifyAtLeastTimeoutElapsed(start);
    }

    private static void verifyAtLeastTimeoutElapsed(long start) {
        Assert.assertTrue(System.currentTimeMillis() - start > 1000,
                          "Elapsed time (" + (System.currentTimeMillis() - start) + ") was less than timeout");
    }

    @Test
    public void givenEventuallyNonEmptySource_whenBufferFillsWithinTimeout_thenFirstPollReturnsNonNull_andTimeoutNotElapsed() {
        // Given
        EventSource<Integer, String> source = new EventuallyNonEmpty(5);

        // When
        long start = System.currentTimeMillis();
        Event<Integer, String> event = source.poll(Duration.ofSeconds(1));

        // Then
        Assert.assertNotNull(event);

        // And
        verifyLessThanTimeoutElapsed(start);
    }

    @Test
    public void givenEventuallyNonEmptySource_whenBufferFillsAfterTimeout_thenFirstPollReturnsNull_andSubsequentPollIsNonNull() {
        // Given
        EventSource<Integer, String> source = new EventuallyNonEmpty(15);

        // When
        long start = System.currentTimeMillis();
        Event<Integer, String> event = source.poll(Duration.ofSeconds(1));

        // Then
        Assert.assertNull(event);
        verifyAtLeastTimeoutElapsed(start);

        // And
        start = System.currentTimeMillis();
        Assert.assertNotNull(source.poll(Duration.ofSeconds(1)));
        verifyLessThanTimeoutElapsed(start);
    }

    private static void verifyLessThanTimeoutElapsed(long start) {
        Assert.assertTrue(System.currentTimeMillis() - start < 1000,
                          "Elapsed time (" + (System.currentTimeMillis() - start) + ") should be less than timeout");
    }

    @Test
    public void givenAlwaysErrorSource_whenPolling_thenErrorsImmediately() {
        // Given
        EventSource<Integer, String> source = new AlwaysErrors();

        // When
        long start = System.currentTimeMillis();
        Assert.assertThrows(EventSourceException.class, () -> source.poll(Duration.ofSeconds(1)));

        // Then
        verifyLessThanTimeoutElapsed(start);
    }
}
