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

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.telicent.smart.cache.observability.metrics.MetricTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class TestRateLimitEngineMetrics extends AbstractRateLimitTests {

    @BeforeMethod
    public void setupMetrics() {
        MetricTestUtils.enableMetricsCapture();
    }

    @AfterMethod
    public void teardownMetrics() {
        MetricTestUtils.disableMetricsCapture();
    }

    @Test
    public void givenRateLimitingEngine_whenRequestPermitted_thenPermittedCounterIncrements() {
        // Given
        try (RateLimitingEngine<List<RateLimitSpec>> engine = new FixedRateLimitingEngine("test", "test")) {

            // When
            RateLimitResult result = engine.rateLimit(List.of(LIMIT));
            verifyPermitted(result);

            // Then
            Assert.assertEquals(MetricTestUtils.getReportedMetric(RateLimitingEngine.METRIC_PREFIX + "permitted"), 1.0);
        }
    }

    @Test
    public void givenRateLimitingEngine_whenInvalidLimits_thenInvalidCounterIncrements() {
        // Given
        try (RateLimitingEngine<List<RateLimitSpec>> engine = new BadDiscoveryRateLimitingEngine("test")) {

            // When
            RateLimitResult result = engine.rateLimit(List.of(LIMIT));
            verifyRejected(result);

            // Then
            Assert.assertEquals(MetricTestUtils.getReportedMetric(RateLimitingEngine.METRIC_PREFIX + "invalid"), 1.0);
        }
    }

    @Test
    public void givenRateLimitingEngine_whenSomeRequestsRejected_thenMetricsIncrementAppropriately() {
        // Given
        RateLimitSpec singleRequestAllowed = RateLimitSpec.builder()
                                                          .name("single")
                                                          .window(1000)
                                                          .requestsPerWindow(1)
                                                          .perUser(true)
                                                          .build();
        try (MultiUserRateLimitingEngine engine = new MultiUserRateLimitingEngine()) {

            // When
            Context userContext = new Context(List.of(singleRequestAllowed), "test");
            RateLimitResult result = engine.rateLimit(userContext);
            verifyPermitted(result);
            result = engine.rateLimit(userContext);
            verifyRejected(result);
            Context anotherUser = new Context(List.of(singleRequestAllowed), "other");
            result = engine.rateLimit(anotherUser);
            verifyPermitted(result);

            // Then
            Assert.assertEquals(MetricTestUtils.getReportedMetric(RateLimitingEngine.METRIC_PREFIX + "permitted"), 2.0);
            Assert.assertEquals(MetricTestUtils.getReportedMetric(RateLimitingEngine.METRIC_PREFIX + "exceeded"), 1.0);
            Assert.assertEquals(MetricTestUtils.getReportedMetric(RateLimitingEngine.METRIC_PREFIX + "exceeding-users",
                                                                  Attributes.of(AttributeKey.stringKey("user"),
                                                                                "test")), 1.0);
        }
    }
}
