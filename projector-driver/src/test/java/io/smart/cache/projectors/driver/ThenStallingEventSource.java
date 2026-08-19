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
import org.awaitility.Awaitility;

import java.time.Duration;

/**
 * An event source that yields a fixed number of events as fast as it can and then stalls permanently, reporting a fixed
 * number of events as still remaining
 * <p>
 * Unlike {@link RemainingInfiniteEventSource}, which randomises its remaining count, this makes the driver's stall
 * handling deterministic.  In particular it guarantees the observed processing rate exceeds the reported remaining
 * events, which is the condition under which the driver issues a processing speed warning.
 * </p>
 * <p>
 * Note that once stalling a {@link #poll(Duration)} always runs for the full poll timeout and does not abort early if
 * the thread is interrupted, so keep the poll timeout short in tests that use this source or cancellation will be slow
 * to take effect.
 * </p>
 */
public class ThenStallingEventSource extends InfiniteEventSource {

    private final int eventsBeforeStall;
    private final long remainingWhenStalled;

    /**
     * Creates a new event source that stalls after yielding some events
     *
     * @param eventsBeforeStall    How many events to yield before stalling
     * @param remainingWhenStalled How many events the source claims are remaining
     */
    public ThenStallingEventSource(int eventsBeforeStall, long remainingWhenStalled) {
        super("Event %,d", 0);
        this.eventsBeforeStall = eventsBeforeStall;
        this.remainingWhenStalled = remainingWhenStalled;
    }

    @Override
    public boolean availableImmediately() {
        // Once we're stalling we have to report that nothing is immediately available, otherwise the driver aborts on
        // the grounds that we told it events were available and then failed to produce them
        return !isStalling() && super.availableImmediately();
    }

    @Override
    public Event<Integer, String> poll(Duration timeout) {
        if (isStalling()) {
            // Mirrors a real event source blocking for the whole poll timeout and then yielding nothing.  There's no
            // condition to wait on here, we're deliberately burning the timeout, so the wait is expressed as a poll
            // delay followed by a condition that's trivially satisfied once it has elapsed.  Polling in the calling
            // thread keeps this on the driver's thread rather than spawning a thread per poll.
            // NB - Unlike a Thread.sleep() this does not abort early on interruption, it merely leaves the interrupt
            //      flag set, hence the short poll timeouts used by the tests that rely on this source.
            Awaitility.await("Poll timeout to elapse without any events being produced")
                      .pollInSameThread()
                      .pollDelay(timeout)
                      .atMost(timeout.plusSeconds(1))
                      .until(() -> true);
            return null;
        }
        return super.poll(timeout);
    }

    @Override
    public Long remaining() {
        return this.remainingWhenStalled;
    }

    private boolean isStalling() {
        return eventsYielded() >= this.eventsBeforeStall;
    }
}
