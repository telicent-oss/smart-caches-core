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
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.EventSourceException;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({ "unchecked", "resource" })
public class TestDistributionLifecycleTracker {

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
        try (DistributionLifecycleTracker tracker = DistributionLifecycleTracker.builder()
                                                                                .eventSource(mock(EventSource.class))
                                                                                .stateStore(mock(
                                                                                        DistributionLifecycleStateStore.class))
                                                                                .listeners(
                                                                                        List.of(new LoggingListener()))
                                                                                .listenerThreads(1)
                                                                                .application("test")
                                                                                .build()) {
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
        try (DistributionLifecycleTracker tracker = DistributionLifecycleTracker.builder()
                                                                                .eventSource(mock(EventSource.class))
                                                                                .stateStore(mock(
                                                                                        DistributionLifecycleStateStore.class))
                                                                                .listeners(
                                                                                        List.of(new LoggingListener()))
                                                                                .listenerThreads(1)
                                                                                .trackerCheckInterval(
                                                                                        Duration.ofMillis(100))
                                                                                .application("test")
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
        EventSource<UUID, LazyEnvelope> eventSource = mock(EventSource.class);
        when(eventSource.isExhausted()).thenReturn(false, true);

        // When and Then
        DistributionLifecycleTracker.builder()
                                    .eventSource(eventSource)
                                    .stateStore(mock(
                                            DistributionLifecycleStateStore.class))
                                    .listeners(
                                            List.of(new LoggingListener()))
                                    .listenerThreads(1)
                                    .application("test")
                                    .build();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*projection failed.*")
    public void givenFailingEventStore_whenCreatingTracker_thenFails() {
        // Given
        EventSource<UUID, LazyEnvelope> eventSource = mock(EventSource.class);
        when(eventSource.poll(any())).thenThrow(new EventSourceException("Authentication failed"));

        // When and Then
        DistributionLifecycleTracker.builder()
                                    .eventSource(eventSource)
                                    .stateStore(mock(
                                            DistributionLifecycleStateStore.class))
                                    .listeners(
                                            List.of(new LoggingListener()))
                                    .listenerThreads(1)
                                    .application("test")
                                    .build();
    }

    @Test
    public void givenOnDemandExhaustedEventStore_whenCreatingTracker_thenSucceeds_andSubsequentStateCheckFails() throws
            InterruptedException {
        // Given
        EventSource<UUID, LazyEnvelope> eventSource = mock(EventSource.class);
        AtomicBoolean isExhausted = new AtomicBoolean(false);
        when(eventSource.isExhausted()).thenAnswer(invocation -> isExhausted.get());

        // When
        try (DistributionLifecycleTracker tracker = DistributionLifecycleTracker.builder()
                                                                                .eventSource(eventSource)
                                                                                .stateStore(mock(
                                                                                        DistributionLifecycleStateStore.class))
                                                                                .listeners(
                                                                                        List.of(new LoggingListener()))
                                                                                .listenerThreads(1)
                                                                                .trackerCheckInterval(
                                                                                        Duration.ofMillis(100))
                                                                                .application("test")
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
        }
    }
}
