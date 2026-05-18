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
package io.telicent.smart.cache.distribution.lifecycle.store.apps;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Abstract application distribution lifecycle state store intended to allow a service to track events and its own
 * acknowledgement state for those events
 * <p>
 * This is intended as an abstract base class for concrete implementations to derive from.  This implementation provides
 * in-memory state tracking, but it is expected that derived implementations would back the state store with persistent
 * storage so applications don't need to re-read all lifecycle events prior to starting up.
 * </p>
 */
public abstract class AbstractAppDistributionLifecycleStore
        implements DistributionLifecycleStateStore {

    protected final String application;
    protected final Map<UUID, LifecycleAction> events = new HashMap<>();
    protected final Map<UUID, ApplicationState> appStates = new HashMap<>();
    protected final Map<String, DistributionLifecycleState> distributions = new HashMap<>();

    protected AbstractAppDistributionLifecycleStore(String app) {
        if (StringUtils.isBlank(app)) {
            throw new IllegalArgumentException("app identifier cannot be null");
        }
        this.application = app;
    }

    @Override
    public ApplicationState getApplicationState(UUID eventId, String application) {
        if (!Objects.equals(this.application, application)) {
            return null;
        }
        return this.appStates.get(eventId);
    }

    @Override
    public Map<String, ApplicationState> getApplicationStates(UUID eventId) {
        ApplicationState state = this.appStates.get(eventId);
        if (state == null) {
            return Collections.emptyMap();
        } else {
            return Map.of(this.application, state);
        }
    }

    @Override
    public void add(LifecycleAction action) {
        // Check that the action does not have an already known Event ID
        // Note that we specifically permit duplicate events to ensure idempotency
        if (this.events.containsKey(action.getEventId())) {
            if (!Objects.equals(action, this.events.get(action.getEventId()))) {
                throw new IllegalStateException(
                        "Lifecycle Action Event " + action.getEventId() + " is already known to this state store");
            } else {
                // If this was a duplicate event we already have updated our state store with it so we can ignore this
                return;
            }
        }
        this.events.put(action.getEventId(), action);

        DistributionLifecycleState current = this.getLifecycleState(action.getDistributionId());
        DistributionLifecycleState target = action.getState().getTo();
        if (!current.canTransition(target)) {
            throw new IllegalStateException(
                    "Distribution Lifecycle state transition from " + current + " to " + target + " is not permitted");
        }
        this.distributions.put(action.getDistributionId(), target);
    }

    @Override
    public void add(String application, LifecycleAcknowledgement ack) {
        if (!Objects.equals(application, this.application)) {
            // Ignore any application other than ourselves
            return;
        }
        // Don't permit acknowledgements for events we aren't aware of
        if (!this.events.containsKey(ack.getEventId())) {
            throw new IllegalStateException(
                    "Lifecycle Action Event " + ack.getEventId() + " is not known to this state store so cannot track application state against this event");
        }

        ApplicationState current = this.getApplicationState(ack.getEventId(), application);
        ApplicationState target = getTargetState(ack, current);

        // Actually update the application state for the event
        this.appStates.put(ack.getEventId(), target);
    }

    /**
     * Given a lifecycle acknowledgement get the target state, plus validate that the target state is valid based on our
     * current known state for the given application
     *
     * @param ack     Acknowledgement
     * @param current Current application state
     * @return Target state
     * @throws IllegalStateException Thrown if the target state is not a valid state based on our current known state
     *                               for the given application
     */
    protected ApplicationState getTargetState(LifecycleAcknowledgement ack,
                                              ApplicationState current) {
        ApplicationState target = ack.getState().getApp();

        // Verify the state transition is legal
        if (current == null) {
            if (target != ApplicationState.Requested) {
                throw new IllegalStateException(
                        "Requested MUST be the initial state for application acknowledgements");
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
    public List<LifecycleAction> activeEvents() {
        return this.events.values().stream().filter(e -> {
            ApplicationState state = this.getApplicationState(e.getEventId(), this.application);
            if (state == null) {
                return true;
            } else {
                return state != ApplicationState.Completed;
            }
        }).toList();
    }

    @Override
    public DistributionLifecycleState getLifecycleState(String distributionId) {
        return this.distributions.getOrDefault(distributionId, DistributionLifecycleState.Unregistered);
    }

    @Override
    public Map<String, DistributionLifecycleState> getLifecycleStates() {
        return Collections.unmodifiableMap(this.distributions);
    }
}
