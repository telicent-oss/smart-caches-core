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

import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.DistributionLifecycleListener;
import lombok.AllArgsConstructor;

/**
 * A test listener that will fail some attempts to accept an action
 */
@AllArgsConstructor
public class TemporarilyFails implements DistributionLifecycleListener {

    private int failures;

    @Override
    public void accept(LifecycleAction action) {
        if (this.failures > 0) {
            this.failures--;
            throw new RuntimeException("Fails");
        }
        // Now ok to succeed
        return;
    }
}
