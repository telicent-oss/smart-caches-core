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
package io.telicent.smart.cache.projectors.utils;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.telicent.smart.cache.observability.AttributeNames;
import io.telicent.smart.cache.observability.MetricNames;
import io.telicent.smart.cache.observability.metrics.MetricTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.concurrent.TimeUnit;

public class TestThroughputTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestThroughputTracker.class);

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(TestThroughputTracker.class);

    @BeforeClass
    public void setup() {
        MetricTestUtils.enableMetricsCapture();
    }

    @AfterClass
    public void teardown() {
        MetricTestUtils.disableMetricsCapture();
    }

    @BeforeMethod
    public void clearTestLogger() {
        testLogger.clear();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoLogger_whenConstructing_thenNPE() {
        // Given, When and Then
        new ThroughputTracker(null, -1, null, null, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*>= 1")
    public void givenNegativeBatchSize_whenConstructing_thenIllegalArgument() {
        // Given, When and Then
        new ThroughputTracker(LOGGER, -1, null, null, null, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullTimeUnit_whenConstructing_thenNPE() {
        // Given, When and Then
        new ThroughputTracker(LOGGER, 100, null, null, null, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*>= 1")
    public void givenTracker_whenProcessedIsZero_thenIllegalArgument() {
        // Given
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, 100, TimeUnit.SECONDS, null, null, null);

        // When and Then
        tracker.itemsProcessed(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*>= 1")
    public void givenTracker_whenProcessedIsNegative_thenIllegalArgument() {
        // Given
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, 100, TimeUnit.SECONDS, null, null, null);

        // When and Then
        tracker.itemsProcessed(-1);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ThroughputTracker.TRACKING_MISMATCH_ERROR)
    public void givenTracker_whenProcessedCalledWithoutCorrespondingReceived_thenIllegalState() {
        // Given
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, 100, TimeUnit.SECONDS, null, null, null);

        // When and Then
        tracker.itemProcessed();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ThroughputTracker.TRACKING_MISMATCH_ERROR)
    public void givenTracker_whenProcessedExceedsReceived_thenIllegalState() {
        // Given
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, 100, TimeUnit.SECONDS, null, null, null);
        for (int i = 0; i < 100; i++) {
            tracker.itemReceived();
        }

        // When and Then
        tracker.itemsProcessed(101);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*already started.*")
    public void givenTracker_whenStartingMultipleTimes_thenIllegalState() {
        // Given
        ThroughputTracker tracker =
                new ThroughputTracker(LOGGER, 100, TimeUnit.SECONDS, ThroughputTracker.DEFAULT_ACTION,
                                      ThroughputTracker.DEFAULT_ITEMS_NAME, null);

        // When and Then
        tracker.start();
        tracker.start();
    }

    @DataProvider(name = "tracker")
    public Object[][] getTrackerTestParameters() {
        return new Object[][] {
                { 10_000, 100 },
                { 10_000, 100_000 },
                { 1_000_000, 10_000 },
                { 1_000_000, 25_000 },
                { 10_000, 333 }
        };
    }

    @Test(dataProvider = "tracker")
    public void givenTrackerInSeconds_whenTrackingItems_thenCountsAreCorrect(int count, int reportBatchSize) {
        // Given
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, reportBatchSize, TimeUnit.SECONDS,
                                                          ThroughputTracker.DEFAULT_ACTION,
                                                          ThroughputTracker.DEFAULT_ITEMS_NAME, null);

        verifyInitialState(tracker);

        // When
        for (int i = 0; i < count; i++) {
            tracker.itemReceived();
            tracker.itemProcessed();
        }

        // Then
        verifyTrackedCounts(count, reportBatchSize, tracker);
    }

    private void verifyTrackedCounts(int count, int reportBatchSize, ThroughputTracker tracker) {
        Assert.assertEquals(tracker.receivedCount(), count);
        Assert.assertEquals(tracker.processedCount(), count);
        if (count > reportBatchSize) {
            Assert.assertEquals(testLogger.getLoggingEvents().size(), count / reportBatchSize);
        } else {
            Assert.assertEquals(testLogger.getLoggingEvents().size(), 0);
        }
    }

    @Test(dataProvider = "tracker")
    public void givenTrackerInMilliseconds_whenTrackingItems_thenCountsAreCorrect(int count, int reportBatchSize) {
        // Given
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, reportBatchSize, TimeUnit.MILLISECONDS,
                                                          ThroughputTracker.DEFAULT_ACTION,
                                                          ThroughputTracker.DEFAULT_ITEMS_NAME, null);

        verifyInitialState(tracker);

        // When
        for (int i = 0; i < count; i++) {
            tracker.itemReceived();
            tracker.itemProcessed();
        }

        // Then
        verifyTrackedCounts(count, reportBatchSize, tracker);
    }

    private static void verifyInitialState(ThroughputTracker tracker) {
        Assert.assertEquals(tracker.receivedCount(), 0);
        Assert.assertEquals(tracker.processedCount(), 0);
        Assert.assertEquals(tracker.getOverallRate(), 0.0);
    }

    @Test(dataProvider = "tracker", timeOut = 1000L)
    public void givenTracker_whenTrackingProcessedIntermittently_thenCountsAreCorrect(int count, int reportBatchSize) {
        // Given
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, reportBatchSize, TimeUnit.SECONDS,
                                                          ThroughputTracker.DEFAULT_ACTION,
                                                          ThroughputTracker.DEFAULT_ITEMS_NAME, null);

        verifyInitialState(tracker);

        // When
        for (int i = 1; i <= count; i++) {
            tracker.itemReceived();
            if (tracker.receivedCount() % reportBatchSize == 0) {
                tracker.itemsProcessed((int) (tracker.receivedCount() - tracker.processedCount()));
            }
        }
        if (tracker.receivedCount() > tracker.processedCount()) {
            tracker.itemsProcessed((int) (tracker.receivedCount() - tracker.processedCount()));
        }

        // Then
        verifyTrackedCounts(count, reportBatchSize, tracker);
    }

    @Test(dataProvider = "tracker")
    public void givenTrackerWithMetricsEnabled_whenTrackingItems_thenCountsAreCorrect_andMetricsAreEmitted(int count,
                                                                                                           int reportBatchSize) {
        // Given
        String metricsLabel = String.format("with_metrics_01_%d_%d", count, reportBatchSize);
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, reportBatchSize, TimeUnit.SECONDS,
                                                          ThroughputTracker.DEFAULT_ACTION,
                                                          ThroughputTracker.DEFAULT_ITEMS_NAME,
                                                          metricsLabel);

        verifyInitialState(tracker);

        // When
        for (int i = 0; i < count; i++) {
            tracker.itemReceived();
            tracker.itemProcessed();
        }

        // Then
        verifyTrackedCounts(count, reportBatchSize, tracker);

        // And
        validateMetrics(tracker, metricsLabel);
    }

    @Test(dataProvider = "tracker")
    public void givenTrackerWithMetricsEnabled_whenTrackingProcessedIntermittently_thenCountsAreCorrect_andMetricsAreEmitted(
            int count, int reportBatchSize) {
        // Given
        String metricsLabel = String.format("with_metrics_02_%d_%d", count, reportBatchSize);
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, reportBatchSize, TimeUnit.SECONDS,
                                                          ThroughputTracker.DEFAULT_ACTION,
                                                          ThroughputTracker.DEFAULT_ITEMS_NAME,
                                                          metricsLabel);

        verifyInitialState(tracker);

        // When
        for (int i = 0; i < count; i++) {
            tracker.itemReceived();
            if (tracker.receivedCount() % reportBatchSize == 0) {
                tracker.itemsProcessed((int) (tracker.receivedCount() - tracker.processedCount()));
            }
        }
        if (tracker.receivedCount() > tracker.processedCount()) {
            tracker.itemsProcessed((int) (tracker.receivedCount() - tracker.processedCount()));
        }

        // Then
        verifyTrackedCounts(count, reportBatchSize, tracker);

        // And
        validateMetrics(tracker, metricsLabel);
    }

    @Test(dataProvider = "tracker")
    public void givenTrackerWithMetricsEnabled_whenTrackingReceivedOnly_thenCountsAreCorrect_andMetricsAreEmitted(
            int count, int reportBatchSize) {
        // Given
        String metricsLabel = String.format("with_metrics_03_%d_%d", count, reportBatchSize);
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, reportBatchSize, TimeUnit.SECONDS,
                                                          ThroughputTracker.DEFAULT_ACTION,
                                                          ThroughputTracker.DEFAULT_ITEMS_NAME,
                                                          metricsLabel);

        verifyInitialState(tracker);

        // When
        for (int i = 0; i < count; i++) {
            tracker.itemReceived();
        }

        // Then
        Assert.assertEquals(tracker.receivedCount(), count);
        Assert.assertEquals(tracker.processedCount(), 0);
        Assert.assertEquals(testLogger.getLoggingEvents().size(), 0);

        // And
        validateMetrics(tracker, metricsLabel);
    }

    private void validateMetrics(ThroughputTracker tracker, String metricsLabel) {
        double reportedMetric =
                MetricTestUtils.getReportedMetric(MetricNames.ITEMS_RECEIVED,
                                                  AttributeNames.ITEMS_TYPE,
                                                  metricsLabel);
        Assert.assertEquals(reportedMetric, (double) tracker.receivedCount());
        try {
            reportedMetric =
                    MetricTestUtils.getReportedMetric(MetricNames.ITEMS_PROCESSED,
                                                      AttributeNames.ITEMS_TYPE,
                                                      metricsLabel);
            Assert.assertEquals(reportedMetric, (double) tracker.processedCount());
        } catch (IllegalStateException e) {
            Assert.assertEquals(0L, tracker.processedCount());
        }

        // Force a throughput report to make sure that the rate metric gets reported
        tracker.reportThroughput();
        reportedMetric =
                MetricTestUtils.getReportedMetric(MetricNames.ITEMS_PROCESSING_RATE, AttributeNames.ITEMS_TYPE,
                                                  metricsLabel);
        Assert.assertEquals(reportedMetric, tracker.getOverallRate(), 500.0);
    }

    @Test
    public void givenBuilder_whenConfiguringForMilliseconds_thenBuilt() {
        // Given and When
        ThroughputTracker tracker = ThroughputTracker.create()
                                                     .logger(TestThroughputTracker.class)
                                                     .inMilliseconds()
                                                     .metricsLabel("test")
                                                     .build();

        // Then
        Assert.assertNotNull(tracker);
    }

    @Test
    public void givenBuilder_whenConfiguringForSeconds_thenBuilt() {
        // Given and When
        ThroughputTracker tracker =
                ThroughputTracker.create().logger("test-logger").inSeconds().metricsLabel("test").build();

        // Then
        Assert.assertNotNull(tracker);
    }

    @Test
    public void givenBuilder_whenConfiguringForMinutes_thenBuilt() {
        // Given and When
        ThroughputTracker tracker = ThroughputTracker.create()
                                                     .logger(LoggerFactory.getLogger("ROOT"))
                                                     .inMinutes()
                                                     .metricsLabel("test")
                                                     .build();

        // Then
        Assert.assertNotNull(tracker);
    }

    @Test
    public void givenBuilder_whenConfiguringForHours_thenBuilt() {
        // Given and When
        ThroughputTracker tracker =
                ThroughputTracker.create().logger("test").reportTimeUnit(TimeUnit.HOURS).metricsLabel("test").build();

        // Then
        Assert.assertNotNull(tracker);
    }

    @DataProvider(name = "awkwardTrackers")
    private Object[][] awkwardTrackerIncrements() {
        return new Object[][] {
                { 10_000, 100, 3 },
                { 10_000, 100, 7 },
                { 10_000, 100, 23 },
                { 10_000, 100, 59 },
                { 10_000, 100, 101 },
                { 10_000, 100, 500 },
                { 10_000, 1_000, 3 },
                { 10_000, 1_000, 7 },
                { 10_000, 1_000, 23 },
                { 10_000, 1_000, 59 },
                { 10_000, 1_000, 1061 },
                { 10_000, 1_000, 2_500 }
        };
    }

    @Test(dataProvider = "awkwardTrackers")
    public void givenTracker_whenTrackingInAwkwardIncrements_thenCountsAreCorrect(int count, int reportBatchSize,
                                                                                  int increment) {
        // Given
        ThroughputTracker tracker = new ThroughputTracker(LOGGER, reportBatchSize, TimeUnit.SECONDS,
                                                          ThroughputTracker.DEFAULT_ACTION,
                                                          ThroughputTracker.DEFAULT_ITEMS_NAME, null);

        verifyInitialState(tracker);

        // When
        for (int i = 1; i <= count; i++) {
            tracker.itemReceived();
            if (tracker.receivedCount() % increment == 0) {
                tracker.itemsProcessed(increment);
            }
        }
        if (tracker.receivedCount() > tracker.processedCount()) {
            tracker.itemsProcessed((int) (tracker.receivedCount() - tracker.processedCount()));
        }

        // Then
        verifyAwkwardTrackedCounts(count, reportBatchSize, increment, tracker);
    }

    private void verifyAwkwardTrackedCounts(int count, int reportBatchSize, int increment, ThroughputTracker tracker) {
        Assert.assertEquals(tracker.receivedCount(), count);
        Assert.assertEquals(tracker.processedCount(), count);
        if (count > reportBatchSize) {
            // Number of reports triggered will depend on batch and increment size
            int reportsByBatchSize = count / reportBatchSize;
            int reportsByIncrement = count / increment;
            if (Math.abs(reportsByBatchSize - reportsByIncrement) == 1) {
                // When the increment is slightly larger than the batch size we'll expect 1 less reports batches BUT
                // since we always do a final itemsProcessed() to bring our processed count exactly to our received
                // count we'll trigger a final report in that case so the larger number of expected reports is correct
                Assert.assertEquals(testLogger.getLoggingEvents().size(),
                                    Math.max(reportsByBatchSize, reportsByIncrement));
            } else {
                // If the increment is less than the batch size, or much greater than it then we'll trigger the smaller
                // number of expected reports
                Assert.assertEquals(testLogger.getLoggingEvents().size(),
                                    Math.min(reportsByBatchSize, reportsByIncrement));
            }
        } else {
            Assert.assertEquals(testLogger.getLoggingEvents().size(), 0);
        }
    }
}
