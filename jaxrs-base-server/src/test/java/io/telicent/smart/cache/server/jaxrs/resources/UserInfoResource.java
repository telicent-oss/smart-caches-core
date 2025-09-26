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

import io.jsonwebtoken.*;
import io.telicent.servlet.auth.jwt.JwtServletConstants;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;


@Path("/userinfo")
public class UserInfoResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Response getUserInfo(@Context ContainerRequestContext request) {
        Jws<Claims> jws = (Jws<Claims>) request.getProperty(JwtServletConstants.REQUEST_ATTRIBUTE_VERIFIED_JWT);
        if (jws == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Not authenticated").build();
        }
        Claims claims = jws.getPayload();
        if ("force-error".equals(claims.getSubject())) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Forced error for testing").build();
        }

        try {
            // Create a mock response, similar to Auth Server
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sub", claims.getSubject());
            userInfo.put("preferred_name", claims.get("preferred_name", String.class));
            userInfo.put("roles", claims.getOrDefault("roles", new String[] { "USER" }));
            userInfo.put("permissions", claims.getOrDefault("permissions", new String[] { "api.read" }));
            userInfo.put("attributes", claims.getOrDefault("attributes", Map.of()));

            return Response.ok(userInfo).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity("Invalid token, ex: " + ex.getMessage())
                           .build();
        }
    }
}
