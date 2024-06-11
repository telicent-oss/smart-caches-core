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
package io.telicent.smart.cache.sources.memory;

import io.telicent.smart.cache.sources.AbstractEventSourceTests;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Test
public class TestInMemoryEventSource extends AbstractEventSourceTests<Integer, String> {
    @Override
    protected EventSource<Integer, String> createEmptySource() {
        return new InMemoryEventSource<>(Collections.emptyList());
    }

    @Override
    protected EventSource<Integer, String> createSource(Collection<Event<Integer, String>> events) {
        return new InMemoryEventSource<>(events);
    }

    @Override
    protected Collection<Event<Integer, String>> createSampleData(int size) {
        AtomicInteger counter = new AtomicInteger(0);
        return createSampleStrings(size).stream()
                                        .map(s -> new SimpleEvent<>(Collections.emptyList(), counter.incrementAndGet(),
                                                                    s))
                                        .collect(Collectors.toList());
    }

    @Override
    public boolean guaranteesImmediateAvailability() {
        return true;
    }

    @Override
    public boolean isUnbounded() {
        return false;
    }
}
