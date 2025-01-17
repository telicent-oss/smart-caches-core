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
package io.telicent.smart.cache.observability.events;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides event source delegate functionality.
 * @param <E> An observable event
 */
@Slf4j
public class EventSourceSupport<E extends ComponentEvent> implements ComponentEventSource<E>, EventDispatcher<E> {
    private final Set<EventListener<E>> listeners = Collections.synchronizedSet(new LinkedHashSet<>());

    /**
     * Adds a listener of events to this event source.
     *
     * @param listener the listener to be added.
     */
    @Override
    public void addListener(EventListener<E> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener of events from this event source.
     *
     * @param listener the listener to be removed.
     * @return true if the lister was present and successfully removed, false otherwise.
     */
    @Override
    public boolean removeListener(EventListener<E> listener) {
        return listeners.remove(listener);
    }

    /**
     * Dispatches the given event to registered listeners of this event source.
     *
     * @param event the event to be dispatched.
     */
    @Override
    public void dispatch(E event) {
        listeners.forEach(l -> {
            try {
                l.on(event);
            } catch (RuntimeException ex) {
                log.error("Caught exception during listener event dispatch [listener class={}]: ", l.getClass().getName(), ex);
            }
        });
    }
}
