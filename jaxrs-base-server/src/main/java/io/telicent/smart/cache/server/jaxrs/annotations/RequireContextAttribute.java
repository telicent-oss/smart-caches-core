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
package io.telicent.smart.cache.server.jaxrs.annotations;

import java.lang.annotation.*;

/**
 * Annotation applied to JAX-RS resource classes or methods to indicate that methods on the resource require a specific
 * {@link jakarta.servlet.ServletContext} attribute to have been set.
 * <p>
 * This is used by the {@link io.telicent.smart.cache.server.jaxrs.filters.RequireContextFilter} to enforce that the
 * specified context attribute is present and of the required type.  If this attribute is not set, or set to a value of
 * the wrong type, then requests will be aborted with a 503 Service Unavailable error.
 * </p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RequireContextAttribute {

    /**
     * Specifies the context attribute that is required to be present
     *
     * @return Context attribute key
     */
    String value();

    /**
     * Specifies the required type of the context attribute that is required to be present
     *
     * @return Required type
     */
    Class<?> type();

    /**
     * Specifies a list of forbidden types.
     * <p>
     * This is useful if you specify {@link #type()} as an interface type, and you have a known marker type that is used
     * to indicate a misconfiguration that should be treated as if the attribute was missing.
     * </p>
     *
     * @return Forbidden types
     */
    Class<?>[] forbiddenTypes() default {};

    /**
     * Specifies the error title to produce when the required context attribute is missing, or of the wrong type
     *
     * @return Error title
     */
    String errorTitle();

    /**
     * Specifies the error detail to produce when the required context attribute is missing, or of the wrong type
     *
     * @return Error detail
     */
    String errorDetail();
}
