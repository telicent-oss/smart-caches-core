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
import io.telicent.smart.cache.distribution.lifecycle.events.IngestStatus;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.store.AbstractDistributionLifecycleStore;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        extends AbstractDistributionLifecycleStore {

    /**
     * Application identifier for the application whose states we are tracking
     */
    protected final String application;
    /**
     * In-memory tracker for the applications states
     */
    protected final Map<UUID, ApplicationState> appStates = new ConcurrentHashMap<>();

    /**
     * Creates a new application scoped distribution lifecycle state store
     *
     * @param app Application Identifier for the application this store is scoped to
     */
    protected AbstractAppDistributionLifecycleStore(String app) {
        if (StringUtils.isBlank(app)) {
            throw new IllegalArgumentException("App identifier cannot be empty or null");
        }
        this.application = app;
    }

    @Override
    public ApplicationState getApplicationState(UUID eventId, String application) {
        ensureNotClosed();
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        } else if (StringUtils.isBlank(application)) {
            throw new IllegalArgumentException("Application ID cannot be null/blank");
        }
        if (!Objects.equals(this.application, application)) {
            return null;
        }
        return this.appStates.get(eventId);
    }

    @Override
    public Map<String, ApplicationState> getApplicationStates(UUID eventId) {
        ensureNotClosed();
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        ApplicationState state = this.appStates.get(eventId);
        if (state == null) {
            return Collections.emptyMap();
        } else {
            return Map.of(this.application, state);
        }
    }

    @Override
    public void add(String application, LifecycleAcknowledgement ack) {
        ensureNotClosed();
        if (StringUtils.isBlank(application)) {
            throw new IllegalArgumentException("Application ID cannot be null/blank");
        }
        if (null == ack) {
            throw new IllegalArgumentException("Acknowledgement cannot be null");
        }
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

    @Override
    public void add(String application, IngestStatus status) {
        ensureNotClosed();
        if (StringUtils.isBlank(application)) {
            throw new IllegalArgumentException("Application ID cannot be null/blank");
        }
        if (null == status) {
            throw new IllegalArgumentException("Ingest status cannot be null");
        }
        if (!Objects.equals(application, this.application)) {
            return;
        }
        super.add(application, status);
    }

    @Override
    public List<LifecycleAction> activeEvents() {
        ensureNotClosed();
        return this.events.values().stream().filter(e -> {
            ApplicationState state = this.getApplicationState(e.getEventId(), this.application);
            if (state == null) {
                return true;
            } else {
                return state != ApplicationState.Completed;
            }
        }).toList();
    }
}
