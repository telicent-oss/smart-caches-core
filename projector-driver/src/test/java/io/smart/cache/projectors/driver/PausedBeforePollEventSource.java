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
import io.telicent.smart.cache.sources.PausableEventSource;

import java.time.Duration;

/**
 * Reports events as immediately available, but its first few polls return nothing because it was paused between the
 * driver checking availability and polling, as can happen when another thread pauses a source with events buffered
 */
public class PausedBeforePollEventSource extends InfiniteEventSource implements PausableEventSource<Integer, String> {

    private int pausedPolls;
    private final boolean reportPaused;
    private boolean lastPollPaused;

    /**
     * @param pausedPolls  How many polls return nothing
     * @param reportPaused Whether those polls report that they were paused (if not, the source is simply lying)
     */
    public PausedBeforePollEventSource(int pausedPolls, boolean reportPaused) {
        super("Event %,d", 0);
        this.pausedPolls = pausedPolls;
        this.reportPaused = reportPaused;
    }

    @Override
    public Event<Integer, String> poll(Duration timeout) {
        if (this.pausedPolls > 0) {
            this.pausedPolls--;
            this.lastPollPaused = this.reportPaused;
            return null;
        }
        this.lastPollPaused = false;
        return super.poll(timeout);
    }

    @Override
    public void pause() {
        // Pausing is simulated by the constructor arguments
    }

    @Override
    public void resume() {
        // Pausing is simulated by the constructor arguments
    }

    @Override
    public boolean isPaused() {
        // Already resumed by the time anyone asks, the case isPaused() alone can't detect
        return false;
    }

    @Override
    public boolean wasPausedOnLastPoll() {
        return this.lastPollPaused;
    }
}
