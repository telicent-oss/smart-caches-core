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
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.AckingListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.DistributionLifecycleListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.LoggingListener;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.store.apps.AppDistributionLifecycleStoreFile;
import io.telicent.smart.cache.distribution.lifecycle.store.global.GlobalDistributionLifecycleStoreMemory;
import io.telicent.smart.cache.observability.LibraryVersion;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeDeserializer;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeSerializer;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.awaitility.Awaitility;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.telicent.smart.cache.distribution.lifecycle.Util.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DockerTestDistributionLifecycleTracker {

    public static final String APP_ID = "test";
    public static final String DLQ_TOPIC = "dlq";
    private final KafkaTestCluster kafka = new BasicKafkaTestCluster();
    private final AtomicInteger consumerId = new AtomicInteger(0);
    private File stateFile;

    @BeforeClass
    public void setup() {
        this.kafka.setup();
        this.kafka.createTopic(DLQ_TOPIC);
    }

    @BeforeMethod
    public void setupState() throws IOException {
        this.stateFile = Files.createTempFile("state", ".json").toFile();
        this.stateFile.delete();
    }

    @AfterMethod
    public void cleanup() {
        this.kafka.resetTestTopic();
        this.kafka.resetTopic(DLQ_TOPIC);
    }

    @AfterClass
    public void teardown() {
        this.kafka.teardown();
        this.stateFile.delete();
    }

    /**
     * Creates an event source connected to the Kafka test cluster
     *
     * @return Event source
     */
    private EventSource<UUID, LazyEnvelope> createSource() {
        return createSource(KafkaTestCluster.DEFAULT_TOPIC);
    }

    private EventSource<UUID, LazyEnvelope> createSource(String topic) {
        return KafkaEventSource.<UUID, LazyEnvelope>create()
                               .bootstrapServers(this.kafka.getBootstrapServers())
                               .topic(topic)
                               .consumerGroup("test-" + consumerId.incrementAndGet())
                               .consumerConfig(this.kafka.getClientProperties())
                               .keyDeserializer(UUIDDeserializer.class)
                               .valueDeserializer(LazyEnvelopeDeserializer.class)
                               .fromBeginning()
                               .commitOnProcessed()
                               .build();
    }

    /**
     * Creates a sink connected to the Kafka test cluster
     */
    private Sink<Event<UUID, LazyEnvelope>> createSink() {
        return createSink(KafkaTestCluster.DEFAULT_TOPIC);
    }

    private Sink<Event<UUID, LazyEnvelope>> createSink(String topic) {
        return KafkaSink.<UUID, LazyEnvelope>create()
                        .bootstrapServers(this.kafka.getBootstrapServers())
                        .topic(topic)
                        .producerConfig(this.kafka.getClientProperties())
                        .lingerMs(50)
                        .keySerializer(UUIDSerializer.class)
                        .valueSerializer(LazyEnvelopeSerializer.class)
                        .build();
    }

    /**
     * Creates a state store for the current tests
     *
     * @return State store
     */
    private DistributionLifecycleStateStore createStateStore() {
        return AppDistributionLifecycleStoreFile.builder().app(APP_ID).stateFile(this.stateFile).build();
    }

    private DistributionLifecycleTracker createTracker(DistributionLifecycleStateStore stateStore,
                                                       List<DistributionLifecycleListener> listeners) {
        return DistributionLifecycleTracker.builder()
                                           .eventSource(createSource())
                                           .stateStore(stateStore)
                                           .listeners(listeners)
                                           .listenerThreads(Math.max(listeners.size(), 1))
                                           .application(APP_ID)
                                           .dlq(createSink(DLQ_TOPIC))
                                           .build();
    }

    /**
     * Creates an {@link AckingListener} that will send acknowledgements into the given sink
     *
     * @param sink Sink
     * @return Ack'ing listener
     */
    private AckingListener createAckListener(Sink<Event<UUID, LazyEnvelope>> sink,
                                             DistributionLifecycleStateStore stateStore) {
        return createAckListener(sink, stateStore, new LoggingListener());
    }

    /**
     * Creates an {@link AckingListener} that will send acknowledgements into the given sink
     *
     * @param sink     Sink
     * @param listener Listener to wrap
     * @return Ack'ing listener
     */
    private AckingListener createAckListener(Sink<Event<UUID, LazyEnvelope>> sink,
                                             DistributionLifecycleStateStore stateStore,
                                             DistributionLifecycleListener listener) {
        return AckingListener.builder()
                             .application(APP_ID)
                             .version(LibraryVersion.get("distribution-lifecycle"))
                             .listener(listener)
                             .sink(sink)
                             .stateStore(stateStore)
                             .build();
    }

    private <T> void awaitEquals(String alias, Supplier<T> supplier, T expected) {
        if (expected != null) {
            Awaitility.await(alias)
                      .pollInterval(Duration.ofMillis(250))
                      .atMost(Duration.ofSeconds(10))
                      .until(supplier::get, t -> Objects.equals(t, expected));
            Assert.assertEquals(supplier.get(), expected, "Failed check " + alias);
        } else {
            // NB - If the expected value is null then have to use a different form of until() as the form used above
            //      fails the check if the supplier returns a null value
            Awaitility.await(alias)
                      .pollInterval(Duration.ofSeconds(1))
                      .atMost(Duration.ofSeconds(10))
                      .until(() -> supplier.get() == null);
            Assert.assertNull(supplier.get(), "Failed check " + alias);
        }
    }

    private void verifyDistributionState(String distributionID, DistributionLifecycleStateStore stateStore,
                                         DistributionLifecycleState expected) {
        awaitEquals("Distribution " + distributionID + " State as expected",
                    () -> stateStore.getLifecycleState(distributionID), expected);
    }

    private void verifyApplicationState(DistributionLifecycleStateStore stateStore, UUID eventId, String appId,
                                        ApplicationState expected) {
        awaitEquals("Event " + eventId + " has correct state for application " + appId,
                    () -> stateStore.getApplicationState(eventId, appId), expected);
    }

    private UUID sendDistributionEvent(Sink<Event<UUID, LazyEnvelope>> sink, String distributionId,
                                       DistributionLifecycleState from, DistributionLifecycleState to) {
        UUID eventId = UUID.randomUUID();
        sink.send(event(LifecycleAction.DOCUMENT_FORMAT, action(eventId, distributionId, from, to)));
        return eventId;
    }

    @Test
    public void givenTracker_whenReceivingEventsFromKafka_thenTrackerUpdatesStateStore() {
        // Given
        try (DistributionLifecycleStateStore stateStore = createStateStore()) {
            try (Sink<Event<UUID, LazyEnvelope>> kafkaSink = createSink()) {
                DistributionLifecycleListener ackListener = createAckListener(kafkaSink, stateStore);
                try (DistributionLifecycleTracker tracker = createTracker(stateStore, List.of(ackListener))) {
                    // When
                    UUID registeredEvent =
                            sendDistributionEvent(kafkaSink, "distro", DistributionLifecycleState.Unregistered,
                                                  DistributionLifecycleState.Registered);
                    UUID activatedEvent =
                            sendDistributionEvent(kafkaSink, "distro", DistributionLifecycleState.Registered,
                                                  DistributionLifecycleState.Active);
                    Assert.assertTrue(tracker.isRunning());

                    // Then
                    verifyDistributionState("distro", stateStore, DistributionLifecycleState.Active);
                    verifyDistributionState("other", stateStore, DistributionLifecycleState.Unregistered);
                    verifyApplicationState(stateStore, registeredEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, activatedEvent, APP_ID, ApplicationState.Completed);
                    Assert.assertTrue(stateStore.activeEvents().isEmpty());
                }
            }
        }
    }

    @Test
    public void givenTracker_whenReceivingEventsForMultipleDistributionsFromKafka_thenTrackerUpdatesStateStore() {
        // Given
        try (DistributionLifecycleStateStore stateStore = createStateStore()) {
            try (Sink<Event<UUID, LazyEnvelope>> kafkaSink = createSink()) {
                DistributionLifecycleListener ackListener = createAckListener(kafkaSink, stateStore);
                try (DistributionLifecycleTracker tracker = createTracker(stateStore, List.of(ackListener))) {
                    // When
                    UUID regAEvent = sendDistributionEvent(kafkaSink, "A", DistributionLifecycleState.Unregistered,
                                                           DistributionLifecycleState.Registered);
                    UUID regBEvent = sendDistributionEvent(kafkaSink, "B", DistributionLifecycleState.Unregistered,
                                                           DistributionLifecycleState.Registered);
                    UUID activateAEvent = sendDistributionEvent(kafkaSink, "A", DistributionLifecycleState.Registered,
                                                                DistributionLifecycleState.Active);
                    UUID deleteBEvent = sendDistributionEvent(kafkaSink, "B", DistributionLifecycleState.Registered,
                                                              DistributionLifecycleState.Deleted);
                    Assert.assertTrue(tracker.isRunning());

                    // Then
                    verifyDistributionState("A", stateStore, DistributionLifecycleState.Active);
                    verifyDistributionState("B", stateStore, DistributionLifecycleState.Deleted);
                    verifyDistributionState("C", stateStore, DistributionLifecycleState.Unregistered);
                    verifyApplicationState(stateStore, regAEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, activateAEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, regBEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, deleteBEvent, APP_ID, ApplicationState.Completed);
                    Assert.assertTrue(stateStore.activeEvents().isEmpty());
                }
            }
        }
    }

    @Test
    public void givenTrackerWithTemporarilyFailingListener_whenReceivingEventsFromKafka_thenTrackerUpdatesStateStore_andAppEventuallyAckdAsCompleted() {
        // Given
        try (DistributionLifecycleStateStore stateStore = createStateStore()) {
            try (Sink<Event<UUID, LazyEnvelope>> kafkaSink = createSink()) {
                DistributionLifecycleListener ackListener =
                        createAckListener(kafkaSink, stateStore, new TemporarilyFails(2));
                try (DistributionLifecycleTracker tracker = createTracker(stateStore, List.of(ackListener))) {
                    // When
                    UUID registeredEvent =
                            sendDistributionEvent(kafkaSink, "distro", DistributionLifecycleState.Unregistered,
                                                  DistributionLifecycleState.Registered);
                    UUID activatedEvent =
                            sendDistributionEvent(kafkaSink, "distro", DistributionLifecycleState.Registered,
                                                  DistributionLifecycleState.Active);
                    Assert.assertTrue(tracker.isRunning());

                    // Then
                    verifyDistributionState("distro", stateStore, DistributionLifecycleState.Active);
                    verifyDistributionState("other", stateStore, DistributionLifecycleState.Unregistered);
                    Assert.assertTrue(tracker.isRunning());
                    verifyApplicationState(stateStore, registeredEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, activatedEvent, APP_ID, ApplicationState.Completed);
                    Assert.assertTrue(stateStore.activeEvents().isEmpty());
                }
            }
        }
    }

    @Test
    public void givenTracker_whenReceivingBadEventsFromKafka_thenTrackerStillUpdatesStateStoreFromGoodEvents_andBadEventsGoToDlq() {
        // Given
        try (DistributionLifecycleStateStore stateStore = createStateStore()) {
            try (Sink<Event<UUID, LazyEnvelope>> kafkaSink = createSink()) {
                DistributionLifecycleListener ackListener = createAckListener(kafkaSink, stateStore);
                try (DistributionLifecycleTracker tracker = createTracker(stateStore, List.of(ackListener))) {
                    // When
                    UUID regOtherEvent =
                            sendDistributionEvent(kafkaSink, "other", DistributionLifecycleState.Unregistered,
                                                  DistributionLifecycleState.Registered);
                    UUID badEvent = sendDistributionEvent(kafkaSink, "other", DistributionLifecycleState.Registered,
                                                          DistributionLifecycleState.Unregistered);
                    UUID registeredEvent =
                            sendDistributionEvent(kafkaSink, "distro", DistributionLifecycleState.Unregistered,
                                                  DistributionLifecycleState.Registered);
                    UUID activatedEvent =
                            sendDistributionEvent(kafkaSink, "distro", DistributionLifecycleState.Registered,
                                                  DistributionLifecycleState.Active);
                    Assert.assertTrue(tracker.isRunning());

                    // Then
                    verifyDistributionState("distro", stateStore, DistributionLifecycleState.Active);
                    verifyDistributionState("other", stateStore, DistributionLifecycleState.Registered);
                    verifyApplicationState(stateStore, regOtherEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, badEvent, APP_ID, null);
                    verifyApplicationState(stateStore, registeredEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, activatedEvent, APP_ID, ApplicationState.Completed);
                    Assert.assertTrue(stateStore.activeEvents().isEmpty());

                    // And
                    EventSource<UUID, LazyEnvelope> dlqSource = createSource(DLQ_TOPIC);
                    Event<UUID, LazyEnvelope> bad = dlqSource.poll(Duration.ofSeconds(5));
                    Assert.assertNotNull(bad);
                    Assert.assertEquals(bad.value().getValue().getBodyAs(LifecycleAction.class).getEventId(), badEvent);
                }
            }
        }
    }

    private void ackToStateStore(DistributionLifecycleStateStore stateStore, UUID eventId, String appId,
                                 ApplicationState... states) {
        for (ApplicationState state : states) {
            stateStore.add(appId, ack(eventId, "distro", state));
        }
    }

    @Test
    public void givenTracker_whenStateStoreHasActiveEvents_thenEventsRetriggeredOnStartup() {
        // Given
        try (DistributionLifecycleStateStore stateStore = createStateStore()) {
            UUID regEvent = UUID.randomUUID();
            UUID activateEvent = UUID.randomUUID();
            UUID deleteEvent = UUID.randomUUID();
            stateStore.add(action(regEvent, "distro", DistributionLifecycleState.Unregistered,
                                  DistributionLifecycleState.Registered));
            stateStore.add(action(activateEvent, "distro", DistributionLifecycleState.Registered,
                                  DistributionLifecycleState.Active));
            stateStore.add(action(deleteEvent, "distro", DistributionLifecycleState.Active,
                                  DistributionLifecycleState.Deleted));
            ackToStateStore(stateStore, regEvent, APP_ID, ApplicationState.Requested, ApplicationState.InProgress,
                            ApplicationState.Completed);
            ackToStateStore(stateStore, activateEvent, APP_ID, ApplicationState.Requested, ApplicationState.InProgress);
            verifyApplicationState(stateStore, regEvent, APP_ID, ApplicationState.Completed);
            verifyApplicationState(stateStore, activateEvent, APP_ID, ApplicationState.InProgress);
            verifyApplicationState(stateStore, deleteEvent, APP_ID, null);
            Assert.assertFalse(stateStore.activeEvents().isEmpty());
            DistributionLifecycleListener listener = Mockito.mock(DistributionLifecycleListener.class);

            try (Sink<Event<UUID, LazyEnvelope>> kafkaSink = createSink()) {
                DistributionLifecycleListener ackListener = createAckListener(kafkaSink, stateStore, listener);
                try (DistributionLifecycleTracker tracker = createTracker(stateStore, List.of(ackListener))) {
                    // When
                    Assert.assertTrue(tracker.isRunning());

                    // Then
                    verifyDistributionState("distro", stateStore, DistributionLifecycleState.Deleted);
                    verifyApplicationState(stateStore, regEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, activateEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, deleteEvent, APP_ID, ApplicationState.Completed);
                    Assert.assertTrue(stateStore.activeEvents().isEmpty());
                    verify(listener, times(2)).accept(any());
                }
            }
        }
    }

    @Test
    public void givenTracker_whenStateStoreHasActiveEventsForOtherApps_thenEventsNotRetriggeredOnStartup() {
        // Given
        try (DistributionLifecycleStateStore stateStore = new GlobalDistributionLifecycleStoreMemory()) {
            UUID regEvent = UUID.randomUUID();
            stateStore.add(action(regEvent, "distro", DistributionLifecycleState.Unregistered,
                                  DistributionLifecycleState.Registered));
            ackToStateStore(stateStore, regEvent, APP_ID, ApplicationState.Requested, ApplicationState.InProgress,
                            ApplicationState.Completed);
            String otherAppId = "other-app";
            ackToStateStore(stateStore, regEvent, otherAppId, ApplicationState.Requested, ApplicationState.InProgress);
            verifyApplicationState(stateStore, regEvent, APP_ID, ApplicationState.Completed);
            verifyApplicationState(stateStore, regEvent, otherAppId, ApplicationState.InProgress);
            Assert.assertFalse(stateStore.activeEvents().isEmpty());
            DistributionLifecycleListener listener = Mockito.mock(DistributionLifecycleListener.class);

            try (Sink<Event<UUID, LazyEnvelope>> kafkaSink = createSink()) {
                DistributionLifecycleListener ackListener = createAckListener(kafkaSink, stateStore, listener);
                try (DistributionLifecycleTracker tracker = createTracker(stateStore, List.of(ackListener))) {
                    // When
                    Assert.assertTrue(tracker.isRunning());

                    // Then
                    verifyDistributionState("distro", stateStore, DistributionLifecycleState.Registered);
                    verifyApplicationState(stateStore, regEvent, APP_ID, ApplicationState.Completed);
                    verifyApplicationState(stateStore, regEvent, otherAppId, ApplicationState.InProgress);
                    Assert.assertFalse(stateStore.activeEvents().isEmpty());
                    verify(listener, never()).accept(any());
                }
            }
        }
    }

}
