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
package io.telicent.smart.cache.distribution.lifecycle.events.listeners;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.Util;
import io.telicent.smart.cache.distribution.lifecycle.events.IngestStatus;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.DistributionOffsets;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.PartitionOffsets;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.store.global.GlobalDistributionLifecycleStoreMemory;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.telicent.smart.cache.distribution.lifecycle.Util.ack;
import static io.telicent.smart.cache.distribution.lifecycle.Util.action;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestDistributionLifecycleStateStoreSink {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*cannot be negative")
    public void givenNegativeFlushFrequency_whenCreatingSink_thenIllegalArgument() {
        // Given
        Duration flushFrequency = Duration.ofSeconds(-1);

        // When and Then
        DistributionLifecycleStateStoreSink.builder()
                                           .flushFrequency(flushFrequency)
                                           .stateStore(mock(DistributionLifecycleStateStore.class))
                                           .executor(mock(ExecutorService.class))
                                           .build();
    }

    @Test(expectedExceptions = SinkException.class)
    public void givenStateStoreSink_whenEventHasBadValue_thenErrors() {
        // Given
        DistributionLifecycleStateStore store = Mockito.mock(DistributionLifecycleStateStore.class);
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(
                                                                                                   Executors.newSingleThreadExecutor())
                                                                                           .stateStore(store)
                                                                                           .build()) {
            // When and Then
            sink.send(new SimpleEvent<>(Collections.emptyList(), UUID.randomUUID(), LazyEnvelope.of(new byte[50])));
        }
    }

    @Test(expectedExceptions = SinkException.class, expectedExceptionsMessageRegExp = ".*unknown/v1.*")
    public void givenStateStoreSink_whenEventHasUnknownPayload_thenErrors() {
        // Given
        DistributionLifecycleStateStore store = Mockito.mock(DistributionLifecycleStateStore.class);
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(
                                                                                                   Executors.newSingleThreadExecutor())
                                                                                           .stateStore(store)
                                                                                           .build()) {
            // When and Then
            sink.send(Util.event("unknown/v1", Map.of()));
        }
    }

    @Test
    public void givenStateStoreSink_whenEventHasNullValue_thenNoOp() {
        // Given
        DistributionLifecycleStateStore store = Mockito.mock(DistributionLifecycleStateStore.class);
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(
                                                                                                   Executors.newSingleThreadExecutor())
                                                                                           .stateStore(store)
                                                                                           .build()) {
            // When
            sink.send(new SimpleEvent<>(Collections.emptyList(), UUID.randomUUID(), null));

            // Then
            verifyNoInteractions(store);
        }
    }

    @Test
    public void givenStateStoreSink_whenLifecycleActionEvent_thenStateStoreAdd() {
        // Given
        DistributionLifecycleStateStore store = Mockito.mock(DistributionLifecycleStateStore.class);
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(
                                                                                                   Executors.newSingleThreadExecutor())
                                                                                           .stateStore(store)
                                                                                           .build()) {
            // When
            sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                                 action(UUID.randomUUID(), "distro", DistributionLifecycleState.Active,
                                   DistributionLifecycleState.Withdrawn)));

            // Then
            verify(store, times(1)).add(any());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void givenStateStoreSink_whenLifecycleAckEvent_thenStateStoreAdd_andFlushedOnClose() {
        // Given
        DistributionLifecycleStateStore store = mock(DistributionLifecycleStateStore.class);
        EventSource<UUID, LazyEnvelope> source = mock(EventSource.class);
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(
                                                                                                   Executors.newSingleThreadExecutor())
                                                                                           .stateStore(store)
                                                                                           .build()) {
            // When
            sink.send(Util.event(LifecycleAcknowledgement.DOCUMENT_FORMAT,
                                 ack(UUID.randomUUID(), "distro", ApplicationState.Requested), source));

            // Then
            verify(store, times(1)).add(anyString(), any());
        }

        // And
        verify(store, times(1)).flush();
    }

    @Test
    public void givenStateStoreSink_whenIngestStatusEvent_thenStateStoreUntouched() {
        // Given
        DistributionLifecycleStateStore store = Mockito.mock(DistributionLifecycleStateStore.class);
        PartitionOffsets partitionOffsets = new PartitionOffsets();
        partitionOffsets.setOffset("test-0", 2468L);
        DistributionOffsets offsets = new DistributionOffsets();
        offsets.setOffsets("distro", partitionOffsets);
        IngestStatus status = IngestStatus.builder()
                    .offsets(offsets)
                    .build();
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(
                                                                                                   Executors.newSingleThreadExecutor())
                                                                                           .stateStore(store)
                                                                                           .build()) {
            // When
            sink.send(Util.event(IngestStatus.DOCUMENT_FORMAT, status));

            // Then
            verifyNoInteractions(store);
        }
    }

    @Test
    public void givenStateStoreSinkAndZeroFlushFrequency_whenLifecycleActionEvent_thenStateStoreAdd_andStateStoreFlushed() {
        // Given
        DistributionLifecycleStateStore store = Mockito.mock(DistributionLifecycleStateStore.class);
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(
                                                                                                   Executors.newSingleThreadExecutor())
                                                                                           .stateStore(store)
                                                                                           .flushFrequency(
                                                                                                   Duration.ZERO)
                                                                                           .build()) {
            // When
            sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                                 action(UUID.randomUUID(), "distro", DistributionLifecycleState.Active,
                                   DistributionLifecycleState.Withdrawn)));

            // Then
            verify(store, times(1)).add(any());

            // And
            verify(store, times(1)).flush();
        }
    }

    @Test
    public void givenStateStoreSink_whenLifecycleActionEvents_thenStateStoreKeptUpToDate_andTriggersListeners() throws
            InterruptedException {
        // Given
        DistributionLifecycleStateStore store = new GlobalDistributionLifecycleStoreMemory();
        DistributionLifecycleListener listener = mock(DistributionLifecycleListener.class);
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(
                                                                                                   Executors.newSingleThreadExecutor())
                                                                                           .stateStore(store)
                                                                                           .listeners(List.of(listener))
                                                                                           .build()) {
            // When
            sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                                 action(UUID.randomUUID(), "distro", DistributionLifecycleState.Unregistered,
                                   DistributionLifecycleState.Registered)));
            sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                                 action(UUID.randomUUID(), "distro", DistributionLifecycleState.Unregistered,
                                   DistributionLifecycleState.Registered)));
            sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                                 action(UUID.randomUUID(), "distro", DistributionLifecycleState.Registered,
                                   DistributionLifecycleState.Active)));

            // Then
            Assert.assertEquals(store.getLifecycleState("distro"), DistributionLifecycleState.Active);
            Assert.assertFalse(store.activeEvents().isEmpty());

            // And
            // NB - Because listeners are fired on a background thread pool wait briefly to allow them time to execute
            Thread.sleep(250);
            verify(listener, times(3)).accept(any());
        }
    }

    @Test
    public void givenStateStoreSinkWithShutdownExecutor_whenLifecycleActionEvent_thenNoListenersTriggered() throws
            InterruptedException {
        // Given
        DistributionLifecycleStateStore store = new GlobalDistributionLifecycleStoreMemory();
        DistributionLifecycleListener listener = mock(DistributionLifecycleListener.class);
        ExecutorService executor = mock(ExecutorService.class);
        when(executor.isShutdown()).thenReturn(true);
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(executor)
                                                                                           .stateStore(store)
                                                                                           .listeners(List.of(listener))
                                                                                           .build()) {
            // When
            sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                                 action(UUID.randomUUID(), "distro", DistributionLifecycleState.Unregistered,
                                   DistributionLifecycleState.Registered)));

            // Then
            Assert.assertEquals(store.getLifecycleState("distro"), DistributionLifecycleState.Registered);
            Assert.assertFalse(store.activeEvents().isEmpty());
            Thread.sleep(250);
            verifyNoInteractions(listener);
            verify(executor, never()).submit(any(Runnable.class));
        }
    }

    @Test
    public void givenStateStoreSinkWithFailingExecutor_whenLifecycleActionEvent_thenErrorSuppressed() throws
            InterruptedException {
        // Given
        DistributionLifecycleStateStore store = new GlobalDistributionLifecycleStoreMemory();
        DistributionLifecycleListener listener = mock(DistributionLifecycleListener.class);
        ExecutorService executor = mock(ExecutorService.class);
        when(executor.submit(any(Runnable.class))).thenThrow(new RuntimeException("Unable to execute"));
        try (DistributionLifecycleStateStoreSink sink = DistributionLifecycleStateStoreSink.builder()
                                                                                           .executor(executor)
                                                                                           .stateStore(store)
                                                                                           .listeners(List.of(listener))
                                                                                           .build()) {
            // When
            sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                                 action(UUID.randomUUID(), "distro", DistributionLifecycleState.Unregistered,
                                   DistributionLifecycleState.Registered)));

            // Then
            Assert.assertEquals(store.getLifecycleState("distro"), DistributionLifecycleState.Registered);
            Assert.assertFalse(store.activeEvents().isEmpty());
            verify(executor, times(1)).submit(any(Runnable.class));
            verifyNoInteractions(listener);
        }
    }
}
