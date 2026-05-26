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

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.TelicentHeaders;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.telicent.smart.cache.distribution.lifecycle.Util.action;
import static io.telicent.smart.cache.distribution.lifecycle.Util.event;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDistributionLifecycleProjector {

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Failed")
    public void givenProjectorWithoutDlq_whenSinkFails_thenThrowsUpwards() {
        // Given
        DistributionLifecycleStateStore store = mock(DistributionLifecycleStateStore.class);
        Sink<Event<UUID, LazyEnvelope>> sink = event -> {throw new RuntimeException("Failed");};
        DistributionLifecycleProjector projector =
                DistributionLifecycleProjector.builder().store(store).application("test").build();

        // When and Then
        projector.project(event(LifecycleAction.DOCUMENT_FORMAT,
                                action(UUID.randomUUID(), "distro", DistributionLifecycleState.Active,
                                       DistributionLifecycleState.Deleted)), sink);
    }

    @Test
    public void givenProjectorWithDlq_whenSinkFails_thenEventGoesToDlq() {
        // Given
        DistributionLifecycleStateStore store = mock(DistributionLifecycleStateStore.class);
        String errorMessage = "Unable to process";
        Sink<Event<UUID, LazyEnvelope>> sink = event -> {throw new RuntimeException(errorMessage);};
        try (CollectorSink<Event<UUID, LazyEnvelope>> dlq = CollectorSink.of()) {
            DistributionLifecycleProjector projector =
                    DistributionLifecycleProjector.builder().store(store).application("test").dlq(dlq).build();

            // When
            projector.project(event(LifecycleAction.DOCUMENT_FORMAT,
                                    action(UUID.randomUUID(), "distro", DistributionLifecycleState.Active,
                                           DistributionLifecycleState.Deleted)), sink);

            // Then
            Assert.assertEquals(dlq.get().size(), 1);
            Event<UUID, LazyEnvelope> event = dlq.get().getFirst();
            Assert.assertEquals(event.lastHeader(TelicentHeaders.DEAD_LETTER_REASON), errorMessage);
        }
    }

    @Test
    public void givenProjectorWithFailingDlq_whenSinkFails_thenErrorSuppressed() {
        // Given
        DistributionLifecycleStateStore store = mock(DistributionLifecycleStateStore.class);
        String errorMessage = "Unable to process";
        Sink<Event<UUID, LazyEnvelope>> sink = event -> {throw new RuntimeException(errorMessage);};
        DistributionLifecycleProjector projector =
                DistributionLifecycleProjector.builder().store(store).application("test").dlq(sink).build();

        // When and Then
        projector.project(event(LifecycleAction.DOCUMENT_FORMAT,
                                action(UUID.randomUUID(), "distro", DistributionLifecycleState.Active,
                                       DistributionLifecycleState.Deleted)), sink);

    }

    @Test
    public void givenProjector_whenSinkOk_thenEventGoesToSink() {
        // Given
        DistributionLifecycleStateStore store = mock(DistributionLifecycleStateStore.class);
        try (CollectorSink<Event<UUID, LazyEnvelope>> sink = CollectorSink.of()) {
            DistributionLifecycleProjector projector =
                    DistributionLifecycleProjector.builder().store(store).application("test").build();

            // When
            projector.project(event(LifecycleAction.DOCUMENT_FORMAT,
                                    action(UUID.randomUUID(), "distro", DistributionLifecycleState.Active,
                                           DistributionLifecycleState.Deleted)), sink);

            // Then
            Assert.assertEquals(sink.get().size(), 1);
        }
    }

    @Test
    public void givenProjector_whenStalled_thenFailedEventsAreRetriggered() {
        // Given
        LifecycleAction action = action(UUID.randomUUID(), "distro", DistributionLifecycleState.Active,
                                        DistributionLifecycleState.Deleted);
        DistributionLifecycleStateStore store = mock(DistributionLifecycleStateStore.class);
        when(store.activeEvents()).thenReturn(List.of(action));
        when(store.getApplicationState(any(), any())).thenReturn(ApplicationState.Failed);

        try (CollectorSink<Event<UUID, LazyEnvelope>> sink = CollectorSink.of()) {
            DistributionLifecycleProjector projector =
                    DistributionLifecycleProjector.builder().store(store).application("test").build();

            // When
            projector.project(event(LifecycleAction.DOCUMENT_FORMAT, action), sink);
            projector.stalled(sink);

            // Then
            Assert.assertEquals(sink.get().size(), 2);
        }
    }

    @Test
    public void givenProjector_whenStalledWithSomeCompletedActiveEvents_thenNoEventsAreRetriggered() {
        // Given
        LifecycleAction action = action(UUID.randomUUID(), "distro", DistributionLifecycleState.Active,
                                        DistributionLifecycleState.Deleted);
        DistributionLifecycleStateStore store = mock(DistributionLifecycleStateStore.class);
        when(store.activeEvents()).thenReturn(List.of(action));
        when(store.getApplicationState(any(), any())).thenReturn(ApplicationState.Completed);

        try (CollectorSink<Event<UUID, LazyEnvelope>> sink = CollectorSink.of()) {
            DistributionLifecycleProjector projector =
                    DistributionLifecycleProjector.builder().store(store).application("test").build();

            // When
            projector.project(event(LifecycleAction.DOCUMENT_FORMAT, action), sink);
            projector.stalled(sink);

            // Then
            Assert.assertEquals(sink.get().size(), 1);
        }
    }

    @Test
    public void givenProjector_whenStalledWithNoActiveEvents_thenNoEventsAreRetriggered() {
        // Given
        LifecycleAction action = action(UUID.randomUUID(), "distro", DistributionLifecycleState.Active,
                                        DistributionLifecycleState.Deleted);
        DistributionLifecycleStateStore store = mock(DistributionLifecycleStateStore.class);
        when(store.activeEvents()).thenReturn(Collections.emptyList());

        try (CollectorSink<Event<UUID, LazyEnvelope>> sink = CollectorSink.of()) {
            DistributionLifecycleProjector projector =
                    DistributionLifecycleProjector.builder().store(store).application("test").build();

            // When
            projector.project(event(LifecycleAction.DOCUMENT_FORMAT, action), sink);
            projector.stalled(sink);

            // Then
            Assert.assertEquals(sink.get().size(), 1);
        }
    }
}
