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

import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.util.Properties;

/**
 * Abstract base class for rate limiting related tests
 */
public abstract class AbstractRateLimitTests {
    protected static final String NAME = "test";

    /**
     * An example limit with a window of 100ms and 1 request per window
     */
    protected static final RateLimitSpec LIMIT = RateLimitSpec.builder()
                                                              .name(NAME)
                                                              .window(100)
                                                              .requestsPerWindow(1)
                                                              .errorDetail("Only 1 request per 100ms is permitted")
                                                              .build();

    protected static void verifyPermitted(RateLimitResult result) {
        Assert.assertTrue(result.isPermitted());
        Assert.assertFalse(result.isRejected());
    }

    protected static void verifyRejected(RateLimitResult result) {
        Assert.assertFalse(result.isPermitted());
        Assert.assertTrue(result.isRejected());
    }

    protected static void configureRateLimitOverrides(Duration windowOverride, Integer requestsOverride,
                                                      Duration waitTimeOverride) {
        Properties properties = new Properties();
        if (windowOverride != null) {
            properties.put(RateLimitConfig.overrideKey(NAME, RateLimitConfig.RATE_LIMIT_WINDOW_SUFFIX),
                           windowOverride.toMillis());
        }
        if (requestsOverride != null) {
            properties.put(RateLimitConfig.overrideKey(NAME, RateLimitConfig.RATE_LIMIT_REQUESTS_SUFFIX),
                           requestsOverride);
        }
        if (waitTimeOverride != null) {
            properties.put(RateLimitConfig.overrideKey(NAME, RateLimitConfig.RATE_LIMIT_TIMEOUT_SUFFIX),
                           waitTimeOverride.toMillis());
        }
        Configurator.setSingleSource(new PropertiesSource(properties));
    }

    @BeforeMethod
    public void setup() {
        Configurator.reset();
    }

    @AfterMethod
    public void teardown() {
        Configurator.reset();
    }
}
