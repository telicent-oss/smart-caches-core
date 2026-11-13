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
package io.telicent.smart.cache.cli.probes.resources;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.function.Supplier;

/** Internal diagnostic endpoint. Disabled unless a dedicated collector token is configured. */
@Path("/admin/distributions")
public class DistributionStatusResource {
    public static final String ATTRIBUTE = DistributionStatusResource.class.getName();
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Response status(@Context ServletContext context, @HeaderParam("Authorization") String authorization) {
        String token = System.getenv("DISTRIBUTION_STATUS_TOKEN");
        if (token == null || token.isBlank()) return Response.status(404).build();
        if (authorization == null || !MessageDigest.isEqual(("Bearer " + token).getBytes(StandardCharsets.UTF_8),
                                                            authorization.getBytes(StandardCharsets.UTF_8))) {
            return Response.status(401).build();
        }
        Supplier<?> supplier = (Supplier<?>) context.getAttribute(ATTRIBUTE);
        if (supplier == null) return Response.status(503).build();
        return Response.ok(supplier.get()).header("Cache-Control", "no-store").build();
    }
}