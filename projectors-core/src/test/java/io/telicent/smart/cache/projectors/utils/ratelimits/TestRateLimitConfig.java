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
package io.telicent.smart.cache.projectors.utils.ratelimits;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

public class TestRateLimitConfig extends AbstractRateLimitTests {

    @Test
    public void givenLimit_whenConvertingToConfig_thenAsExpected() {
        // Given and When
        RateLimiterConfig config = RateLimitConfig.toConfig(LIMIT);

        // Then
        Assert.assertEquals(config.getLimitForPeriod(), LIMIT.getRequestsPerWindow());
        Assert.assertEquals(config.getLimitRefreshPeriod(), Duration.ofMillis(LIMIT.getWindow()));
        Assert.assertEquals(config.getTimeoutDuration(), Duration.ofMillis(LIMIT.getWaitTime()));
    }

    @DataProvider(name = "overrides")
    public static Object[][] configOverrides() {
        return new Object[][] {
                { Duration.ofMillis(1000), 10, Duration.ofMillis(10) },
                { Duration.ofMillis(1000), null, null },
                { null, 10, null },
                { null, null, Duration.ofMillis(10) },
                { null, null, Duration.ofMillis(-100) }
        };
    }

    @Test(dataProvider = "overrides")
    public void givenLimitWithOverrides_whenConvertingToConfig_thenOverridesHonoured(Duration windowOverride,
                                                                                     Integer requestsOverride,
                                                                                     Duration waitTimeOverride) {
        // Given
        configureRateLimitOverrides(windowOverride, requestsOverride, waitTimeOverride);

        // When
        RateLimiterConfig config = RateLimitConfig.toConfig(LIMIT);

        // Then
        if (requestsOverride != null) {
            Assert.assertEquals(config.getLimitForPeriod(), requestsOverride);
        } else {
            Assert.assertEquals(config.getLimitForPeriod(), LIMIT.getRequestsPerWindow());
        }
        if (windowOverride != null) {
            Assert.assertEquals(config.getLimitRefreshPeriod(), windowOverride);
        } else {
            Assert.assertEquals(config.getLimitRefreshPeriod(), Duration.ofMillis(LIMIT.getWindow()));
        }
        if (waitTimeOverride != null && waitTimeOverride.compareTo(Duration.ZERO) > 0) {
            Assert.assertEquals(config.getTimeoutDuration(), waitTimeOverride);
        } else {
            Assert.assertEquals(config.getTimeoutDuration(), Duration.ofMillis(LIMIT.getWaitTime()));
        }
    }

    @DataProvider(name = "invalidOverrides")
    public static Object[][] invalidOverrides() {
        return new Object[][] {
                { Duration.ofMillis(-10), null, null },
                { null, -10, null }
        };
    }

    @Test(dataProvider = "invalidOverrides", expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*MUST be greater than zero.*")
    public void givenLimitWithInvalidOverrides_whenConvertingToConfig_thenIllegalArgument(Duration windowOverride,
                                                                                     Integer requestsOverride,
                                                                                     Duration waitTimeOverride) {
        // Given
        configureRateLimitOverrides(windowOverride, requestsOverride, waitTimeOverride);

        // When and Then
        RateLimitConfig.toConfig(LIMIT);
    }
}
