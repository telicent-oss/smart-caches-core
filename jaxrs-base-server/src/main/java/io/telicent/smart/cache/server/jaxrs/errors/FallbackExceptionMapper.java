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
package io.telicent.smart.cache.server.jaxrs.errors;

import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps otherwise unhandled errors into RFC 7807 Problem responses
 */
@Provider
public class FallbackExceptionMapper implements ExceptionMapper<Exception> {

    private String buildDetail(Throwable e) {
        StringBuilder builder = new StringBuilder();
        while (e != null) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            if (e.getMessage() != null) {
                builder.append(e.getMessage());
            }
            e = e.getCause();
        }

        return builder.toString();
    }

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException webEx) {
            // If something represents a specific JAX-RS exception then reformat that into a Problem response
            //@formatter:off
            return new Problem(webEx.getClass().getSimpleName(),
                               null,
                               webEx.getResponse().getStatus(),
                               webEx.getMessage(),
                               webEx.getClass().getCanonicalName()).toResponse();
            //@formatter:on
        }

        // For any other error just translate into a 500 Internal Server Error
        //@formatter:off
        return new Problem("InternalServerError",
                           "Unexpected Error",
                           500,
                           buildDetail(exception),
                           exception.getClass().getCanonicalName()).toResponse();
        //@formatter:on
    }
}
