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
package io.telicent.smart.cache.server.jaxrs.filters;

import io.telicent.smart.cache.server.jaxrs.annotations.RequireContextAttribute;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@RequireContextAttribute(value = "a", type = Object.class, errorTitle = "A Unavailable", errorDetail = "Required A is unavailable")
public class Example {

    @GET
    @Path("additional")
    @RequireContextAttribute(value = "map", type = Map.class, forbiddenTypes = { LinkedHashMap.class }, errorTitle = "Map Unavailable", errorDetail = "Required map is unavailable")
    @Produces(MediaType.TEXT_PLAIN)
    public Response additional() {
        return Response.ok().build();
    }

    @GET
    @Path("basic")
    @Produces(MediaType.TEXT_PLAIN)
    public Response basic() {
        return Response.ok().build();
    }
}
