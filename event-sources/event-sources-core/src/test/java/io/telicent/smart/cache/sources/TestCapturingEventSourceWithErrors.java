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

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;

import java.util.Collection;
import java.util.Collections;

public class TestCapturingEventSourceWithErrors extends TestCapturingEventSource {
    @Override
    protected EventSource<Integer, String> createEmptySource() {
        return new CapturingEventSource<>(new InMemoryEventSource<>(Collections.emptyList()),
                                          new ErrorThrowingSink());
    }

    @Override
    protected EventSource<Integer, String> createSource(Collection<Event<Integer, String>> events) {
        return new CapturingEventSource<>(new InMemoryEventSource<>(events),
                                          new ErrorThrowingSink());
    }

    private static final class ErrorThrowingSink implements Sink<Event<Integer, String>> {

        @Override
        public void send(Event<Integer, String> item) {
            throw new SinkException("Fail");
        }
    }
}
