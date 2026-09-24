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
import io.telicent.servlet.auth.jwt.jaxrs3.JwtSecurityContext;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.server.jaxrs.annotations.RateLimit;
import io.telicent.smart.cache.server.jaxrs.init.RateLimitInit;
import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * A filter that implements application level rate limit
 * <p>
 * Application resource classes and methods may be annotated with the
 * {@link io.telicent.smart.cache.server.jaxrs.annotations.RateLimit} annotation to define rate limits.  The filter
 * finds all limits that apply to a given request and applies all of them.
 * </p>
 * <p>
 * Note that each {@link io.telicent.smart.cache.server.jaxrs.annotations.RateLimit} <strong>MUST</strong> have a unique
 * {@link RateLimit#name()} value, if not the filter will reject the request with a {@code 500 Internal Service Error}.
 * This is because unique names are required to manage the Resilience4J
 * </p>
 */
@Provider
@Priority(Priorities.USER)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class RateLimitFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitFilter.class);
    protected static final String UNKNOWN = "<unknown>";

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private ServletContext servletContext;

    @Context
    private SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (resourceInfo != null) {
            try {
                // NB - Firstly see what rate limits, if any, apply
                //      We cache the discovery of these to avoid having to scan annotations on every request, if rate
                //      limiting was not properly configured then this won't work BUT we only want that to be treated
                //      as an error if the application actually tries to apply rate limits
                //      i.e. if there's no rate limiter configuration AND no rate limits that apply then ignore
                //
                //      See also AbstractApplication.isRateLimitingEnabled()
                Cache<String, List<RateLimit>> limitsCache = getLimitsCache();
                if (limitsCache == null) {
                    if (!discoverRateLimits().isEmpty()) {
                        throw new IllegalStateException("No rate limits discovery cache configured");
                    } else {
                        return;
                    }
                }
                List<RateLimit> limits = limitsCache.get(resourceKey(), n -> discoverRateLimits());

                if (!limits.isEmpty()) {
                    // Sanity check that all limits have unique names
                    long distinctLimits = limits.stream().map(RateLimit::name).distinct().count();
                    if (distinctLimits < limits.size()) {
                        LOGGER.warn(
                                "Request to {} rejected due to invalid rate limits configuration, only {} distinct limit names from {} configured limits",
                                this.uriInfo.getRequestUri(), distinctLimits, limits.size());
                        invalidRateLimits(requestContext);
                        return;
                    }

                    // Actually apply all applicable rate limits
                    RateLimiterRegistry registry = getRegistry();
                    Cache<String, RateLimiterConfig> configurations = getConfigurations();
                    for (RateLimit limit : limits) {
                        try {
                            if (!requestPermitted(limit, registry, configurations)) {
                                abortRequest(requestContext, limit);
                                return;
                            }
                        } catch (IllegalArgumentException e) {
                            LOGGER.warn("Request to {} rejected due to invalid rate limit {} configuration: {}",
                                        this.uriInfo.getPath(), limit.name(), e.getMessage());
                            invalidRateLimits(requestContext);
                            return;
                        }
                    }
                }
            } catch (IllegalStateException e) {
                LOGGER.warn(
                        "Request to {} rejected due to lack of rate limiter registry and/or configuration caches in servlet context",
                        this.uriInfo.getPath());
                invalidRateLimits(requestContext);
            }
        }
    }

    private @NonNull List<RateLimit> discoverRateLimits() {
        return AnnotationsLocator.findAnnotations(
                resourceInfo.getResourceMethod(), resourceInfo.getResourceClass(), RateLimit.class);
    }

    /**
     * Gets a cache key for this resource to avoid us needing to scan for annotations on every request
     *
     * @return Resource key
     */
    private String resourceKey() {
        return resourceKey(this.resourceInfo);
    }

    /**
     * Gets a cache key for this resource to avoid us needing to scan for annotations on every request
     *
     * @return Resource key
     */
    static String resourceKey(ResourceInfo resourceInfo) {
        String prefix =
                resourceInfo.getResourceClass() != null ? resourceInfo.getResourceClass().getCanonicalName() :
                UNKNOWN;
        String suffix =
                resourceInfo.getResourceMethod() != null ? resourceInfo.getResourceMethod().getName() :
                UNKNOWN;
        return prefix + "-" + suffix;
    }

    @SuppressWarnings("unchecked")
    private Cache<String, List<RateLimit>> getLimitsCache() {
        return (Cache<String, List<RateLimit>>) this.servletContext.getAttribute(
                RateLimitInit.ATTRIBUTE_RATE_LIMITS_CACHE);
    }

    /**
     * Gets the rate limiter registry from the servlet context
     *
     * @return Registry
     */
    private RateLimiterRegistry getRegistry() {
        RateLimiterRegistry registry = (RateLimiterRegistry) this.servletContext.getAttribute(
                RateLimitInit.ATTRIBUTE_RATE_LIMITS_REGISTRY);
        if (registry == null) {
            throw new IllegalStateException("No rate limiter registry configured");
        }
        return registry;
    }

    /**
     * Gets the rate limiter configurations cache from the servlet context
     *
     * @return Configurations cache
     */
    @SuppressWarnings("unchecked")
    private Cache<String, RateLimiterConfig> getConfigurations() {
        Cache<String, RateLimiterConfig> configurations =
                (Cache<String, RateLimiterConfig>) this.servletContext.getAttribute(
                        RateLimitInit.ATTRIBUTE_RATE_LIMITS_CONFIGURATIONS);
        if (configurations == null) {
            throw new IllegalStateException("No rate limiter configurations cache configured");
        }
        return configurations;
    }

    /**
     * Aborts the request with a suitable {@link Problem} response
     *
     * @param requestContext Request Context to abort
     */
    private void invalidRateLimits(ContainerRequestContext requestContext) {
        requestContext.abortWith(Problem.builder()
                                        .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                                        .title("Misconfigured Rate Limits")
                                        .detail("This application configures invalid rate limits for requests to /" + this.uriInfo.getPath())
                                        .type("Invalid Configuration")
                                        .build()
                                        .toResponse(this.httpHeaders));
    }

    /**
     * Aborts the request with a suitable {@link Problem} response
     *
     * @param requestContext Request Context to abort
     * @param rateLimit      Requirement that wasn't met
     */
    private void abortRequest(ContainerRequestContext requestContext, RateLimit rateLimit) {
        requestContext.abortWith(Problem.builder()
                                        .status(Response.Status.TOO_MANY_REQUESTS.getStatusCode())
                                        .title(rateLimit.errorTitle())
                                        .detail(rateLimit.errorDetail())
                                        .type("Too Many Requests")
                                        .build()
                                        .toResponse(this.httpHeaders));
    }

    /**
     * Checks whether the given request is permitted by the rate limit
     *
     * @param limit Requirement
     * @return True if the requirement met, false if not met
     * @throws IllegalArgumentException May be thrown if the rate limit configuration is invalid
     */
    private boolean requestPermitted(RateLimit limit, RateLimiterRegistry registry,
                                     Cache<String, RateLimiterConfig> configurations) {
        if (StringUtils.isBlank(limit.name())) {
            throw new IllegalArgumentException("Rate Limits must have a non-blank name provided");
        }

        // For per-user limits modify the limiter name with the authenticated username so that each user gets a unique
        // rate limit
        String limiterName = limit.name();
        if (limit.perUser()) {
            if (this.securityContext instanceof JwtSecurityContext) {
                limiterName = limiterName + "-" + this.securityContext.getUserPrincipal().getName();
            } else {
                LOGGER.warn(
                        "Limit {} is configured as a per-user limit BUT no user is authenticated for this request to {} so limit will apply globally",
                        limiterName, this.uriInfo.getRequestUri());
            }
        }

        RateLimiterConfig config = configurations.get(limit.name(), n -> toConfig(limit));
        RateLimiter limiter = registry.rateLimiter(limiterName, config);
        return limiter.acquirePermission();
    }

    private RateLimiterConfig toConfig(RateLimit limit) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                                                   .limitForPeriod(requests(limit))
                                                   .limitRefreshPeriod(windowDuration(limit))
                                                   .timeoutDuration(timeoutDuration(limit))
                                                   .build();
        LOGGER.info("Rate limit {} has resolved configuration of {}", limit.name(), config);
        return config;
    }

    private static int requests(RateLimit limit) {
        int requests =
                Configurator.get(RateLimitInit.overrideKey(limit, RateLimitInit.RATE_LIMIT_REQUESTS_SUFFIX), Integer::parseInt,
                                 limit.requestsPerWindow());
        if (requests <= 0) {
            throw new IllegalArgumentException("Rate limit requestsPerWindow MUST be greater than zero");
        }
        return requests;
    }

    private static Duration timeoutDuration(RateLimit limit) {
        long waitTime = Configurator.get(RateLimitInit.overrideKey(limit, RateLimitInit.RATE_LIMIT_TIMEOUT_SUFFIX), Long::parseLong,
                                         limit.waitTime());
        if (waitTime <= 0) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(waitTime);
    }

    private static Duration windowDuration(RateLimit limit) {
        long window = Configurator.get(RateLimitInit.overrideKey(limit, RateLimitInit.RATE_LIMIT_WINDOW_SUFFIX), Long::parseLong,
                                       limit.window());
        if (window <= 0) {
            throw new IllegalArgumentException("Rate Limit window MUST be greater than zero milliseconds");
        }
        return Duration.ofMillis(window);
    }
}
