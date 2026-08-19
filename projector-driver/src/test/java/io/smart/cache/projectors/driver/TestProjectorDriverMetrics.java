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
package io.smart.cache.projectors.driver;

import io.telicent.smart.cache.observability.AttributeNames;
import io.telicent.smart.cache.observability.metrics.MetricTestUtils;
import io.telicent.smart.cache.projectors.NoOpProjector;
import io.telicent.smart.cache.projectors.driver.DriverMetricNames;
import io.telicent.smart.cache.projectors.driver.ProjectorDriver;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.sources.Event;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TestProjectorDriverMetrics {

    private static final String ITEM_TYPE_EVENTS = "events";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @BeforeClass
    public void setup() {
        // NB - Has to happen before any driver is built as a driver registers its metrics upon construction
        MetricTestUtils.enableMetricsCapture();
    }

    @AfterClass
    public void cleanup() {
        MetricTestUtils.disableMetricsCapture();
        this.executor.shutdownNow();
    }

    @Test
    public void givenStalledSource_whenProjecting_thenStallMetricsAreReported() throws InterruptedException {
        // Given
        final InfiniteEventSource source = new InfiniteEventSource("Event %,d", 5_000);
        final ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(NullSink.of())
                               .unlimited()
                               .unlimitedStalls()
                               .pollTimeout(Duration.ofMillis(100))
                               .build();

        // When
        final Future<?> future = this.executor.submit(driver);
        while (driver.getConsecutiveStalls() < 2) {
            Thread.sleep(50);
        }

        // Then
        // NB - The consecutive stalls gauge is reported via a callback that the driver closes when it stops running, so
        //      it has to be collected while the projection is still in-flight
        Assert.assertTrue(
                MetricTestUtils.getReportedMetric(DriverMetricNames.STALLS_CONSECUTIVE, AttributeNames.ITEMS_TYPE,
                                                  ITEM_TYPE_EVENTS) >= 2.0);
        Assert.assertTrue(
                MetricTestUtils.getReportedMetric(DriverMetricNames.STALLS_TOTAL, AttributeNames.ITEMS_TYPE,
                                                  ITEM_TYPE_EVENTS) >= 2.0);

        // And
        driver.cancel();
        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Ignore, only interested in the driver having stopped
        }
        Assert.assertTrue(source.isClosed());
    }
}
