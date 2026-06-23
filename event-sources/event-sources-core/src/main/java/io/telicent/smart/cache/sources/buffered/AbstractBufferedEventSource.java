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
package io.telicent.smart.cache.sources.buffered;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An abstract event source implementation that uses an internal buffer.
 * <p>
 * The buffered events are stored as an intermediate type to allow derived implementations to store additional
 * information alongside the actual event.  Derived implementations are responsible for refilling the buffer when
 * requested and handling any bookkeeping that might require.
 * </p>
 * <h3>Notes for Implementors</h3>
 * <p>
 * There are three key methods in this abstract class:
 * </p>
 * <ul>
 *     <li>{@link #bufferExhausted()} which is called whenever the internal buffer is exhausted.  This allows an
 *     implementation to do any internal bookkeeping such as committing offsets.</li>
 *     <li>{@link #tryFillBuffer(Duration)} which is called after {@link #bufferExhausted()} and gives the
 *     implementation an opportunity to refill the buffer.</li>
 *     <li>{@link #decodeEvent(Object)} which is used to convert the intermediate event type into an actual concrete
 *     {@link Event} instance.</li>
 * </ul>
 * <p>
 * Please see the Javadoc on each of those methods for more detailed discussion.  Implementators may also need to
 * override the {@link #availableImmediately()}, {@link #isExhausted()} and {@link #close()} methods appropriately
 * depending on the event source they are exposing.
 * </p>
 *
 * @param <TIntermediate> Intermediate event type
 * @param <TKey>          Event key type
 * @param <TValue>        Event value type
 */
public abstract class AbstractBufferedEventSource<TIntermediate, TKey, TValue> implements EventSource<TKey, TValue> {

    /**
     * Tracks whether the event source has been closed
     */
    protected volatile boolean closed = false;
    /**
     * The buffer of intermediate events
     */
    protected final Queue<TIntermediate> events = new ConcurrentLinkedQueue<>();

    /**
     * Creates a new buffered event source
     */
    public AbstractBufferedEventSource() {
    }

    @Override
    public final boolean availableImmediately() {
        return !this.closed && !this.events.isEmpty();
    }

    @Override
    public boolean isExhausted() {
        return this.closed;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public final boolean isClosed() {
        return this.closed;
    }

    /**
     * Decodes from the internal event representation used when buffering events to the actual {@link Event} instance
     * <p>
     * The implementation must return {@code null} if it receives a {@code null} input since {@code null} is used to
     * represent no event available.
     * </p>
     *
     * @param internalEvent Internal event representation
     * @return Decoded event
     */
    protected abstract Event<TKey, TValue> decodeEvent(TIntermediate internalEvent);

    @Override
    public final Event<TKey, TValue> poll(Duration timeout) {
        if (this.closed) {
            throw new IllegalStateException("Event source has been closed");
        }

        // If we have some events buffered continue returning them, no need to worry about timeout as this should be
        // essentially immediate
        if (!events.isEmpty()) {
            return this.decodeEvent(events.poll());
        }

        // The buffer has now been exhausted, allow the derived implementation chance to do any state management it
        // needs and then ask it to refill the buffer
        // Depending on the derived implementation it's possible this might fail to fill the buffer BUT still have time
        // remaining on its timeout, in which case we keep going round the loop and decreasing our timeout until it is
        // fully exhausted
        long start = System.currentTimeMillis();
        Duration remainingTimeout = timeout;
        while (events.isEmpty() && remainingTimeout != null) {
            bufferExhausted();
            if (tryFillBuffer(remainingTimeout)) {
                // If tryFillBuffer() indicates it was interrupted/genuinely empty then return immediately regardless of
                // whether we have remaining timeout
                return this.decodeEvent(events.poll());
            }

            if (events.isEmpty()) {
                remainingTimeout = updateTimeout(start, timeout);
            }
        }

        // Return the next buffered event, or null if no events available
        return this.decodeEvent(events.poll());
    }

    /**
     * Calculates a new timeout based on elapsed time
     *
     * @param start   When the operation was started
     * @param timeout The original timeout to reduce by the elapsed time
     * @return Reduced timeout, or {@code null} if no time is remaining
     */
    public static Duration updateTimeout(long start, Duration timeout) {
        long elapsed = System.currentTimeMillis() - start;
        long remainingTime = timeout.toMillis() - elapsed;
        if (remainingTime <= 0) {
            return null;
        } else {
            return Duration.ofMillis(remainingTime);
        }
    }

    /**
     * Try to refill the buffer with the next available events.  If no new events are available within the timeout leave
     * the buffer unmodified.
     * <p>
     * The return value of this method controls whether the {@link #poll(Duration)} method may call this method again to
     * retry refilling the buffer.  A {@code true} value indicates that the refill operation was interrupted,
     * <strong>OR</strong> genuinely had no new available, in which case {@link #poll(Duration)} will return
     * immediately.  Therefore, implementations should ensure that they return {@code true} or {@code false}
     * appropriately.
     * </p>
     *
     * @param timeout Timeout
     * @return True if interrupted or genuinely no events currently available, false otherwise
     * @throws io.telicent.smart.cache.sources.EventSourceException Should be thrown if an unrecoverable error is
     *                                                              encountered
     */
    protected abstract boolean tryFillBuffer(Duration timeout);

    /**
     * Gives derived implementations a chance to carry out any state management they might need to track the fact that
     * all the previously buffered events have now been processed.
     */
    protected void bufferExhausted() {
        // No-op
    }
}
