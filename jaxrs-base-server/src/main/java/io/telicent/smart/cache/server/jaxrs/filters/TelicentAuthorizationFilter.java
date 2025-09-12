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
import com.github.benmanes.caffeine.cache.Caffeine;
import io.telicent.smart.cache.server.jaxrs.auth.JaxRsAuthorizationEngine;
import io.telicent.smart.cache.server.jaxrs.auth.JwtAuthorizationContext;
import io.telicent.smart.cache.server.jaxrs.model.Problem;
import io.telicent.smart.caches.server.auth.roles.AuthorizationResult;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

/**
 * A JAX-RS authorization filter that implements Telicent's role and permissions based authorization policies for
 * services.
 * <p>
 * The filter locates the role annotation (if any), and permissions annotation (if any), that apply to the resource
 * method that has been matched and enforces that users hold at least one of the allowed roles, and all the required
 * permissions.  Requests that do not meet these criteria are rejected with a suitable 401 Unauthorized response.
 * </p>
 * <p>
 * Uses the {@link io.telicent.smart.caches.server.auth.roles.TelicentAuthorizationEngine} to make the actual
 * authorization decisions passing in a {@link JwtAuthorizationContext} with the necessary context information for the
 * engine to make the decisions.
 * </p>
 */
@Produces
// Declare our priority such that we are applied after authentication has happened
@Priority(Priorities.AUTHORIZATION)
public class TelicentAuthorizationFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelicentAuthorizationFilter.class);

    /**
     * Default size of the exclusion warnings cache
     */
    protected static final int EXCLUSIONS_CACHE_SIZE = 10;
    /**
     * We create a basic cache to control the flow of path exclusion warnings because without this these warnings can
     * dominate the logs of relatively quiet services if automated monitoring tools are regularly pinging a health
     * status endpoint (or other equivalent) that's been configured for exclusion and detracts from actual useful
     * logging from the service.
     * <p>
     * Note that we set the cache size intentionally quite small (see {@link #EXCLUSIONS_CACHE_SIZE}) as applications
     * should generally have very few exclusions, if they have too many paths being excluded then that's most likely a
     * sign that they are misconfigured.  In that case we want them to be spammed by the warnings so they realise their
     * mistake!
     * </p>
     */
    protected static final Cache<String, Boolean> EXCLUSION_WARNINGS_CACHE =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofMinutes(15))
                    .initialCapacity(EXCLUSIONS_CACHE_SIZE)
                    .maximumSize(
                            EXCLUSIONS_CACHE_SIZE)
                    .build();

    /**
     * Static instance of the authorization engine, since the implementation is stateless can have a global shared
     * instance as it's the request context that'll drive the engines authorization decisions
     */
    private static final JaxRsAuthorizationEngine AUTHORIZATION_ENGINE = new JaxRsAuthorizationEngine();

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private UriInfo uriInfo;

    @Context
    private HttpHeaders httpHeaders;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        JwtAuthorizationContext authorizationContext =
                new JwtAuthorizationContext(requestContext, this.resourceInfo, this.uriInfo);
        AuthorizationResult result = AUTHORIZATION_ENGINE.authorize(authorizationContext);
        String allReasons = StringUtils.join(result.reasons(), ", ");
        switch (result.status()) {
            case DENIED:
                LOGGER.warn("{} Request to {} rejected: {}", requestContext.getMethod(), this.uriInfo.getRequestUri(),
                            allReasons);
                requestContext.abortWith(Problem.builder()
                                                .title("Unauthorized")
                                                .detail("Rejected due to servers authorization policy: " + allReasons)
                                                .status(Response.Status.UNAUTHORIZED.getStatusCode())
                                                .build()
                                                .toResponse(this.httpHeaders));
                break;
            case ALLOWED:
                LOGGER.info("{} Request to {} successfully authorized: {}", requestContext.getMethod(),
                            this.uriInfo.getRequestUri(), allReasons);
                break;
            case NOT_APPLICABLE:
                // Use a cache to prevent these warnings being spammed endlessly, this is especially true when something
                // like a health status endpoint is excluded from authentication and being regularly hit by automated
                // monitoring tools
                String path = this.uriInfo.getRequestUri().toString();
                if (EXCLUSION_WARNINGS_CACHE.getIfPresent(path) == null) {
                    LOGGER.warn("Request to path {} is excluded from Authorization: {}",
                                path, allReasons);
                    EXCLUSION_WARNINGS_CACHE.put(path, Boolean.TRUE);
                }
                break;
        }
    }

    /**
     * Does the user have the given permissions
     *
     * @param permsContext Permissions context
     * @param p            Permission required
     * @return True if they have the permission, false otherwise
     */
    private boolean hasPermission(Object permsContext, String p) {
        // TODO Implement properly once we have a means to get UserInfo for a user
        return false;
    }
}
