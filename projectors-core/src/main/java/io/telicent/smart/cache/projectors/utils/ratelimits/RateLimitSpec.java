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
package io.telicent.smart.cache.projectors.utils.ratelimits;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;


/**
 * Represents a defined rate limit
 */
@Getter
@ToString
@EqualsAndHashCode
public class RateLimitSpec {

    /**
     * Default rate limit error title
     */
    public static final String DEFAULT_ERROR_TITLE = "Too Many Requests";
    /**
     * Default rate limit error detail message
     */
    public static final String DEFAULT_ERROR_DETAIL = "Too Many Requests, please try again later";

    private final String name;
    private final String errorTitle;
    private final String errorDetail;
    private final long window;
    private final int requestsPerWindow;
    private final long waitTime;
    private final boolean perUser;

    /**
     * Creates a new rate limit spec
     *
     * @param name              Name
     * @param errorTitle        Error title
     * @param errorDetail       Error detail
     * @param window            Window (in milliseconds)
     * @param requestsPerWindow Permitted requests per window
     * @param waitTime          Wait time to block for when obtaining a permit from a rate limiter
     * @param perUser           Whether this limit applies on a per-user basis i.e. if {@code true} then each user has
     *                          their own unique rate limit for their requests, if {@code false} the rate limit applies
     *                          globally to all requests
     */
    @Builder
    private RateLimitSpec(String name, String errorTitle, String errorDetail, long window, int requestsPerWindow,
                          long waitTime, boolean perUser) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Limit name cannot be blank/empty");
        }
        this.name = name;
        this.errorTitle = StringUtils.firstNonBlank(errorTitle, DEFAULT_ERROR_TITLE);
        this.errorDetail = StringUtils.firstNonBlank(errorDetail, DEFAULT_ERROR_DETAIL);
        if (window <= 0) {
            throw new IllegalArgumentException("Window must be a positive number of milliseconds");
        }
        this.window = window;
        if (requestsPerWindow <= 0) {
            throw new IllegalArgumentException("Requests per window must be a positive number");
        }
        this.requestsPerWindow = requestsPerWindow;
        // Normalise a negative wait time to a zero wait time
        this.waitTime = waitTime >= 0 ? waitTime : 0;
        this.perUser = perUser;
    }
}
