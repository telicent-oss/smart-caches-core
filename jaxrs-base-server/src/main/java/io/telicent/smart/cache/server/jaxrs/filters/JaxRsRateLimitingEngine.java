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
package io.telicent.smart.cache.server.jaxrs.filters;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import io.telicent.smart.cache.projectors.utils.ratelimits.CaffeineRegistryStore;
import io.telicent.smart.cache.projectors.utils.ratelimits.RateLimitConfig;
import io.telicent.smart.cache.projectors.utils.ratelimits.RateLimitSpec;
import io.telicent.smart.cache.projectors.utils.ratelimits.RateLimitingEngine;
import io.telicent.smart.cache.server.jaxrs.annotations.RateLimit;
import jakarta.ws.rs.container.ResourceInfo;
import lombok.Builder;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A JAX-RS compatible rate limiting engine.
 * <p>
 * In normal operation this is configured by {@link io.telicent.smart.cache.server.jaxrs.init.RateLimitInit} and then
 * applied by {@link RateLimitFilter}
 * </p>
 */
public class JaxRsRateLimitingEngine extends RateLimitingEngine<JaxRsRatelimitContext> implements Closeable {
    private final Cache<String, List<RateLimitSpec>> discoveryCache;
    private final Cache<String, RateLimiter> limitCache;
    private final Cache<String, RateLimiterConfig> configurationCache;
    private final RateLimiterRegistry registry;

    /**
     * Creates a new JAX-RS rate limiting engine
     *
     * @param discoveryCache     Limit discovery cache
     * @param configurationCache Limit configuration cache
     * @param limitCache         Limit cache
     */
    @Builder
    JaxRsRateLimitingEngine(Cache<String, List<RateLimitSpec>> discoveryCache,
                            Cache<String, RateLimiterConfig> configurationCache,
                            Cache<String, RateLimiter> limitCache) {
        this.discoveryCache = Objects.requireNonNull(discoveryCache, "Discovery cache cannot be null");
        this.configurationCache = Objects.requireNonNull(configurationCache, "Configuration cache cannot be null");
        this.limitCache = Objects.requireNonNull(limitCache, "Limit cache cannot be null");
        this.registry =
                new InMemoryRateLimiterRegistry(Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap(),
                                                new CaffeineRegistryStore<>(limitCache));
    }

    /**
     * Gets a cache key for this resource to avoid us needing to scan for annotations on every request
     *
     * @return Resource key
     */
    static String resourceKey(ResourceInfo resourceInfo) {
        String prefix = resourceInfo.getResourceClass() != null ? resourceInfo.getResourceClass().getCanonicalName() :
                        RateLimitFilter.UNKNOWN;
        String suffix = resourceInfo.getResourceMethod() != null ? resourceInfo.getResourceMethod().getName() :
                        RateLimitFilter.UNKNOWN;
        return prefix + "-" + suffix;
    }

    @Override
    protected RateLimiterRegistry getRegistry(JaxRsRatelimitContext context) {
        return this.registry;
    }

    static RateLimitSpec toSpec(RateLimit limit) {
        return RateLimitSpec.builder()
                            .name(limit.name())
                            .errorTitle(limit.errorTitle())
                            .errorDetail(limit.errorDetail())
                            .window(limit.window())
                            .requestsPerWindow(limit.requestsPerWindow())
                            .waitTime(limit.waitTime())
                            .perUser(limit.perUser())
                            .build();
    }

    @Override
    protected List<RateLimitSpec> discoverRateLimits(JaxRsRatelimitContext context) {
        return this.discoveryCache.get(resourceKey(context.resourceInfo()), k -> AnnotationsLocator.findAnnotations(
                                                                                                           context.resourceInfo().getResourceMethod(), context.resourceInfo().getResourceClass(), RateLimit.class)
                                                                                                   .stream()
                                                                                                   .map(JaxRsRateLimitingEngine::toSpec)
                                                                                                   .toList());
    }

    /**
     * Gets the discovered rate limits (if any), intended only for inspection in tests
     *
     * @param resourceKey Resource key
     * @return Discovered limits, these may be an empty list, or {@code null} if not yet discovered
     */
    List<RateLimitSpec> discovered(String resourceKey) {
        return this.discoveryCache.getIfPresent(resourceKey);
    }

    @Override
    protected RateLimiterConfig getOrCreateConfiguration(RateLimitSpec limit) {
        return this.configurationCache.get(limit.getName(), l -> RateLimitConfig.toConfig(limit));
    }

    @Override
    protected String getRequestTarget(JaxRsRatelimitContext context) {
        return context.uriInfo() != null && context.uriInfo().getRequestUri() != null ? context.uriInfo().getRequestUri().toString() : null;
    }

    @Override
    protected String getUser(JaxRsRatelimitContext context) {
        if (context.requestContext().getSecurityContext() != null && context.requestContext()
                                                                            .getSecurityContext()
                                                                            .getUserPrincipal() != null) {
            return context.requestContext().getSecurityContext().getUserPrincipal().getName();
        } else {
            return null;
        }
    }


    @Override
    public void close() {
        this.limitCache.invalidateAll();
        this.configurationCache.invalidateAll();
        this.discoveryCache.invalidateAll();
    }
}
