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
package io.telicent.smart.cache.distribution.lifecycle.sinks;

import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.store.global.GlobalDistributionLifecycleStoreMemory;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDistributionAcceptanceSink {

    protected static final String DISTRO_1 = "https://example.org/distros/1";

    @Test
    public void givenSink_whenSendingEventsWithNoDistributionId_thenAlwaysRejected() {
        // Given
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                              .stateStore(
                                                                                                      mock(DistributionLifecycleStateStore.class))
                                                                                              .build()) {
                // When and Then
                Event<Integer, String> event = new SimpleEvent<>(Collections.emptyList(), 1, "test");
                for (int i = 1; i <= 100; i++) {
                    Assert.assertThrows(SinkException.class, () -> sink.send(event));
                }
                Assert.assertTrue(collector.get().isEmpty(), "Events without Distribution IDs must never be forwarded");
            }
        }
    }

    @Test
    public void givenSinkWithEmptyStateStore_whenSendingEventsForUnregisteredDistribution_thenFirstRejected_andSubsequentDiscarded() {
        // Given
        DistributionLifecycleStateStore stateStore = new GlobalDistributionLifecycleStoreMemory();
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                              .stateStore(stateStore)
                                                                                              .rejectUnregistered()
                                                                                              .unregisteredGracePeriod(
                                                                                                      Duration.ofMillis(
                                                                                                              100),
                                                                                                      Duration.ofMillis(
                                                                                                              10))
                                                                                              .destination(collector)
                                                                                              .build()) {
                // When and Then
                SimpleEvent<Integer, String> event =
                        new SimpleEvent<>(List.of(new Header(TelicentHeaders.DISTRIBUTION_ID, DISTRO_1)), 1, "test");
                Assert.assertThrows(SinkException.class, () -> sink.send(event));
                Assert.assertTrue(collector.get().isEmpty(), "Event must not be forwarded");

                // And
                sink.send(event);
                Assert.assertTrue(collector.get().isEmpty(), "Event must not be forwarded");
            }
        }
    }

    @Test
    public void givenSinkWithStateStoreWhereDistributionIsEventuallyRegistered_whenSendingEventsForUnregisteredDistribution_thenAccepted() {
        // Given
        DistributionLifecycleStateStore stateStore = mock(DistributionLifecycleStateStore.class);
        when(stateStore.getLifecycleState(DISTRO_1)).thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Registered);
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                              .stateStore(stateStore)
                                                                                              .rejectUnregistered()
                                                                                              .unregisteredGracePeriod(
                                                                                                      Duration.ofMillis(
                                                                                                              100),
                                                                                                      Duration.ofMillis(
                                                                                                              10))
                                                                                              .destination(collector)
                                                                                              .build()) {
                // When and Then
                SimpleEvent<Integer, String> event =
                        new SimpleEvent<>(List.of(new Header(TelicentHeaders.DISTRIBUTION_ID, DISTRO_1)), 1, "test");
                sink.send(event);
                Assert.assertFalse(collector.get().isEmpty(),
                                   "Event must be forwarded when distribution registered during grace period");
            }
        }
    }

    @Test
    public void givenSinkWithStateStoreWhereDistributionIsEventuallyRegisteredAfterGracePeriod_whenSendingEventsForUnregisteredDistribution_thenFirstEventRejected_andSubsequentEventsAccepted() {
        // Given
        DistributionLifecycleStateStore stateStore = mock(DistributionLifecycleStateStore.class);
        when(stateStore.getLifecycleState(DISTRO_1)).thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Unregistered)
                                                    .thenReturn(DistributionLifecycleState.Registered);
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                              .stateStore(stateStore)
                                                                                              .rejectUnregistered()
                                                                                              .unregisteredGracePeriod(
                                                                                                      Duration.ofMillis(
                                                                                                              100),
                                                                                                      Duration.ofMillis(
                                                                                                              10))
                                                                                              .destination(collector)
                                                                                              .build()) {
                // When and Then
                SimpleEvent<Integer, String> event =
                        new SimpleEvent<>(List.of(new Header(TelicentHeaders.DISTRIBUTION_ID, DISTRO_1)), 1, "test");
                Assert.assertThrows(SinkException.class, () -> sink.send(event));
                Assert.assertTrue(collector.get().isEmpty(), "First event must not be forwarded");

                // And
                sink.send(event);
                Assert.assertFalse(collector.get().isEmpty(),
                                   "Event must be forwarded when distribution is later registered even if events for unregistered distribution previously rejected");
            }
        }
    }

    @Test
    public void givenSinkWithStateStoreWhereDistributionIsDeleted_whenSendingEvents_thenFirstEventRejected_andSubsequentEventsDiscarded() {
        // Given
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            DistributionLifecycleStateStore stateStore = mock(DistributionLifecycleStateStore.class);
            when(stateStore.getLifecycleState(DISTRO_1)).thenReturn(DistributionLifecycleState.Deleted);
            try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                              .stateStore(
                                                                                                      stateStore)
                                                                                              .build()) {
                // When
                Event<Integer, String> event =
                        new SimpleEvent<>(List.of(new Header(TelicentHeaders.DISTRIBUTION_ID, DISTRO_1)), 1, "test");
                for (int i = 1; i <= 100; i++) {
                    if (i == 1) {
                        // Then
                        Assert.assertThrows(SinkException.class, () -> sink.send(event));
                    } else {
                        // And
                        // Subsequent events should be discarded so no error expected
                        sink.send(event);
                    }
                }
                Assert.assertTrue(collector.get().isEmpty(),
                                  "Events for deleted Distributions must never be forwarded");
            }
        }
    }

    @Test
    public void givenSinkWithStateStoreWhereDistributionIsDeletedAndAlwaysReject_whenSendingEvents_thenAlwaysRejected() {
        // Given
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            DistributionLifecycleStateStore stateStore = mock(DistributionLifecycleStateStore.class);
            when(stateStore.getLifecycleState(DISTRO_1)).thenReturn(DistributionLifecycleState.Deleted);
            try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                              .stateStore(
                                                                                                      stateStore)
                                                                                              .rejectAfterRejection()
                                                                                              .build()) {
                // When
                Event<Integer, String> event =
                        new SimpleEvent<>(List.of(new Header(TelicentHeaders.DISTRIBUTION_ID, DISTRO_1)), 1, "test");
                for (int i = 1; i <= 100; i++) {
                    // Then
                    Assert.assertThrows(SinkException.class, () -> sink.send(event));
                }
                Assert.assertTrue(collector.get().isEmpty(),
                                  "Events for deleted Distributions must never be forwarded");
            }
        }
    }

    @Test
    public void givenSinkConfiguredToAcceptUnregistered_whenSendingEventsForUnregisteredDistribution_thenForwarded() {
        // Given
        DistributionLifecycleStateStore stateStore = new GlobalDistributionLifecycleStoreMemory();
        try (CollectorSink<Event<Integer, String>> collector = CollectorSink.of()) {
            try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                              .stateStore(stateStore)
                                                                                              .acceptUnregistered()
                                                                                              .unregisteredGracePeriod(
                                                                                                      Duration.ofMillis(
                                                                                                              100),
                                                                                                      Duration.ofMillis(
                                                                                                              10))
                                                                                              .destination(collector)
                                                                                              .build()) {
                // When
                SimpleEvent<Integer, String> event =
                        new SimpleEvent<>(List.of(new Header(TelicentHeaders.DISTRIBUTION_ID, DISTRO_1)), 1, "test");
                sink.send(event);

                // Then
                Assert.assertFalse(collector.get().isEmpty(),
                                   "Event should be forwarded when configured to accept unregistered");
                Assert.assertEquals(collector.get().getFirst(), event);
            }
        }
    }
}
