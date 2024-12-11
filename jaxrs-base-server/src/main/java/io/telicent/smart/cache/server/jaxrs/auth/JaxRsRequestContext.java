package io.telicent.smart.cache.server.jaxrs.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.telicent.smart.caches.security.requests.MinimalRequestContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;

import java.net.URI;
import java.util.List;

/**
 * A JAX-RS Request Context for use by {@link io.telicent.smart.caches.security.plugins.SecurityPlugin} API
 */
public class JaxRsRequestContext extends MinimalRequestContext {

    private final ContainerRequestContext request;

    /**
     * Creates a new request context
     *
     * @param jwt      Verified JWT
     * @param username Username
     * @param request  Request Context
     */
    public JaxRsRequestContext(@NonNull Jws<Claims> jwt,
                               @NonNull String username, @NonNull ContainerRequestContext request) {
        super(jwt, username);
        this.request = request;
    }

    @Override
    public List<String> requestHeader(String header) {
        return super.requestHeader(header);
    }

    @Override
    public URI requestUri() {
        return this.request.getUriInfo().getRequestUri();
    }

    @Override
    public String requestPath() {
        return this.request.getUriInfo().getPath();
    }

    @Override
    public String requestMethod() {
        return this.request.getMethod();
    }
}
