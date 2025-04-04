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
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * An exception mapper that handles {@link NotAllowedException}'s i.e. HTTP 406s
 */
@Provider
public class NotAllowedExceptionMapper implements ExceptionMapper<NotAllowedException> {

    @Context
    private UriInfo uri;

    @Context
    private HttpHeaders headers;

    @Context
    private Request request;

    @Override
    public Response toResponse(NotAllowedException exception) {
        //@formatter:off
        return new Problem("MethodNotAllowed",
                           null,
                           Response.Status.METHOD_NOT_ALLOWED.getStatusCode(),
                           String.format("/%s does not permit %s requests",
                                         this.uri.getPath(), this.request.getMethod()),
                           null)
                .toResponse(this.headers);
        //@formatter:on
    }
}
