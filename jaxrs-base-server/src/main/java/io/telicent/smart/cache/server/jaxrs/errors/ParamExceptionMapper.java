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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ParamException;

/**
 * Maps parameter exceptions into RFC 7807 Problem responses
 */
@Provider
public class ParamExceptionMapper implements ExceptionMapper<ParamException> {
    @Override
    public Response toResponse(ParamException exception) {
        return new Problem("BadRequestParameter",
                           null,
                           400,
                           String.format("%s Parameter '%s' received invalid value",
                                         exception.getParameterType()
                                                  .getSimpleName()
                                                  .replace("Param", ""),
                                         exception.getParameterName()),
                           null).toResponse();
    }
}
