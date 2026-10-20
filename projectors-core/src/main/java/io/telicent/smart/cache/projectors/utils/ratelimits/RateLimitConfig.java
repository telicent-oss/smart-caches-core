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
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.ConfigurationSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Provides static constants and helper methods related to configuring rate limits
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RateLimitConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitConfig.class);

    /**
     * Configuration key used that when explicitly set to {@code false} disables rate limit enforcement
     */
    public static final String DISABLE_RATE_LIMITS = "DISABLE_RATE_LIMITS";
    /**
     * Configuration key prefix used with {@link #overrideKey(String, String)} to allow overriding the application
     * defined {@link RateLimitSpec} configurations.
     */
    public static final String RATE_LIMIT_OVERRIDE_PREFIX = "RATE_LIMIT_OVERRIDE";
    /**
     * Configuration key suffix used with {@link #overrideKey(String, String)} to allow overriding the
     * {@link RateLimitSpec#getWindow()} configuration for a rate limit
     */
    public static final String RATE_LIMIT_WINDOW_SUFFIX = "WINDOW";
    /**
     * Configuration key suffix used with {@link #overrideKey(String, String)} to allow overriding the
     * {@link RateLimitSpec#getRequestsPerWindow()} configuration for a rate limit
     */
    public static final String RATE_LIMIT_REQUESTS_SUFFIX = "REQUESTS";
    /**
     * Configuration key suffix used with {@link #overrideKey(String, String)} to allow overriding the
     * {@link RateLimitSpec#getWaitTime()} configuration for a rate limit
     */
    public static final String RATE_LIMIT_TIMEOUT_SUFFIX = "TIMEOUT";

    /**
     * Given a rate limit specification converts it into a Resilience4j {@link RateLimiterConfig} applying any runtime
     * environment overrides via {@link #overrideKey(String, String)}
     *
     * @param limit Limit specification
     * @return Resolved configuration, this may have overridden some aspects of the specification depending on runtime
     * configuration
     */
    public static RateLimiterConfig toConfig(RateLimitSpec limit) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                                                    .limitForPeriod(requests(limit))
                                                    .limitRefreshPeriod(windowDuration(limit))
                                                    .timeoutDuration(timeoutDuration(limit))
                                                    .build();
        // Fine to log at info level as in production this is only called once per unique limit after which the returned
        // configuration should be cached.  It is however useful information to log as our override feature may mean
        // that the effective runtime configuration for a limit is quite different from the application defined limit.
        LOGGER.info("Rate limit {} ({}) has resolved configuration of {}", limit.getName(), limit, config);
        return config;
    }

    private static int requests(RateLimitSpec limit) {
        int requests = Configurator.get(overrideKey(limit.getName(), RATE_LIMIT_REQUESTS_SUFFIX), Integer::parseInt,
                                        limit.getRequestsPerWindow());
        if (requests <= 0) {
            throw new IllegalArgumentException("Rate limit requestsPerWindow MUST be greater than zero");
        }
        return requests;
    }

    private static Duration timeoutDuration(RateLimitSpec limit) {
        long waitTime = Configurator.get(overrideKey(limit.getName(), RATE_LIMIT_TIMEOUT_SUFFIX), Long::parseLong,
                                         limit.getWaitTime());
        if (waitTime <= 0) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(waitTime);
    }

    private static Duration windowDuration(RateLimitSpec limit) {
        long window = Configurator.get(overrideKey(limit.getName(), RATE_LIMIT_WINDOW_SUFFIX), Long::parseLong,
                                       limit.getWindow());
        if (window <= 0) {
            throw new IllegalArgumentException("Rate Limit window MUST be greater than zero milliseconds");
        }
        return Duration.ofMillis(window);
    }

    /**
     * Creates am override configuration key for the given rate limit that overrides one aspect of the rate limit
     * annotations configuration
     * <p>
     * This will be of the form {@value #RATE_LIMIT_OVERRIDE_PREFIX}{@code _}{@code <NAME>}{@code _}{@code <SUFFIX>}
     * where {@code <NAME>} is the {@link RateLimitSpec#getName()} transformed into environment variable form via
     * {@link ConfigurationSource#asEnvironmentVariableKey(String)}.  For example a rate limit named {@code data-write}
     * would generate keys of the form {@code RATE_LIMIT_OVERRIDE_DATA_WRITE_<SUFFIX>}
     * </p>
     *
     * @param limitName Rate Limit name
     * @param suffix    Variable suffix
     * @return Override configuration key
     */
    public static String overrideKey(String limitName, String suffix) {
        return String.format("%s_%s_%s", RATE_LIMIT_OVERRIDE_PREFIX,
                             ConfigurationSource.asEnvironmentVariableKey(limitName), suffix);
    }
}
