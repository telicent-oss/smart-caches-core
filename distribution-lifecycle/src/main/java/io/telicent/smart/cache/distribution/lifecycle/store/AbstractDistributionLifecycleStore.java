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
import io.telicent.smart.cache.distribution.lifecycle.store.apps.AbstractAppDistributionLifecycleStore;
import io.telicent.smart.cache.distribution.lifecycle.store.global.AbstractGlobalDistributionLifecycleStore;
import io.telicent.smart.cache.distribution.lifecycle.store.apps.AppDistributionLifecycleStoreFile;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract distribution lifecycle store with two further abstract subclasses:
 * <ul>
 *     <li>
 *         {@link AbstractAppDistributionLifecycleStore} intended as a state store for a single application with a
 *         concrete implementation {@link AppDistributionLifecycleStoreFile}.
 *     </li>
 *     <li>
 *         {@link AbstractGlobalDistributionLifecycleStore} intended as a state store for a central service tracking
 *         distribution lifecycle across the platform.  This variant intentionally does not have a concrete
 *         implementation as persistent state storage is considered an implementation detail of the central state
 *         service.
 *     </li>
 * </ul>
 */
public abstract class AbstractDistributionLifecycleStore implements DistributionLifecycleStateStore {
    /**
     * In-memory tracker of lifecycle action events
     */
    protected final Map<UUID, LifecycleAction> events = new ConcurrentHashMap<>();
    /**
     * In-memory tracker of distribution lifecycle states
     */
    protected final Map<String, DistributionLifecycleState> distributions = new ConcurrentHashMap<>();

    /**
     * Indicates whether the store is closed
     */
    protected volatile boolean closed = false;

    /**
     * Method that should be called from implementation methods to check the store hasn't been closed before beginning
     * an operation
     */
    protected final void ensureNotClosed() {
        if (this.closed) {
            throw new IllegalStateException("State Store is closed");
        }
    }

    @Override
    public void add(LifecycleAction action) {
        ensureNotClosed();
        Objects.requireNonNull(action, "Action cannot be null");

        // Check that the action does not have an already known Event ID
        // Note that we specifically permit duplicate events to ensure idempotency
        if (this.events.containsKey(action.getEventId())) {
            if (!Objects.equals(action, this.events.get(action.getEventId()))) {
                throw new IllegalStateException(
                        "a Lifecycle Action Event " + action.getEventId() + " with differing content is already known to this state store");
            } else {
                // If this was a duplicate event we already have updated our state store with it so we can ignore this
                return;
            }
        }
        DistributionLifecycleState current = this.getLifecycleState(action.getDistributionId());
        DistributionLifecycleState target = action.getState().getTo();
        if (!current.canTransition(target)) {
            throw new IllegalStateException(
                    "Distribution Lifecycle state transition from " + current + " to " + target + " is not permitted");
        }
        this.events.put(action.getEventId(), action);
        this.distributions.put(action.getDistributionId(), target);
    }

    /**
     * Given a lifecycle acknowledgement get the target state, plus validate that the target state is valid based on our
     * current known state for the given application
     *
     * @param ack     Acknowledgement
     * @param current Current known application state
     * @return Target state
     * @throws IllegalStateException Thrown if the target state is not a valid state based on our current known state
     *                               for the given application
     */
    public static ApplicationState getTargetState(LifecycleAcknowledgement ack, ApplicationState current) {
        ApplicationState target = ack.getState().getApp();

        // Verify the state transition is legal
        if (current == null) {
            if (target != ApplicationState.Requested) {
                throw new IllegalStateException("Requested MUST be the initial state for application acknowledgements");
            }
        } else {
            if (!current.canTransition(target)) {
                throw new IllegalStateException(
                        "An application state transition from " + current + " to " + target + " is not permitted");
            }
        }
        return target;
    }

    @Override
    public DistributionLifecycleState getLifecycleState(String distributionId) {
        ensureNotClosed();
        if (StringUtils.isBlank(distributionId)) {
            throw new IllegalArgumentException("Distribution ID cannot be null/blank");
        }
        return this.distributions.getOrDefault(distributionId, DistributionLifecycleState.Unregistered);
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public Map<String, DistributionLifecycleState> getLifecycleStates() {
        ensureNotClosed();
        return Collections.unmodifiableMap(this.distributions);
    }
}
