package io.telicent.smart.caches.security.requests;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * A minimal request context implementation that only has the JWT and username available, generally a more complete
 * implementation should be created by extending this.
 */
@AllArgsConstructor
public class MinimalRequestContext implements RequestContext {

    @NonNull
    private final Jws<Claims> jwt;
    @NonNull
    private final String username;


    @Override
    public Jws<Claims> verifiedJwt() {
        return this.jwt;
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public List<String> requestHeader(String header) {
        return Collections.emptyList();
    }

    @Override
    public URI requestUri() {
        return null;
    }

    @Override
    public String requestPath() {
        return null;
    }

    @Override
    public String requestMethod() {
        return null;
    }
}
