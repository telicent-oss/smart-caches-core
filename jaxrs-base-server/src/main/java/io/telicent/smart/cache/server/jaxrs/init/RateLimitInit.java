/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.cache.server.jaxrs.init;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.core.RegistryStore;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.configuration.sources.ConfigurationSource;
import io.telicent.smart.cache.server.jaxrs.annotations.RateLimit;
import jakarta.servlet.ServletContextEvent;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * An initialiser that sets up supporting registries and caches for the
 * {@link io.telicent.smart.cache.server.jaxrs.filters.RateLimitFilter}
 */
public class RateLimitInit implements ServerConfigInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitInit.class);

    public static final String ATTRIBUTE_RATE_LIMITS_DISCOVERY_CACHE = "rate-limits-discovery-cache";
    public static final String ATTRIBUTE_RATE_LIMITS_CONFIGURATION_CACHE = "rate-limits-configuration-cache";
    public static final String ATTRIBUTE_RATE_LIMITS_REGISTRY = "rate-limits-registry";
    public static final String ATTRIBUTE_RATE_LIMITS_INSTANCE_CACHE = "rate-limits-instance-cache";

    private static final String[] CACHE_ATTRIBUTES =
            {
                    ATTRIBUTE_RATE_LIMITS_DISCOVERY_CACHE,
                    ATTRIBUTE_RATE_LIMITS_CONFIGURATION_CACHE,
                    ATTRIBUTE_RATE_LIMITS_INSTANCE_CACHE
            };

    /**
     * Configuration key used that when explicitly set to {@code false} disables rate limit enforcement
     */
    public static final String DISABLE_RATE_LIMITS = "DISABLE_RATE_LIMITS";

    /**
     * Controls the size of the cache used to map {@link RateLimit} annotations to their ready to enforce
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
     * Configuration key prefix used with {@link #overrideKey(RateLimit, String)} to allow overriding the application
     * defined {@link RateLimit} configurations.
     */
    public static final String RATE_LIMIT_OVERRIDE_PREFIX = "RATE_LIMIT_OVERRIDE";
    /**
     * Configuration key suffix used with {@link #overrideKey(RateLimit, String)} to allow overriding the
     * {@link RateLimit#window()} configuration for a rate limit
     */
    public static final String RATE_LIMIT_WINDOW_SUFFIX = "WINDOW";
    /**
     * Configuration key suffix used with {@link #overrideKey(RateLimit, String)} to allow overriding the
     * {@link RateLimit#requestsPerWindow()} configuration for a rate limit
     */
    public static final String RATE_LIMIT_REQUESTS_SUFFIX = "REQUESTS";
    /**
     * Configuration key suffix used with {@link #overrideKey(RateLimit, String)} to allow overriding the
     * {@link RateLimit#waitTime()} configuration for a rate limit
     */
    public static final String RATE_LIMIT_TIMEOUT_SUFFIX = "TIMEOUT";

    /**
     * Creates am override configuration key for the given rate limit that overrides one aspect of the rate limit
     * annotations configuration
     * <p>
     * This will be of the form {@value #RATE_LIMIT_OVERRIDE_PREFIX}{@code _}{@code <NAME>}{@code _}{@code <SUFFIX>}
     * where {@code <NAME>} is the {@link RateLimit#name()} transformed into environment variable form via
     * {@link ConfigurationSource#asEnvironmentVariableKey(String)}.  For example a rate limit named {@code data-write}
     * would generate keys of the form {@code RATE_LIMIT_OVERRIDE_DATA_WRITE_<SUFFIX>}
     * </p>
     *
     * @param limit  Rate Limit
     * @param suffix Variable suffix
     * @return Override configuration key
     */
    public static String overrideKey(RateLimit limit, String suffix) {
        return overrideKey(limit.name(), suffix);
    }

    /**
     * Creates am override configuration key for the given rate limit that overrides one aspect of the rate limit
     * annotations configuration
     * <p>
     * This will be of the form {@value #RATE_LIMIT_OVERRIDE_PREFIX}{@code _}{@code <NAME>}{@code _}{@code <SUFFIX>}
     * where {@code <NAME>} is the {@link RateLimit#name()} transformed into environment variable form via
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
        for (String attribute : CACHE_ATTRIBUTES) {
            Cache<?, ?> cache = (Cache<?, ?>) sce.getServletContext().getAttribute(attribute);
            if (cache != null) {
                cache.invalidateAll();
                LOGGER.info("Cleared Rate Limit cache {}", attribute);
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (Configurator.get(DISABLE_RATE_LIMITS, Boolean::parseBoolean, false)) {
            LOGGER.warn("Rate Limiting explicitly disabled via configuration");
            return;
        }

        Long discoveryCacheSize =
                Configurator.get(RATE_LIMIT_DISCOVERY_CACHE_SIZE, Long::parseLong, DEFAULT_DISCOVERY_CACHE_SIZE);
        Cache<String, List<RateLimit>> discoveryCache = Caffeine.newBuilder().maximumSize(discoveryCacheSize).build();
        sce.getServletContext().setAttribute(ATTRIBUTE_RATE_LIMITS_DISCOVERY_CACHE, discoveryCache);
        Long configurationCacheSize =
                Configurator.get(RATE_LIMIT_CONFIGURATION_CACHE_SIZE, Long::parseLong,
                                 DEFAULT_CONFIGURATION_CACHE_SIZE);
        Cache<String, RateLimiterConfig> configurationCache =
                Caffeine.newBuilder().maximumSize(discoveryCacheSize).build();
        sce.getServletContext().setAttribute(ATTRIBUTE_RATE_LIMITS_CONFIGURATION_CACHE, configurationCache);
        Long limitCacheSize = Configurator.get(RATE_LIMIT_CACHE_SIZE, Long::parseLong, DEFAULT_CACHE_SIZE);
        Cache<String, RateLimiter> limitCache = Caffeine.newBuilder().maximumSize(limitCacheSize).build();
        sce.getServletContext().setAttribute(ATTRIBUTE_RATE_LIMITS_INSTANCE_CACHE, limitCache);
        sce.getServletContext()
           .setAttribute(ATTRIBUTE_RATE_LIMITS_REGISTRY,
                         new InMemoryRateLimiterRegistry(Collections.emptyMap(), Collections.emptyList(),
                                                         Collections.emptyMap(),
                                                         new CaffeineRegistryStore<>(limitCache)));
        LOGGER.info(
                "Rate Limiting enabled with discovery cache size {}, configuration cache size {} and rate limiter cache size {}",
                discoveryCacheSize, configurationCacheSize, limitCacheSize);
    }

    /**
     * A Caffeine backed instance of a Resilience4j registry store that bounds the maximum number of registered
     * instances for an application
     *
     * @param <T> Instance type
     */
    @AllArgsConstructor
    public static final class CaffeineRegistryStore<T> implements RegistryStore<T> {

        @NonNull
        private final Cache<String, T> cache;

        @Override
        public T computeIfAbsent(String key, Function<? super String, ? extends T> mappingFunction) {
            return this.cache.get(key, mappingFunction);
        }

        @Override
        public T putIfAbsent(String key, T value) {
            return this.computeIfAbsent(key, k -> value);
        }

        @Override
        public Optional<T> find(String key) {
            return Optional.ofNullable(this.cache.getIfPresent(key));
        }

        @Override
        public Optional<T> remove(String name) {
            T oldValue = this.cache.getIfPresent(name);
            this.cache.invalidate(name);
            return Optional.ofNullable(oldValue);
        }

        @Override
        public Optional<T> replace(String name, T newEntry) {
            T oldValue = this.cache.getIfPresent(name);
            this.cache.put(name, newEntry);
            return Optional.ofNullable(oldValue);
        }

        @Override
        public Collection<T> values() {
            return this.cache.asMap().values();
        }
    }
}
