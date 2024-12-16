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

import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("problems")
public class ProblemsResource {

    @GET
    @Produces({ MediaType.APPLICATION_JSON, Problem.MEDIA_TYPE, MediaType.TEXT_PLAIN , "application/custom"})
    public Response getProblem(@Context HttpHeaders headers,
                               @QueryParam("type") @DefaultValue("RuntimeException") String type,
                               @QueryParam("title") @DefaultValue("Unexpected Error") String title,
                               @QueryParam("status") @DefaultValue("500") int status,
                               @QueryParam("detail") @DefaultValue("") String detail) {
        return new Problem(type, title, status, detail, null).toResponse(headers);
    }

    @GET
    @Path("/throw")
    @Produces(MediaType.APPLICATION_JSON)
    public Response throwError(@QueryParam("message") @DefaultValue("Unexpected error") String message) {
        throw new RuntimeException(message);
    }


}
