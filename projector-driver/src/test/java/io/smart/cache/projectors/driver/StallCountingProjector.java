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

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.driver.StallAwareProjector;

import java.util.concurrent.atomic.AtomicLong;

public class StallCountingProjector<TInput, TOutput> implements StallAwareProjector<TInput, TOutput> {

    private final AtomicLong stalls = new AtomicLong(0);

    @Override
    public void stalled(Sink<TOutput> sink) {
        this.stalls.incrementAndGet();
    }

    public long getStalls() {
        return this.stalls.get();
    }

    @Override
    public void project(TInput tInput, Sink<TOutput> sink) {

    }
}
