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
package io.telicent.smart.cache.live;

import io.telicent.smart.cache.live.model.IODescriptor;
import io.telicent.smart.cache.live.model.LiveHeartbeat;
import io.telicent.smart.cache.live.model.LiveStatus;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * A reporter creates a background thread within your JVM that periodically reports a
 * {@link io.telicent.smart.cache.live.model.LiveHeartbeat} that is used by Telicent Live to present information about
 * the state of applications running on the Telicent Core Platform.
 */
public class LiveReporter {
    /**
     * Default Kafka topic for Live Heartbeat Reporting
     */
    public static final String DEFAULT_LIVE_TOPIC = "provenance.live";
    /**
     * Default reporting period (in seconds)
     */
    public static final int DEFAULT_REPORTING_PERIOD_SECONDS = 15;
    /**
     * Default reporting period (as a duration)
     */
    public static final Duration DEFAULT_REPORTING_PERIOD_DURATION =
            Duration.ofSeconds(DEFAULT_REPORTING_PERIOD_SECONDS);
    private static final Logger LOGGER = LoggerFactory.getLogger(LiveReporter.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Sink<Event<Bytes, LiveHeartbeat>> sink;
    private final Duration reportingPeriod;
    private BackgroundReporter backgroundReporter = null;
    private Future<?> reporterFuture = null;
    private final LiveHeartbeat baseHeartbeat;

    /**
     * Creates a new {@link LiveReporterBuilder} for building a {@link LiveReporter} instance
     *
     * @return Builder
     */
    public static LiveReporterBuilder create() {
        return new LiveReporterBuilder();
    }

    /**
     * Creates a new live reporter
     *
     * @param sink            Sink
     * @param reportingPeriod Reporting period i.e. how often to report a heartbeat status
     * @param id              Application ID
     * @param name            Application Name
     * @param componentType   Component Type e.g. adapter, mapper, projector
     * @param input           Input descriptor
     * @param output          Output descriptor
     */
    LiveReporter(Sink<Event<Bytes, LiveHeartbeat>> sink, Duration reportingPeriod, String id, String name,
                 String componentType, IODescriptor input, IODescriptor output) {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("Application id cannot be null");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Application name cannot be null");
        }
        if (StringUtils.isBlank(componentType)) {
            throw new IllegalArgumentException("Application component type cannot be null");
        }
        Objects.requireNonNull(reportingPeriod, "Reporting period cannot be null");
        if (reportingPeriod.compareTo(Duration.ZERO) <= 0) {
            throw new IllegalArgumentException("Reporting period cannot be <= 0");
        }
        Objects.requireNonNull(input, "Input descriptor cannot be null");
        Objects.requireNonNull(output, "Output descriptor cannot be null");

        this.sink = sink != null ? sink : NullSink.of();
        if (sink == null) {
            LOGGER.warn("No sink specified, live heartbeats are not being reported anywhere!");
        }
        this.reportingPeriod = reportingPeriod;

        this.baseHeartbeat =
                new LiveHeartbeat(id, null, name, componentType, Date.from(Instant.now()),
                                  reportingPeriod.toSeconds(), input, output, LiveStatus.STARTED);
    }

    /**
     * Starts the reporter running
     */
    public void start() {
        if (this.reporterFuture != null) {
            throw new IllegalStateException("Reporter is already running");
        }

        synchronized (this.executor) {
            this.backgroundReporter =
                    new BackgroundReporter(this.baseHeartbeat.copy(), sink, this.reportingPeriod.toMillis(),
                                           TimeUnit.MILLISECONDS);
            this.reporterFuture = this.executor.submit(this.backgroundReporter);
            LOGGER.info("Live Reporter started with a reporting interval of {} milliseconds",
                        this.reportingPeriod.toMillis());

            // Add a shutdown hook that automatically stops the background reporter (assuming a graceful shutdown)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> backgroundReporter.stop(LiveStatus.TERMINATED)));
        }
    }

    /**
     * Stops the reporter running
     *
     * @param stopStatus Final status to report
     */
    public void stop(LiveStatus stopStatus) {
        if (this.reporterFuture == null) {
            LOGGER.warn("Reporter was already stopped");
            return;
        }

        synchronized (this.executor) {
            this.backgroundReporter.stop(stopStatus);
            LOGGER.info("Live Reporter stop requested with final status of {}", stopStatus);

            // Wait to allow the background thread to stop
            try {
                this.reporterFuture.get(3, TimeUnit.SECONDS);
            } catch (Throwable e) {
                // Intentionally ignored, means the background thread either died or didn't finish in time
            }
            if (this.reporterFuture.isDone()) {
                LOGGER.info(
                        "Live Reporter detects that the background reporter thread has completed.  This instance can now be restarted at a future time.");
                this.reporterFuture = null;
            } else {
                LOGGER.warn(
                        "Live Reporter detects that the background reporter is still running after asked to stop.  Please try calling stop() again.");
            }
        }
    }

