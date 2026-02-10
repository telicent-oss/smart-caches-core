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

import io.telicent.smart.cache.server.jaxrs.filters.SecurityPluginContextFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import io.telicent.smart.cache.security.requests.RequestContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@Path("security")

public class SecurityPluginContextResource {

    @GET
    @Path("authenticated")
    public Response isAuthenticated(@Context SecurityContext securityContext) {
        if (securityContext != null) {
            return Response.noContent().build();
        } else {
            return unauthorized();
        }
    }

    @GET
    @Path("username/direct")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getUsernameDirect(@Context SecurityContext securityContext) {
        if (securityContext != null) {
            return Response.ok().entity(securityContext.getUserPrincipal().getName()).build();
        } else {
            return unauthorized();
        }
    }

    private static Response unauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("username/plugin")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getUsernamePlugin(@Context HttpServletRequest request) {
        try {
            RequestContext context = (RequestContext) request.getAttribute(SecurityPluginContextFilter.ATTRIBUTE);
            if (context != null) {
                return Response.ok().entity(context.username()).build();
            } else {
                return unauthorized();
            }
        } catch (ClassCastException e) {
            return unauthorized();
        }
    }

    @GET
    @Path("headers/{header}/direct")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getHeaderDirect(@Context HttpHeaders headers, @PathParam("header") String header) {
        List<String> values = headers.getRequestHeaders().get(header);
        return createHeadersResponse(values);
    }

    private static Response createHeadersResponse(List<String> values) {
        if (CollectionUtils.isNotEmpty(values)) {
            return Response.ok()
                           .entity(StringUtils.join(
                                   values.stream().flatMap(v -> Arrays.stream(StringUtils.split(v, ','))).toList(),
                                   "\n"))
                           .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("headers/{header}/plugin")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getHeaderPlugin(@Context HttpServletRequest request, @PathParam("header") String header) {
        try {
            RequestContext context = (RequestContext) request.getAttribute(SecurityPluginContextFilter.ATTRIBUTE);
            if (context != null) {
                List<String> values = context.requestHeader(header);
                return createHeadersResponse(values);
            } else {
                return unauthorized();
            }
        } catch (ClassCastException e) {
            return unauthorized();
        }
    }

    @GET
    @Path("method/direct")
    public Response getMethodDirect() {
        return echoMethodDirect(HttpMethod.GET);
    }

    @POST
    @Path("method/direct")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response postMethodDirect(@Context Request request, String body) {
        return echoMethodDirect(request.getMethod());
    }

    @DELETE
    @Path("method/direct")
    public Response deleteMethodDirect(Request request) {
        return echoMethodDirect(request.getMethod());
    }

    private Response echoMethodDirect(String method) {
        return Response.ok().entity(method).build();
    }

    @GET
    @Path("method/plugin")
    public Response getMethodPlugin(@Context HttpServletRequest request) {
        return echoMethodPlugin(request);
    }

    @POST
    @Path("method/plugin")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response postMethodPlugin(@Context HttpServletRequest request, String body) {
        return echoMethodPlugin(request);
    }

    @DELETE
    @Path("method/plugin")
    public Response deleteMethodPlugin(@Context HttpServletRequest request) {
        return echoMethodPlugin(request);
    }

    private Response echoMethodPlugin(HttpServletRequest request) {
        try {
            RequestContext context = (RequestContext) request.getAttribute(SecurityPluginContextFilter.ATTRIBUTE);
            if (context != null) {
                return Response.ok().entity(context.requestMethod()).build();
            } else {
                return unauthorized();
            }
        } catch (ClassCastException e) {
            return unauthorized();
        }
    }

    @GET
    @Path("uri/direct")
    public Response getUriDirect(@Context UriInfo uriInfo) {
        return Response.ok().entity(uriInfo.getRequestUri().toString()).build();
    }

    @GET
    @Path("uri/plugin")
    public Response getUriPlugin(@Context HttpServletRequest request) {
        try {
            RequestContext context = (RequestContext) request.getAttribute(SecurityPluginContextFilter.ATTRIBUTE);
            if (context != null) {
                return Response.ok().entity(context.requestUri().toString()).build();
            } else {
                return unauthorized();
            }
        } catch (ClassCastException e) {
            return unauthorized();
        }
    }

    @GET
    @Path("path/{item}/direct")
    public Response getUriDirect(@Context UriInfo uriInfo, @PathParam("item") String item) {
        return Response.ok().entity(uriInfo.getPath()).build();
    }

    @GET
    @Path("path/{item}/plugin")
    public Response getPathPlugin(@Context HttpServletRequest request, @PathParam("item") String item) {
        try {
            RequestContext context = (RequestContext) request.getAttribute(SecurityPluginContextFilter.ATTRIBUTE);
            if (context != null) {
                return Response.ok().entity(context.requestPath()).build();
            } else {
                return unauthorized();
            }
        } catch (ClassCastException e) {
            return unauthorized();
        }
    }
}
