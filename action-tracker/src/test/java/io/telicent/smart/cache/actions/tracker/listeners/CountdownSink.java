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
package io.telicent.smart.cache.actions.tracker.listeners;

import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.AbstractTransformingSink;
import io.telicent.smart.cache.sources.Event;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * A sink that is eventually successful in sending an event
 */
final class CountdownSink
        extends AbstractTransformingSink<Event<UUID, ActionTransition>, Event<UUID, ActionTransition>> {
    private final int initialCount;
    private final AtomicInteger count;
    private final Supplier<RuntimeException> errorSupplier;

    public CountdownSink(Sink<Event<UUID, ActionTransition>> destination, int initialCount,
                         Supplier<RuntimeException> errorSupplier) {
        super(destination);
        this.initialCount = initialCount;
        this.count = new AtomicInteger(initialCount);
        this.errorSupplier = errorSupplier;
    }

    @Override
    protected Event<UUID, ActionTransition> transform(Event<UUID, ActionTransition> event) {
        // If count still greater than zero after counting down one then error
        if (this.count.decrementAndGet() > 0) {
            throw this.errorSupplier.get();
        }
        // Otherwise reset countdown and accept it
        this.count.set(this.initialCount);
        return event;
    }
}
