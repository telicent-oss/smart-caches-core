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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RateLimit {

    /**
     * Specifies a unique name for this rate limit, used only for internal identification
     * <p>
     * See {@link #errorTitle()} and {@link #errorDetail()} for configuring user facing errors when this rate limit is
     * violated.
     * </p>
     *
     * @return Internal name of the rate limit
     */
    String name();

    /**
     * Specifies the error title to produce when this rate limit is violated
     *
     * @return Error title
     */
    String errorTitle();

    /**
     * Specifies the error detail to produce when this rate limit is violated
     *
     * @return Error detail
     */
    String errorDetail();

    /**
     * Specifies the rate limit window in milliseconds
     * <p>
     * <strong>MUST</strong> be a non-negative, non-zero value.
     * </p>
     *
     * @return Window milliseconds
     */
    long window();

    /**
     * Specifies the maximum wait time in milliseconds if a request arrives while the rate limit is exceeded for the
     * current {@link #window()}.
     * <p>
     * Defaults to {@code 0} meaning that any request arriving that exceeds the rate limit is immediately rejected.  Any
     * negative value is also interpreted as {@code 0}, i.e. no wait time.
     * </p>
     *
     * @return Wait time milliseconds
     */
    long waitTime() default 0;

    /**
     * Specifies the maximum number of requests permitted in each window
     * <p>
     * <strong>MUST</strong> be a non-negative, non-zero value.
     * </p>
     *
     * @return Requests per window
     */
    int requestsPerWindow();

    /**
     * Specifies whether this rate limit applies per-user
     * <p>
     * By default, rate limits are applied globally to all users i.e. all users share the permitted
     * {@link #requestsPerWindow()} for each {@link #window()} milliseconds.  However, for some endpoints, it may be
     * useful to provide a per-user rate limit where each unique user gets their own separate rate limit.
     * </p>
     *
     * @return True if the rate limit should be per-user, false otherwise
     */
    boolean perUser() default false;
}
