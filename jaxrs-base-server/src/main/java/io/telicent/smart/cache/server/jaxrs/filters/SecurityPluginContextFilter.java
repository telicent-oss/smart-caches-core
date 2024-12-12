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

@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityPluginContextFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPluginContextFilter.class);

    public static final String ATTRIBUTE = RequestContext.class.getName();

    @Override
    @SuppressWarnings("unchecked")
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext.getSecurityContext() != null) {
            try {
                Jws<Claims> jwt =
                        (Jws<Claims>) requestContext.getProperty(JwtServletConstants.REQUEST_ATTRIBUTE_VERIFIED_JWT);
                JaxRsRequestContext pluginRequestContext =
                        new JaxRsRequestContext(jwt, requestContext.getSecurityContext().getUserPrincipal().getName(),
                                                requestContext);
                requestContext.setProperty(ATTRIBUTE, pluginRequestContext);
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
            pluginRequestContext.close();

            // And remove it from the request context
            requestContext.removeProperty(ATTRIBUTE);
        } catch (ClassCastException e) {
            // Something changed the value of our attribute unexpectedly
            LOGGER.warn(
                    "Telicent Security Plugin Request Context attribute does not contain a RequestContext instance of the correct type");
        }
    }
}
