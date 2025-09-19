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

import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.builder.SinkBuilder;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.function.Supplier;

/**
 * A builder for {@link ProjectorDriver} instances
 *
 * @param <TKey>    Key type
 * @param <TValue>  Value type
 * @param <TOutput> Output type
 */
public class ProjectorDriverBuilder<TKey, TValue, TOutput> {

    private EventSource<TKey, TValue> source;
    private Duration pollTimeout = Duration.ofSeconds(30);
    private Projector<Event<TKey, TValue>, TOutput> projector;
    private Supplier<Sink<TOutput>> sinkSupplier;
    private long limit = -1, maxStalls = 0, reportBatchSize = 10_000L;
    private String logLabel;
    private boolean processingSpeedWarnings = true;

    /**
     * Specifies the event source for the projector driver
     *
     * @param source Event source
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> source(EventSource<TKey, TValue> source) {
        this.source = source;
        return this;
    }

    /**
     * Specifies the poll timeout used when polling for events from the event source
     *
     * @param duration Duration
     * @param unit     Temporal Unit
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> pollTimeout(long duration, TemporalUnit unit) {
        return pollTimeout(Duration.of(duration, unit));
    }

    /**
     * Specifies the poll timeout used when polling for events from the event source
     *
     * @param pollTimeout Duration
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> pollTimeout(Duration pollTimeout) {
        this.pollTimeout = pollTimeout;
        return this;
    }

    /**
     * Specifies the projector used to project the incoming events from the configured event source to the configured
     * destination sink
     *
     * @param projector Projector
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> projector(Projector<Event<TKey, TValue>, TOutput> projector) {
        this.projector = projector;
        return this;
    }

    /**
     * Specifies the destination sink to which the output of the projection is sent
     *
     * @param sink Destination sink
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> destination(Sink<TOutput> sink) {
        return destination(() -> sink);
    }

    /**
     * Specifies the destination sink to which the output of the projection is sent
     *
     * @param sinkBuilder   Destination sink builder
     * @param <TOutputSink> Output sink type
     * @return Builder
     */
    public <TOutputSink extends Sink<TOutput>> ProjectorDriverBuilder<TKey, TValue, TOutput> destinationBuilder(
            SinkBuilder<TOutput, TOutputSink> sinkBuilder) {
        return destination(sinkBuilder::build);
    }

    /**
     * Specifies the destination sink to which the output of the projection is sent
     *
     * @param sinkSupplier Destination sink supplier
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> destination(Supplier<Sink<TOutput>> sinkSupplier) {
        this.sinkSupplier = sinkSupplier;
        return this;
    }

    /**
     * Specifies that the driver will not impose any limit on the number of projected events i.e. it will run
     * indefinitely unless {@link #maxStalls(long)} has been configured
     *
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> unlimited() {
        return limit(-1);
    }

    /**
     * Specifies the maximum number of events that the driver will project before exiting
     *
     * @param limit Limit
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> limit(long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Specifies that the driver will not impose any limit on the number of stalls (see {@link #maxStalls(long)} for
     * definition of a stall) i.e. the driver will run indefinitely unless a {@link #limit(long)} has been configured
     *
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> unlimitedStalls() {
        return maxStalls(0);
    }

    /**
     * Specifies the maximum number of permitted consecutive stalls, a stall is when the driver polls the event source
     * and receives no new events within the configured poll timeout.
     * <p>
     * As a practical example if you have a 30 second poll timeout and 5 maximum stalls then if the event source
     * produces no new events for a 2.5 minute period (30 seconds times 5) the driver would abort itself.
     * </p>
     *
     * @param maxStalls Maximum permitted stalls
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> maxStalls(long maxStalls) {
        this.maxStalls = maxStalls;
        return this;
    }

    /**
     * Specifies the reporting batch size i.e. how often the driver will report progress.  This batch size is expressed
     * in terms of the number of events read from the event source, so a batch size of {@code 5000} would mean progress
     * is reported after every 5000 events.
     *
     * @param reportBatchSize Reporting batch size
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> reportBatchSize(long reportBatchSize) {
        this.reportBatchSize = reportBatchSize;
        return this;
    }

    /**
     * Configures whether processing speed warnings are enabled.
     * <p>
     * These warnings are normally triggered when the projection is stalled, i.e. no new events have been received, if
     * the observed processing rate (events/second) exceeds the number of events remaining in the {@link EventSource}.
     * These warnings can be useful as they indicate when the projection is running faster than the upstream
     * applications that are feeding events into the source e.g. writing to a Kafka topic.
     * </p>
     * <p>
     * However, in some usage scenarios, where an event source is known to be low throughput these warnings are spurious
     * and don't add any value so it may be useful to disable them.
     * </p>
     *
     * @param processingSpeedWarnings Whether processing speed warnings are enabled
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> processingSpeedWarnings(boolean processingSpeedWarnings) {
        this.processingSpeedWarnings = processingSpeedWarnings;
        return this;
    }

    /**
     * Enables processing speed warnings (the default)
     * <p>
     * See {@link #processingSpeedWarnings(boolean)} for details.
     * </p>
     *
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> enableProcessingSpeedWarnings() {
        return processingSpeedWarnings(true);
    }

    /**
     * Disables processing speed warnings
     * <p>
     * See {@link #processingSpeedWarnings(boolean)} for details.
     * </p>
     *
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> disabledProcessingSpeedWarnings() {
        return processingSpeedWarnings(false);
    }

    /**
     * Specifies a label that will be used at the start of log messages
     * <p>
     * This is useful for applications that run multiple {@link ProjectorDriver} instances to help make log output
     * clearer.
     * </p>
     *
     * @param logLabel Log label
     * @return Builder
     */
    public ProjectorDriverBuilder<TKey, TValue, TOutput> logLabel(String logLabel) {
        this.logLabel = logLabel;
        return this;
    }

    /**
     * Builds a new projector driver
     *
     * @return Projector Driver
     */
    public ProjectorDriver<TKey, TValue, TOutput> build() {
        return new ProjectorDriver<>(source, pollTimeout, projector, sinkSupplier, limit, maxStalls, reportBatchSize,
                                     logLabel, processingSpeedWarnings);
    }
}
