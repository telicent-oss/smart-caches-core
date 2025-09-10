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

import io.telicent.servlet.auth.jwt.JwtServletConstants;
import io.telicent.servlet.auth.jwt.configuration.AutomatedConfiguration;
import io.telicent.servlet.auth.jwt.errors.AuthenticationConfigurationError;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.caches.configuration.auth.AuthConstants;
import jakarta.servlet.ServletContextEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet context initializer for initializing a JWT Verifier for use by
 * {@link io.telicent.servlet.auth.jwt.jaxrs3.JwtAuthFilter}
 */
public class JwtAuthInitializer implements ServerConfigInit {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Attempting to initialize JSON Web Token (JWT) authentication...");

        String jwksUrl = Configurator.get(AuthConstants.ENV_JWKS_URL);
        if (AuthConstants.AUTH_DISABLED.equalsIgnoreCase(jwksUrl)) {
            LOGGER.warn("JWT Authentication explicitly disabled, not configuring it");
            return;
        }

        // Defer to JWT Servlet Auth libraries automatic configuration mechanism providing our own config adaptor
        AutomatedConfiguration.configure(new ServletConfigurationAdaptor(sce.getServletContext()));
        Object verifier = sce.getServletContext().getAttribute(JwtServletConstants.ATTRIBUTE_JWT_VERIFIER);
        if (verifier != null) {
            LOGGER.info("Successfully configured JWT Authentication with verifier {}", verifier);
        } else {
            LOGGER.warn(
                    "JWT authentication not configured, server is running in secure mode BUT all requests will be rejected as unauthenticated");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(JwtServletConstants.ATTRIBUTE_JWT_VERIFIER);
        sce.getServletContext().removeAttribute(JwtServletConstants.ATTRIBUTE_JWT_ENGINE);
    }

    @Override
    public String getName() {
        return "JWT Authentication";
    }

    @Override
    public int priority() {
        return 100;
    }
}
