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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.telicent.servlet.auth.jwt.JwtServletConstants;
import io.telicent.smart.cache.server.jaxrs.auth.JaxRsRequestContext;
import io.telicent.smart.cache.security.requests.RequestContext;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A JAX-RS filter that gets installed when authentication is enabled within an application.  It fires after the
 * {@link io.telicent.servlet.auth.jwt.jaxrs3.JwtAuthFilter} has fired so it has access to the authentication
 * information which it needs to construct the Telicent Security Plugin {@link RequestContext} object.  This is placed
 * into a {@link #ATTRIBUTE} within the request via {@link ContainerRequestContext#setProperty(String, Object)} so can
 * be later retrieved by other JAX-RS filters/resources as needed to make authorization decisions.
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityPluginContextFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPluginContextFilter.class);

    /**
     * The attribute used to store the Security Plugin Request Context within the HTTP Request Attributes
     */
    public static final String ATTRIBUTE = RequestContext.class.getName();

    @Override
    @SuppressWarnings("unchecked")
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext.getSecurityContext() != null) {
            try {
                Jws<Claims> jwt =
                        (Jws<Claims>) requestContext.getProperty(JwtServletConstants.REQUEST_ATTRIBUTE_VERIFIED_JWT);
                if (jwt != null) {
                    JaxRsRequestContext pluginRequestContext = new JaxRsRequestContext(jwt,
                                                                                       requestContext.getSecurityContext()
                                                                                                     .getUserPrincipal()
                                                                                                     .getName(),
                                                                                       requestContext);
                    requestContext.setProperty(ATTRIBUTE, pluginRequestContext);
                } else {
                    LOGGER.warn(
                            "Request is authenticated via {} instead of JWT, unable to prepare a Telicent Security Plugin Request Context",
                            requestContext.getSecurityContext().getAuthenticationScheme());
                }
            } catch (ClassCastException e) {
                LOGGER.warn(
                        "Failed to prepare a Telicent Security Plugin Request Context, authorization may fail as a result");
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws
            IOException {
        try {
            // Explicitly close the plugin request context to stop it holding a reference to the JAX-RS request context
            // The JAX-RS server is likely going to clean that up anyway, but better to explicitly release the reference
            JaxRsRequestContext pluginRequestContext = (JaxRsRequestContext) requestContext.getProperty(ATTRIBUTE);
            if (pluginRequestContext != null) {
                pluginRequestContext.close();

                // And remove it from the request context
                requestContext.removeProperty(ATTRIBUTE);
            } else {
                LOGGER.warn("No Telicent Security Plugin Request Context found in request context");
            }
        } catch (ClassCastException e) {
            // Something changed the value of our attribute unexpectedly
            LOGGER.warn(
                    "Telicent Security Plugin Request Context attribute does not contain a RequestContext instance of the correct type");
        }
    }
}
