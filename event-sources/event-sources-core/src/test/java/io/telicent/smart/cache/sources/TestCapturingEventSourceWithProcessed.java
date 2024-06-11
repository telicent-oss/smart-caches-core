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
package io.telicent.smart.cache.sources;

import io.telicent.smart.cache.projectors.sinks.events.EventProcessedSink;
import io.telicent.smart.cache.sources.CapturingEventSource;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.TestCapturingEventSource;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;

import java.util.Collection;
import java.util.Collections;

public class TestCapturingEventSourceWithProcessed extends TestCapturingEventSource {
    @Override
    protected EventSource<Integer, String> createEmptySource() {
        return new CapturingEventSource<>(new InMemoryEventSource<>(Collections.emptyList()),
                                          EventProcessedSink.<Integer, String>create().build());
    }

    @Override
    protected EventSource<Integer, String> createSource(Collection<Event<Integer, String>> events) {
        return new CapturingEventSource<>(new InMemoryEventSource<>(events),
                                          EventProcessedSink.<Integer, String>create().build());
    }
}
