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

import io.telicent.smart.cache.server.jaxrs.model.Mode;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("params")
public class ParamsResource {

    private static Mode MODE = Mode.A;

    @GET
    @Path("/mode")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getMode() {
        return Response.ok().entity(MODE).build();
    }

    @POST
    @Path("/mode")
    public Response setMode(@QueryParam("mode") @NotNull Mode mode) {
        MODE = mode;
        return Response.noContent().build();
    }

    @POST
    @Path("/everything/{path}")
    @Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
    public Response everything(@PathParam("path") String path, @QueryParam("query") String query,
                               @HeaderParam("X-Custom-Header") String header, @CookieParam("cookie") String cookie,
                               @FormParam("form") String form) {
        return Response.noContent().build();
    }
}
