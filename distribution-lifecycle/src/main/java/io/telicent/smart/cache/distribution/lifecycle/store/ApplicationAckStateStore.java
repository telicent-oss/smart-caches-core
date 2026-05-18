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
package io.telicent.smart.cache.distribution.lifecycle.store;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;

import java.util.Map;
import java.util.UUID;

/**
 * The application acknowledgement store tracks applications responses to lifecycle events
 */
public interface ApplicationAckStateStore {

    /**
     * Gets the current application state for a given applications acknowledgement of a given lifecycle event
     *
     * @param eventId     Lifecycle Event ID
     * @param application Application ID
     * @return Current application state, or {@code null} if this application has no known acknowledgements for this
     * event
     */
    ApplicationState getApplicationState(UUID eventId, String application);

    /**
     * Sets the application state for a given applications acknowledgement of a given lifecycle event
     *
     * @param eventId     Lifecycle Event ID
     * @param application Application ID
     * @param state       New application state
     * @throws IllegalArgumentException <strong>MUST</strong> be thrown if the provided state is not a valid transition
     *                                  from the current state
     */
    void setApplicationState(UUID eventId, String application, ApplicationState state);

    /**
     * Gets all application states for a given lifecycle event
     *
     * @param eventId Lifecycle Event ID
     * @return Map of applications to states
     */
    Map<String, ApplicationState> getApplicationStates(UUID eventId);
}
