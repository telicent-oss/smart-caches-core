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
package io.telicent.smart.cache.projectors.driver;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.telicent.smart.cache.observability.AttributeNames;
import io.telicent.smart.cache.observability.TelicentMetrics;
import io.telicent.smart.cache.projectors.Library;
import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.utils.ThroughputTracker;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * A projector driver connects an event source up to a projector and an output sink.
 * <p>
 * This basically wraps up a bunch of useful logic around polling for events from an {@link EventSource} and pushing the
 * events through a {@link Projector}.  It includes automated management of the polling loop alongside throughput
 * monitoring and reporting.
 * </p>
 *
 * @param <TKey>    Event key type
 * @param <TValue>  Event value type
 * @param <TOutput> Output type
 */
public class ProjectorDriver<TKey, TValue, TOutput> implements Runnable {

    /**
     * Creates a new builder for a {@link ProjectorDriver} instance
     *
     * @param <TKey>    Key type
     * @param <TValue>  Value type
     * @param <TOutput> Output type
     * @return Builder
     */
    public static <TKey, TValue, TOutput> ProjectorDriverBuilder<TKey, TValue, TOutput> create() {
        return new ProjectorDriverBuilder<>();
    }

    /**
     * Default items name used in throughput tracking output of the driver
     */
    public static final String DEFAULT_ITEMS_NAME = "Events";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectorDriver.class);

    private static final String ITEM_TYPE_EVENTS = "events";

    @Getter
    private final EventSource<TKey, TValue> source;
    @Getter
    private final Duration pollTimeout;
    @Getter
    private final Projector<Event<TKey, TValue>, TOutput> projector;
    private final StallAwareProjector<Event<TKey, TValue>, TOutput> stallAware;
    private final Supplier<Sink<TOutput>> sinkSupplier;
    @Getter
    private final long limit, maxStalls;
    private long consecutiveStallsCount;
    private final ThroughputTracker tracker;
    private volatile boolean shouldRun = true;
    private final Attributes metricAttributes;
    private final LongCounter stalls;

    /**
     * Creates a new driver
     *
     * @param source             Event source from which to read events
     * @param pollTimeout        Maximum time to wait for an {@link EventSource#poll(Duration)} operation to succeed
     * @param projector          Projector to project the events with
     * @param outputSinkSupplier A supplier that can provide a sink to which projected events will be output
     * @param limit              The maximum number of events to project before stopping, negative values are
     *                           interpreted as no limit
     * @param maxStalls          The maximum number of consecutive stalls, i.e. occasions where the event source fails
     *                           to return any new events, after which projection should be aborted.
     * @param reportBatchSize    Reporting batch size i.e. how often the driver should report throughput statistics
     */
    @SuppressWarnings("resource")
    ProjectorDriver(EventSource<TKey, TValue> source, Duration pollTimeout,
                    Projector<Event<TKey, TValue>, TOutput> projector, Supplier<Sink<TOutput>> outputSinkSupplier,
                    long limit, long maxStalls, long reportBatchSize) {
        Objects.requireNonNull(source, "Event Source cannot be null");
        Objects.requireNonNull(projector, "Projector cannot be null");
        Objects.requireNonNull(outputSinkSupplier, "Sink Supplier cannot be null");
        Objects.requireNonNull(pollTimeout, "Poll Timeout cannot be null");

        this.source = source;
        this.pollTimeout = pollTimeout;
        this.projector = projector;
        this.sinkSupplier = outputSinkSupplier;
        this.limit = limit;
        this.maxStalls = maxStalls;

        if (this.projector instanceof StallAwareProjector<Event<TKey, TValue>, TOutput> stallAwareProjector) {
            this.stallAware = stallAwareProjector;
        } else {
            this.stallAware = null;
        }

        this.metricAttributes = Attributes.of(AttributeKey.stringKey(AttributeNames.ITEMS_TYPE), ITEM_TYPE_EVENTS,
                                              AttributeKey.stringKey(AttributeNames.INSTANCE_ID),
                                              UUID.randomUUID().toString());
        Meter meter = TelicentMetrics.getMeter(Library.NAME);
        this.stalls = meter.counterBuilder(DriverMetricNames.STALLS_TOTAL)
                           .setDescription(DriverMetricNames.STALLS_TOTAL_DESCRIPTION)
                           .build();
        ObservableLongGauge consecutiveStalls = meter.gaugeBuilder(DriverMetricNames.STALLS_CONSECUTIVE)
                                                     .setDescription(DriverMetricNames.STALLS_CONSECUTIVE_DESCRIPTION)
                                                     .ofLongs()
                                                     .buildWithCallback(
                                                             measure -> measure.record(getConsecutiveStalls(),
                                                                                       this.metricAttributes));

        this.tracker = ThroughputTracker.create()
                                        .logger(LOGGER)
                                        .reportBatchSize(reportBatchSize)
                                        .inSeconds()
                                        .action("Projected")
                                        .itemsName(DEFAULT_ITEMS_NAME)
                                        .metricsLabel(ITEM_TYPE_EVENTS)
                                        .build();
    }

    /**
     * Gets how many times consecutively the projection has been stalled i.e. the number of consecutive
     * {@link EventSource#poll(Duration)} calls that have returned no new events
     *
     * @return Consecutive stall count
     */
    public long getConsecutiveStalls() {
        return this.consecutiveStallsCount;
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName("ProjectorDriver");
        } catch (Throwable e) {
            // Ignore if unable to set thread name
        }

        try (Sink<TOutput> sink = this.sinkSupplier.get()) {
            this.tracker.start();

            while (this.shouldRun) {
                if (this.source.isClosed()) {
                    LOGGER.warn("Event Source has been closed outside of our control, aborting projection");
                    throw new IllegalStateException("Event Source closed externally");
                }

                if (this.limit >= 0 && this.tracker.processedCount() >= this.limit) {
                    FmtLog.info(LOGGER, "Reached configured event limit of %,d events", this.limit);
                    this.shouldRun = false;
                    break;
                }

                Event<TKey, TValue> event;
                boolean expectToBlock = !this.source.availableImmediately();

                if (this.source.isExhausted()) {
                    LOGGER.info("Event Source indicates all events have been exhausted, ending projection");
                    this.shouldRun = false;
                    break;
                }

                event = this.source.poll(this.pollTimeout);

                if (event == null) {
                    // Log timeout, whether we choose to abort depends on whether we were expecting to block or not i.e.
                    // whether the source reliably reported the availability of further events
                    LOGGER.debug("Timed out waiting for Event Source to return more events, waited {}",
                                 this.pollTimeout);
                    this.stalls.add(1, this.metricAttributes);
                    this.consecutiveStallsCount++;

                    if (!expectToBlock) {
                        LOGGER.warn(
                                "Event Source incorrectly indicated that events were available but failed to return them, aborting projection");
                        this.shouldRun = false;
                        break;
                    }

                    if (this.maxStalls > 0 && this.consecutiveStallsCount >= this.maxStalls) {
                        LOGGER.info(
                                "Event Source is stalled, no new events have been received on the last {} polls, aborting projection",
                                this.maxStalls);
                        this.shouldRun = false;
                        break;
                    }

                    // If the projector is stall-aware inform it now
                    if (this.consecutiveStallsCount == 1 && this.stallAware != null) {
                        // Only do this on the first consecutive stall as otherwise we might inform it too frequently
                        this.stallAware.stalled(sink);
                    }

                    // If we've timed out check with the Event Source as to what events are remaining since a timeout
                    // may be indicative of being close to the end of the topic.
                    Long remaining = this.source.remaining();
                    if (remaining != null) {
                        if (this.consecutiveStallsCount == 1) {
                            // Only log this on the first time we have stalled otherwise we can be unnecessarily noisy
                            // and actually make it harder to debug any real problems that occur
                            if (remaining == 0L) {
                                LOGGER.info(
                                        "Event Source reports it currently has 0 events remaining i.e. all events have been processed");
                            } else {
                                FmtLog.info(LOGGER, "Event Source reports it only has %,d events remaining", remaining);
                            }

                            // Also if our current throughput is higher than the remaining events then we are being blocked
                            // by a slower downstream producer and should highlight this
                            double overallRate = this.tracker.getOverallRate();
                            if (overallRate > remaining) {
                                FmtLog.warn(LOGGER,
                                            "Overall processing rate (%.3f events/seconds) is greater than remaining events (%,d).  Application performance is being reduced by a slower upstream producer writing to %s",
                                            overallRate, remaining, this.source.toString());
                            }
                        }
                    }
                } else {
                    this.consecutiveStallsCount = 0;
                    this.tracker.itemReceived();
                    this.projector.project(event, sink);
                    this.tracker.itemProcessed();
                }
            }
        } catch (Throwable e) {
            // Log only if not some form of interrupt
            if (!StringUtils.contains(e.getClass().getCanonicalName(), "Interrupt")) {
                LOGGER.warn("Projector Driver aborting due to error: {}", e.getMessage());
                throw e;
            }
        } finally {
            this.tracker.reportThroughput();
            this.shouldRun = false;
            closeSource();
        }

    }

    private void closeSource() {
        if (!this.source.isClosed()) this.source.close();
    }

    /**
     * Cancels the projection
     * <p>
     * Since a {@link ProjectorDriver} is a {@link Runnable} task that will be running on a thread this method is used
     * from another thread to signal that projection should be cancelled.  Note that cancellation may not take effect
     * immediately depending on what the driver is doing at the time.
     * </p>
     */
    public void cancel() {
        this.shouldRun = false;

        // Interrupt the event source to speed up cancellation
        if (!this.source.isClosed()) {
            this.source.interrupt();
        }
    }
}
