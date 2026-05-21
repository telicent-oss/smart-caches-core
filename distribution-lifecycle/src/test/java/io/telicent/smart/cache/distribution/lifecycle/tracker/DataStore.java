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
import org.apache.commons.lang3.RandomUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A toy data store to demonstrate that long-running listeners eventually complete
 */
public class DataStore {

    private final Map<String, AtomicLong> data = new ConcurrentHashMap<>();

    public void addData(String distributionId, long quantity) {
        // We "add" data by incrementing our data counter to indicate the quantity of fake data we have for this
        // distribution
        this.data.computeIfAbsent(distributionId, k -> new AtomicLong(0)).addAndGet(quantity);
    }

    public void deleteData(String distributionId) {
        AtomicLong data = this.data.get(distributionId);
        if (data == null) {
            return;
        } else {
            // We "delete" data by slowly decrementing the data counter to indicate the quantity of fake data we have
            // for this distribution.  We simulate deletion taking time by sleeping briefly between each decrement.
            while (data.get() > 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                data.addAndGet(-10_000);
            }
            this.data.remove(distributionId);
        }
    }

    public boolean hasData(String distributionId) {
        return this.data.containsKey(distributionId);
    }

    @AllArgsConstructor
    public static final class Listener implements DistributionLifecycleListener {

        @NonNull
        private final DataStore store;

        @Override
        public void accept(LifecycleAction action) {
            // This listener fakes adding up to 2-5 million data items for a distribution
            // When deleteData() is called the sleep logic means this will take 2-5 seconds to delete
            switch (action.getState().getTo()) {
                case Registered -> this.store.addData(action.getDistributionId(),
                                                      RandomUtils.insecure().randomLong(2_000_000, 5_000_000));
                case Deleted -> this.store.deleteData(action.getDistributionId());
            }
        }
    }
}
