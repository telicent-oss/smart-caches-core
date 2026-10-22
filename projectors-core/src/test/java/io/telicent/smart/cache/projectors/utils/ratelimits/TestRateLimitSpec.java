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

public class TestRateLimitSpec extends AbstractRateLimitTests {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Limit name.*")
    public void givenNoArgs_whenBuildingSpec_thenFails() {
        // Given, When and Then
        RateLimitSpec.builder().build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Window.*positive.*")
    public void givenBadWindow_whenBuildingSpec_thenFails() {
        // Given, When and Then
        RateLimitSpec.builder().name(NAME).window(-1).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Requests.*positive.*")
    public void givenBadRequestsPerWindow_whenBuildingSpec_thenFails() {
        // Given, When and Then
        RateLimitSpec.builder().name(NAME).window(100).requestsPerWindow(-1).build();
    }

    @Test
    public void givenMinimalArgs_whenBuildingSpec_thenOk() {
        // Given and When
        RateLimitSpec spec = RateLimitSpec.builder().name(NAME).window(100).requestsPerWindow(5).build();

        // Then
        Assert.assertEquals(spec.getName(), NAME);
        Assert.assertEquals(spec.getErrorTitle(), RateLimitSpec.DEFAULT_ERROR_TITLE);
        Assert.assertEquals(spec.getErrorDetail(), RateLimitSpec.DEFAULT_ERROR_DETAIL);
        Assert.assertEquals(spec.getWindow(), 100);
        Assert.assertEquals(spec.getRequestsPerWindow(), 5);
        Assert.assertEquals(spec.getWaitTime(), 0);
        Assert.assertFalse(spec.isPerUser());
    }

    @Test
    public void givenBlankErrorArgs_whenBuildingSpec_thenDefaultsUsed() {
        // Given and When
        RateLimitSpec spec = RateLimitSpec.builder()
                                          .name(NAME)
                                          .window(100)
                                          .requestsPerWindow(5)
                                          .errorTitle("   ")
                                          .errorDetail(null)
                                          .build();

        // Then
        Assert.assertEquals(spec.getErrorTitle(), RateLimitSpec.DEFAULT_ERROR_TITLE);
        Assert.assertEquals(spec.getErrorDetail(), RateLimitSpec.DEFAULT_ERROR_DETAIL);
    }

    @Test
    public void givenNegativeWaitTimeArgs_whenBuildingSpec_thenDefaultsToZero() {
        // Given and When
        RateLimitSpec spec = RateLimitSpec.builder()
                                          .name(NAME)
                                          .window(100)
                                          .requestsPerWindow(5)
                                          .waitTime(-100)
                                          .build();

        // Then
        Assert.assertEquals(spec.getWaitTime(), 0);
    }

    @Test
    public void givenFullArgs_whenBuildingSpec_thenOk() {
        // Given and When
        String title = "Failed";
        String detail = "Permitted request rate exceeded";
        RateLimitSpec spec = RateLimitSpec.builder()
                                          .name(NAME)
                                          .window(100)
                                          .requestsPerWindow(5)
                                          .perUser(true)
                                          .waitTime(10)
                                          .errorTitle(title)
                                          .errorDetail(detail)
                                          .build();

        // Then
        Assert.assertEquals(spec.getName(), NAME);
        Assert.assertEquals(spec.getErrorTitle(), title);
        Assert.assertEquals(spec.getErrorDetail(), detail);
        Assert.assertEquals(spec.getWindow(), 100);
        Assert.assertEquals(spec.getRequestsPerWindow(), 5);
        Assert.assertEquals(spec.getWaitTime(), 10);
        Assert.assertTrue(spec.isPerUser());
    }
}
