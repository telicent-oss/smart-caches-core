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
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.MultiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

/**
 * Maps {@link org.glassfish.hk2.api.MultiException} errors into RFC 7807 Problem responses
 * <p>
 * A {@link MultiException} occurs when a bunch of errors happen while HK2 is trying to process the injections for a
 * JAX-RS resource.  This might be because the annotations parameters are of the wrong type for the request, or some
 * other reason.  We explicitly handle these exceptions instead of letting them bubble up to our final
 * {@link FallbackExceptionMapper} as the stack traces for these can be very large, and it's not useful to dump them all
 * into the logs.  Instead, with this exception mapper we log only the message of the error which will contain the
 * messages from all the individual exceptions the {@link MultiException} is wrapping.
 * </p>
 */
@Provider
public class MultiExceptionMapper implements ExceptionMapper<MultiException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiExceptionMapper.class);

    @Context
    private UriInfo uri;

    @Context
    private Request request;

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(MultiException exception) {
        // Explicitly log the error for diagnostic purposes
        // Intentionally DO NOT log the stack trace as this could be huge for a MultiException
        // We do log information about the HTTP Request so developers can diagnose what exactly went wrong i.e. what
        // kind of request provoked the error
        LOGGER.error(
                "{} {} (Content-Type: {}, Accept: {}) produced a multi-exception with {} errors.  This may be due to the client sending a badly malformed request, or a server resource declaring incorrect parameter annotations:\n  {}",
                getRequestMethod(),
                getRequestUri(),
                getContentType(),
                getAccept(),
                exception.getErrors().size(),
                StringUtils.replaceChars(exception.getMessage(), "\n", "\n  "));

        // Translate into a 500 Internal Server Error as we don't know whether the client/server was at fault
        // Intentionally only summarise how many errors occurred when reporting the problem to the user, this does two
        // things:
        // 1 - Avoids leaking errors that are almost certainly internal implementation detail to the end user
        // 2 - Avoids double logging the messages (which might be long) since FailureLoggingFilter will also unpack
        //     our generated Problem and log its details again
        //@formatter:off
        return new Problem("InternalServerError",
                           "Multiple Errors",
                           500,
                           String.format("%,d internal errors occurred", exception.getErrors().size()),
                           exception.getClass().getCanonicalName())
                .toResponse(this.headers);
        //@formatter:on
    }

    private String getRequestUri() {
        return uri != null ? uri.getRequestUri().toString() : "<unknown-uri>";
    }

    private String getRequestMethod() {
        return request != null ? request.getMethod() : "<unknown-method>";
    }

    private String getAccept() {
        return headers != null ? headers.getAcceptableMediaTypes().stream().map(MediaType::toString).collect(Collectors.joining(",")) : "<none>";
    }

    private String getContentType() {
        return headers != null && headers.getMediaType() != null ? headers.getMediaType().toString() : "<none>";
    }
}
