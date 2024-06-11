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

/**
 * A dispatcher of events.
 *
 * @param <T> the type of event dispatched by this dispatcher.
 */
@FunctionalInterface
public interface EventDispatcher<T extends ComponentEvent> {
    /**
     * Dispatches the given event to interested parties.
     *
     * @param event the event to be dispatched.
     */
    void dispatch(final T event);
}
