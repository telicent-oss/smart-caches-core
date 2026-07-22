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
package io.telicent.smart.cache.distribution.lifecycle.tracker;

import io.telicent.smart.cache.distribution.lifecycle.events.listeners.LoggingListener;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.EventSourceException;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;

@SuppressWarnings({ "unchecked", "resource" })
public class TestDistributionLifecycleTracker {

    private static final Duration SHORT_STARTUP_TIMEOUT = Duration.ofMillis(500);
    private static final Duration SHORT_POLL_TIMEOUT = Duration.ofMillis(50);
    private static final Duration SHORT_CHECK_INTERVAL = Duration.ofMillis(100);

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Event Source.*")
    public void givenNullEventSource_whenCreatingTracker_thenNPE() {
        // Given, When and Then
        DistributionLifecycleTracker.builder().eventSource(null).build();
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*State store.*")
    public void givenNullStateStore_whenCreatingTracker_thenNPE() {
        // Given, When and Then
        DistributionLifecycleTracker.builder().eventSource(mock(EventSource.class)).stateStore(null).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*must be greater than zero")
    public void givenNegativeListenerThreads_whenCreatingTracker_thenIllegalArgument() {
        // Given, When and Then
        DistributionLifecycleTracker.builder().eventSource(mock(EventSource.class)).stateStore(mock(
                DistributionLifecycleStateStore.class)).listenerThreads(-1).build();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullApplication_whenCreatingTracker_thenNPE() {
        // Given, When and Then
        DistributionLifecycleTracker.builder().eventSource(mock(EventSource.class)).stateStore(mock(
                DistributionLifecycleStateStore.class)).listenerThreads(1).application(null).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*must be > 0")
    public void givenNegativeCheckInterval_whenCreatingTracker_thenIllegalArgument() {
        // Given, When and Then
        DistributionLifecycleTracker.builder().eventSource(mock(EventSource.class)).stateStore(mock(
                DistributionLifecycleStateStore.class)).listenerThreads(1).application("test").trackerCheckInterval(
                Duration.ofMinutes(-1)).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*must be > 0")
    public void givenZeroCheckInterval_whenCreatingTracker_thenIllegalArgument() {
        // Given, When and Then
        DistributionLifecycleTracker.builder().eventSource(mock(EventSource.class)).stateStore(mock(
                DistributionLifecycleStateStore.class)).listenerThreads(1).application("test").trackerCheckInterval(
                Duration.ZERO).build();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*exhausted")
    public void givenExhaustedEventSource_whenCreatingTracker_thenIllegalState() {
        // Given, When and Then
        DistributionLifecycleTracker.builder()
                                    .eventSource(new InMemoryEventSource<>(Collections.emptyList()))
                                    .stateStore(mock(DistributionLifecycleStateStore.class))
                                    .listenerThreads(1)
                                    .application("test")
                                    .build();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*closed")
    public void givenClosedEventSource_whenCreatingTracker_thenIllegalState() {
        // Given, When and Then
        InMemoryEventSource<UUID, LazyEnvelope> eventSource = new InMemoryEventSource<>(Collections.emptyList());
        eventSource.close();
        DistributionLifecycleTracker.builder()
                                    .eventSource(eventSource)
                                    .stateStore(mock(DistributionLifecycleStateStore.class))
                                    .listenerThreads(1)
                                    .application("test")
                                    .build();
    }

    @Test
    public void givenValidConfig_whenCreatingTracker_thenOk() {
        // Given and When
        try (DistributionLifecycleTracker tracker = trackerBuilder(blockingEventSource()).build()) {
            // Then
            Assert.assertTrue(tracker.isRunning());
            tracker.close();
            Assert.assertFalse(tracker.isRunning());
            Assert.assertEquals(tracker.getTrackerState(), TrackerState.CLOSED);
        }
    }

    @Test
    public void givenValidTracker_whenCheckingStateOverTime_thenRemainsRunning() throws InterruptedException {
        // Given and When
        try (DistributionLifecycleTracker tracker = trackerBuilder(blockingEventSource())
                .trackerCheckInterval(SHORT_CHECK_INTERVAL)
                .build()) {
            // Then
            Assert.assertTrue(tracker.isRunning());
            Assert.assertEquals(tracker.getTrackerState(), TrackerState.RUNNING);
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 1000L) {
                Assert.assertTrue(tracker.isRunning());
                Assert.assertEquals(tracker.getTrackerState(), TrackerState.RUNNING);
                Thread.sleep(50);
            }
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*exited prematurely")
    public void givenToBeExhaustedEventStore_whenCreatingTracker_thenFails() {
        // Given
        AtomicInteger exhaustionChecks = new AtomicInteger();
        EventSource<UUID, LazyEnvelope> eventSource = new TestEventSource(
                () -> exhaustionChecks.incrementAndGet() >= 2,
                () -> null);

        // When and Then
        trackerBuilder(eventSource).build();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*projection failed.*")
    public void givenFailingEventStore_whenCreatingTracker_thenFails() {
        // Given
        EventSource<UUID, LazyEnvelope> eventSource = new TestEventSource(() -> false, () -> null,
                                                                          timeout -> {
                                                                              throw new EventSourceException(
                                                                                      "Authentication failed");
                                                                          });

        // When and Then
        trackerBuilder(eventSource).build();
    }

    @Test
    public void givenOnDemandExhaustedEventStore_whenCreatingTracker_thenSucceeds_andSubsequentStateCheckFails() throws
            InterruptedException {
        // Given
        AtomicBoolean isExhausted = new AtomicBoolean(false);
        EventSource<UUID, LazyEnvelope> eventSource = new TestEventSource(isExhausted::get, () -> null);

        // When
        try (DistributionLifecycleTracker tracker = trackerBuilder(eventSource)
                .trackerCheckInterval(SHORT_CHECK_INTERVAL)
                .build()) {

            // Then
            Assert.assertTrue(tracker.isRunning());
            Assert.assertEquals(tracker.getTrackerState(), TrackerState.RUNNING);
            // This will cause the tracker projection to terminate but subsequent state check will return cached state
            // until check interval has expired
            isExhausted.set(true);
            Assert.assertEquals(tracker.getTrackerState(), TrackerState.RUNNING);

            // And
            // Once we wait longer than the check interval then state should be re-checked and report failed
            Thread.sleep(200);
            Assert.assertFalse(tracker.isRunning());
            Assert.assertEquals(tracker.getTrackerState(), TrackerState.FAILED);
            Assert.assertFalse(tracker.isRunning());
        }
    }

    @Test
    public void givenLaggyEventStore_whenCreatingTracker_thenSucceedsOnceLagIsZero() {
        // Given
        EventSource<UUID, LazyEnvelope> eventSource = laggyEventSource(2);

        // When
        try (DistributionLifecycleTracker tracker = trackerBuilder(eventSource)
                .trackerCheckInterval(SHORT_CHECK_INTERVAL)
                .build()) {

            // Then
            Assert.assertTrue(tracker.isRunning());
            Assert.assertEquals(tracker.getTrackerState(), TrackerState.RUNNING);
        }
    }

    private static EventSource<UUID, LazyEnvelope> laggyEventSource(int initialLag) {
        AtomicLong lag = new AtomicLong(initialLag);
        return new TestEventSource(() -> false, () -> lag.get() > 0L ? lag.decrementAndGet() : 0L);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*lag.*up to date decisions.*")
    public void givenVeryLaggyEventStore_whenCreatingTracker_thenFailsOnceTimeElapsed() {
        // Given
        EventSource<UUID, LazyEnvelope> eventSource = laggyEventSource(1_000_000);

        // When
        try (DistributionLifecycleTracker tracker = DistributionLifecycleTracker.builder()
                                                                                .eventSource(eventSource)
                                                                                .stateStore(mock(
                                                                                        DistributionLifecycleStateStore.class))
                                                                                .listeners(
                                                                                        List.of(new LoggingListener()))
                                                                                .listenerThreads(1)
                                                                                .pollTimeout(SHORT_POLL_TIMEOUT)
                                                                                .trackerStartupTimeout(
                                                                                        SHORT_STARTUP_TIMEOUT)
                                                                                .trackerCheckInterval(
                                                                                        SHORT_CHECK_INTERVAL)
                                                                                .application("test")
                                                                                .build()) {

            // Then
            Assert.fail("Expected creation to fail due to high lag");
        }
    }

    private static DistributionLifecycleTracker.DistributionLifecycleTrackerBuilder trackerBuilder(EventSource<UUID, LazyEnvelope> eventSource) {
        return DistributionLifecycleTracker.builder()
                                           .eventSource(eventSource)
                                           .stateStore(mock(DistributionLifecycleStateStore.class))
                                           .listeners(List.of(new LoggingListener()))
                                           .listenerThreads(1)
                                           .pollTimeout(SHORT_POLL_TIMEOUT)
                                           .trackerStartupTimeout(SHORT_STARTUP_TIMEOUT)
                                           .application("test");
    }

    private static EventSource<UUID, LazyEnvelope> blockingEventSource() {
        return new TestEventSource(() -> false, () -> null);
    }

    private static final class TestEventSource implements EventSource<UUID, LazyEnvelope> {

        private static final long POLL_SLICE_MILLIS = 10L;

        private final BooleanSupplier exhaustedSupplier;
        private final Supplier<Long> remainingSupplier;
        private final Function<Duration, Event<UUID, LazyEnvelope>> poller;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final AtomicBoolean interrupted = new AtomicBoolean(false);
        private final AtomicReference<Thread> pollingThread = new AtomicReference<>();

        private TestEventSource(BooleanSupplier exhaustedSupplier, Supplier<Long> remainingSupplier) {
            this(exhaustedSupplier, remainingSupplier, null);
        }

        private TestEventSource(BooleanSupplier exhaustedSupplier, Supplier<Long> remainingSupplier,
                                Function<Duration, Event<UUID, LazyEnvelope>> poller) {
            this.exhaustedSupplier = exhaustedSupplier;
            this.remainingSupplier = remainingSupplier;
            this.poller = poller != null ? poller : this::pollWithTimeout;
        }

        @Override
        public boolean availableImmediately() {
            return false;
        }

        @Override
        public boolean isExhausted() {
            return this.closed.get() || this.exhaustedSupplier.getAsBoolean();
        }

        @Override
        public void close() {
            this.closed.set(true);
            interrupt();
        }

        @Override
        public boolean isClosed() {
            return this.closed.get();
        }

        @Override
        public Event<UUID, LazyEnvelope> poll(Duration timeout) {
            if (this.closed.get()) {
                throw new IllegalStateException("Source has been closed");
            }

            this.interrupted.set(false);
            this.pollingThread.set(Thread.currentThread());
            try {
                return this.poller.apply(timeout);
            } finally {
                this.pollingThread.compareAndSet(Thread.currentThread(), null);
            }
        }

        private Event<UUID, LazyEnvelope> pollWithTimeout(Duration timeout) {
            long remainingMillis = Math.max(timeout.toMillis(), 1L);
            while (!this.closed.get() && !this.interrupted.get() && !this.exhaustedSupplier.getAsBoolean()) {
                if (remainingMillis <= 0L) {
                    return null;
                }

                long sleepMillis = Math.min(POLL_SLICE_MILLIS, remainingMillis);
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                remainingMillis -= sleepMillis;
            }
            return null;
        }

        @Override
        public Long remaining() {
            return this.remainingSupplier.get();
        }

        @Override
        public void processed(Collection<Event<?, ?>> processedEvents) {
            // No-op
        }

        @Override
        public void interrupt() {
            this.interrupted.set(true);
            Thread thread = this.pollingThread.get();
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
}
