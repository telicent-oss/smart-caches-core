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
package io.telicent.smart.cache.distribution.lifecycle;

/**
 * Possible Application States while processing a lifecycle event
 */
public enum ApplicationState {
    /**
     * Application has received the request to process a lifecycle event
     */
    Requested,
    /**
     * Application is in the process of applying a lifecycle event
     */
    InProgress,
    /**
     * Application has completed the application of a lifecycle event
     */
    Completed,
    /**
     * Application has failed to apply a lifecycle event
     */
    Failed;

    /**
     * Checks whether a transition from the current state to another is permitted
     *
     * @param target Target state
     * @return True if a legal transition, false otherwise
     */
    public boolean canTransition(ApplicationState target) {
        return canTransition(this, target);
    }

    /**
     * Checks whether a transition from one state to another is permitted
     *
     * @param current Current state
     * @param target  Target state
     * @return True if a legal transition, false otherwise
     */
    public static boolean canTransition(ApplicationState current, ApplicationState target) {
        if (current == null || target == null) {
            return false;
        } else if (current == target) {
            return true;
        }

        return switch (current) {
            // Requested can transition to any later reachable state
            case Requested -> target == InProgress || target == Completed || target == Failed;
            // InProgress can transition to Completed/Failed
            case InProgress -> target == Completed || target == Failed;
            // Failed can retry from InProgress or report a final completion
            case Failed -> target == InProgress || target == Completed;
            // Completed is the terminal state and no transitions are permitted
            case Completed -> false;
        };
    }
}
