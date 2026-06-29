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
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

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
        }
    }
}
