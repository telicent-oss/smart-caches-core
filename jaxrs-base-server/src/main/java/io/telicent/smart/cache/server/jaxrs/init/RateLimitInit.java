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
package io.telicent.smart.cache.server.jaxrs.init;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.ConfigurationSource;
import io.telicent.smart.cache.projectors.utils.ratelimits.RateLimitConfig;
import io.telicent.smart.cache.projectors.utils.ratelimits.RateLimitSpec;
import io.telicent.smart.cache.server.jaxrs.annotations.RateLimit;
import io.telicent.smart.cache.server.jaxrs.filters.JaxRsRateLimitingEngine;
import jakarta.servlet.ServletContextEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * An initialiser that sets up supporting registries and caches for the
 * {@link io.telicent.smart.cache.server.jaxrs.filters.RateLimitFilter}
 */
public class RateLimitInit implements ServerConfigInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitInit.class);

    public static final String ATTRIBUTE_RATE_LIMIT_ENGINE = "rate-limit-engine";

    /**
     * Controls the size of the cache used to map rate limits annotations to their ready to enforce
     * {@link io.github.resilience4j.ratelimiter.RateLimiter} instances
     */
    public static final String RATE_LIMIT_CACHE_SIZE = "RATE_LIMIT_CACHE_SIZE";

    /**
     * Default value for {@link #RATE_LIMIT_CACHE_SIZE} if not explicitly configured.  Since when per-user rate limits
     * are used each user gets a unique instance of the appropriate rate limiter this cache must be sized appropriately.
     * The default value is {@code 1000} meaning up to 1000 rate limits may be tracked and enforced at any one time, if
     * you have more users, or don't use per-user rate limits, then you should adjust up/down accordingly.
     */
    public static final long DEFAULT_CACHE_SIZE = 1000;

    /**
     * Controls the duration of the rate limit cache.  This is particularly important when per-user limits are in-use as
     * otherwise we could hold onto a {@link RateLimiter} instance for a long time for a user who is no longer
     * interacting with the service.
     * <p>
     * This should be configured to a suitable duration so that rate limiters are not discarded too soon, e.g. if this
     * duration is less than the window for a rate limit then the rate limit will not be enforced correctly. For example
     * a malicious user could space their traffic to allow the rate limit to expire from the cache and then submit
     * another burst of traffic thereby allowing them to exceed the desired rate limit.
     * </p>
     */
    public static final String RATE_LIMIT_CACHE_DURATION = "RATE_LIMIT_CACHE_DURATION";

    /**
     * Default value for {@link #RATE_LIMIT_CACHE_DURATION} if not explicitly configured
     */
    public static final Duration DEFAULT_CACHE_DURATION = Duration.ofMinutes(5);

    /**
     * Default value for {@link #RATE_LIMIT_DISCOVERY_CACHE_SIZE} if not explicitly configured.  The default value
     * assumes you have no more than 100 unique endpoints in your application.
     */
    public static final long DEFAULT_DISCOVERY_CACHE_SIZE = 100;

    /**
     * Controls the size of the cache used to cache the mapping from {@link jakarta.ws.rs.container.ResourceInfo} to the
     * discovered {@link RateLimit} annotations.
     */
    public static final String RATE_LIMIT_DISCOVERY_CACHE_SIZE = "RATE_LIMIT_DISCOVERY_CACHE_SIZE";

    /**
     * Default value for {@link #RATE_LIMIT_CONFIGURATION_CACHE_SIZE} if not explicitly configured.  The default value
     * assumes you have no more than 10 {@link RateLimit} annotations present in your application.
     */
    public static final long DEFAULT_CONFIGURATION_CACHE_SIZE = 10;

    /**
     * Controls the size of the cache used to cache the mapping from {@link jakarta.ws.rs.container.ResourceInfo} to the
     * discovered {@link RateLimit} annotations.
     */
    public static final String RATE_LIMIT_CONFIGURATION_CACHE_SIZE = "RATE_LIMIT_CONFIGURATION_CACHE_SIZE";

    /**
     * Creates am override configuration key for the given rate limit that overrides one aspect of the rate limit
     * annotations configuration
     * <p>
     * This will be of the form
     * {@value RateLimitConfig#RATE_LIMIT_OVERRIDE_PREFIX}{@code _}{@code <NAME>}{@code _}{@code <SUFFIX>} where
     * {@code <NAME>} is the {@link RateLimit#name()} transformed into environment variable form via
     * {@link ConfigurationSource#asEnvironmentVariableKey(String)}.  For example a rate limit named {@code data-write}
     * would generate keys of the form {@code RATE_LIMIT_OVERRIDE_DATA_WRITE_<SUFFIX>}
     * </p>
     *
     * @param limit  Rate Limit
     * @param suffix Variable suffix
     * @return Override configuration key
     */
    public static String overrideKey(RateLimit limit, String suffix) {
        return RateLimitConfig.overrideKey(limit.name(), suffix);
    }

    @Override
    public String getName() {
        return "Rate Limiting";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JaxRsRateLimitingEngine engine =
                (JaxRsRateLimitingEngine) sce.getServletContext().getAttribute(ATTRIBUTE_RATE_LIMIT_ENGINE);
        if (engine != null) {
            engine.close();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (Configurator.get(RateLimitConfig.DISABLE_RATE_LIMITS, Boolean::parseBoolean, false)) {
            LOGGER.warn("Rate Limiting explicitly disabled via configuration");
            return;
        }

        Long discoveryCacheSize =
                Configurator.get(RATE_LIMIT_DISCOVERY_CACHE_SIZE, Long::parseLong, DEFAULT_DISCOVERY_CACHE_SIZE);
        Cache<String, List<RateLimitSpec>> discoveryCache =
                Caffeine.newBuilder().maximumSize(discoveryCacheSize).build();
        Long configurationCacheSize = Configurator.get(RATE_LIMIT_CONFIGURATION_CACHE_SIZE, Long::parseLong,
                                                       DEFAULT_CONFIGURATION_CACHE_SIZE);
        Cache<String, RateLimiterConfig> configurationCache =
                Caffeine.newBuilder().maximumSize(discoveryCacheSize).build();
        Long limitCacheSize = Configurator.get(RATE_LIMIT_CACHE_SIZE, Long::parseLong, DEFAULT_CACHE_SIZE);
        Duration limitCacheDuration =
                Configurator.get(RATE_LIMIT_CACHE_DURATION, Duration::parse, DEFAULT_CACHE_DURATION);
        Cache<String, RateLimiter> limitCache =
                Caffeine.newBuilder().maximumSize(limitCacheSize).expireAfterAccess(limitCacheDuration).build();

        sce.getServletContext()
           .setAttribute(ATTRIBUTE_RATE_LIMIT_ENGINE, JaxRsRateLimitingEngine.builder()
                                                                             .discoveryCache(discoveryCache)
                                                                             .configurationCache(configurationCache)
                                                                             .limitCache(limitCache)
                                                                             .build());

        LOGGER.info(
                "Rate Limiting enabled with discovery cache size {}, configuration cache size {} and rate limiter cache size {} (expiry {})",
                discoveryCacheSize, configurationCacheSize, limitCacheSize, limitCacheDuration);
    }

}
