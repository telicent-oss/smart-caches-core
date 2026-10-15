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

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of a {@link RateLimitingEngine} decision
 */
@Getter
@ToString
@EqualsAndHashCode
public class RateLimitResult {

    private final RateLimitStatus status;
    private final List<String> limitNames = new ArrayList<>();
    private final String errorTitle;
    private final String errorDetail;

    /**
     * Creates a result indicating the given rate limit was exceeded
     *
     * @param spec Rate Limit specification
     * @return Exceeded result
     */
    public static RateLimitResult exceeded(RateLimitSpec spec) {
        return RateLimitResult.builder()
                              .limitNames(List.of(spec.getName()))
                              .status(RateLimitStatus.Exceeded)
                              .errorTitle(spec.getErrorTitle())
                              .errorDetail(spec.getErrorDetail())
                              .build();
    }

    /**
     * Creates a result indicating that no rate limits were applicable
     *
     * @return Not Applicable result
     */
    public static RateLimitResult notApplicable() {
        return RateLimitResult.builder()
                              .limitNames(Collections.emptyList())
                              .status(RateLimitStatus.NotApplicable)
                              .build();
    }

    /**
     * Creates a result indicating that the request was permitted
     *
     * @param limits Rate limit specifications
     * @return Permitted result
     */
    public static RateLimitResult permitted(List<RateLimitSpec> limits) {
        return RateLimitResult.builder()
                              .limitNames(limits.stream().map(RateLimitSpec::getName).toList())
                              .status(RateLimitStatus.Permitted)
                              .build();
    }

    @Builder
    private RateLimitResult(RateLimitStatus status, List<String> limitNames, String errorTitle, String errorDetail) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.limitNames.addAll(Objects.requireNonNull(limitNames, "Limit names cannot be null"));
        this.errorTitle = StringUtils.firstNonBlank(errorTitle, RateLimitSpec.DEFAULT_ERROR_TITLE);
        this.errorDetail = StringUtils.firstNonBlank(errorDetail, RateLimitSpec.DEFAULT_ERROR_DETAIL);
    }

    /**
     * Does this result indicate the request is permitted?
     *
     * @return True if permitted, false otherwise
     */
    public boolean isPermitted() {
        return this.status == RateLimitStatus.NotApplicable || this.status == RateLimitStatus.Permitted;
    }

    /**
     * Does this result indicate the request is rejected?
     *
     * @return True if rejected, false otherwise
     */
    public boolean isRejected() {
        return this.status == RateLimitStatus.Invalid || this.status == RateLimitStatus.Exceeded;
    }
}
