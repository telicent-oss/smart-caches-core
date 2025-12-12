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

import jakarta.ws.rs.core.*;

import java.util.stream.Collectors;

/**
 * Abstract base class for {@link jakarta.ws.rs.ext.ExceptionMapper} implementations that provides {@link Context}
 * variables for accessing the {@link HttpHeaders}, {@link UriInfo} and the {@link Request} itself.  Plus various
 * utility methods for safely accessing information from those even if they aren't populated.
 */
public class AbstractExceptionMapper {
    /**
     * Placeholder value used if {@link #getRequestUri()} cannot find a valid Request URI
     */
    public static final String UNKNOWN_URI = "<unknown-uri>";
    /**
     * Placeholder value used if {@link #getRequestMethod()} cannot determine the HTTP Method
     */
    public static final String UNKNOWN_METHOD = "<unknown-method>";
    /**
     * Placeholder value used if {@link #getContentType()}/{@link #getAccept()} determine that no such HTTP Header is
     * set
     */
    public static final String NONE = "<none>";

    /**
     * HTTP Headers for the JAX-RS Request
     */
    @Context
    protected HttpHeaders headers;

    /**
     * URI Information for the JAX-RS Request
     */
    @Context
    protected UriInfo uri;

    /**
     * The JAX-RS request that was being processed when the exception occurred
     */
    @Context
    protected Request request;

    /**
     * Gets the request URI for the request
     *
     * @return Request URI, or {@value #UNKNOWN_URI} if not available
     */
    protected String getRequestUri() {
        return uri != null && uri.getRequestUri() != null ? uri.getRequestUri().toString() : UNKNOWN_URI;
    }

    /**
     * Gets the HTTP Method for the request
     *
     * @return HTTP Method, or {@value #UNKNOWN_METHOD} if not available
     */
    protected String getRequestMethod() {
        return request != null ? request.getMethod() : UNKNOWN_METHOD;
    }

    /**
     * Gets the {@value HttpHeaders#ACCEPT} Header for the request
     *
     * @return Accept Header, or {@value #NONE} if not available
     */
    protected String getAccept() {
        return headers != null ? headers.getAcceptableMediaTypes().stream().map(MediaType::toString).collect(
                Collectors.joining(",")) : NONE;
    }

    /**
     * Gets the {@value HttpHeaders#CONTENT_TYPE} Header for the request
     *
     * @return Content Type header, or {@value #NONE} if not available
     */
    protected String getContentType() {
        return headers != null && headers.getMediaType() != null ? headers.getMediaType().toString() : NONE;
    }
}
