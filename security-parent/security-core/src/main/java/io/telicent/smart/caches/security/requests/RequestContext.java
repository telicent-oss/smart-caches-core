package io.telicent.smart.caches.security.requests;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import java.net.URI;
import java.util.List;

/**
 * Interface for request context that supplies additional request context to relevant Security APIs
 */
public interface RequestContext {

    /**
     * The cryptographically verified JWT for the user
     *
     * @return Verified JWT
     */
    Jws<Claims> verifiedJwt();

    /**
     * The username for the user, as extracted by the
     * {@link io.telicent.smart.caches.security.identity.IdentityProvider}
     *
     * @return Username
     */
    String username();

    /**
     * Access the value(s) for a given request header
     * <p>
     * Depending on the connection protocol used for the request there may be no headers present.
     * </p>
     *
     * @param header Header name
     * @return Values, an empty list if no such headers are present
     */
    List<String> requestHeader(String header);

    /**
     * For HTTP requests provides access to the full Request URI
     *
     * @return Request URI, or {@code null} if not applicable
     */
    URI requestUri();

    /**
     * For API requests provides access to the path within the API
     * <p>
     * This may be present even if {@link #requestUri()} is {@code null} since requests arriving via other connection
     * protocols likely still have some concept of request path/routing even if they aren't HTTP APIs.
     * </p>
     *
     * @return Request path
     */
    String requestPath();

    /**
     * Supplies the request method API requests where multiple operations are supported on the same path, for HTTP
     * requests this is the HTTP verb e.g. {@code GET} or {@code POST}
     *
     * @return Request method, or {@code null} if not applicable
     */
    String requestMethod();
}
