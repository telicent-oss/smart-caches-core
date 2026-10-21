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
package io.telicent.smart.cache.sources.buffered;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.EventSourceException;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collection;

// java:S2925 - Thread.sleep is required when waiting on real Kafka/Docker in integration tests
// java:S1124 - modifier order kept as-is
@SuppressWarnings({"java:S2925", "java:S1124"})
public class TestBufferedEventSource {

    private static abstract class DummySource
            extends AbstractBufferedEventSource<SimpleEvent<Integer, String>, Integer, String> {
        @Override
        protected Event<Integer, String> decodeEvent(SimpleEvent<Integer, String> internalEvent) {
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
            return true;
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
                this.events.add(new SimpleEvent<>(null, 1, "test"));
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
    public void givenAlwaysEmptySource_whenPolling_thenReturnsNullImmediately() {
        // Given
        EventSource<Integer, String> source = new AlwaysEmpty();

        // When
        long start = System.currentTimeMillis();
        Event<Integer, String> event = source.poll(Duration.ofSeconds(1));

        // Then
        Assert.assertNull(event);
        verifyLessThanTimeoutElapsed(start);
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

    private static final class PausedBufferedSource extends DummySource {
        private boolean deliveryPaused;
        private int pausedPolls;
        private int fetchAttempts;
        private Duration pausedTimeout;

        @Override
        protected boolean isDeliveryPaused() {
            return this.deliveryPaused;
        }

        @Override
        protected void whileDeliveryPaused(Duration timeout) {
            super.whileDeliveryPaused(timeout);
            this.pausedPolls++;
            this.pausedTimeout = timeout;
        }

        @Override
        protected boolean tryFillBuffer(Duration timeout) {
            this.fetchAttempts++;
            return true;
        }
    }

    @Test
    public void givenBufferedEvents_whenPausedAndResumed_thenRetainsEventsAndTracksLastPollState() {
        PausedBufferedSource source = new PausedBufferedSource();
        SimpleEvent<Integer, String> event = new SimpleEvent<>(null, 1, "retained");
        source.events.add(event);
        Assert.assertTrue(source.availableImmediately());
        Assert.assertFalse(source.wasPausedOnLastPoll());

        source.deliveryPaused = true;
        Assert.assertFalse(source.availableImmediately());
        Duration timeout = Duration.ofMillis(25);
        Assert.assertNull(source.poll(timeout));
        Assert.assertTrue(source.wasPausedOnLastPoll());
        Assert.assertEquals(source.pausedPolls, 1);
        Assert.assertEquals(source.pausedTimeout, timeout);
        Assert.assertEquals(source.events.size(), 1);

        source.deliveryPaused = false;
        Assert.assertTrue(source.wasPausedOnLastPoll(), "Resume must not change the previous poll's state");
        Assert.assertTrue(source.availableImmediately());
        Assert.assertSame(source.poll(Duration.ZERO), event);
        Assert.assertFalse(source.wasPausedOnLastPoll());
        Assert.assertFalse(source.availableImmediately());
        source.close();
        Assert.assertFalse(source.availableImmediately());
    }

    @Test
    public void givenEmptyPausedSource_whenPolling_thenRunsPausedHookWithoutFetching() {
        PausedBufferedSource source = new PausedBufferedSource();
        source.deliveryPaused = true;
        Assert.assertFalse(source.availableImmediately());
        Assert.assertNull(source.poll(null));
        Assert.assertTrue(source.wasPausedOnLastPoll());
        Assert.assertEquals(source.pausedPolls, 1);
        Assert.assertNull(source.pausedTimeout);
        Assert.assertEquals(source.fetchAttempts, 0);
        source.deliveryPaused = false;
        Assert.assertNull(source.poll(Duration.ZERO));
        Assert.assertFalse(source.wasPausedOnLastPoll());
        Assert.assertEquals(source.fetchAttempts, 1);
        source.close();
        Assert.expectThrows(IllegalStateException.class, () -> source.poll(Duration.ZERO));
    }
}
