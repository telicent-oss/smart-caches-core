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

import lombok.AllArgsConstructor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;

public class TestChronological {

    @AllArgsConstructor
    private static final class Chrono implements Chronological {
        private final Instant startedAt, endedAt;

        @Override
        public Instant getStartedAt() {
            return this.startedAt;
        }

        @Override
        public Instant getEndedAt() {
            return this.endedAt;
        }
    }

    @DataProvider(name = "components")
    public static Object[][] components() {
        Instant base = Instant.now();
        return new Object[][] {
                { null, null, Duration.ZERO },
                { base, null, Duration.ZERO },
                { null, base, Duration.ZERO },
                { base, base, Duration.ZERO },
                { base, base.plusSeconds(10), Duration.ofSeconds(10) },
                { base, base.plusMillis(100), Duration.ofMillis(100) },
                // Chronological coerces to absolute duration so if components are swapped by mistake we still get a
                // positive duration reported
                { base.plusMillis(100), base, Duration.ofMillis(100) },
                { base, base.minusMillis(100), Duration.ofMillis(100)}
        };
    }

    @Test(dataProvider = "components")
    public void givenComponents_whenComputingDuration_thenAsExpected(Instant startedAt, Instant endedAt, Duration expected) {
        // Given
        Chrono chrono = new Chrono(startedAt, endedAt);

        // When
        Duration actual = chrono.getDuration();

        // Then
        Assert.assertEquals(actual, expected);
    }
}
