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
package io.telicent.smart.cache.projectors.sinks;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A shared close latch is used to track open and close operations to allow a {@link CloseLatchSink} to share a
 * destination sink across multiple sink pipelines
 */
public final class CloseLatch {

    private final AtomicInteger latch = new AtomicInteger(0);

    /**
     * Increases the latch counter by 1
     */
    public void open() {
        this.latch.incrementAndGet();
    }

    /**
     * Reduces the latch counter by 1 returning {@code true} if the count has reached zero
     *
     * @return True if latch count reached zero, false otherwise
     */
    public boolean close() {
        return this.latch.decrementAndGet() <= 0;
    }

    /**
     * Indicates whether the latch is considered closed
     *
     * @return True if closed, false if open
     */
    public boolean isClosed() {
        return this.latch.get() <= 0;
    }
}
