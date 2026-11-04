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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

public class TestRateLimitResult extends AbstractRateLimitTests {

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Status cannot be null")
    public void givenNullStatus_whenBuildingResult_thenFails() {
        // Given, When and Then
        RateLimitResult.builder().status(null).build();
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Limit names cannot be null")
    public void givenNullLimitNames_whenBuildingResult_thenFails() {
        // Given, When and Then
        RateLimitResult.builder().status(RateLimitStatus.Exceeded).limitNames(null).build();
    }

    @Test
    public void givenNoLimits_whenPermitting_thenPermitted() {
        // Given and When
        RateLimitResult result = RateLimitResult.permitted(Collections.emptyList());

        // Then
        verifyPermitted(result);
    }

    @Test
    public void givenExceededLimit_whenExceeded_thenRejected() {
        // Given and When
        RateLimitResult result = RateLimitResult.exceeded(LIMIT);

        // Then
        verifyRejected(result);
    }

    @Test
    public void givenNoApplicableLimits_whenNotApplicable_thenPermitted() {
        // Given and When
        RateLimitResult result = RateLimitResult.notApplicable();

        // Then
        verifyPermitted(result);
    }

    @Test
    public void givenInvalidLimits_whenBuildingInvalidResult_thenRejectedWithDefaultError() {
        // Given and When
        RateLimitResult result =
                RateLimitResult.builder().status(RateLimitStatus.Invalid).limitNames(List.of(NAME)).build();

        // Then
        verifyRejected(result);
        Assert.assertEquals(result.getErrorTitle(), RateLimitSpec.DEFAULT_ERROR_TITLE);
        Assert.assertEquals(result.getErrorDetail(), RateLimitSpec.DEFAULT_ERROR_DETAIL);
    }

    @Test
    public void givenInvalidLimits_whenBuildingInvalidResultWithCustomError_thenRejectedWithCustomError() {
        // Given and When
        String title = "Custom";
        String detail = "Custom details";
        RateLimitResult result = RateLimitResult.builder()
                                                .status(RateLimitStatus.Invalid)
                                                .limitNames(List.of(NAME))
                                                .errorTitle(title)
                                                .errorDetail(detail)
                                                .build();

        // Then
        verifyRejected(result);
        Assert.assertEquals(result.getErrorTitle(), title);
        Assert.assertEquals(result.getErrorDetail(), detail);
    }
}
