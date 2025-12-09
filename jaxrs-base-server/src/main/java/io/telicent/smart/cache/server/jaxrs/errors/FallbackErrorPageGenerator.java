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

import com.apicatalog.jsonld.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.telicent.smart.cache.server.jaxrs.model.Problem;
import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A custom error page generator that replaces the default Grizzly one, this just presents a simple HTML page with the
 * error encoded as a JSON {@link Problem} in a {@code <pre>} block.  It also serves to log these failed requests as if
 * this is triggered it means normal JAX-RS request processing has not fully kicked in (or failed in a way that
 * couldn't be handled by JAX-RS {@link jakarta.ws.rs.ext.ExceptionMapper}'s).  Therefore, we will likely not hit our
 * normal {@link io.telicent.smart.cache.server.jaxrs.filters.FailureLoggingFilter} as we'd expect to.
 */
public class FallbackErrorPageGenerator implements ErrorPageGenerator {
    private final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackErrorPageGenerator.class);

    @Override
    public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
        // Firstly log the error so it doesn't get lost, and we see something useful in the logs, the
        // default error page generator doesn't log anything so bad requests were effectively invisible
        LOGGER.error("{} {} (Content-Type: {}, Accept: {}) produced error status {} {} with additional detail: {}",
                     getRequestMethod(request), getRequestURI(request), getContentType(request), getAccept(request),
                     status, reasonPhrase, getExceptionMessage(exception));

        // Generate a problem for the error and format that into JSON for presentation on our error page
        String errMsg = getExceptionMessage(exception);
        if (Objects.equals(errMsg, AbstractExceptionMapper.NONE)) {
            errMsg = "";
        } else {
            errMsg = "\n\n" + errMsg;
        }
        Problem problem = Problem.builder().title(reasonPhrase).detail(description + errMsg).status(status).build();
        String problemJson;
        try {
            problemJson = JSON.writeValueAsString(problem);
        } catch (JsonProcessingException e) {
            problemJson = String.format("HTTP %d %s\n%s\n%s", status, reasonPhrase, description,
                                        getExceptionMessage(exception));
        }

        // Render a minimalist HTML Error page per the API contract for ErrorPageGenerator
        return String.format("""
                                     <html>
                                       <head>
                                         <title>
                                           HTTP Error %d %s
                                         </title>
                                       </head>
                                       <body>
                                         <pre>
                                     %s
                                         </pre>
                                       </body>
                                     </html>
                                     """, status, reasonPhrase, problemJson);
    }

    /**
     * Gets the {@value HttpHeaders#ACCEPT} header for the request
     *
     * @param request Request
     * @return Accept header, or {@value AbstractExceptionMapper#NONE} if not available
     */
    private String getAccept(Request request) {
        return request != null && request.getHeader(HttpHeaders.ACCEPT) != null ?
               request.getHeader(HttpHeaders.ACCEPT) : AbstractExceptionMapper.NONE;
    }

    /**
     * Gets the {@value HttpHeaders#CONTENT_TYPE} header for the request
     *
     * @param request Request
     * @return Content-Type header, or {@value AbstractExceptionMapper#NONE} if not available
     */
    private String getContentType(Request request) {
        return request != null && StringUtils.isNotBlank(request.getContentType()) ? request.getContentType() :
               AbstractExceptionMapper.NONE;
    }

    /**
     * Gets the Request URI
     *
     * @param request Request
     * @return Request URI, or {@value AbstractExceptionMapper#UNKNOWN_URI} if not available
     */
    private static String getRequestURI(Request request) {
        return request != null && request.getRequestURI() != null ? request.getRequestURI() :
               AbstractExceptionMapper.UNKNOWN_URI;
    }

    /**
     * Gets the Request Method
     *
     * @param request Request
     * @return Request Method, or {@value AbstractExceptionMapper#UNKNOWN_METHOD} if not available
     */
    private static String getRequestMethod(Request request) {
        return request != null && request.getMethod() != null ? request.getMethod().getMethodString() :
               AbstractExceptionMapper.UNKNOWN_METHOD;
    }

    /**
     * Gets the exception message
     *
     * @param exception Exception
     * @return Exception message, or {@value AbstractExceptionMapper#NONE} if not available
     */
    private String getExceptionMessage(Throwable exception) {
        return exception != null ? exception.getMessage() : AbstractExceptionMapper.NONE;
    }
}
