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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Registry for the singleton action tracker
 * <p>
 * For production usage each microservice <strong>SHOULD</strong> have one, and only one {@link ActionTracker} instance
 * which it utilises.  This registry provides a convenient, globally accessible location in which to hold that
 * instance and prevents applications that use this API from registering multiple trackers.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActionTrackerRegistry {

    private static ActionTracker INSTANCE = null;

    /**
     * Gets the previously registered (via {@link #setInstance(ActionTracker)}) singleton tracker instance
     *
     * @return Tracker
     */
    public static ActionTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the singleton tracker instance
     *
     * @param tracker Tracker
     */
    public static void setInstance(ActionTracker tracker) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Singleton instance already set");
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
