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

import io.telicent.smart.cache.observability.Chronological;

/**
 * <p>
 * An observable event, reflecting some real-world event that has transpired.
 * </p>
 *
 * <p>
 * Events are designed to be quite granular, balancing requirements to clearly and visibly determine the purpose of each
 * event quickly and furnish events with rich parameters or properties reflecting the event that has transpired.
 * </p>
 */
public interface ComponentEvent extends Chronological {
    /**
     * The name of the event that has transpired.
     *
     * @return the name of the event, which will never be null and defaults to the fully-qualified event class name in this implementation.
     */
    default String getEventName() {
        return getClass().getName();
    }
}