    /**
     * The actual background reporter runnable that sends the heartbeat messages to the destination sink
     */
    private static final class BackgroundReporter implements Runnable {
        private boolean shouldRun = true, stopped = false;
        private LiveHeartbeat heartbeat;
        private final Sink<Event<Bytes, LiveHeartbeat>> sink;
        private final long reportingPeriod;
        private final TimeUnit reportingUnit;
        private final Semaphore lock = new Semaphore(1);
        private LiveStatus stopStatus = LiveStatus.COMPLETED;
        private boolean traceEnabled = LOGGER.isTraceEnabled();

        /**
         * Creates a new background reporter
         *
         * @param heartbeat       Base Heartbeat to send
         * @param sink            Destination Sink
         * @param reportingPeriod Reporting period
         * @param unit            Reporting period time unit
         */
        public BackgroundReporter(LiveHeartbeat heartbeat, Sink<Event<Bytes, LiveHeartbeat>> sink, long reportingPeriod,
                                  TimeUnit unit) {
            this.heartbeat = heartbeat;

            // Update the heartbeat status with initial parameters i.e. a unique Instance ID, started status and current
            // timestamp
            this.heartbeat.setInstanceId(UUID.randomUUID().toString());
            this.heartbeat.setStatus(LiveStatus.STARTED);
            this.heartbeat.setTimestampToNow();

            this.sink = sink;
            this.reportingPeriod = reportingPeriod;
            this.reportingUnit = unit;

            // Acquire the lock immediately on the foreground thread, then when in our run() method we try to acquire it
            // on the background thread we will be blocked.  This effectively acts as a wait without needing an explicit
            // Thread.sleep() anywhere.  It also allows us to quickly unblock the background thread by releasing the
            // lock allowing the reporter to promptly report final status
            try {
                this.lock.acquire();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Unable to acquire initial lock for background live reporter thread");
            }
        }


        @Override
        public void run() {
            try {
                Thread.currentThread().setName("BackgroundLiveReporter");
            } catch (Throwable e) {
                // Ignore if unable to set thread name
            }

            this.traceEnabled = LOGGER.isTraceEnabled();
            LOGGER.info("Background Live Reporter thread started");

            while (this.shouldRun) {
                // Send the current heartbeat status
                sendHeartbeat();

                // Try to acquire the lock.  This either blocks us for the reporting interval or if we've been asked to
                // stop succeeds and allows us to promptly proceed with our termination check
                try {
                    this.lock.tryAcquire(this.reportingPeriod, this.reportingUnit);
                } catch (InterruptedException interruptWhileBlocked) {
                    // Ignored
                }

                // Update heartbeat before we go round the loop/exit and send it again
                updateHeartbeat(LiveStatus.RUNNING);
            }

            // Send final heartbeat status and quit if we've been told to terminate
            updateHeartbeat(this.stopStatus);
            this.sendHeartbeat();
            LOGGER.info("Background Live Reporter thread terminated");

            this.sink.close();
        }

        /**
         * Updates the heartbeat status
         *
         * @param newStatus New status
         */
        private void updateHeartbeat(LiveStatus newStatus) {
            this.heartbeat = this.heartbeat.copy();
            this.heartbeat.setTimestampToNow();
            this.heartbeat.setStatus(newStatus);
        }

        /**
         * Sends the heartbeat
         */
        private void sendHeartbeat() {
            if (traceEnabled) {
                LOGGER.trace("Sending Live Heartbeat with status {}", this.heartbeat.getStatus());
            }
            this.sink.send(new SimpleEvent<>(Collections.emptyList(), null, this.heartbeat));
        }

        /**
         * Stops the background reporter
         *
         * @param status Final status to report
         */
        public void stop(LiveStatus status) {
            if (this.stopped) {
                return;
            }

            synchronized (this.lock) {
                this.stopStatus = status;
                this.shouldRun = false;
                // By releasing the lock we cause the run() method to be unblocked if it was in one of its blocked lock
                // acquisitions allowing it to run to termination as we've set the shouldRun flag to false
                this.lock.release();
                this.stopped = true;
            }
        }
    }
}
