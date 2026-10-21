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
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A toy data store to demonstrate that long-running listeners eventually complete
 */
// java:S131 - switch is a deliberate partial guard, not exhaustive dispatch
@SuppressWarnings("java:S131")
public class DataStore {

    private final Map<String, AtomicLong> data = new ConcurrentHashMap<>();

    public void addData(String distributionId, long quantity) {
        // We "add" data by incrementing our data counter to indicate the quantity of fake data we have for this
        // distribution
        this.data.computeIfAbsent(distributionId, k -> new AtomicLong(0)).addAndGet(quantity);
    }

    public void deleteData(String distributionId) {
        this.data.remove(distributionId);
    }

    public boolean hasData(String distributionId) {
        return this.data.containsKey(distributionId);
    }

    @AllArgsConstructor
    public static final class Listener implements DistributionLifecycleListener {

        @NonNull
        private final DataStore store;

        @NonNull
        private final CountDownLatch allowDeletion;

        @Override
        public void accept(LifecycleAction action) {
            // Keep deletion in progress until the test has verified the intermediate state.
            switch (action.getState().getTo()) {
                case Registered -> this.store.addData(action.getDistributionId(), 2_000_000);
                case Deleted -> {
                    try {
                        this.allowDeletion.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Interrupted while waiting to delete test data", e);
                    }
                    this.store.deleteData(action.getDistributionId());
                }
            }
        }
    }
}
