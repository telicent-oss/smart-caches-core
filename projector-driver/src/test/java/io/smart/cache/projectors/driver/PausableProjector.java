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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A projector that pauses at a safe point on request, mirroring how downstream projectors coordinate a pause with
 * another thread that needs the projection quiesced, e.g. a dataset restore that must not run while events are being
 * applied.
 * <p>
 * The pause point can only be reached on the projector's own thread, so this only works if the driver hands control
 * back to the projector while no events are flowing.
 * </p>
 */
public class PausableProjector<I, O> implements StallAwareProjector<I, O> {

    private final Object pauseMonitor = new Object();
    private volatile boolean paused = false;
    private volatile boolean atPausePoint = false;

    @Override
    public void project(I input, Sink<O> sink) {
        awaitResumeIfPaused();
    }

    @Override
    public void stalled(Sink<O> sink) {
        awaitResumeIfPaused();
    }

    @Override
    public void idle(Sink<O> sink) {
        awaitResumeIfPaused();
    }

    /**
     * Requests that the projector pauses at its next safe point, does not wait for it to do so
     */
    public void requestPause() {
        synchronized (this.pauseMonitor) {
            this.paused = true;
            this.pauseMonitor.notifyAll();
        }
    }

    /**
     * Releases a previously requested pause
     */
    public void requestResume() {
        synchronized (this.pauseMonitor) {
            this.paused = false;
            this.pauseMonitor.notifyAll();
        }
    }

    /**
     * Whether the projector thread has actually reached its pause point
     *
     * @return True if paused at a safe point, false otherwise
     */
    public boolean isAtPausePoint() {
        return this.atPausePoint;
    }

    /**
     * Waits for the projector to reach its pause point
     *
     * @param timeout Maximum time to wait
     * @return True if the pause point was reached within the timeout, false otherwise
     * @throws InterruptedException Thrown if interrupted while waiting
     */
    public boolean awaitPausePoint(Duration timeout) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + timeout.toNanos();
        synchronized (this.pauseMonitor) {
            while (!this.atPausePoint) {
                long remainingNanos = deadlineNanos - System.nanoTime();
                if (remainingNanos <= 0) {
                    return false;
                }
                // NB - Releases the monitor while waiting, so the projector thread can still reach its pause point, and
                //      is woken as soon as it signals that it has
                TimeUnit.NANOSECONDS.timedWait(this.pauseMonitor, remainingNanos);
            }
            return true;
        }
    }

    private void awaitResumeIfPaused() {
        if (!this.paused) {
            return;
        }
        synchronized (this.pauseMonitor) {
            this.atPausePoint = true;
            // Signal anyone in awaitPausePoint() that we've reached the pause point
            this.pauseMonitor.notifyAll();
            try {
                while (this.paused) {
                    this.pauseMonitor.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                this.atPausePoint = false;
                this.pauseMonitor.notifyAll();
            }
        }
    }
}
