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
import io.telicent.smart.cache.sources.EventSourceException;

import java.time.Duration;

/**
 * An event source that always throws a {@link EventSourceException}
 */
public class ErroringEventSource extends InfiniteEventSource {

    public ErroringEventSource() {
        super("Event %,d", 0);
    }

    @Override
    public Event<Integer, String> poll(Duration timeout) {
        throw new EventSourceException("Error");
    }
}
