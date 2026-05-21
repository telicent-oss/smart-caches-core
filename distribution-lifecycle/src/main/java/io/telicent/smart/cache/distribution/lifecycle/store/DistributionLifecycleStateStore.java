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
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The distribution lifecycle state store tracks the active lifecycle events and their acknowledgements
 */
public interface DistributionLifecycleStateStore {

    /**
     * Adds a lifecycle action to the store
     *
     * @param action Lifecycle action
     */
    void add(LifecycleAction action);

    /**
     * Adds a lifecycle acknowledgement to the store
     *
     * @param ack Lifecycle acknowledgement
     */
    void add(String application, LifecycleAcknowledgement ack);

    /**
     * Gets all the lifecycle events that are considered active
     * <p>
     * This <strong>MUST</strong> return any event where there are either zero application acknowledgements, or there is
     * one/more application that has yet to reach the
     * {@link io.telicent.smart.cache.distribution.lifecycle.ApplicationState#Completed} state.
     * </p>
     *
     * @return Active events (if any)
     */
    List<LifecycleAction> activeEvents();

    /**
     * Gets the states of all known distributions
     *
     * @return Distribution lifecycle states
     */
    Map<String, DistributionLifecycleState> getLifecycleStates();

    /**
     * Gets the current lifecycle state for the given distribution
     *
     * @param distributionId Distribution ID
     * @return Current lifecycle state, <strong>MUST</strong> return {@link DistributionLifecycleState#Unregistered} if
     * not a known Distribution ID
     */
    DistributionLifecycleState getLifecycleState(String distributionId);

    /**
     * Gets all application states for a given lifecycle event
     *
     * @param eventId Lifecycle Event ID
     * @return Map of applications to states
     */
    Map<String, ApplicationState> getApplicationStates(UUID eventId);

    /**
     * Gets the current application state for a given applications acknowledgement of a given lifecycle event
     *
     * @param eventId     Lifecycle Event ID
     * @param application Application ID
     * @return Current application state, or {@code null} if this application has no known acknowledgements for this
     * event
     */
    ApplicationState getApplicationState(UUID eventId, String application);
}
