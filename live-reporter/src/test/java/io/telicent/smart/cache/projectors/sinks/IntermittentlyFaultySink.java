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
import io.telicent.smart.cache.projectors.SinkException;

import java.util.Random;

/**
 * A sink for testing that randomly faults when trying to send items onwards to its destination based on a configurable
 * fault percentage
 *
 * @param <T> Item type
 */
public class IntermittentlyFaultySink<T> extends AbstractTransformingSink<T, T> {

    private final Random random = new Random();
    private final double faultPercentage;

    /**
     * Creates a new intermittently faulty sink
     *
     * @param destination     Destination
     * @param faultPercentage Fault percentage as a value between 0.0 and 1.0, e.g. 0.7 means 70% of items will produce
     *                        a fault ({@link SinkException}) when trying to {@link #send(Object)} them.  For values
     *                        outside the range then the sink always faults.
     */
    public IntermittentlyFaultySink(Sink<T> destination, double faultPercentage) {
        super(destination);
        this.faultPercentage = faultPercentage;
    }

    @Override
    public void send(T item) throws SinkException {
        // If fault percentage set outside allowed range then always faulty
        if (this.faultPercentage < 0.0 || this.faultPercentage >= 1.0) {
            throw new SinkException("always faulty");
        } else if (this.faultPercentage == 0.0) {
            // If set to 0 then never faulty
            super.send(item);
            return;
        }
        // Otherwise generate a random number and throw error if less than configured fault percentage
        double test = random.nextDouble();
        if (test <= this.faultPercentage) {
            throw new SinkException("sometimes faulty");
        }
        super.send(item);
    }

    @Override
    protected T transform(T item) {
        return item;
    }
}
