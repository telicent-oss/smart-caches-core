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
package io.telicent.smart.cache.projectors.sinks;

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.Strings.CS;

public class TestCircuitBreakerSink {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*at least 1")
    public void givenBadQueueSize_whenCreatingCircuitBreakerSink_thenIllegalArgument() {
        // Given, When and Then
        try(CircuitBreakerSink<Object> ignored = CircuitBreakerSink.create().queueSize(-10).build()) {
            // to avoid compiler warning
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullInitialState_whenCreatingCircuitBreakerSink_thenNPE() {
        // Given, When and Then
        try(CircuitBreakerSink<Object> ignored = new CircuitBreakerSink<>(null, null, 10, true)) {
            // to avoid compiler warning
        }
    }

    private void sendItems(Sink<Integer> sink, int count) {
        sendItems(sink, 0, count);
    }

    private void sendItems(Sink<Integer> sink, int start, int count) {
        for (int i = 1; i <= count; i++) {
            sink.send(start + i);
        }
    }

    private void verifyEvents(CollectorSink<Integer> sink, int expected) {
        Assert.assertEquals(sink.get().size(), expected);
        for (int i = 1; i <= expected; i++) {
            Assert.assertEquals(sink.get().get(i - 1), i);
        }
    }

    @Test
    public void givenCircuitBreakerInClosedState_whenSendingItems_thenSent() {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                      .queueSize(10)
                                                                      .closed()
                                                                      .destination(collector)
                                                                      .build()) {
                // When
                sendItems(sink, 100);

                // Then
                verifyEvents(collector, 100);
            }
        }
    }

