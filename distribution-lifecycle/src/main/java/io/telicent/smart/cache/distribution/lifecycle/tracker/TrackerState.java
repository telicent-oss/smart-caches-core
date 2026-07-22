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
package io.telicent.smart.cache.distribution.lifecycle.tracker;

/**
 * Possible states of a {@link DistributionLifecycleTracker} used by applications to determine whether their tracker is
 * ready for usage, and thus they can start using it to make distribution lifecycle decisions
 */
public enum TrackerState {
    /**
     * The tracker has been created but not yet starting up
     */
    CREATED,
    /**
     * The tracker is starting up and performing initial checks
     */
    STARTING,
    /**
     * The tracker is running and ready for usage
     */
    RUNNING,
    /**
     * The tracker has failed and should not be used
     */
    FAILED,
    /**
     * The tracker is in the process of being closed and should no longer be used
     */
    CLOSING,
    /**
     * The tracker has been explicitly closed and can no longer be used
     */
    CLOSED
}
