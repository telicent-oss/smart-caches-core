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

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Utilities for working with common events.
 */
public class EventUtil {
    /**
     * Emits one or more event(s) through the given event dispatcher.
     *
     * @param dispatcher the dispatcher through which the event will be dispatched.
     * @param events the event(s) to be dispatched.
     * @param <E> the type of the event to be dispatched.
     */
    @SafeVarargs
    public static <E extends ComponentEvent> void emit(final EventDispatcher<E> dispatcher, E ... events) {
        emit(dispatcher, asList(events));
    }

    /**
     * Emits one or more event(s) through the given event dispatcher/
     *
     * @param dispatcher the dispatcher through which the emitted events will be dispatched.
     * @param events the event(s) to be dispatched.
     * @param <E> the type of the event to be dispatched.
     */
    public static <E extends ComponentEvent> void emit(final EventDispatcher<E> dispatcher, List<E> events) {
        events.forEach(dispatcher::dispatch);
    }
}
