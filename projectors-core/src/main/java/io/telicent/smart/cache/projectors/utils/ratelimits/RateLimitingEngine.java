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
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.*;
import io.telicent.smart.cache.observability.TelicentMetrics;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Abstract rate limiting engine which allows rate limiting to be applied in various contexts, e.g. see the
 * {@code RateLimitFilter} in our {@code jaxrs-base-server} module.
 * <p>
 * In particular see {@link #rateLimit(Object)} for discussion of behaviour.
 * </p>
 *
 * @param <TRequest> Request type
 */
// S119 - Properly named generic parameters are far more informative than single character ones
@SuppressWarnings("java:S119")
public abstract class RateLimitingEngine<TRequest> implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingEngine.class);
    protected static final String ANONYMOUS = "<anonymous>";
    protected static final String METRIC_PREFIX = "rate.limit.";

    private final LongCounter exceededCounter;
    private final LongCounter notApplicableCounter;
    private final LongCounter invalidCounter;
    private final LongCounter permittedCounter;
    private final ObservableLongGauge badUsersGauge;
    private final Map<String, AtomicLong> badUsers = new HashMap<>();

    /**
     * Creates a new rate limiting engine
     */
    protected RateLimitingEngine() {
        Meter meter = TelicentMetrics.getMeter("projectors-core");
        this.exceededCounter = rateLimitCounter(meter, "exceeded",
                                                "Counts how many requests were rejected by the rate limiter for exceeding the rate limit");
        this.notApplicableCounter = rateLimitCounter(meter, "not-applicable",
                                                     "Counts how many requests were permitted by the rate limiter as no rate limits applied to them");
        this.invalidCounter = rateLimitCounter(meter, "invalid",
                                               "Counts how many requests were rejected due to invalid rate limit configuration");
        this.permittedCounter = rateLimitCounter(meter, "permitted",
                                                 "Counts how many requests were permitted by the rate limiter as they were within the applicable rate limits");
        this.badUsersGauge = meter.gaugeBuilder(METRIC_PREFIX + "exceeding-users")
                                  .setDescription("Gauge tracking users who exceed the rate limits")
                                  .ofLongs()
                                  .buildWithCallback(badUsersCallback());
    }

    /**
     * Creates a callback for the bad users gauge that tracks rate limit violations by user
     *
     * @return Callback
     */
    private Consumer<ObservableLongMeasurement> badUsersCallback() {
        return m -> this.badUsers.forEach(
                (key, value) -> m.record(value.get(), Attributes.of(AttributeKey.stringKey("user"), key)));
    }

    /**
     * Creates a new rate limit counter metric
     *
     * @param meter       Meter
     * @param suffix      Metric name suffix
     * @param description Metric description
     * @return Rate Limit counter
     */
    private static LongCounter rateLimitCounter(Meter meter, String suffix, String description) {
        return meter.counterBuilder(METRIC_PREFIX + suffix).setDescription(description).build();
    }

    /**
     * Discovers and applies rate limits, returning the result
     * <p>
     * Callers <strong>MUST</strong> inspect the result and act accordingly, this engine guarantees not to throw an
     * {@link Exception} transforming any error into an {@link RateLimitStatus#Invalid} result.
     * </p>
     * <p>
     * The rate limit application process is as follows:
     * </p>
     * <ol>
     *     <li>Discovers the applicable rate limits (calls {@link #discoverRateLimits(Object)}).</li>
     *     <li>If no rate limits apply return a {@link RateLimitResult#notApplicable()} result.</li>
     *     <li>Validates discovered rate limits (see below), if invalid returns an invalid result.</li>
     *     <li>Obtains the rate limiter register (calls {@link #getRegistry(Object)}).</li>
     *     <li>For each discovered rate limit calls
     *     {@link #requestPermitted(Object, RateLimitSpec, RateLimiterRegistry)}, if that returns {@code false} then
     *     returns a {@link RateLimitResult#exceeded(RateLimitSpec)} result.
     *     </li>
     *     <li>If all rate limits passed then returns a {@link RateLimitResult#permitted(List)} result.</li>
     * </ol>
     * <h3>Rate Limit Validity</h3>
     * <p>
     * The engine enforces two main constraints on the discovered rate limits.  Firstly every rate limit
     * <strong>MUST</strong> have a unique name, non-unique names are not permitted as names are used to map limits to
     * actual {@link RateLimiter} instances which track rate limits.  Therefore, a non-unique name could lead to the
     * wrong rate limit being applied.
     * </p>
     * <p>
     * Secondly no rate limit name may be a prefix of another rate limit name, this is because when per-user rate limits
     * are used the limit name is used as a prefix with the username appended as a suffix.  If one limit name were a
     * prefix of another appending the username could lead to the wrong rate limit being selected.
     * </p>
     * <p>
     * Note also that {@link RateLimitSpec} enforces validity on the actual rate limit itself e.g. window must be
     * greater than 0 milliseconds, requests per window must be greater than 0 etc.
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
                this.notApplicableCounter.add(1);
                return RateLimitResult.notApplicable();
            }

            // Ensure all limits have distinct names, and no name is a prefix of any other name, if not return invalid
            // result
            List<String> limitNames = limits.stream().map(RateLimitSpec::getName).toList();
            long distinctLimits = limitNames.stream().distinct().count();
            if (distinctLimits < limits.size()) {
                this.invalidCounter.add(1);
                return invalidNonUniqueNames(limitNames, distinctLimits);
            }
            for (int i = 0; i < limitNames.size(); i++) {
                for (int j = 0; j < limitNames.size(); j++) {
                    // A name will always be a prefix of itself so don't check that otherwise we'd always consider
                    // limits invalid
                    if (i == j) {
                        continue;
                    }
                    if (Strings.CS.startsWith(limitNames.get(i), limitNames.get(j))) {
                        this.invalidCounter.add(1);
                        return invalidSharedNamePrefix(limitNames, i, j);
                    }
                }
            }

            // Actually apply all applicable rate limits
            RateLimiterRegistry registry = getRegistry(request);
            for (RateLimitSpec limit : limits) {
                if (!requestPermitted(request, limit, registry)) {
                    this.exceededCounter.add(1);
                    return RateLimitResult.exceeded(limit);
                }
            }

            // If we reach here then all rate limit checks were passed
            this.permittedCounter.add(1);
            return RateLimitResult.permitted(limits);
        } catch (Exception e) {
            // Any exception in applying rate limits is treated as invalid configuration
            this.invalidCounter.add(1);
            return invalidConfiguration(e);
        }
    }

    /**
     * Generates a result indicating that rate limit names were not unique
     *
     * @param limitNames     Discovered rate limit names
     * @param distinctLimits The number of unique discovered rate limit names
     * @return Invalid result
     */
    private static RateLimitResult invalidNonUniqueNames(List<String> limitNames, long distinctLimits) {
        return RateLimitResult.builder()
                              .status(RateLimitStatus.Invalid)
                              .limitNames(limitNames)
                              .errorTitle("Non-unique rate limit names")
                              .errorDetail(String.format(
                                      "Only %,d unique rate limit names from %,d discovered limits, all rate limits MUST have unique names",
                                      distinctLimits, limitNames.size()))
                              .build();
    }

    /**
     * Generates a result indicating that rate limit names had a shared prefix which could lead to rate limit selection
     * confusion
     *
     * @param limitNames Limit Names
     * @param i          Index of one name
     * @param j          Index of another name
     * @return Invalid result
     */
    private static RateLimitResult invalidSharedNamePrefix(List<String> limitNames, int i, int j) {
        return RateLimitResult.builder()
                              .status(RateLimitStatus.Invalid)
                              .limitNames(limitNames)
                              .errorTitle("Shared rate limit name prefix")
                              .errorDetail(String.format(
                                      "Rate limit name '%s' shares a prefix with '%s'.  No limit name may be a prefix of another name as this could lead to incorrect rate limit selection when per-user limits are in-use.",
                                      limitNames.get(i), limitNames.get(j)))
                              .build();
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

    /**
     * Discovers the rate limits that apply, how these are discovered is left to the concrete implementation
     *
     * @param request Request
     * @return Discovered rate limits, may be an empty list if no rate limits apply
     */
    protected abstract List<RateLimitSpec> discoverRateLimits(TRequest request);

    /**
     * Determines whether a given request is permitted for a given rate limit
     * <p>
     * Whether a request is permitted involves the following steps:
     * </p>
     * <ol>
     *     <li>Form the actual limiter name, using the limit name as-is for global limits, or as a prefix with the
     *     username (from {@link #getUser(Object)}) appended for per-user limits.</li>
     *     <li>Gets the {@link RateLimiterConfig} for the limit by calling
     *     {@link #getOrCreateConfiguration(RateLimitSpec)}.</li>
     *     <li>Acquires the actual {@link RateLimiter} instance from the provided {@link RateLimiterRegistry}.</li>
     *     <li>Calls {@link RateLimiter#acquirePermission()} to determine if the request is within the rate limit,
     *     returning {@code true} or {@code false} as appropriate.</li>
     * </ol>
     *
     * @param request  Request
     * @param limit    Rate Limit
     * @param registry Rate Limiter registry
     * @return True if permitted, false otherwise
     */
    private boolean requestPermitted(TRequest request, RateLimitSpec limit, RateLimiterRegistry registry) {
        // For per-user limits modify the limiter name with the authenticated username so that each user gets a unique
        // rate limit
        String limiterName = limit.getName();
        String username = getUser(request);
        if (limit.isPerUser()) {
            if (StringUtils.isNotBlank(username)) {
                limiterName = limiterName + "-" + username;
            } else {
                // This most likely happens when a developer configures a per-user limit onto a resource that doesn't
                // require authentication.  Thus, the rate limit applies globally, i.e. all unauthenticated requests
                // share the rate limit.
                // There is also a scenario where sometimes requests are authenticated and sometimes they are not.  In
                // that case then the rate limit does apply per-user for authenticated requests, and any unauthenticated
                // requests share a separate instance of the rate limit.
                // Regardless we issue a log warning since this is indicative of bad configuration that a developer
                // should correct
                LOGGER.warn(
                        "Limit {} is configured as a per-user limit BUT no user is authenticated for this request to {} so limit will apply globally",
                        limiterName, getRequestTarget(request));
            }
        }

        // Actually obtain and acquire the rate limiter
        RateLimiterConfig config = getOrCreateConfiguration(limit);
        RateLimiter limiter = registry.rateLimiter(limiterName, config);
        boolean permitted = limiter.acquirePermission();
        if (!permitted) {
            badUsers.computeIfAbsent(username != null ? username : ANONYMOUS, k -> new AtomicLong(0)).incrementAndGet();
        }
        return permitted;
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

    public void close() {
        // Close the bad user gauge
        this.badUsersGauge.close();
    }
}
