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
import io.telicent.smart.cache.security.data.requests.RequestContext;
import io.telicent.smart.caches.configuration.auth.UserInfo;
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
import java.security.Principal;

/**
 * A JAX-RS filter that gets installed when authentication is enabled within an application.  It fires after the
 * {@link io.telicent.servlet.auth.jwt.jaxrs3.JwtAuthFilter} and {@link UserInfoFilter} have fired so it has access to
 * the authentication information which it needs to construct the Data Security Plugin {@link RequestContext} object.
 * This is placed into a {@link #ATTRIBUTE} within the request via
 * {@link ContainerRequestContext#setProperty(String, Object)} so can be later retrieved by other JAX-RS
 * filters/resources as needed to make data access authorization decisions.
 */
@Provider
@Priority(Priorities.AUTHORIZATION - 9)
public class DataSecurityPluginContextFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSecurityPluginContextFilter.class);

    /**
     * The attribute used to store the Security Plugin Request Context within the HTTP Request Attributes
     */
    public static final String ATTRIBUTE = RequestContext.class.getName();

    @Override
    @SuppressWarnings("unchecked")
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var securityContext = requestContext.getSecurityContext();
        if (securityContext == null) {
            return;
        }
        var principal = securityContext.getUserPrincipal();
        if (principal == null) {
            return;
        }

        Object rawJwt = requestContext.getProperty(JwtServletConstants.REQUEST_ATTRIBUTE_VERIFIED_JWT);
        if (rawJwt == null) {
            return;
        }
        if (!(rawJwt instanceof Jws<?>)) {
            LOGGER.warn(
                    "Failed to prepare a Telicent Security Plugin Request Context because verified JWT request attribute had unexpected type");
            return;
        }

        Jws<Claims> jwt = (Jws<Claims>) rawJwt;
        UserInfo userInfo = (UserInfo) requestContext.getProperty(UserInfo.class.getCanonicalName());
        JaxRsRequestContext pluginRequestContext = JaxRsRequestContext.builder()
                                                                      .jwt(jwt)
                                                                      .username(principal.getName())
                                                                      .userInfo(userInfo)
                                                                      .request(requestContext)
                                                                      .build();
        requestContext.setProperty(ATTRIBUTE, pluginRequestContext);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws
            IOException {
        Object rawPluginRequestContext = requestContext.getProperty(ATTRIBUTE);
        if (rawPluginRequestContext == null) {
            return;
        }
        if (!(rawPluginRequestContext instanceof JaxRsRequestContext pluginRequestContext)) {
            // Something changed the value of our attribute unexpectedly
            LOGGER.warn(
                    "Telicent Security Plugin Request Context attribute does not contain a RequestContext instance of the correct type");
            return;
        }

        // Explicitly close the plugin request context to stop it holding a reference to the JAX-RS request context
        // The JAX-RS server is likely going to clean that up anyway, but better to explicitly release the reference
        pluginRequestContext.close();

        // And remove it from the request context
        requestContext.removeProperty(ATTRIBUTE);
    }
}
