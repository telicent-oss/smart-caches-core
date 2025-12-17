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
package io.telicent.smart.cache.server.jaxrs.applications;

import io.telicent.servlet.auth.jwt.jaxrs3.JwtAuthFilter;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.server.jaxrs.errors.*;
import io.telicent.smart.cache.server.jaxrs.filters.*;
import io.telicent.smart.cache.server.jaxrs.resources.AbstractHealthResource;
import io.telicent.smart.cache.server.jaxrs.resources.VersionInfoResource;
import io.telicent.smart.cache.server.jaxrs.writers.ProblemPlainTextWriter;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Abstract definition of our JAX-RS Search Application
 * <p>
 * This installs a set of common exception mappers and request/response filters.  Derived application classes should
 * override {@link #getClasses()}, ensuring they call this implementation of it and appending their application specific
 * classes to the list before returning it.
 * </p>
 */
@ApplicationPath("/")
public abstract class AbstractApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractApplication.class);

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new LinkedHashSet<>();
        // Exception Mappers
        // These translate the error responses into RFC 7807 compliant Problem JSON structures
        classes.add(ConstraintViolationMapper.class);
        classes.add(ParamExceptionMapper.class);
        classes.add(NotFoundExceptionMapper.class);
        classes.add(NotAllowedExceptionMapper.class);
        classes.add(MultiExceptionMapper.class);
        classes.add(FallbackExceptionMapper.class);

        // Request Filters
        if (this.isAuthEnabled()) {
            // We add authentication and authorization only if the application indicates auth is enabled.
            classes.add(JwtAuthFilter.class);
            // NB - For now there is a feature flag that can be used to disable authorization enforcement to allow us to
            //      transition applications to having authorization policy.  Yet we can still deploy them in
            //      environments that don't yet have the new Telicent Auth server available.
            if (Configurator.get(AuthConstants.FEATURE_FLAG_AUTHORIZATION, Boolean::parseBoolean, true)) {
                classes.add(UserInfoFilter.class);
                classes.add(TelicentAuthorizationFilter.class);
            } else {
                LOGGER.warn(
                        "The Authorization Policy enforcement feature has been explicitly disabled via configuration variable {}.  Any authorization policy defined on application resources will be ignored and NOT enforced.  If this was not intended please ensure that this feature flag is set to true in your environment.",
                        AuthConstants.FEATURE_FLAG_AUTHORIZATION);
            }
        }
        classes.add(RequestIdFilter.class); // Add Request-ID to requests
        classes.add(RejectEmptyBodyFilter.class); // Reject POST/PUT/PATCH with empty body when resource requires a body
        classes.add(FailureLoggingFilter.class); // Log any responses with status codes >= 400

        // Message Body Writers
        classes.add(ProblemPlainTextWriter.class);

        // Health Resource
        Class<? extends AbstractHealthResource> healthResourceClass = getHealthResourceClass();
        if (healthResourceClass != null) {
            classes.add(healthResourceClass);
        } else {
            LOGGER.warn("No standardised Health Resource available for application {}",
                        this.getClass().getCanonicalName());
        }

        // Version Info Resource
        classes.add(VersionInfoResource.class);

        return classes;
    }

    /**
     * Indicates whether authentication should be enabled
     *
     * @return True if enabled, false otherwise
     */
    protected boolean isAuthEnabled() {
        return false;
    }

    /**
     * Gets the health resource class that should be used for the application
     * <p>
     * The derived application <strong>MAY</strong> choose to return {@code null} here to indicate that they don't want
     * to provide a {@code /healthz} endpoint, or that they are implementing their own endpoint without using
     * {@link AbstractHealthResource}.  However, implementations <strong>SHOULD</strong> create their health endpoint by
     * deriving from {@link AbstractHealthResource} wherever possible as it handles common error conditions and DoS
     * mitigations for the endpoint.
     * </p>
     *
     * @return Health Resource class, or {@code null}
     */
    protected abstract Class<? extends AbstractHealthResource> getHealthResourceClass();
}
