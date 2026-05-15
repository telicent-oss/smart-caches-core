/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.cache.distribution.lifecycle;

/**
 * Possible Distribution Lifecycle states
 */
public enum DistributionLifecycleState {

    /**
     * Distribution is not yet known to the platform, its data <strong>MUST NOT</strong> be ingested.
     */
    Unregistered,
    /**
     * Distribution is registered with the platform, its data <strong>MUST</strong> be accepted for ingest but
     * <strong>MUST NOT</strong> be visible to users.
     */
    Registered,
    /**
     * Distribution is active, its data <strong>MUST</strong> be accepted for ingest and <strong>MUST</strong> be
     * visible to suitably authorised users.
     */
    Active,
    /**
     * Distribution is withdrawn, its data <strong>MUST</strong> continue to be accepted for ingest but
     * <strong>MUST NOT</strong> be visible to end users.
     */
    Withdrawn,
    /**
     * Distribution is deleted, its data <strong>MUST NOT</strong> be ingested and <strong>MUST NOT</strong> be visible
     * to users.  Also, a lifecycle aware service <strong>MUST</strong> actively start removing data belonging to that
     * distribution from its persistent storage.
     */
    Deleted;

    /**
     * Checks whether a transition from the current state to another is permitted
     *
     * @param target  Target state
     * @return True if legal transition, false otherwise
     */
    public boolean canTransition(DistributionLifecycleState target) {
        return canTransition(this, target);
    }

    /**
     * Checks whether a transition from one state to another is permitted
     *
     * @param current Current state
     * @param target  Target state
     * @return True if legal transition, false otherwise
     */
    public static boolean canTransition(DistributionLifecycleState current, DistributionLifecycleState target) {
        if (current == null || target == null) {
            return false;
        } else if (current == target) {
            return true;
        }

        return switch (current) {
            // An Unregistered distribution can transition to any later reachable state
            case Unregistered -> target == Registered || target == Active || target == Withdrawn || target == Deleted;
            // A Registered/Withdrawn distribution can transition to any state reachable from there
            case Registered -> target == Active || target == Withdrawn || target == Deleted;
            case Withdrawn -> target == Active || target == Deleted;
            // An Active distribution can transition to Withdrawn/Deleted
            case Active -> target == Withdrawn || target == Deleted;
            // A Deleted distribution is permanently deleted and cannot transition to another state
            case Deleted -> false;
        };
    }
}
