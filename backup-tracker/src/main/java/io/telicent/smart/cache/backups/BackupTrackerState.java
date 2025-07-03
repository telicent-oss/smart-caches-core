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
package io.telicent.smart.cache.backups;


/**
 * Possible Backup Tracker States
 */
public enum BackupTrackerState {
    /**
     * Application is starting up
     */
    STARTING,
    /**
     * Application is ready
     */
    READY,
    /**
     * Application is processing a backup operation
     */
    BACKING_UP,
    /**
     * Application is processing a restore operation
     */
    RESTORING;

    /**
     * Checks whether a transition from one state to another is permitted
     *
     * @param current Current state
     * @param target  Target state to transition to
     * @return True if legal transition, false otherwise
     */
    public static boolean canTransition(BackupTrackerState current, BackupTrackerState target) {
        return switch (current) {
            // From STARTING state we can transition to any other state, including ourselves if we're still starting up
            case STARTING -> true;
            // From READY state we can transition to anything other than STARTING, including ourselves
            case READY -> target != BackupTrackerState.STARTING;
            // From BACKING_UP/RESTORING we can only transition back to READY
            case BACKING_UP, RESTORING -> target == READY;
        };
    }
}
