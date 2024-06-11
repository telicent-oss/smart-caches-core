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
package io.smart.cache.projectors.driver;

import io.telicent.smart.cache.sources.Event;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A variant on {@link InfiniteEventSource} that periodically stalls i.e. produces no new events for a set duration
 */
public class StallingInfiniteEventSource extends InfiniteEventSource {

    private final int stallFrequency;
    private final long stallDuration;
    private final AtomicInteger nextStallIn;

    /**
     * Creates a new stalling infinite event source
     *
     * @param valueFormat    Value format
     * @param stallDuration  Stall duration in milliseconds
     * @param stallFrequency How often, in terms of {@link #poll(Duration)} operations, the source will stall
     */
    public StallingInfiniteEventSource(String valueFormat, long stallDuration, int stallFrequency) {
        super(valueFormat, 0);

        this.stallDuration = stallDuration;
        this.stallFrequency = stallFrequency;
        this.nextStallIn = new AtomicInteger(stallFrequency);
    }

    @Override
    public boolean availableImmediately() {
        return this.nextStallIn.get() > 1 && super.availableImmediately();
    }

    @Override
    public Event<Integer, String> poll(Duration timeout) {
        if (this.nextStallIn.decrementAndGet() == 0) {
            // Produce a stall
            try {
                Thread.sleep(Math.min(this.stallDuration, timeout.toMillis()));
            } catch (InterruptedException e) {
                // Ignore
            }
            // Reset next stall counter
            this.nextStallIn.set(this.stallFrequency);

            // If stall was longer than poll duration return nothing
            if (timeout.toMillis() < this.stallDuration) {
                return null;
            }
        }

        return super.poll(timeout);
    }
}
