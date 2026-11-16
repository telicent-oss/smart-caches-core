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
package io.telicent.smart.cache.distribution.lifecycle.sinks;

import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.observability.metrics.MetricTestUtils;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDistributionAcceptanceSinkMetrics {

    private final static String UNREGISTERED = "https://example.org/unregistered";
    private final static String ACTIVE = "https://example.org/active";
    private final static String DELETED = "https://example.org/deleted";
    private final static AtomicInteger ID = new AtomicInteger();

    @BeforeMethod
    public void setup() {
        MetricTestUtils.enableMetricsCapture();
    }

    @AfterMethod
    public void teardown() {
        MetricTestUtils.disableMetricsCapture();
    }

    private Event<Integer, String> event(String distributionId) {
        return new SimpleEvent<>(List.of(new Header(TelicentHeaders.DISTRIBUTION_ID, distributionId)),
                                 ID.incrementAndGet(), "Event " + Integer.toString(ID.get()));
    }

    private static DistributionLifecycleStateStore mockStateStore() {
        DistributionLifecycleStateStore stateStore = mock(DistributionLifecycleStateStore.class);
        when(stateStore.getLifecycleState(UNREGISTERED)).thenReturn(DistributionLifecycleState.Unregistered);
        when(stateStore.getLifecycleState(ACTIVE)).thenReturn(DistributionLifecycleState.Active);
        when(stateStore.getLifecycleState(DELETED)).thenReturn(DistributionLifecycleState.Deleted);
        return stateStore;
    }

    @Test
    public void givenSink_whenReceivingEvents_thenMetricsUpdatedAppropriately() {
        // Given
        DistributionLifecycleStateStore stateStore = mockStateStore();
        try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                          .stateStore(stateStore)
                                                                                          .unregisteredGracePeriod(
                                                                                                  Duration.ofMillis(
                                                                                                          100),
                                                                                                  Duration.ofMillis(10))
                                                                                          .build()) {
            // When
            Assert.assertThrows(SinkException.class, () -> sink.send(event(null)));
            Assert.assertThrows(SinkException.class, () -> sink.send(event(UNREGISTERED)));
            sink.send(event(UNREGISTERED));
            sink.send(event(ACTIVE));
            Assert.assertThrows(SinkException.class, () -> sink.send(event(DELETED)));

            // Then
            verifyMetric(DistributionAcceptanceSink.ACCEPTED_METRIC_NAME, ACTIVE, 1.0);
            verifyMetric(DistributionAcceptanceSink.PROCESSED_METRIC_NAME, ACTIVE, 1.0);
            verifyMetric(DistributionAcceptanceSink.REJECTED_METRIC_NAME,
                         DistributionAcceptanceSink.MISSING_DISTRIBUTION, 1.0);
            verifyMetric(DistributionAcceptanceSink.REJECTED_METRIC_NAME, UNREGISTERED, 2.0);
            verifyMetric(DistributionAcceptanceSink.REJECTED_METRIC_NAME, DELETED, 1.0);
        }
    }

    /**
     * Verifies that the metric with the given name was reported for the given Distribution ID with the expected value
     *
     * @param name           Metric name
     * @param distributionId Distribution ID
     * @param expected       Expected value
     */
    private static void verifyMetric(String name, String distributionId, double expected) {
        Assert.assertEquals(
                MetricTestUtils.getReportedMetric(name, DistributionAcceptanceSink.METRIC_ATTRIBUTE_DISTRIBUTION_ID,
                                                  distributionId), expected);
    }

    private void sendEvents(Sink<Event<Integer, String>> sink, int total, String distributionId,
                            boolean expectRejection) {
        for (int i = 1; i <= total; i++) {
            if (expectRejection) {
                Assert.assertThrows(SinkException.class, () -> sink.send(event(distributionId)));
            } else {
                sink.send(event(distributionId));
            }
        }
    }

    @Test(invocationCount = 10)
    public void givenSink_whenReceivingManyEvents_thenMetricsUpdatedAppropriately() {
        // Given
        DistributionLifecycleStateStore stateStore = mockStateStore();
        int noIdCount = RandomUtils.insecure().randomInt(10, 100);
        int unregisteredCount = RandomUtils.insecure().randomInt(50, 100);
        int validCount = RandomUtils.insecure().randomInt(100, 500);
        int deletedCount = RandomUtils.insecure().randomInt(50, 100);
        try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                          .stateStore(stateStore)
                                                                                          .unregisteredGracePeriod(
                                                                                                  Duration.ofMillis(
                                                                                                          100),
                                                                                                  Duration.ofMillis(10))
                                                                                          .rejectAfterRejection()
                                                                                          .build()) {
            // When
            sendEvents(sink, noIdCount, null, true);
            sendEvents(sink, unregisteredCount, UNREGISTERED, true);
            sendEvents(sink, validCount, ACTIVE, false);
            sendEvents(sink, deletedCount, DELETED, true);

            // Then
            verifyMetric(DistributionAcceptanceSink.ACCEPTED_METRIC_NAME, ACTIVE, validCount);
            verifyMetric(DistributionAcceptanceSink.PROCESSED_METRIC_NAME, ACTIVE, validCount);
            verifyMetric(DistributionAcceptanceSink.REJECTED_METRIC_NAME,
                         DistributionAcceptanceSink.MISSING_DISTRIBUTION, noIdCount);
            verifyMetric(DistributionAcceptanceSink.REJECTED_METRIC_NAME, UNREGISTERED, unregisteredCount);
            verifyMetric(DistributionAcceptanceSink.REJECTED_METRIC_NAME, DELETED, deletedCount);
        }
    }

    @Test(invocationCount = 10)
    public void givenSinkThatAcceptsUnregistered_whenReceivingManyEvents_thenMetricsUpdatedAppropriately() {
        // Given
        DistributionLifecycleStateStore stateStore = mockStateStore();
        int noIdCount = RandomUtils.insecure().randomInt(10, 100);
        int unregisteredCount = RandomUtils.insecure().randomInt(50, 100);
        int validCount = RandomUtils.insecure().randomInt(100, 500);
        int deletedCount = RandomUtils.insecure().randomInt(50, 100);
        try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                          .stateStore(stateStore)
                                                                                          .acceptUnregistered()
                                                                                          .rejectAfterRejection()
                                                                                          .build()) {
            // When
            sendEvents(sink, noIdCount, null, true);
            sendEvents(sink, unregisteredCount, UNREGISTERED, false);
            sendEvents(sink, validCount, ACTIVE, false);
            sendEvents(sink, deletedCount, DELETED, true);

            // Then
            verifyMetric(DistributionAcceptanceSink.ACCEPTED_METRIC_NAME, ACTIVE, validCount);
            verifyMetric(DistributionAcceptanceSink.PROCESSED_METRIC_NAME, ACTIVE, validCount);
            verifyMetric(DistributionAcceptanceSink.REJECTED_METRIC_NAME,
                         DistributionAcceptanceSink.MISSING_DISTRIBUTION, noIdCount);
            verifyMetric(DistributionAcceptanceSink.ACCEPTED_METRIC_NAME, UNREGISTERED, unregisteredCount);
            verifyMetric(DistributionAcceptanceSink.PROCESSED_METRIC_NAME, UNREGISTERED, unregisteredCount);
            verifyMetric(DistributionAcceptanceSink.REJECTED_METRIC_NAME, DELETED, deletedCount);
        }
    }
}
