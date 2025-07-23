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
package io.telicent.smart.cache.projectors.sinks;

import io.telicent.smart.cache.observability.AttributeNames;
import io.telicent.smart.cache.observability.MetricNames;
import io.telicent.smart.cache.observability.metrics.MetricTestUtils;
import io.telicent.smart.cache.projectors.RejectSink;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.commons.lang3.Strings.CS;

public class TestRejectSink extends AbstractSinkTests {

    @Test
    public void givenAlwaysTruePredicate_whenRejectingItems_thenNothingIsRejected() {
        // Given
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> true;

        // When and Then
        verifyReject(values, filter, values);
    }

    @Test
    public void givenAlwaysFalsePredicate_whenRejectingItems_thenEverythingIsRejected() {
        // Given
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> false;

        // When and Then
        verifyReject(values, filter, Collections.emptyList());
    }

    @Test
    public void givenAlwaysFalsePredicate_whenRejectingItems_thenEverythingIsRejected_andMetricsAreCorrect() {
        // Given
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> false;

        // When, Then, And
        verifyReject(values, filter, "reject_02", Collections.emptyList());
    }

    @Test
    public void givenActualPredicate_whenRejectingItems_thenSomeItemsAreRejected() {
        // Given
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> CS.startsWith(x, "f");

        // When and Then
        verifyReject(values, filter, Arrays.asList("foo", "faz"));
    }

    @Test
    public void givenActualPredicate_whenRejectingItems_thenSomeItemsAreRejected_andMetricsAreCorrect() {
        // Given
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> CS.startsWith(x, "f");

        // When, Then, And
        verifyReject(values, filter, "reject_03", Arrays.asList("foo", "faz"));
    }

    @Test
    public void givenAlwaysTruePredicate_whenRejectingNoItems_thenNothingIsRejected() {
        // Given
        Predicate<String> filter = x -> true;

        // When and Then
        verifyReject(Collections.emptyList(), filter, Collections.emptyList());
    }

    @Test
    public void givenNullPredicate_whenRejectingItems_thenNothingIsRejected() {
        // Given
        List<String> values = Arrays.asList("foo", "bar", "faz");

        // When and Then
        // Null predicate should default to accepting all items i.e. it sets the predicate to x -> true
        verifyReject(values, null, values);
    }

    @Test(expectedExceptions = SinkException.class, expectedExceptionsMessageRegExp = "Custom Message")
    public void givenAlwaysFalsePredicate_whenRejectingItemsWithCustomError_thenCustomErrorThrown() {
        // Given
        try (Sink<String> sink = Sinks.<String>reject()
                                      .predicate(x -> false)
                                      .errorMessageGenerator(e -> "Custom Message")
                                      .build()) {
            // When and Then
            sink.send("test");
        }
    }

    protected void verifyReject(List<String> values, Predicate<String> filter, List<String> expected) {
        // When and Then
        verifyReject(values, filter, null, expected);
    }

    protected void verifyReject(List<String> values, Predicate<String> filter, String metricsLabel,
                                List<String> expected) {
        // When
        try (CollectorSink<String> collector = new CollectorSink<>()) {
            try (RejectSink<String> sink = Sinks.<String>reject()
                                                .destination(collector)
                                                .predicate(filter)
                                                .metricsLabel(metricsLabel)
                                                .build()) {
                for (String value : values) {
                    try {
                        sink.send(value);
                        if (!expected.contains(value)) {
                            Assert.fail("Unexpected item " + value + " was accepted instead of rejected");
                        }
                    } catch (SinkException e) {
                        Assert.assertFalse(expected.contains(value), "Unexpected item " + value + " was rejected");
                    }
                }

                // Then
                verifyCollectedValues(collector, expected);

                // And
                // After close() the close should be passed down to the destination sink clearing the collection
                sink.close();
                Assert.assertEquals(collector.get().size(), 0);
            }
        }

        // And
        // If metrics were enabled then the appropriate number of filtered items should have been reported
        if (StringUtils.isNotBlank(metricsLabel)) {
            double metricValue =
                    MetricTestUtils.getReportedMetric(MetricNames.ITEMS_FILTERED, AttributeNames.ITEMS_TYPE,
                                                      metricsLabel);
            Assert.assertEquals(metricValue, values.size() - expected.size());
        }
    }

    @Test
    public void givenRejectSinkWithSimpleDestination_whenToString_thenOutputIndicatesDestination() {
        // Given
        try (RejectSink<String> sink = Sinks.<String>reject().collect().build()) {
            // When
            String output = sink.toString();

            // Then
            Assert.assertEquals(output, """
                    RejectSink(super=FilterSink(super={
                      destination=CollectorSink()
                    }))""");
        }
    }

    @Test
    public void givenRejectSinkWithComplexDestination_whenToString_thenOutputIndicatesDestination() {
        // Given
        try (RejectSink<String> sink = Sinks.<String>reject()
                                            .suppressDuplicates(s -> s.cacheSize(100)
                                                                      .expireCacheAfter(Duration.ofMinutes(5))
                                                                      .filter(AbstractForwardingSinkBuilder::discard))
                                            .build()) {
            // When
            String output = sink.toString();

            // Then
            Assert.assertEquals(output,
                                """
                                        RejectSink(super=FilterSink(super={
                                          destination=SuppressDuplicatesSink(super={
                                            destination=FilterSink(super={
                                              destination=NullSink(counter=0)
                                            })
                                          }, suppressed=0, lastCacheOperationAt=-1, expireCacheAfter=300000)
                                        }))""");
        }
    }
}
