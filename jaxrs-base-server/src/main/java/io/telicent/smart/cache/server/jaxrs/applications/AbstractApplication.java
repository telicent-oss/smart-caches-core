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
import io.telicent.smart.cache.server.jaxrs.errors.*;
import io.telicent.smart.cache.server.jaxrs.filters.FailureLoggingFilter;
import io.telicent.smart.cache.server.jaxrs.filters.RequestIdFilter;
import io.telicent.smart.cache.server.jaxrs.resources.VersionInfoResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

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
public class AbstractApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new LinkedHashSet<>();
        // Exception Mappers
        // These translate the error responses into RFC 7807 compliant Problem JSON structures
        classes.add(ConstraintViolationMapper.class);
        classes.add(ParamExceptionMapper.class);
        classes.add(NotFoundExceptionMapper.class);
        classes.add(NotAllowedExceptionMapper.class);
        classes.add(FallbackExceptionMapper.class);
        // Request Filters
        if (this.isAuthEnabled()) {
            classes.add(JwtAuthFilter.class);
        }
        classes.add(RequestIdFilter.class);
        classes.add(FailureLoggingFilter.class);
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
}
