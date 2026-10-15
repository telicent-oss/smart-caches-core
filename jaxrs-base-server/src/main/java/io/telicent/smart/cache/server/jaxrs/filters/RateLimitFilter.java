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
import io.telicent.smart.cache.projectors.utils.ratelimits.RateLimitResult;
import io.telicent.smart.cache.projectors.utils.ratelimits.RateLimitStatus;
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
import java.util.Collections;
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

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (resourceInfo == null) {
            // If we've not got our resource info populated we cannot find any rate limits so nothing to do
            return;
        }
        JaxRsRateLimitingEngine engine =
                (JaxRsRateLimitingEngine) this.servletContext.getAttribute(RateLimitInit.ATTRIBUTE_RATE_LIMIT_ENGINE);
        if (engine == null) {
            // If no engine configured then consider this invalid configuration
            invalidRateLimits(requestContext, RateLimitResult.builder()
                                                             .status(RateLimitStatus.Invalid)
                                                             .limitNames(Collections.emptyList())
                                                             .errorTitle("No Rate Limiting Engine Configured")
                                                             .errorDetail(
                                                                     "No Rate Limiting Engine was configured but the RateLimitFilter has been registered in the application.  Refusing to service requests as unclear if rate limits should apply or not.")
                                                             .build());
            return;
        }

        // Apply rate limits
        RateLimitResult result =
                engine.rateLimit(new JaxRsRatelimitContext(requestContext, this.resourceInfo, this.uriInfo));
        if (result.isRejected()) {
            // For requests that are rejected abort the request appropriately
            if (result.getStatus() == RateLimitStatus.Exceeded) {
                abortRequest(requestContext, result);
            } else {
                invalidRateLimits(requestContext, result);
            }
        }
        // Otherwise the request was permitted/no rate limits were applicable
    }

    /**
     * Aborts the request with a suitable {@link Problem} response
     *
     * @param requestContext Request Context to abort
     */
    private void invalidRateLimits(ContainerRequestContext requestContext, RateLimitResult result) {
        // Log the actual invalid reason server side
        LOGGER.warn("Invalid rate limits ({}): {}", StringUtils.join(result.getLimitNames(), ","),
                    result.getErrorDetail());
        // Send the client a generic error message
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
     * @param result         Rate limiting result
     */
    private void abortRequest(ContainerRequestContext requestContext, RateLimitResult result) {
        requestContext.abortWith(Problem.builder()
                                        .status(Response.Status.TOO_MANY_REQUESTS.getStatusCode())
                                        .title(result.getErrorTitle())
                                        .detail(result.getErrorDetail())
                                        .type("Too Many Requests")
                                        .build()
                                        .toResponse(this.httpHeaders));
    }
}
