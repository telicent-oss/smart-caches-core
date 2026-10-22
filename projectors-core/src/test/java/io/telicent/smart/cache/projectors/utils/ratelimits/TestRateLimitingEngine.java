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

import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class TestRateLimitingEngine extends AbstractRateLimitTests {

    protected static final String USER = "user";

    @Test
    public void givenEngineThatDiscoversNoLimits_whenApplying_thenNotApplicable() {
        // Given
        FixedRateLimitingEngine engine = FixedRateLimitingEngine.builder().user(USER).build();

        // When
        RateLimitResult result = engine.rateLimit(Collections.emptyList());

        // Then
        Assert.assertEquals(result.getStatus(), RateLimitStatus.NotApplicable);
        verifyPermitted(result);
    }

    @Test
    public void givenEngineThatDiscoversLimits_whenApplyingOnce_thenPermitted() {
        // Given
        FixedRateLimitingEngine engine = FixedRateLimitingEngine.builder().user(USER).build();

        // When
        RateLimitResult result = engine.rateLimit(List.of(LIMIT));

        // Then
        Assert.assertEquals(result.getStatus(), RateLimitStatus.Permitted);
        verifyPermitted(result);
    }

    @Test
    public void givenEngineThatDiscoversLimits_whenApplyingRepeatedly_thenEventuallyRejected() {
        // Given
        FixedRateLimitingEngine engine = FixedRateLimitingEngine.builder().user(USER).build();

        // When and Then
        verifyEventuallyRejected(engine, List.of(LIMIT));
    }

    private static <T> void verifyEventuallyRejected(RateLimitingEngine<T> engine, T context) {
        Awaitility.await("Rate Limit to be exceeded")
                  .atMost(Duration.ofSeconds(1))
                  .pollDelay(Duration.ZERO)
                  .pollInterval(Duration.ZERO)
                  .untilAsserted(() -> {
                      RateLimitResult result = engine.rateLimit(context);
                      Assert.assertEquals(result.getStatus(), RateLimitStatus.Exceeded);
                      verifyRejected(result);
                  });
    }

    @Test
    public void givenEngineThatDiscoversNonDistinctLimits_whenApplyingOnce_thenInvalid() {
        // Given
        FixedRateLimitingEngine engine = FixedRateLimitingEngine.builder().user(USER).build();

        // When
        RateLimitResult result = engine.rateLimit(List.of(LIMIT, LIMIT));

        // Then
        Assert.assertEquals(result.getStatus(), RateLimitStatus.Invalid);
        verifyRejected(result);
    }

    @Test
    public void givenEngineThatDiscoversLimitsWithSharedNamePrefix_whenApplyingOnce_thenInvalid() {
        // Given
        FixedRateLimitingEngine engine = FixedRateLimitingEngine.builder().user(USER).build();
        RateLimitSpec other =
                RateLimitSpec.builder().window(1).requestsPerWindow(1).name(LIMIT.getName() + "-suffix").build();

        // When
        RateLimitResult result = engine.rateLimit(List.of(LIMIT, other));
        RateLimitResult result2 = engine.rateLimit(List.of(other, LIMIT));

        // Then
        Assert.assertEquals(result.getStatus(), RateLimitStatus.Invalid);
        Assert.assertEquals(result2.getStatus(), RateLimitStatus.Invalid);
        verifyRejected(result);
        verifyRejected(result2);
    }

    @Test
    public void givenEngineThatFailsDiscoveringLimits_whenApplyingOnce_thenInvalid() {
        FixedRateLimitingEngine engine = new BadDiscoveryRateLimitingEngine(USER);

        // When
        RateLimitResult result = engine.rateLimit(List.of(LIMIT, LIMIT));

        // Then
        Assert.assertEquals(result.getStatus(), RateLimitStatus.Invalid);
        verifyRejected(result);
    }

    @Test
    public void givenPerUserLimit_whenApplyingRepeatedlyForOneUser_thenRejected_andApplyingForAnotherUserAccepted() {
        // Given
        RateLimitSpec perUserLimit =
                RateLimitSpec.builder().name(NAME).window(100).requestsPerWindow(10).perUser(true).build();
        MultiUserRateLimitingEngine engine = new MultiUserRateLimitingEngine();

        // When and Then
        verifyEventuallyRejected(engine, new Context(List.of(perUserLimit), USER));

        // And
        RateLimitResult result = engine.rateLimit(new Context(List.of(perUserLimit), "other"));
        verifyPermitted(result);
    }

    @Test
    public void givenPerUserLimit_whenApplyingRepeatedlyWithNoUser_thenRejected_andApplyingForAnotherUserAccepted() {
        // Given
        RateLimitSpec perUserLimit =
                RateLimitSpec.builder().name(NAME).window(100).requestsPerWindow(10).perUser(true).build();
        MultiUserRateLimitingEngine engine = new MultiUserRateLimitingEngine();

        // When and Then
        verifyEventuallyRejected(engine, new Context(List.of(perUserLimit), null));

        // And
        RateLimitResult result = engine.rateLimit(new Context(List.of(perUserLimit), "other"));
        verifyPermitted(result);
    }

    @Test
    public void givenPerUserLimit_whenApplyingRepeatedlyWithForOneUser_thenRejected_andApplyingForNoUserAccepted() {
        // Given
        RateLimitSpec perUserLimit =
                RateLimitSpec.builder().name(NAME).window(100).requestsPerWindow(10).perUser(true).build();
        MultiUserRateLimitingEngine engine = new MultiUserRateLimitingEngine();

        // When and Then
        verifyEventuallyRejected(engine, new Context(List.of(perUserLimit), USER));

        // And
        RateLimitResult result = engine.rateLimit(new Context(List.of(perUserLimit), null));
        verifyPermitted(result);
    }
}
