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
package io.telicent.smart.cache.distribution.lifecycle.events.listeners;

import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;

/**
 * Interface for listeners to distribution lifecycle events, used by
 * {@link io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTracker}
 * <p>
 * A listener <strong>MUST</strong> process action events in an idempotent manner as if the application fails to
 * complete an action then it will be periodically retriggered so a listener <strong>MAY</strong> receive the same
 * action multiple times.
 * </p>
 */
public interface DistributionLifecycleListener {

    /**
     * Accepts a lifecycle action event
     *
     * @param action Lifecycle action
     */
    void accept(LifecycleAction action);
}
