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
package io.telicent.smart.cache.actions.tracker;

import io.telicent.smart.cache.actions.tracker.listeners.ActionTransitionListener;
import io.telicent.smart.cache.actions.tracker.model.ActionState;
import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * Provides a basic state machine for tracking the state of ongoing actions
 * <p>
 * Derived implementations can register one/more listeners that are triggered when a state transition happens.
 * </p>
 */
@ToString
public class SimpleActionTracker implements ActionTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleActionTracker.class);

    protected final List<ActionTransitionListener> listeners;
    @Getter
    protected final String application;
    @Getter
    protected volatile ActionState state = ActionState.STARTING;
    @ToString.Exclude
    protected final Object transitionLock = new Object();
    @Getter
    protected volatile String action = null;

    /**
     * Creates a new simple tracker with no listeners
     */
    public SimpleActionTracker(String application) {
        this(application, null);
    }

    /**
     * Creates a new simple tracker with the supplied listeners
     *
     * @param application Application name
     * @param listeners Listeners
     */
    public SimpleActionTracker(String application, List<ActionTransitionListener> listeners) {
        this.listeners = Objects.requireNonNullElse(listeners, Collections.emptyList());
        if (StringUtils.isBlank(application)) {
            throw new IllegalArgumentException("application cannot be null or empty");
        }
        this.application = application;
    }

    /**
     * Tells the tracker that application start up is complete
     * <p>
     * <strong>SHOULD</strong> only be called once by an application, subsequent calls are generally ignored
     * </p>
     */
    @Override
    public final void startupComplete() {
        attemptTransition(ActionState.READY);
    }

    private void attemptTransition(ActionState newState) {
        synchronized (this.transitionLock) {
            if (ActionState.canTransition(this.state, newState)) {
                updateState(newState);
            } else {
                throw illegalTransition(newState);
            }
        }
    }

    private IllegalStateException illegalTransition(ActionState target) {
        return new IllegalStateException(
                "Action Tracker in state " + this.state + ", transition to state " + target + " not currently permitted");
    }

    private void updateState(ActionState newState) {
        ActionState oldState = state;
        this.state = newState;
        ActionTransition transition =
                ActionTransition.builder()
                                .id(UUID.randomUUID())
                                .application(this.application)
                                .from(oldState)
                                .to(newState)
                                .action(this.action)
                                .timestamp(Date.from(Instant.now()))
                                .build();
        this.onTransition(transition);

    }

    @Override
    public final void start(String action) {
        synchronized (this.transitionLock) {
            if (this.action != null) {
                throw new IllegalStateException(
                        String.format("Cannot start action '%s' as action '%s' is already in progress", action,
                                      this.action));
            }
            this.action = action;
            attemptTransition(ActionState.PROCESSING);
        }
    }

    @Override
    public final void finish(String action) {
        synchronized (this.transitionLock) {
            if (this.state != ActionState.PROCESSING) {
                throw new IllegalStateException("Can't finish action from state " + this.state);
            }
            if (!Objects.equals(this.action, action)) {
                throw new IllegalStateException(
                        String.format("Cannot finish action '%s' as action '%s' is currently in progress", action,
                                      this.action));
            }
            attemptTransition(ActionState.READY);
            this.action = null;
        }
    }

    /**
     * Called as part of {@link #updateState(ActionState)} and handles calling the registered listeners to allow them to
     * respond to the transition appropriately, including handling any errors they might produce
     *
     * @param transition Transition
     */
    private void onTransition(ActionTransition transition) {
        // Apply all registered listeners
        for (ActionTransitionListener listener : this.listeners) {
            // Ignore any null listeners
            if (listener == null) {
                continue;
            }
            try {
                listener.accept(this, transition);
            } catch (Throwable e) {
                LOGGER.warn("onTransition from {} to {} listener {} failed: {}", transition.getFrom(),
                            transition.getTo(), listener, e.getMessage());
            }
        }
    }

    /**
     * Closes the action tracker
     * <p>
     * This implementation is a no-op as it holds only in-memory state, derived implementations may have external
     * resources they need to close so should override this method and clean up accordingly.
     * </p>
     */
    @Override
    public void close() {
        // Does nothing by default
    }

}
