package io.telicent.smart.cache.server.jaxrs.auth;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.UriInfo;

public record JwtAuthorizationContext(ContainerRequestContext requestContext, ResourceInfo resourceInfo, UriInfo uriInfo) {
}
