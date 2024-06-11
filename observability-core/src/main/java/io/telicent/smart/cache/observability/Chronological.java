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
package io.telicent.smart.cache.observability;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Something that occurs chronologically, defined in terms of starting and ending instants
 * ({@link #getStartedAt()} and {@link #getEndedAt()} properties, respectively).
 */
public interface Chronological {
    /**
     * The instant this chronological event is known to have occurred at.
     *
     * @return the instant this event occurred, which may be null if not known.
     */
    Instant getStartedAt();

    /**
     * The instant this chronological event is known to have completed at.
     *
     * @return the instant this event completed, which may be null if not known.
     */
    Instant getEndedAt();

    /**
     * Determines the duration of this chronological event from the absolute difference between the {@link #getStartedAt()} and
     * {@link #getEndedAt()} properties.
     *
     * @return the duration of the event, or zero if either the {@link #getStartedAt()} or {@link #getEndedAt()}
     * properties are not set.
     */
    default Duration getDuration() {
        final Instant occurredAt = getStartedAt();
        final Instant completedAt = getEndedAt();

        if ( occurredAt == null || completedAt == null) return Duration.ofMillis(0);

        return Duration.of(Math.abs(completedAt.toEpochMilli() - occurredAt.toEpochMilli()), ChronoUnit.MILLIS);
    }
}
