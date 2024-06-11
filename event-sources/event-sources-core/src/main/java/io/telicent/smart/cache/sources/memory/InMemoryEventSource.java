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
package io.telicent.smart.cache.sources.memory;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * An in-memory event source intended primarily for testing
 *
 * @param <TKey>   Event key type
 * @param <TValue> Event value type
 */
public class InMemoryEventSource<TKey, TValue> implements EventSource<TKey, TValue> {

    private final Queue<Event<TKey, TValue>> events = new LinkedList<>();
    private boolean closed = false;

    /**
     * Creates a new in-memory event source
     *
     * @param events Events that the source will provide
     */
    public InMemoryEventSource(Collection<Event<TKey, TValue>> events) {
        Objects.requireNonNull(events, "Events cannot be null");
        CollectionUtils.addAll(this.events, events);
    }

    @Override
    public boolean availableImmediately() {
        return !this.closed && !this.events.isEmpty();
    }

    @Override
    public boolean isExhausted() {
        return !availableImmediately();
    }

    @Override
    public void close() {
        this.closed = true;
        this.events.clear();
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public Event<TKey, TValue> poll(Duration timeout) {
        checkNotClosed();

        if (!this.events.isEmpty()) {
            return this.events.poll();
        }

        return null;
    }

    private void checkNotClosed() {
        if (this.closed) {
            throw new IllegalStateException("Event Source has been closed");
        }
    }

    @Override
    public Long remaining() {
        return (long) this.events.size();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void processed(Collection<Event> processedEvents) {
        checkNotClosed();
        // No-op
    }
}
