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

/**
 * A projector that pauses at a safe point on request, mirroring how downstream projectors coordinate a pause with
 * another thread that needs the projection quiesced, e.g. a dataset restore that must not run while events are being
 * applied.
 * <p>
 * The pause point can only be reached on the projector's own thread, so this only works if the driver hands control
 * back to the projector while no events are flowing.
 * </p>
 */
public class PausableProjector<TInput, TOutput> implements StallAwareProjector<TInput, TOutput> {

    private final Object pauseMonitor = new Object();
    private volatile boolean paused = false;
    private volatile boolean atPausePoint = false;

    @Override
    public void project(TInput input, Sink<TOutput> sink) {
        awaitResumeIfPaused();
    }

    @Override
    public void stalled(Sink<TOutput> sink) {
        awaitResumeIfPaused();
    }

    @Override
    public void idle(Sink<TOutput> sink) {
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
        while (!this.atPausePoint) {
            if (System.nanoTime() >= deadlineNanos) {
                return false;
            }
            Thread.sleep(20);
        }
        return true;
    }

    private void awaitResumeIfPaused() {
        if (!this.paused) {
            return;
        }
        synchronized (this.pauseMonitor) {
            this.atPausePoint = true;
            try {
                while (this.paused) {
                    this.pauseMonitor.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                this.atPausePoint = false;
            }
        }
    }
}
