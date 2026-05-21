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
package io.telicent.smart.cache.distribution.lifecycle.store.global;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.store.AbstractDistributionLifecycleStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract global distribution lifecycle state store intended to allow a central service to track lifecycle events and
 * system-wide acknowledgements of those events.
 * <p>
 * This is intended as an abstract base class for concrete implementations to derive from.  This implementation provides
 * in-memory state tracking, but it is expected that derived implementations would back the state store with persistent
 * storage so the central service doesn't need to re-read all lifecycle events at each start up.
 * </p>
 */
public abstract class AbstractGlobalDistributionLifecycleStore extends AbstractDistributionLifecycleStore {

    /**
     * In-memory tracker of Lifecycle Events to Application States
     */
    protected final Map<UUID, Map<String, ApplicationState>> appStates = new ConcurrentHashMap<>();

    @Override
    public ApplicationState getApplicationState(UUID eventId, String application) {
        return getApplicationStates(eventId).get(application);
    }

    @Override
    public Map<String, ApplicationState> getApplicationStates(UUID eventId) {
        return Collections.unmodifiableMap(this.appStates.getOrDefault(eventId, Collections.emptyMap()));
    }

    @Override
    public void add(String application, LifecycleAcknowledgement ack) {
        // Don't permit acknowledgements for events we aren't aware of
        if (!this.events.containsKey(ack.getEventId())) {
            throw new IllegalStateException(
                    "Lifecycle Action Event " + ack.getEventId() + " is not known to this state store so cannot track application state against this event");
        }

        ApplicationState current = this.getApplicationState(ack.getEventId(), application);
        ApplicationState target = getTargetState(ack, current);

        // Actually update the application state for the event
        this.appStates.computeIfAbsent(ack.getEventId(), ignored -> new ConcurrentHashMap<>()).put(application, target);
    }

    @Override
    public List<LifecycleAction> activeEvents() {
        return this.events.values().stream().filter(e -> {
            Map<String, ApplicationState> states = this.getApplicationStates(e.getEventId());
            if (states.isEmpty()) {
                return true;
            } else {
                return states.values().stream().anyMatch(state -> state != ApplicationState.Completed);
            }
        }).toList();
    }

}
