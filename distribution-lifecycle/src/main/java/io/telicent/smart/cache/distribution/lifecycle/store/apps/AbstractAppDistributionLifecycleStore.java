package io.telicent.smart.cache.distribution.lifecycle.store.apps;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.store.ApplicationAckStateStore;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleEventStore;
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
        implements ApplicationAckStateStore, DistributionLifecycleEventStore, DistributionLifecycleStateStore {

    private final String application;
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
    public void setApplicationState(UUID eventId, String application, ApplicationState state) {
        if (!Objects.equals(this.application, application)) {
            // Ignored
            return;
        }
        ApplicationState current = this.getApplicationState(eventId, application);

        // Verify the state transition is legal
        if (current == null) {
            if (state != ApplicationState.Requested) {
                throw new IllegalArgumentException(
                        "Requested MUST be the initial state for application acknowledgements");
            }
        } else {
            if (current.canTransition(state)) {
                throw new IllegalArgumentException(
                        "An application state transition from " + current + " to " + state + " is not permitted");
            }
        }

        // Actually update the application state for the event
        this.appStates.put(eventId, state);
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
        if (this.events.containsKey(action.getEventId())) {
            throw new IllegalStateException(
                    "Lifecycle Action Event " + action.getEventId() + " is already known to this state store");
        }
        this.events.put(action.getEventId(), action);
    }

    @Override
    public void add(LifecycleAcknowledgement ack) {
        this.setApplicationState(ack.getEventId(), this.application, ack.getState().getApp());
    }

    @Override
    public List<LifecycleAction> activeEvents() {
        return this.events.values().stream().filter(e -> {
            ApplicationState state = this.getApplicationState(e.getEventId(), this.application);
            if (state == null) {
                return false;
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
    public void setLifecycleState(String distributionId, DistributionLifecycleState state) {
        DistributionLifecycleState current = this.getLifecycleState(distributionId);
        if (!current.canTransition(state)) {
            throw new IllegalArgumentException(
                    "Distribution Lifecycle state transition from " + current + " to " + state + " is not permitted");
        }
        this.distributions.put(distributionId, current);
    }

    @Override
    public Map<String, DistributionLifecycleState> getLifecycleStates() {
        return Collections.unmodifiableMap(this.distributions);
    }
}
