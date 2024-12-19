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

import io.telicent.smart.cache.projectors.Sink;
import lombok.ToString;

/**
 * A test sink implementation that introduces delays into proceedings
 *
 * @param <T> Item type
 */
@ToString(callSuper = true)
public class DelaySink<T> extends AbstractTransformingSink<T, T> {
    private final long delay;

    /**
     * Creates a new sink
     *
     * @param destination Destination sink
     * @param delay       Delay in milliseconds
     */
    public DelaySink(Sink<T> destination, long delay) {
        super(destination);
        if (delay < 0) {
            throw new IllegalArgumentException("Delay must be >= 0");
        }
        this.delay = delay;
    }

    @Override
    protected T transform(T t) {
        if (this.delay > 0) {
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                // Ignored, just introducing a delay for testing purposes
            }
        }
        return t;
    }
}
