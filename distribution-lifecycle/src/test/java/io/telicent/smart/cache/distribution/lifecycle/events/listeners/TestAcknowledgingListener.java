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
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.store.apps.AppDistributionLifecycleStoreFile;
import io.telicent.smart.cache.distribution.lifecycle.tracker.TemporarilyFails;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.sources.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.telicent.smart.cache.distribution.lifecycle.Util.action;
import static org.mockito.Mockito.mock;

public class TestAcknowledgingListener {

    public static final String APP_ID = "test";

    private static final class Ok implements DistributionLifecycleListener {

        @Override
        public void accept(LifecycleAction action) {
            // No-op
        }
    }

    private static final class Fails implements DistributionLifecycleListener {

        @Override
        public void accept(LifecycleAction action) {
            throw new RuntimeException("fails");
        }
    }

    private static final class Infinite implements DistributionLifecycleListener {

        @Override
        public void accept(LifecycleAction action) {
            try {
                Thread.sleep(Duration.ofHours(100));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void verifyAcks(CollectorSink<Event<UUID, LazyEnvelope>> collector, ApplicationState... ackSequence) {
        List<Event<UUID, LazyEnvelope>> events = collector.get();
        Assert.assertEquals(events.size(), ackSequence.length);

        for (ApplicationState expected : ackSequence) {
            Assert.assertFalse(events.isEmpty());

            Event<UUID, LazyEnvelope> event = events.removeFirst();
            Assert.assertNotNull(event);

            Envelope envelope = event.value().getValue();
            Assert.assertNotNull(envelope);
            Assert.assertEquals(envelope.getMetadata().getDocumentFormat(), LifecycleAcknowledgement.DOCUMENT_FORMAT);

            LifecycleAcknowledgement ack = envelope.getBodyAs(LifecycleAcknowledgement.class);
            Assert.assertNotNull(ack);
            Assert.assertEquals(ack.getState().getApp(), expected);
        }
    }

    @Test
    public void givenAckingListenerAndInnerListenerSucceeds_whenAcceptingActions_thenAckedAsCompleted() {
        // Given
        try (CollectorSink<Event<UUID, LazyEnvelope>> collector = CollectorSink.of()) {
            AcknowledgingListener listener = AcknowledgingListener.builder()
                                                                  .listener(new Ok())
                                                                  .sink(collector)
                                                                  .stateStore(mock(DistributionLifecycleStateStore.class))
                                                                  .application(APP_ID)
                                                                  .version("1.2.3")
                                                                  .build();

            // When
            listener.accept(action(UUID.randomUUID(), "distro", DistributionLifecycleState.Registered,
                                   DistributionLifecycleState.Active));

            // Then
            verifyAcks(collector, ApplicationState.Requested, ApplicationState.InProgress, ApplicationState.Completed);
        }
    }

    @Test
    public void givenAckingListenerAndInnerListenerFails_whenAcceptingActions_thenAckedAsFailed() {
        // Given
        try (CollectorSink<Event<UUID, LazyEnvelope>> collector = CollectorSink.of()) {
            AcknowledgingListener listener = AcknowledgingListener.builder()
                                                                  .listener(new Fails())
                                                                  .sink(collector)
                                                                  .stateStore(mock(DistributionLifecycleStateStore.class))
                                                                  .application(APP_ID)
                                                                  .version("1.2.3")
                                                                  .build();

            // When
            Assert.assertThrows(RuntimeException.class, () -> listener.accept(
                    action(UUID.randomUUID(), "distro", DistributionLifecycleState.Registered,
                           DistributionLifecycleState.Active)));

            // Then
            verifyAcks(collector, ApplicationState.Requested, ApplicationState.InProgress, ApplicationState.Failed);
        }
    }

    @Test
    public void givenAckingListenerAndInnerListenerNeverCompletes_whenAcceptingActions_thenAckedAsInProgress() throws
            InterruptedException {
        // Given
        try (CollectorSink<Event<UUID, LazyEnvelope>> collector = CollectorSink.of()) {
            AcknowledgingListener listener = AcknowledgingListener.builder()
                                                                  .listener(new Infinite())
                                                                  .sink(collector)
                                                                  .stateStore(mock(DistributionLifecycleStateStore.class))
                                                                  .application(APP_ID)
                                                                  .version("1.2.3")
                                                                  .build();

            // When
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                executor.submit(() -> listener.accept(
                        action(UUID.randomUUID(), "distro", DistributionLifecycleState.Registered,
                               DistributionLifecycleState.Active)));

                // Then
                Thread.sleep(250);
                verifyAcks(collector, ApplicationState.Requested, ApplicationState.InProgress);
            } finally {
                executor.shutdownNow();
            }
        }
    }

    @Test
    public void givenAckingListenerAndInnerListenerTemporarilyFails_whenAcceptingActionTwice_thenAckedAsFailedThenCompleted() throws
            IOException {
        // Given
        LifecycleAction action = action(UUID.randomUUID(), "distro", DistributionLifecycleState.Registered,
                                        DistributionLifecycleState.Active);
        File stateFile = Files.createTempFile("state", ".json").toFile();
        stateFile.delete();
        try (DistributionLifecycleStateStore stateStore = AppDistributionLifecycleStoreFile.builder()
                                                                                      .app(APP_ID)
                                                                                      .stateFile(stateFile)
                                                                                      .build()) {
            stateStore.add(action);
            try (CollectorSink<Event<UUID, LazyEnvelope>> collector = CollectorSink.of()) {
                AcknowledgingListener listener = AcknowledgingListener.builder()
                                                                      .listener(new TemporarilyFails(1))
                                                                      .sink(e -> {
                                                            LifecycleAcknowledgement ack =
                                                                    e.value().getValue().getBodyAs(
                                                                            LifecycleAcknowledgement.class);
                                                            stateStore.add(APP_ID, ack);
                                                            collector.send(e);
                                                        })
                                                                      .stateStore(stateStore)
                                                                      .application(APP_ID)
                                                                      .version("1.2.3")
                                                                      .build();

                // When
                Assert.assertThrows(RuntimeException.class, () -> listener.accept(action));
                listener.accept(action);

                // Then
                verifyAcks(collector, ApplicationState.Requested, ApplicationState.InProgress, ApplicationState.Failed,
                           ApplicationState.InProgress, ApplicationState.Completed);
            }
        }
    }
}
