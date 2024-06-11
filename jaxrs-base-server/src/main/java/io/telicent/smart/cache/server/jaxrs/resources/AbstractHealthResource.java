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

import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;

/**
 * An abstract JAX-RS resource that provides a {@code /healthz} endpoint for reporting server health
 */
@Path("/")
public abstract class AbstractHealthResource {

    /**
     * Error message returned when the derived resource fails to generate a Health Status
     */
    public static final String UNEXPECTED_ERROR_REASON =
            "Server encountered an unexpected error generating its health status";

    /**
     * Returns a health response
     *
     * @param servletContext Servlet Context
     * @return Health response
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("healthz")
    public Response healthy(@Context ServletContext servletContext) {
        HealthStatus status;
        try {
            status = this.determineStatus(servletContext);
        } catch (Throwable t) {
            // There is a bug we've seen where a derived resources determineStatus() method hits an error that then
            // results in a 500 Internal Server Error rather than an actual 503 Service Unavailable so catch that
            // possibility here and report it appropriately
            status = new HealthStatus(false, List.of(UNEXPECTED_ERROR_REASON, t.getMessage()), Collections.emptyMap());
        }
        return Response.status(status.isHealthy() ? Response.Status.OK : Response.Status.SERVICE_UNAVAILABLE)
                       .entity(status)
                       .build();
    }

    /**
     * Determines the status for the server
     *
     * @param context Servlet Context
     * @return Health Status
     */
    protected abstract HealthStatus determineStatus(ServletContext context);
}
