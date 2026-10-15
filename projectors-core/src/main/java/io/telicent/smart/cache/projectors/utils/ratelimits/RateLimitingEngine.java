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

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Abstract rate limiting engine which allows rate limiting to be applied in various contexts, e.g. see the
 * {@code RateLimitFilter} in our {@code jaxrs-base-server} module.
 *
 * @param <TRequest> Request type
 */
public abstract class RateLimitingEngine<TRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingEngine.class);


    /**
     * Discovers and applies rate limits, returning the result
     * <p>
     * Callers <strong>MUST</strong> inspect the result and act accordingly, this engine guarantees not to throw an
     * {@link Exception} transforming any error into an {@link RateLimitStatus#Invalid} result.
     * </p>
     *
     * @param request Request
     * @return Result
     */
    public final RateLimitResult rateLimit(TRequest request) {
        try {
            // Firstly discover the rate limits, fail if any of these prove to be invalid
            List<RateLimitSpec> limits = discoverRateLimits(request);

            // If no limits discovered then rate limiting is not applicable
            if (limits.isEmpty()) {
                return RateLimitResult.notApplicable();
            }

            // Ensure all limits have distinct names, if not return invalid result
            List<String> limitNames = limits.stream().map(RateLimitSpec::getName).toList();
            long distinctLimits = limitNames.stream().distinct().count();
            if (distinctLimits < limits.size()) {
                return RateLimitResult.builder()
                                      .status(RateLimitStatus.Invalid)
                                      .limitNames(limitNames)
                                      .errorTitle("Invalid Rate Limit Configuration")
                                      .errorDetail(
                                              String.format(
                                                      "Only %,d unique rate limit names from %,d discovered limits",
                                                      distinctLimits, limitNames.size()))
                                      .build();
            }

            // Actually apply all applicable rate limits
            RateLimiterRegistry registry = getRegistry(request);
            for (RateLimitSpec limit : limits) {
                if (!requestPermitted(request, limit, registry)) {
                    return RateLimitResult.exceeded(limit);
                }
            }

            // If we reach here then all rate limit checks were passed
            return RateLimitResult.permitted(limits);
        } catch (Exception e) {
            // Any exception in applying rate limits is treated as invalid configuration
            return invalidConfiguration(e);
        }
    }

    /**
     * Generates an invalid configuration result
     *
     * @param e Invalid configuration error
     * @return Invalid result
     */
    private static RateLimitResult invalidConfiguration(Exception e) {
        return RateLimitResult.builder()
                              .status(RateLimitStatus.Invalid)
                              .limitNames(Collections.emptyList())
                              .errorTitle("Invalid Rate Limit Configuration")
                              .errorDetail(e.getMessage())
                              .build();
    }

    /**
     * Gets the rate limiter registry that is in use
     *
     * @param request Request
     * @return Rate Limiter registry
     */
    protected abstract RateLimiterRegistry getRegistry(TRequest request);

    protected abstract List<RateLimitSpec> discoverRateLimits(TRequest request);

    private boolean requestPermitted(TRequest request, RateLimitSpec limit, RateLimiterRegistry registry) {
        // For per-user limits modify the limiter name with the authenticated username so that each user gets a unique
        // rate limit
        String limiterName = limit.getName();
        if (limit.isPerUser()) {
            String username = getUser(request);
            if (StringUtils.isNotBlank(username)) {
                limiterName = limiterName + "-" + username;
            } else {
                LOGGER.warn(
                        "Limit {} is configured as a per-user limit BUT no user is authenticated for this request to {} so limit will apply globally",
                        limiterName, getRequestTarget(request));
            }
        }

        // Actually obtain and acquire the rate limiter
        RateLimiterConfig config = getOrCreateConfiguration(limit);
        RateLimiter limiter = registry.rateLimiter(limiterName, config);
        return limiter.acquirePermission();
    }

    /**
     * Gets or creates a {@link RateLimiterConfig} based on the given {@link RateLimitSpec}
     * <p>
     * Implementations <strong>SHOULD</strong> ideally cache the mapping from specification to configuration, especially
     * if they utilise the {@link RateLimitConfig#toConfig(RateLimitSpec)} method to allow runtime overrides of the
     * application configuration.
     * </p>
     *
     * @param limit Rate limit specification
     * @return Rate limiter configuration
     */
    protected abstract RateLimiterConfig getOrCreateConfiguration(RateLimitSpec limit);

    /**
     * Gets the target of the request, used in some log messages
     *
     * @param request Request
     * @return Request target
     */
    protected abstract String getRequestTarget(TRequest request);

    /**
     * Gets the authenticated user for the request (if any)
     *
     * @param request Request
     * @return Authenticated user, or {@code null} if none
     */
    protected abstract String getUser(TRequest request);

}
