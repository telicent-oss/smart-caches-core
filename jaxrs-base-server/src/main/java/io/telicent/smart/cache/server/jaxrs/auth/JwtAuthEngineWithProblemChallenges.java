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
package io.telicent.smart.cache.server.jaxrs.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.telicent.servlet.auth.jwt.JwtHttpConstants;
import io.telicent.servlet.auth.jwt.challenges.Challenge;
import io.telicent.servlet.auth.jwt.configuration.ClaimPath;
import io.telicent.servlet.auth.jwt.jaxrs3.JaxRs3JwtAuthenticationEngine;
import io.telicent.servlet.auth.jwt.sources.HeaderSource;
import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * A JWT authentication engine for JAX-RS which uses {@link Problem} responses in its challenges
 */
public class JwtAuthEngineWithProblemChallenges extends JaxRs3JwtAuthenticationEngine {

    /**
     * Creates a new engine with default settings
     */
    public JwtAuthEngineWithProblemChallenges() {
        super();
    }

    /**
     * Creates a new engine that looks for the username in multiple claims
     *
     * @param usernameClaims Username claims
     */
    public JwtAuthEngineWithProblemChallenges(List<ClaimPath> usernameClaims) {
        this(List.of(new HeaderSource(JwtHttpConstants.HEADER_AUTHORIZATION, JwtHttpConstants.AUTH_SCHEME_BEARER)),
             null,
             usernameClaims, null);
    }

    /**
     * Creates a new engine with custom settings
     *
     * @param headers        HTTP Headers from which the JWT might originate
     * @param realm          Realm
     * @param usernameClaims Username claims i.e. claims within the verified JWT that can contain the username, in order
     *                       or preference.  If none are specified then the implementation falls back to taking the
     *                       {@code sub}, aka subject, claim from the JWT.
     */
    public JwtAuthEngineWithProblemChallenges(List<HeaderSource> headers, String realm,
                                              List<ClaimPath> usernameClaims, ClaimPath rolesClaim) {
        super(headers, realm, usernameClaims, rolesClaim);
    }


    /**
     * Allows extracting the username for a JWS, intended only for unit testing
     * @param jws JWS
     * @return Username
     */
    String extractUsernameForTesting(Jws<Claims> jws) {
        return super.extractUsername(jws);
    }

    @Override
    protected Response buildChallengeResponse(String authChallenge, Challenge challenge) {
        // Since we'll be deployed behind a load balancer and always expect the authentication headers to be added any
        // failure of authentication is treated as 403 Forbidden.
        // This is regardless of whether the Bearer auth spec (RFC 6750) would usually expect a 400 Bad Request/401
        // Unauthorized to be returned
        return Response.status(Response.Status.FORBIDDEN)
                       .header(JwtHttpConstants.HEADER_WWW_AUTHENTICATE, authChallenge)
                       .entity(new Problem("AuthenticationRequired", "Authentication Required", 403,
                                           challenge.errorDescription(), null))
                       .build();
    }

    @Override
    protected String selectRealm(String defaultRealm) {
        // null means no realm will be used in the HTTP Authentication Challenges
        //
        // This is needed because the default realm is the request URL which potentially is different on every call to
        // the API because of the querystring parameters.  And in a production environment we're running behind a load
        // balancer so the private URLs at which we receive requests from our servers' perspective may be completely
        // different to the public URLs that are used to call our API
        return null;
    }
}
