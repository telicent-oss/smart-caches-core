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
package io.telicent.smart.cache.sources;

/**
 * An event source whose delivery of events can be paused and later resumed without disconnecting from the underlying
 * source.
 * <p>
 * While paused, {@link #poll(java.time.Duration)} returns {@code null} (no event available) and no further events are
 * delivered, but the source keeps its connection to the underlying event stream alive so that resuming continues from
 * exactly where delivery stopped. Events the source had already buffered are retained and delivered after resuming.
 * For example a Kafka source keeps calling {@code poll()} on its consumer, with its partitions paused, so it stays a
 * member of its consumer group however long it is paused.
 * </p>
 * <p>
 * This is intended for callers that need to stop processing temporarily, e.g. while a maintenance operation runs
 * against the data being projected into, but must keep calling {@link #poll(java.time.Duration)} while paused.
 * </p>
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
// java:S119 - TKey/TValue generic naming convention is used across the codebase
@SuppressWarnings("java:S119")
public interface PausableEventSource<TKey, TValue> extends EventSource<TKey, TValue> {

    /**
     * Pauses delivery of events, taking effect from the next call to {@link #poll(java.time.Duration)}
     * <p>
     * May be called from any thread. Calling this when already paused has no effect.
     * </p>
     */
    void pause();

    /**
     * Resumes delivery of events, taking effect from the next call to {@link #poll(java.time.Duration)}
     * <p>
     * May be called from any thread. Calling this when not paused has no effect.
     * </p>
     */
    void resume();

    /**
     * @return Whether delivery of events is currently paused
     */
    boolean isPaused();

    /**
     * Whether the most recent call to {@link #poll(java.time.Duration)} returned {@code null} because delivery was
     * paused, rather than because no events were available
     * <p>
     * Intended for the polling thread, immediately after a {@code poll()}. Unlike {@link #isPaused()} this reflects
     * the state that {@code poll()} actually saw, so it is still accurate if the source was paused (or resumed) after an
     * earlier {@link #availableImmediately()} call, or around the {@code poll()} itself.
     * </p>
     *
     * @return Whether the last poll was paused
     */
    boolean wasPausedOnLastPoll();
}