    @Test(expectedExceptions = SinkException.class, expectedExceptionsMessageRegExp = ".*100 queued items.*")
    public void givenCircuitBreakerInOpenState_whenSendingItems_thenNothingSent() {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                      .queueSize(1000)
                                                                      .opened()
                                                                      .destination(collector)
                                                                      .build()) {
                // When
                sendItems(sink, 100);

                // Then
                verifyEvents(collector, 0);
            }
        }
    }

    @Test
    public void givenCircuitBreakerInOpenState_whenSendingItems_thenNothingSent_andSentOnceClosed() {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                      .queueSize(1000)
                                                                      .opened()
                                                                      .destination(collector)
                                                                      .build()) {
                // When
                sendItems(sink, 100);

                // Then
                verifyEvents(collector, 0);

                // And
                sink.setState(CircuitBreakerSink.State.CLOSED);
                verifyEvents(collector, 100);
            }
        }
    }

    @Test(expectedExceptions = SinkException.class, expectedExceptionsMessageRegExp = ".*10 queued items.*")
    public void givenCircuitBreakerInOpenState_whenSendingMoreItemsThanQueue_thenBlocked_andNothingSent() throws
            ExecutionException, InterruptedException {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                      .queueSize(10)
                                                                      .opened()
                                                                      .destination(collector)
                                                                      .build()) {
                // When
                AtomicInteger sent = new AtomicInteger();
                Thread thread = new Thread(() -> {
                    for (int i = 1; i <= 100; i++) {
                        sink.send(i);
                        sent.incrementAndGet();
                    }
                });
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<?> future = executor.submit(thread);

                // Then
                try {
                    future.get(3, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    // Expected, the thread should be running and blocked
                }
                Assert.assertEquals(sent.get(), 10);

                // And
                verifyEvents(collector, 0);
                executor.shutdownNow();
            }
        }
    }

    @Test
    public void givenCircuitBreakerInOpenState_whenSendingItemsAndClosing_thenQueuedItemsReceivedBeforeSubsequentItems() throws
            InterruptedException {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                      .queueSize(100)
                                                                      .opened()
                                                                      .destination(collector)
                                                                      .build()) {
                // When
                Semaphore semaphore = new Semaphore(1);
                Thread thread = new Thread(() -> {
                    // Send 100 events on a background thread, closing the circuit breaker once they are sent
                    try {
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendItems(sink, 100);
                    semaphore.release();
                    sink.setState(CircuitBreakerSink.State.CLOSED);
                });
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(thread);
                Thread.sleep(100);

                // Meanwhile once the initial 100 events are sent by the background thread the main thread sends another
                // 100 items, these should wait for the items queued by the background thread to be forwarded before
                // they are forwarding ensuring items remain in their original order
                semaphore.acquire();
                sendItems(sink, 100, 100);
                semaphore.release();

                // Then
                verifyEvents(collector, 200);
            }
        }
    }

    @Test
    public void givenCircuitBreakerInOpenStateWithDelayForwarding_whenSendingItemsAndClosing_thenQueuedItemsReceivedBeforeSubsequentItems() throws
            InterruptedException {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (DelaySink<Integer> delay = new DelaySink<>(collector, 10)) {
                try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                          .queueSize(100)
                                                                          .opened()
                                                                          .destination(delay)
                                                                          .build()) {
                    // When
                    Semaphore semaphore = new Semaphore(1);
                    Thread thread = new Thread(() -> {
                        // Send 100 events on a background thread, closing the circuit breaker once they are sent
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        sendItems(sink, 100);
                        semaphore.release();
                        sink.setState(CircuitBreakerSink.State.CLOSED);
                    });
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(thread);
                    Thread.sleep(100);

                    // Meanwhile once the initial 100 events are sent by the background thread the main thread sends another
                    // 100 items, these should wait for the items queued by the background thread to be forwarded before
                    // they are forwarding ensuring items remain in their original order
                    semaphore.acquire();
                    sendItems(sink, 100, 100);
                    semaphore.release();

                    // Then
                    verifyEvents(collector, 200);
                }
            }
        }
    }

    @Test
    public void givenCircuitBreakerInOpenStateWithDelayForwarding_whenSendingItemsAndCallingClose_thenNotAllItemsAreForwarded() throws
            InterruptedException {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (DelaySink<Integer> delay = new DelaySink<>(collector, 10)) {
                try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                          .queueSize(100)
                                                                          .opened()
                                                                          .destination(delay)
                                                                          .build()) {
                    // When
                    Semaphore semaphore = new Semaphore(1);
                    Thread thread = new Thread(() -> {
                        // Send 100 events on a background thread, closing the circuit breaker once they are sent
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        sendItems(sink, 100);
                        semaphore.release();
                        sink.setState(CircuitBreakerSink.State.CLOSED);
                    });
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(thread);
                    Thread.sleep(100);

                    // Meanwhile once the initial 100 events are sent by the background thread the main thread calls
                    // close() on the sink which aborts pending sends
                    semaphore.acquire();
                    try {
                        // Wait briefly to allow the queue to start draining on the background thread
                        Thread.sleep(50);
                        sink.close();
                    } catch (SinkException e) {
                        // Expected, sink was closed while the queue was non-empty, ignore
                        Assert.assertTrue(CS.contains(e.getMessage(), "queued items"));
                    }
                    semaphore.release();

                    // Then
                    Assert.assertTrue(collector.get().size() < 100);
                }
            }
        }
    }

    @Test
    public void givenCircuitBreakerSink_whenOpeningRepeatedly_thenRemainsOpen() {
        // Given
        try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create().queueSize(10).build()) {
            // When
            sink.setState(CircuitBreakerSink.State.OPEN);
            Assert.assertEquals(sink.getState(), CircuitBreakerSink.State.OPEN);
            sink.setState(CircuitBreakerSink.State.OPEN);

            // Then
            Assert.assertEquals(sink.getState(), CircuitBreakerSink.State.OPEN);
        }
    }

    @Test(expectedExceptions = SinkException.class, expectedExceptionsMessageRegExp = ".*already closed")
    public void givenCircuitBreakerSink_whenCloseCalled_thenNoFurtherItemsAccepted() {
        // Given
        try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create().queueSize(10).build()) {
            // When
            sink.close();
            sink.close();

            // Then
            sink.send(1);
        }
    }

    @Test
    public void givenCircuitBreakerSink_whenSendingNull_thenNotSent() {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                      .queueSize(10)
                                                                      .destination(collector)
                                                                      .build()) {
                // When
                sink.send(null);

                // Then
                verifyEvents(collector, 0);
            }
        }
    }

    private static final class CloseTrackingSink<T> implements Sink<T> {
        boolean closed = false;

        @Override
        public void send(T item) {
            // No-op
        }

        @Override
        public void close() {
            this.closed = true;
        }
    }

    @Test
    public void givenCircuitBreakerSinkInClosedState_whenCloseIsCalled_thenDestinationIsClosed() {
        // Given
        try (CloseTrackingSink<Integer> destination = new CloseTrackingSink<>()) {
            try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                      .queueSize(10)
                                                                      .closed()
                                                                      .destination(destination)
                                                                      .build()) {
                // When
                sink.close();

                // Then
                Assert.assertTrue(destination.closed);
            }
        }
    }

    @Test
    public void givenCircuitBreakerSinkInOpenState_whenCloseIsCalled_thenDestinationIsClosed() {
        // Given
        try (CloseTrackingSink<Integer> destination = new CloseTrackingSink<>()) {
            try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                      .queueSize(10)
                                                                      .opened()
                                                                      .destination(destination)
                                                                      .build()) {
                // When
                sink.close();

                // Then
                Assert.assertTrue(destination.closed);
            }
        }
    }

    @Test
    public void givenCircuitBreakerSinkInOpenStateWithPropagateClosedOnOpenDisabled_whenCloseIsCalled_thenDestinationIsClosed() {
        // Given
        try (CloseTrackingSink<Integer> destination = new CloseTrackingSink<>()) {
            try (CircuitBreakerSink<Integer> sink = CircuitBreakerSink.<Integer>create()
                                                                      .queueSize(10)
                                                                      .opened()
                                                                      .propagateCloseWhenOpen(false)
                                                                      .destination(destination)
                                                                      .build()) {
                // When
                sink.close();

                // Then
                Assert.assertFalse(destination.closed);
            }
        }
    }

    private static final class QueueEditableBreaker<T> extends CircuitBreakerSink<T> {

        /**
         * Creates a new circuit breaker sink
         *
         * @param destination    Destination sink
         * @param initialState   Initial state
         * @param queueSize      Queue size
         * @param propagateClose Whether when the circuit breaker is open a {@link #close()} is propagated  to the destination
         *                       sink
         */
        QueueEditableBreaker(Sink<T> destination, State initialState, int queueSize, boolean propagateClose) {
            super(destination, initialState, queueSize, propagateClose);
        }

        public void addToQueue(T item) {
            this.queue.add(item);
        }
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void givenCircuitBreakerSinkInClosedState_whenQueueIsNonEmpty_thenSendingItemBlocks() throws
            ExecutionException, InterruptedException, TimeoutException {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (QueueEditableBreaker<Integer> sink = new QueueEditableBreaker<>(collector,
                                                                                 CircuitBreakerSink.State.CLOSED, 10,
                                                                                 true)) {
                // When
                sink.addToQueue(1);

                // Then
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<?> future = executor.submit(() -> sink.send(2));
                future.get(1, TimeUnit.SECONDS);
                Assert.fail("Should have blocked trying to send an item");
            }
        }
    }

    @Test(expectedExceptions = ExecutionException.class, expectedExceptionsMessageRegExp = ".*already closed")
    public void givenCircuitBreakerSinkInClosedState_whenQueueIsNonEmpty_thenSendingItemBlocks_andUnblocksOnceSinkIsClosed() throws
            ExecutionException, InterruptedException, TimeoutException {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (QueueEditableBreaker<Integer> sink = new QueueEditableBreaker<>(collector,
                                                                                 CircuitBreakerSink.State.CLOSED, 10,
                                                                                 true)) {
                // When
                sink.addToQueue(1);

                // Then
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<?> future = executor.submit(() -> sink.send(2));
                Thread.sleep(250);

                // And
                try {
                    sink.close();
                } catch (SinkException e) {
                    // Expected as queue is non-empty
                }
                future.get(1, TimeUnit.SECONDS);
            }
        }
    }

    @Test(expectedExceptions = CancellationException.class)
    public void givenCircuitBreakerSinkInClosedState_whenQueueIsNonEmpty_thenSendingItemBlocks_andUnblocksWhenInterruptedByCancellation() throws
            ExecutionException, InterruptedException, TimeoutException {
        // Given
        try (CollectorSink<Integer> collector = CollectorSink.of()) {
            try (QueueEditableBreaker<Integer> sink = new QueueEditableBreaker<>(collector,
                                                                                 CircuitBreakerSink.State.CLOSED, 10,
                                                                                 true)) {
                // When
                sink.addToQueue(1);

                // Then
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<?> future = executor.submit(() -> sink.send(2));
                Thread.sleep(250);

                // And
                future.cancel(true);
                future.get(1, TimeUnit.SECONDS);
            }
        }
    }
}
