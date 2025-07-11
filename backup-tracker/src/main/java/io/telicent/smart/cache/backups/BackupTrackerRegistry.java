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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Registry for the singleton backup tracker
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackupTrackerRegistry {

    private static BackupTracker INSTANCE = null;

    /**
     * Gets the previously registered (via {@link #setInstance(BackupTracker)}) singleton tracker instance
     *
     * @return Tracker
     */
    public static BackupTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the singleton tracker instance
     *
     * @param tracker Tracker
     */
    public static void setInstance(BackupTracker tracker) {
        if (INSTANCE != null) {
            throw new IllegalStateException("BackupTracker already set");
        }
        INSTANCE = Objects.requireNonNull(tracker, "Cannot set the singleton tracker to null");
    }

    /**
     * Resets the singleton instance, only intended for testing usage
     */
    static void reset() {
        if (INSTANCE != null) {
            INSTANCE.close();
        }
        INSTANCE = null;
    }
}
