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
package io.telicent.smart.cache.sources.combiner;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import lombok.Builder;
import lombok.experimental.Delegate;

import java.time.Duration;

@Builder
public class SlowEventSource<TKey, TValue> implements EventSource<TKey, TValue> {

    @Delegate
    private final EventSource<TKey, TValue> delegate;
    private final Duration pollDelay;

    @Override
    public Event<TKey, TValue> poll(Duration timeout) {
        long start = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();
        try {
            Thread.sleep(Math.min(this.pollDelay.toMillis(), timeoutMillis));
        } catch (InterruptedException e) {
            // Ignored
        }

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed >= timeoutMillis) {
            return null;
        }
        return this.delegate.poll(Duration.ofMillis(timeoutMillis - elapsed));
    }
}
