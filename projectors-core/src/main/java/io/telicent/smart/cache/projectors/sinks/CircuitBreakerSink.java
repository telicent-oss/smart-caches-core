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
    private volatile boolean closed = false;
    @Getter
    private volatile State state;
    @ToString.Exclude
    private final LinkedBlockingQueue<T> queue;

    /**
     * Creates a new circuit breaker sink
     *
     * @param destination  Destination sink
     * @param initialState Initial state
     * @param queueSize    Queue size
     */
    CircuitBreakerSink(Sink<T> destination, State initialState, int queueSize) {
        super(destination);
        if (queueSize < 1) throw new IllegalArgumentException("queueSize must be at least 1");
        this.state = Objects.requireNonNull(initialState, "initialState must not be null");
        this.queue = new LinkedBlockingQueue<>(queueSize);
    }

    /**
     * Sets the state for the circuit breaker
     *
     * @param state New state
     */
    public void setState(State state) {
        this.state = Objects.requireNonNull(state);

        // If we transitioned into the Closed state forward on any previously queued items
        if (this.state == State.CLOSED) {
            while (!this.queue.isEmpty() && !this.closed) {
                // NB - If the queue is non-empty, and since we don't permit null items (see shouldForward()) we are
                //      guaranteed to always poll() a non-null item to forward on at this point
                this.forward(this.queue.poll());
            }
        }
    }

    @Override
    protected boolean shouldForward(T item) {
        ensureNotClosed();

        if (item == null) {
            return false;
        }

        switch (this.state) {
            case OPEN:
                // If we're open then we use our queue to hold items temporarily, this will block if our configured
                // queue size has been reached
                try {
                    this.queue.put(item);
                } catch (InterruptedException e) {
                    throw new SinkException("Failed to add item to queue while circuit breaker was open", e);
                }
                return false;
            case CLOSED:
            default:
                // If we're closed then pass the items on immediately unless there's stuff in the queue in which case
                // wait for the queue to drain first.  This can happen if another thread has recently closed the circuit
                // breaker and is still forwarding on the previously queued items
                while (!this.queue.isEmpty() && !this.closed) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // Ignore and continue waiting
                    }
                }
                // Double check we haven't been closed in the meantime
                ensureNotClosed();
        }
        return true;
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

        // Ensure our destination is also closed
        this.destination.close();

        // If our queue was non-empty throw an error as this implies something went wrong if the circuit breaker was
        // open when the sink was closed
        if (!this.queue.isEmpty()) {
            throw new SinkException(
                    String.format("Circuit breaker had %,d queued items when sink closed due to being in state %s",
                                  this.queue.size(),
                                  this.state));
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

        @Override
        public CircuitBreakerSink<T> build() {
            return new CircuitBreakerSink<T>(this.getDestination(), this.state, this.queueSize);
        }
    }
}
