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

package io.telicent.smart.cache.server.jaxrs.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.telicent.smart.cache.server.jaxrs.applications.MockKeyServer;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

//TODO
// "this should probably just require that a JWT signed by the mock server be presented
// and echo back the JWT in a format similar to what the /userinfo endpoint of the actual Auth server does"

@Path("/userinfo")
public class UserInfoResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoResource.class);
    private static MockKeyServer keyServer;
    public UserInfoResource() {
    }

    public static void setKeyServer(MockKeyServer keyServer) {
        UserInfoResource.keyServer = keyServer;
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfo(@HeaderParam("Authorization") String authorization) {
        // Check if the token is there, just for testing purposes
        if (authorization == null ) {
            System.out.println("MISSING TOKEN");
            return Response.status(Response.Status.UNAUTHORIZED).entity("Missing token").build();
        }
        String token = authorization.substring("Bearer ".length());
        try {
            String headerJson = new String(
                    Base64.getUrlDecoder().decode(token.split("\\.")[0]),
                    StandardCharsets.UTF_8
            );
            Map<String,Object> headerMap = new ObjectMapper().readValue(headerJson, Map.class);
            String kid = (String) headerMap.get("kid");
            if (kid == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Missing key ID in token").build();
            }

            // Lookup matching public key from MockKeyServer
            Key publicKey = keyServer.getPublicKeys().getKeys().stream()
                    .filter(k -> kid.equals(k.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown key ID: " + kid))
                    .toKey();

            // Parse and verify JWT signature with that public key
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith((PublicKey) publicKey)
                    .build()
                    .parseSignedClaims(token);
            Claims claims = jws.getPayload();
            if ("force-error".equals(claims.getSubject())) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Forced error for testing")
                        .build();
            }
            // Create a mock response, similar to Auth Server
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", claims.getSubject());
            userInfo.put("preferred_name", claims.get("preferred_name", String.class));
            userInfo.put("roles", claims.getOrDefault("roles", new String[]{"USER"}));
            userInfo.put("permissions", claims.getOrDefault("permissions", new String[]{"api.read"}));
            userInfo.put("attributes", claims.getOrDefault("attributes", Map.of()));

            return Response.ok(userInfo).build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token, ex: " + e.getMessage()).build();
        }
    }
}
