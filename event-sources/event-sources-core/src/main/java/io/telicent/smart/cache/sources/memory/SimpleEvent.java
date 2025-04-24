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
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.Header;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple implementation of an Event
 *
 * @param <TKey>   Event key type
 * @param <TValue> Event value type
 */
public class SimpleEvent<TKey, TValue> implements Event<TKey, TValue> {

    private final List<EventHeader> headers;
    private final TKey key;
    private final TValue value;
    private final EventSource source;

    /**
     * Creates a new event
     *
     * @param headers Headers
     * @param key     Key
     * @param value   Value
     */
    public SimpleEvent(Collection<EventHeader> headers, TKey key, TValue value) {
        this(headers, key, value, null);
    }

    /**
     * Creates a new event
     *
     * @param headers Headers
     * @param key     Key
     * @param value   Value
     * @param source  Event source that originated this event
     */
    public SimpleEvent(Collection<EventHeader> headers, TKey key, TValue value, EventSource source) {
        this.headers = CollectionUtils.isNotEmpty(headers) ? new ArrayList<>(headers) : Collections.emptyList();

        // Note that an Event may have either a null key or value, however an event with both null is considered invalid
        if (key == null && value == null) {
            throw new NullPointerException("Both key and value cannot be null");
        }

        this.key = key;
        this.value = value;
        this.source = source;
    }

    @Override
    public Stream<EventHeader> headers() {
        return this.headers.stream();
    }

    @Override
    public Stream<String> headers(String key) {
        return this.headers.stream().filter(h -> Objects.equals(h.key(), key)).map(EventHeader::value);
    }

    @Override
    public String lastHeader(String key) {
        List<String> values = this.headers(key).collect(Collectors.toList());
        return CollectionUtils.isEmpty(values) ? null : values.get(values.size() - 1);
    }

    @Override
    public TKey key() {
        return this.key;
    }

    @Override
    public TValue value() {
        return this.value;
    }

    @Override
    public <TNewKey> Event<TNewKey, TValue> replaceKey(TNewKey newKey) {
        return new SimpleEvent<>(this.headers, newKey, this.value, this.source);
    }

    @Override
    public <TNewValue> Event<TKey, TNewValue> replaceValue(TNewValue newValue) {
        return new SimpleEvent<>(this.headers, this.key, newValue, this.source);
    }

    @Override
    public <TNewKey, TNewValue> Event<TNewKey, TNewValue> replace(TNewKey newKey, TNewValue newValue) {
        return new SimpleEvent<>(this.headers, newKey, newValue, this.source);
    }

    @Override
    public Event<TKey, TValue> replaceHeaders(Stream<EventHeader> headers) {
        return new SimpleEvent<>(headers.toList(), this.key, this.value, this.source);
    }

    @Override
    public Event<TKey, TValue> addHeaders(Stream<EventHeader> headers) {
        List<EventHeader> newHeaders = new ArrayList<>(this.headers);
        headers.forEach(newHeaders::add);
        return new SimpleEvent<>(newHeaders, this.key, this.value, this.source);
    }

    @Override
    public EventSource source() {
        return this.source;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Event<?, ?> other)) {
            return false;
        }

        if (!SetUtils.isEqualSet(this.headers, other.headers().toList())) {
            return false;
        }

        if (!Objects.equals(this.key, other.key())) {
            return false;
        }
        if (!Objects.equals(this.value, other.value())) {
            return false;
        }

        return true;
    }
}
