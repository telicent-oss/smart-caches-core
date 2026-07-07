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
import io.telicent.smart.cache.distribution.lifecycle.events.IngestStatus;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.PartitionOffsets;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The distribution lifecycle state store tracks the active lifecycle events and their acknowledgements
 * <p>
 * Note that as a general API contract after {@link #close()} has been called invoking any operation in this API
 * <strong>MUST</strong> throw an {@link IllegalStateException} indicating the store is closed
 * </p>
 */
public interface DistributionLifecycleStateStore extends AutoCloseable {

    /**
     * Adds a lifecycle action to the store updating distribution lifecycle state appropriately
     * <p>
     * A store <strong>MUST</strong> reject an event if it reuses a previously seen Event ID with different event
     * content.  Additionally, it must apply events idempotently such that if it receives the same event twice it
     * <strong>MUST</strong> ensure that the event is only applied once to the state store.
     * </p>
     *
     * @param action Lifecycle action
     * @throws NullPointerException  Thrown if the provided action is {@code null}
     * @throws IllegalStateException Thrown if the state transition represented by the action is not a legal transition,
     *                               or if the provided action has the same ID as an existing action in the state store
     *                               and is not an exact duplicate of the existing action, or if the store is closed
     */
    void add(LifecycleAction action);

    /**
     * Adds a lifecycle acknowledgement to the store
     * <p>
     * A state store may be application scoped in which case it only tracks acknowledgements pertaining to a single
     * application and will discard acknowledgements for any other application.
     * </p>
     *
     * @param application Application ID of the application that sent the acknowledgement
     * @param ack         Lifecycle acknowledgement
     * @throws NullPointerException     Thrown if the provided acknowledgement is {@code null}
     * @throws IllegalArgumentException Thrown if the application ID is {@code null}/blank
     * @throws IllegalStateException    Thrown if the acknowledgement is for an action not known to this store, or if
     *                                  the acknowledgement represents an illegal state transition, or if the store is
     *                                  closed
     */
    void add(String application, LifecycleAcknowledgement ack);

    /**
     * Adds an ingest status to the store
     * <p>
     * A state store may be application scoped in which case it only tracks statuses pertaining to a single
     * application and will discard statuses for any other application.
     * </p>
     * <p>
     * Stores mmust be replay tolerant when tracking ingest offsets:
     * </p>
     * <ul>
     *     <li>Lower offsets than the current offset for a partition <strong>MUST</strong> be ignored.</li>
     *     <li>Equal offsets <strong>MUST</strong> be treated idempotently and ignored.</li>
     *     <li>Higher offsets <strong>MUST</strong> advance the stored offset for that partition.</li>
     * </ul>
     *
     * @param application Application ID of the application that sent the status
     * @param status      Ingest status
     * @throws NullPointerException     Thrown if the provided status is {@code null}
     * @throws IllegalArgumentException Thrown if the application ID is {@code null}/blank
     * @throws IllegalStateException    Thrown if the store is closed
     */
    void add(String application, IngestStatus status);

    /**
     * Gets all the lifecycle events that are considered active
     * <p>
     * This <strong>MUST</strong> return any event where there are either zero application acknowledgements, or there is
     * one/more application that has yet to reach the
     * {@link io.telicent.smart.cache.distribution.lifecycle.ApplicationState#Completed} state.  Each active event
     * must be returned only once, even if there are multiple applications which have reported partial
     * progress on applying an event.
     * </p>
     *
     * @return Active events (if any)
     * @throws IllegalStateException Thrown if the store is closed
     */
    List<LifecycleAction> activeEvents();

    /**
     * Gets the states of all known distributions
     *
     * @return Distribution lifecycle states
     * @throws IllegalStateException Thrown if the store is closed
     */
    Map<String, DistributionLifecycleState> getLifecycleStates();

    /**
     * Gets the current lifecycle state for the given distribution
     *
     * @param distributionId Distribution ID
     * @return Current lifecycle state, <strong>MUST</strong> return {@link DistributionLifecycleState#Unregistered} if
     * not a known Distribution ID
     * @throws IllegalArgumentException Thrown if the distribution ID is {@code null} or blank
     * @throws IllegalStateException    Thrown if the store is closed
     */
    DistributionLifecycleState getLifecycleState(String distributionId);

    /**
     * Gets all application states for a given lifecycle event
     *
     * @param eventId Lifecycle Event ID
     * @return Map of applications to states, empty if not a known event
     * @throws IllegalArgumentException Thrown if the Event ID is {@code null} or blank
     * @throws IllegalStateException    Thrown if the store is closed
     */
    Map<String, ApplicationState> getApplicationStates(UUID eventId);

    /**
     * Gets the current application state for a given applications acknowledgement of a given lifecycle event
     * <p>
     * A state store may be application scoped in which case it only tracks states pertaining to a single application
     * and will return {@code null} for any other application.
     * </p>
     *
     * @param eventId     Lifecycle Event ID
     * @param application Application ID
     * @return Current application state, or {@code null} if this application has no known acknowledgements for this
     * event
     * @throws IllegalArgumentException Thrown if the Event ID or Application ID are {@code null} or blank
     * @throws IllegalStateException    Thrown if the store is closed
     */
    ApplicationState getApplicationState(UUID eventId, String application);

    /**
     * Gets the latest ingest statuses known for a given application grouped by distribution ID
     *
     * @param application Application ID
     * @return Latest ingest statuses by distribution ID, empty if no statuses are known for the application
     * @throws IllegalArgumentException Thrown if the application ID is {@code null}/blank
     * @throws IllegalStateException    Thrown if the store is closed
     */
    Map<String, PartitionOffsets> getIngestStatuses(String application);

    /**
     * Gets the latest ingest status known for a specific distribution within a given application
     *
     * @param application    Application ID
     * @param distributionId Distribution ID
     * @return Latest partition offsets, or {@code null} if no ingest status is known for the distribution
     * @throws IllegalArgumentException Thrown if the application ID or distribution ID are {@code null}/blank
     * @throws IllegalStateException    Thrown if the store is closed
     */
    PartitionOffsets getIngestStatus(String application, String distributionId);

    /**
     * Gets the latest ingest offset known for a specific partition of a specific distribution within a given
     * application
     *
     * @param application    Application ID
     * @param distributionId Distribution ID
     * @param partition      Partition
     * @return Latest offset, or {@code null} if no offset is known
     * @throws IllegalArgumentException Thrown if the application ID, distribution ID or partition are
     *                                  {@code null}/blank
     * @throws IllegalStateException    Thrown if the store is closed
     */
    Long getIngestOffset(String application, String distributionId, String partition);

    /**
     * Gets the latest ingest statuses known for all applications grouped by application then distribution ID
     *
     * @return Latest ingest statuses grouped by application and distribution ID
     * @throws IllegalStateException Thrown if the store is closed
     */
    Map<String, Map<String, PartitionOffsets>> getAllIngestStatuses();

    /**
     * Requests that the state store actively flushes state to underlying persistent storage (if any)
     *
     * @throws IllegalStateException Thrown if the store is closed
     */
    default void flush() {
    }

    /**
     * Closes the state store, this includes flushing state to underlying persistent storage (if any)
     * <p>
     * Calling this multiple times should be safe and not result in any errors.  Once this has been called all other
     * methods <strong>MUST</strong> throw an {@link IllegalStateException} indicating the store is closed.
     * </p>
     */
    @Override
    void close();
}
