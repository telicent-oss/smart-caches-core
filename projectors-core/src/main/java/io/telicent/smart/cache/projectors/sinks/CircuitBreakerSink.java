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
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A circuit breaker sink is used, as the name implies, as a circuit breaker within a pipeline.
 * <p>
 * This allows pipelines to be temporarily paused in response to some outside event and resumed when appropriate.  When
 * the circuit breaker is in the {@link State#OPEN} state then items are not forwarded but held temporarily in a queue,
 * if the queue size is reached then the pipeline will be blocked until the circuit breaker returns to the
 * {@link State#CLOSED} state.  When the circuit breaker is closed any temporarily held items are forwarded onto the
 * destination <strong>prior</strong> to any further items in order to retain item ordering.
 * </p>
 *
 * @param <T> Item type
 */
@ToString(callSuper = true)
public class CircuitBreakerSink<T> extends AbstractTransformingSink<T, T> {

    /**
     * How long, in milliseconds, a blocked thread waits before rechecking the state of the circuit breaker
     */
    private static final long WAIT_INTERVAL_MS = 10;

    /**
     * Possible states for the circuit breaker
     */
    public enum State {
        /**
         * Open i.e. events will be held and not forwarded on
         */
        OPEN,
        /**
         * Closed, events will be forwarded on as normal
         */
        CLOSED
    }

    @ToString.Exclude
    protected volatile boolean closed = false;
    protected final boolean propagateCloseWhenOpen;
    @Getter
    protected volatile State state;
    @ToString.Exclude
    protected final LinkedBlockingQueue<T> queue;
    /**
     * Guards the state transitions and the queue draining so that a thread sending an item can never overtake an item
     * that the draining thread has already removed from the queue but not yet forwarded
     */
    @ToString.Exclude
    private final ReentrantLock lock = new ReentrantLock();
    @ToString.Exclude
    private final Condition condition = this.lock.newCondition();
    /**
     * Whether a thread is currently draining the queue, i.e. forwarding on the items that were held while the circuit
     * breaker was {@link State#OPEN}
     */
    @ToString.Exclude
    private boolean draining = false;

    /**
     * Creates a new circuit breaker sink
     *
     * @param destination    Destination sink
     * @param initialState   Initial state
     * @param queueSize      Queue size
     * @param propagateClose Whether when the circuit breaker is open a {@link #close()} is propagated  to the
     *                       destination sink
     */
    CircuitBreakerSink(Sink<T> destination, State initialState, int queueSize, boolean propagateClose) {
        super(destination);
        if (queueSize < 1) throw new IllegalArgumentException("queueSize must be at least 1");
        this.state = Objects.requireNonNull(initialState, "initialState must not be null");
        this.queue = new LinkedBlockingQueue<>(queueSize);
        this.propagateCloseWhenOpen = propagateClose;
    }

    /**
     * Sets the state for the circuit breaker
     *
     * @param state New state
     */
    public void setState(State state) {
        Objects.requireNonNull(state);

        boolean drain;
        this.lock.lock();
        try {
            this.state = state;
            // If we transitioned into the Closed state we need to forward on any previously queued items.  Only one
            // thread drains at a time, if another thread is already draining then it will forward on our items too.
            drain = state == State.CLOSED && !this.draining;
            if (drain) {
                this.draining = true;
            }
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }

        if (!drain) {
            return;
        }

        // NB - The draining flag remains set until the final item has actually been forwarded, NOT merely removed from
        //      the queue.  Other threads sending items wait on that flag (see shouldForward()) so they can't overtake
        //      an item that is still in-flight to the destination.
        try {
            while (!this.closed) {
                T item = this.queue.poll();
                if (item == null) {
                    break;
                }
                this.forward(item);
            }
        } finally {
            this.lock.lock();
            try {
                this.draining = false;
                this.condition.signalAll();
            } finally {
                this.lock.unlock();
            }
        }
    }

    @Override
    protected boolean shouldForward(T item) {
        ensureNotClosed();

        if (item == null) {
            return false;
        }

        this.lock.lock();
        try {
            while (true) {
                // Double check we haven't been closed in the meantime
                ensureNotClosed();

                if (this.state == State.OPEN) {
                    // If we're open then we use our queue to hold items temporarily, this will block if our configured
                    // queue size has been reached
                    if (this.queue.offer(item)) {
                        return false;
                    }
                    // Queue is full, wait for the pipeline to be unblocked by the circuit breaker being closed
                    awaitChange("Interrupted while trying to add item to queue while circuit breaker was open");
                } else {
                    // If we're closed then pass the items on immediately unless there are items still to be drained, in
                    // which case wait for the queue to drain first.  This can happen if another thread has recently
                    // closed the circuit breaker and is still forwarding on the previously queued items
                    if (!this.draining && this.queue.isEmpty()) {
                        return true;
                    }
                    awaitChange("Interrupted while waiting for circuit breaker queue to drain");
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Waits for the state of the circuit breaker, or of its queue, to change
     * <p>
     * Must be called while holding {@link #lock}, awaiting releases the lock so that the draining thread can make
     * progress.  A bounded wait is used so that a missed signal can never leave a thread waiting indefinitely.
     * </p>
     *
     * @param interruptedMessage Message used if the waiting thread is interrupted
     */
    private void awaitChange(String interruptedMessage) {
        try {
            this.condition.await(WAIT_INTERVAL_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SinkException(interruptedMessage, e);
        }
    }

    private void ensureNotClosed() {
        if (this.closed) {
            throw new SinkException("Sink is already closed");
        }
    }

    @Override
    protected T transform(T item) {
        return item;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;

        // Wake up any threads that are waiting on the queue so that they fail fast
        this.lock.lock();
        try {
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }

        // Ensure our destination is also closed UNLESS we're open and configured not to do so
        if (this.state == State.CLOSED || this.propagateCloseWhenOpen) {
            this.destination.close();
        }

        // If our queue was non-empty throw an error as this implies something went wrong if the circuit breaker was
        // open when the sink was closed
        if (!this.queue.isEmpty()) {
            throw new SinkException(
                    String.format("Circuit breaker had %,d queued items when sink closed due to being in state %s",
                                  this.queue.size(), this.state));
        }
    }

    /**
     * Gets a builder for a {@link CircuitBreakerSink}
     *
     * @return Builder
     */
    public static <T> Builder<T> create() {
        return new Builder<>();
    }

    /**
     * A builder for a {@link CircuitBreakerSink}
     *
     * @param <T> Item type
     */
    public static final class Builder<T>
            extends AbstractForwardingSinkBuilder<T, T, CircuitBreakerSink<T>, Builder<T>> {

        private State state = State.CLOSED;
        private int queueSize = 100;
        private boolean propagateCloseWhenOpen = true;

        /**
         * Sets that the initial state of the circuit breaker will be {@link State#OPEN} i.e. it holds items until it is
         * moved to the {@link State#CLOSED} state.
         *
         * @return Builder
         */
        public Builder<T> opened() {
            this.state = State.OPEN;
            return this;
        }

        /**
         * Sets that the initial state of the circuit breaker will be {@link State#CLOSED} i.e. it allows items to pass
         * through normally
         *
         * @return Builder
         */
        public Builder<T> closed() {
            this.state = State.CLOSED;
            return this;
        }

        /**
         * Sets the queue size for the circuit breaker
         * <p>
         * When the circuit breaker is {@link State#OPEN} this controls how many items it will enqueue before blocking
         * the entire pipeline.  When the circuit breaker is returned to the {@link State#CLOSED} state then any queued
         * items are forwarded on to the destination.
         * </p>
         *
         * @param queueSize Queue size
         * @return Builder
         */
        public Builder<T> queueSize(int queueSize) {
            this.queueSize = queueSize;
            return this;
        }

        /**
         * Sets whether when the circuit breaker is open a {@link Sink#close()} operation is propagated to the
         * destination sink, if set to {@code false} then the {@link Sink#close()} only affects the circuit breaker and
         * <strong>DOES NOT</strong> affect the destination sink.
         * <p>
         * Whether you want this behaviour, and whether it is safe for a given pipeline, should be determined by the
         * developer using this API.
         * </p>
         *
         * @param propagateCloseWhenOpen Whether when the circuit breaker is open {@link Sink#close()} operations
         *                               propagate to the destination sink
         * @return Builder
         */
        public Builder<T> propagateCloseWhenOpen(boolean propagateCloseWhenOpen) {
            this.propagateCloseWhenOpen = propagateCloseWhenOpen;
            return this;
        }

        @Override
        public CircuitBreakerSink<T> build() {
            return new CircuitBreakerSink<T>(this.getDestination(), this.state, this.queueSize,
                                             this.propagateCloseWhenOpen);
        }
    }
}
