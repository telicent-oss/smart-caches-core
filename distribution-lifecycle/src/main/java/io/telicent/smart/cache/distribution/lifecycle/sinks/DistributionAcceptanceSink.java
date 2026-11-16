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

import com.apicatalog.jsonld.StringUtils;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.observability.TelicentMetrics;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.projectors.sinks.AbstractTransformingSink;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.TelicentHeaders;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A sink which decides whether to accept events based on distribution lifecycle state, throwing a
 * {@link io.telicent.smart.cache.projectors.SinkException} for rejected events to allow higher level logic to deal with
 * DLQ'ing as appropriate.
 * <p>
 * This is intended for injection into the start of an applications {@link Sink} pipeline to provide standardised logic
 * about the acceptance/rejection (and discarding) of events based on their declared
 * {@value TelicentHeaders#DISTRIBUTION_ID}, and the distributions current state as reported by the provided
 * {@link DistributionLifecycleStateStore}.  This then avoids each application having to implement their own version of
 * the same logic, and provides a single place for customising behaviour.  See also
 * {@link
 * io.telicent.smart.cache.distribution.lifecycle.config.DistributionLifecycleConfiguration#acceptanceSinkBuilder(DistributionLifecycleStateStore)}
 * for a configuration driven helper for building an instance of this.
 * </p>
 * <p>
 * Events are acceptable if all of the following are true:
 * </p>
 * <ul>
 *   <li>The event has a {@value TelicentHeaders#DISTRIBUTION_ID} header present with a non-blank value.</li>
 *   <li>The provided state store indicates that the distribution is in the
 *       {@link DistributionLifecycleState#Registered},  {@link DistributionLifecycleState#Active} or
 *       {@link DistributionLifecycleState#Withdrawn} states.</li>
 *   <li>The event is <strong>not</strong> in the {@link DistributionLifecycleState#Deleted} state.</li>
 *   <li>If the distribution is in the {@link DistributionLifecycleState#Unregistered} state, and is the first event
 *       we've seen for that distribution, the distribution moves into one of the eligible states within the grace
 *       period.</li>
 * </ul>
 * <p>
 * For some kinds of rejections once an event has been rejected for the distribution subsequent events for the
 * distribution are simply discarded without throwing an explicit error.  This helps to avoid a bad distribution filling
 * the DLQs excessively.
 * </p>
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 * @see Builder#unregisteredGracePeriod(Duration, Duration)
 */
@ToString(onlyExplicitlyIncluded = true)
public class DistributionAcceptanceSink<TKey, TValue>
        extends AbstractTransformingSink<Event<TKey, TValue>, Event<TKey, TValue>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionAcceptanceSink.class);

    /**
     * Error message generated when an event has is missing the {@value TelicentHeaders#DISTRIBUTION_ID} header, or it
     * carries a blank value
     */
    public static final String MISSING_DISTRIBUTION_ID_ERROR =
            "Rejected event because ROUTE_TO_NAMED_GRAPHS=true and the required Distribution-Id header was missing";
    /**
     * Error message generated when an event is rejected due to being associated with a deleted distribution
     */
    public static final String DELETED_DISTRIBUTION_ERROR =
            "Rejected event because its distribution has been deleted: ";
    /**
     * Error message generated when an event is rejected due to being associated with an unregistered distribution
     */
    public static final String UNREGISTERED_DISTRIBUTION_ERROR =
            "Rejected event because its distribution is not registered with the distribution lifecycle: ";

    /**
     * Default grace period to wait before rejecting the first event seen for a previously unknown unregistered
     * distribution, see {@link Builder#unregisteredGracePeriod(Duration, Duration)} for details.
     */
    public static final Duration DEFAULT_UNREGISTERED_GRACE_PERIOD = Duration.ofSeconds(30);
    /**
     * Default grace period check interval used to wait between queries to the state store during the grace period to
     * see if a previously unknown unregistered distribution has moved to a state that makes it eligible for ingest. See
     * {@link Builder#unregisteredGracePeriod(Duration, Duration)} for details.
     */
    public static final Duration DEFAULT_UNREGISTERED_RECHECK_INTERVAL = Duration.ofMillis(500);
    /**
     * Common metric name prefix for metrics emitted by this sink
     */
    public static final String METRIC_PREFIX = "distribution.lifecycle.ingest.";
    /**
     * Name of the metric that reports how many events have been rejected due to missing Distribution ID, or a
     * Distribution state ineligible for ingest.
     *
     * @see #METRIC_ATTRIBUTE_DISTRIBUTION_ID
     */
    public static final String REJECTED_METRIC_NAME = METRIC_PREFIX + "rejected";
    /**
     * Description of the {@link #REJECTED_METRIC_NAME} metric
     */
    public static final String REJECTED_METRIC_DESCRIPTION =
            "Counter that tracks how many events were rejected/discarded by the distribution lifecycle ingest filtering per Distribution ID";
    /**
     * Name of the metric that reports how many events have been accepted for ingest
     *
     * @see #METRIC_ATTRIBUTE_DISTRIBUTION_ID
     */
    public static final String ACCEPTED_METRIC_NAME = METRIC_PREFIX + "accepted";
    /**
     * Description of the {@link #ACCEPTED_METRIC_NAME}
     */
    public static final String ACCEPTED_METRIC_DESCRIPTION =
            "Counter that tracks how many events were accepted by the distribution lifecycle ingest filtering per Distribution ID";

    /**
     * Name of the metric that reports how many events have been successfully processed by the destination sink
     *
     * @see #METRIC_ATTRIBUTE_DISTRIBUTION_ID
     */
    public static final String PROCESSED_METRIC_NAME = METRIC_PREFIX + "processed";
    /**
     * Description of the {@link #PROCESSED_METRIC_NAME}
     */
    public static final String PROCESSED_METRIC_DESCRIPTION =
            "Counter that tracks how many events were successfully processed by the destination sink per Distribution ID";
    /**
     * Metric attribute used to associate counts with specific distributions, this means that we effectively emit a
     * separate count for each Distribution ID.
     *
     * @see #MISSING_DISTRIBUTION
     */
    public static final String METRIC_ATTRIBUTE_DISTRIBUTION_ID = "distributionId";
    /**
     * Special value used with the {@link #METRIC_ATTRIBUTE_DISTRIBUTION_ID} when rejecting an event due to it being
     * missing a Distribution ID
     */
    public static final String MISSING_DISTRIBUTION = "<missing>";

    @ToString.Include
    private final DistributionLifecycleStateStore stateStore;
    @ToString.Include
    @Getter
    private final boolean rejectUnregistered;
    @ToString.Include
    @Getter
    private final boolean discardAfterRejection;
    @ToString.Include
    @Getter
    private final Duration unregisteredGracePeriod;
    @ToString.Include
    @Getter
    private final Duration unregisteredCheckInterval;
    // NB - in-memory only, so after a restart the first event for a still-deleted distribution is dead-lettered again,
    //      and an unregistered distribution gets its grace period again
    private final Set<String> deletedAlreadyRejected = ConcurrentHashMap.newKeySet();
    private final Set<String> unregisteredGraceSpent = ConcurrentHashMap.newKeySet();
    private volatile boolean closed = false;
    private final LongCounter rejectedCounter;
    private final LongCounter acceptedCounter;
    private final LongCounter processedCounter;
    private final Map<String, Attributes> metricAttributes = new HashMap<>();
    /**
     * A semaphore used by {@link #awaitRegistration(String)} to allow {@link #close()} to immediately interrupt a wait
     */
    private final Semaphore sleep = new Semaphore(0);

    /**
     * Creates a new sink with an optional forwarding destination
     * <p>
     * If no forwarding destination is provided then the discarding {@link NullSink} is used.
     * </p>
     *
     * @param stateStore                Distribution Lifecycle State Store
     * @param rejectUnregistered        Whether unregistered distributions are rejected (subject to the grace period),
     *                                  or accepted
     * @param unregisteredGracePeriod   Unregistered grace period, see
     *                                  {@link Builder#unregisteredGracePeriod(Duration, Duration)}
     * @param unregisteredCheckInterval Unregistered grace period check interval, see
     *                                  {@link Builder#unregisteredGracePeriod(Duration, Duration)}
     * @param discardAfterRejection     Whether events for a distribution which have already been rejected should be
     *                                  discarded, or explicitly rejected.
     * @param destination               Forwarding destination
     */
    protected DistributionAcceptanceSink(DistributionLifecycleStateStore stateStore, boolean rejectUnregistered,
                                         Duration unregisteredGracePeriod, Duration unregisteredCheckInterval,
                                         boolean discardAfterRejection, Sink<Event<TKey, TValue>> destination) {
        super(destination);
        this.stateStore = Objects.requireNonNull(stateStore, "State store cannot be null");
        this.rejectUnregistered = rejectUnregistered;
        this.unregisteredGracePeriod =
                Objects.requireNonNullElse(unregisteredGracePeriod, DEFAULT_UNREGISTERED_GRACE_PERIOD);
        this.unregisteredCheckInterval =
                Objects.requireNonNullElse(unregisteredCheckInterval, DEFAULT_UNREGISTERED_RECHECK_INTERVAL);
        if (this.rejectUnregistered) {
            if (this.unregisteredGracePeriod.isZero() || this.unregisteredGracePeriod.isNegative()) {
                throw new IllegalArgumentException("Unregistered grace period must be a positive duration");
            }
            if (this.unregisteredCheckInterval.isZero() || this.unregisteredCheckInterval.isNegative()) {
                throw new IllegalArgumentException("Unregistered check interval must be a positive duration");
            }
        }
        this.discardAfterRejection = discardAfterRejection;

        Meter meter = TelicentMetrics.getMeter("distribution-lifecycle");
        this.rejectedCounter =
                meter.counterBuilder(REJECTED_METRIC_NAME).setDescription(REJECTED_METRIC_DESCRIPTION).build();
        this.acceptedCounter =
                meter.counterBuilder(ACCEPTED_METRIC_NAME).setDescription(ACCEPTED_METRIC_DESCRIPTION).build();
        this.processedCounter =
                meter.counterBuilder(PROCESSED_METRIC_NAME).setDescription(PROCESSED_METRIC_DESCRIPTION).build();
    }

    @Override
    protected Event<TKey, TValue> transform(Event<TKey, TValue> event) {
        // No-op, this sink does not transform events
        return event;
    }

    @Override
    protected boolean shouldForward(Event<TKey, TValue> event) {
        // A closed sink immediately rejects events
        if (this.closed) {
            throw new IllegalStateException("Sink already closed");
        }

        // A missing Distribution ID is always an error
        String distributionId = event.lastHeader(TelicentHeaders.DISTRIBUTION_ID);
        if (StringUtils.isBlank(distributionId)) {
            LOGGER.debug("Rejected event with no distribution declared");
            this.rejectedCounter.add(1, distributionAttributes(MISSING_DISTRIBUTION));
            throw new SinkException(MISSING_DISTRIBUTION_ID_ERROR);
        }

        DistributionLifecycleState state = this.stateStore.getLifecycleState(distributionId);

        // If the distribution is unregistered, and we haven't previously waited the grace period for that distribution,
        // then wait to see if we receive a registration event
        if (shouldAwaitRegistration(distributionId, state)) {
            state = awaitRegistration(distributionId);
            if (state != DistributionLifecycleState.Unregistered) {
                LOGGER.info("Distribution {} entered state {} during grace period", distributionId, state);
            }
        }

        // If the distribution is deleted explicitly reject the first such event seen and discard the subsequent ones
        if (state == DistributionLifecycleState.Deleted) {
            this.unregisteredGraceSpent.remove(distributionId);
            this.rejectedCounter.add(1, distributionAttributes(distributionId));
            if (this.deletedAlreadyRejected.add(distributionId) || !this.discardAfterRejection) {
                LOGGER.warn("Rejected event for deleted distribution {}", distributionId);
                throw new SinkException(DELETED_DISTRIBUTION_ERROR + distributionId);
            } else {
                LOGGER.debug("Discarded event for previously rejected deleted distribution {}", distributionId);
                return false;
            }
        }
        // If the distribution is unregistered, and we aren't accepting unregistered distributions, then reject the
        // first event and discard subsequent ones
        if (state == DistributionLifecycleState.Unregistered && this.rejectUnregistered) {
            this.rejectedCounter.add(1, distributionAttributes(distributionId));
            if (this.unregisteredGraceSpent.add(distributionId) || !this.discardAfterRejection) {
                LOGGER.warn("Rejected event for unregistered distribution {}", distributionId);
                throw new SinkException(UNREGISTERED_DISTRIBUTION_ERROR + distributionId);
            } else {
                LOGGER.debug("Discarded event for previously rejected unregistered distribution {}", distributionId);
                return false;
            }
        }

        // Viable - forget any earlier rejection so it is re-evaluated if the state later changes again
        // Also update the per distribution accepted counter so metrics track how many events have been accepted
        this.deletedAlreadyRejected.remove(distributionId);
        this.unregisteredGraceSpent.remove(distributionId);
        this.acceptedCounter.add(1, distributionAttributes(distributionId));
        return true;
    }

    /**
     * Determines whether the sink should call {@link #awaitRegistration(String)} for the distribution before making a
     * decision about whether to accept/reject the event.
     *
     * @param distributionId Distribution ID
     * @param state          Distribution State
     * @return True if it should await, false if the state is not {@link DistributionLifecycleState#Unregistered}, we
     * have already used the grace period for this unregistered distribution, or we are accepting unregistered
     * distributions
     */
    private boolean shouldAwaitRegistration(String distributionId, DistributionLifecycleState state) {
        return state == DistributionLifecycleState.Unregistered && this.rejectUnregistered && !this.unregisteredGraceSpent.contains(
                distributionId);
    }

    @Override
    protected void forward(Event<TKey, TValue> event) {
        // Forward as normal,
        super.forward(event);

        // Update processed counter metric if successful, note that shouldForward() already rejected anything without a
        // Distribution ID so guaranteed to be a valid value for the header at this point
        String distributionId = event.lastHeader(TelicentHeaders.DISTRIBUTION_ID);
        this.processedCounter.add(1, distributionAttributes(distributionId));
    }

    /**
     * Gets the metric attributes used for reporting accepted/rejected counts for different distributions
     *
     * @param distributionId Distribution ID
     * @return Distribution Attributes
     */
    private Attributes distributionAttributes(String distributionId) {
        return metricAttributes.computeIfAbsent(distributionId, id -> Attributes.of(
                AttributeKey.stringKey(METRIC_ATTRIBUTE_DISTRIBUTION_ID), id));
    }

    /**
     * Waits to see if a registration event is going to be received by the application that would allow an event to be
     * accepted.
     * <p>
     * This deals with the fact that an application is processing incoming data events, and incoming lifecycle events in
     * parallel on separate threads.  So there may be a delay between a registration event being emitted onto Kafka, and
     * actually being received and applied to an applications state store.  Allowing a grace period avoids us rejecting
     * events where we simply haven't caught up with lifecycle events sufficiently and helps to ensure we only reject
     * events when the distribution is genuinely unregistered.
     * </p>
     * <p>
     * This method blocks up to the grace period unless one of the following conditions is met:
     * </p>
     * <ul>
     *     <li>The distribution is in a state other than {@link DistributionLifecycleState#Unregistered}</li>
     *     <li>The sink is closed</li>
     * </ul>
     *
     * @param distributionId Distribution ID
     * @return Updated distribution state, may still be {@link DistributionLifecycleState#Unregistered} if no
     * registration event is received within the grace period or the sink was closed while waiting.
     */
    private DistributionLifecycleState awaitRegistration(String distributionId) {
        final long deadline = System.currentTimeMillis() + this.unregisteredGracePeriod.toMillis();
        DistributionLifecycleState state = this.stateStore.getLifecycleState(distributionId);
        while (!this.closed && state == DistributionLifecycleState.Unregistered && System.currentTimeMillis() < deadline) {
            try {
                // If we do successfully acquire a permit this means close() has been called as that's the only place
                // where permits are released.  In that case the application is likely shutting down so want to return
                // immediately
                if (this.sleep.tryAcquire(1, this.unregisteredCheckInterval.toMillis(), TimeUnit.MILLISECONDS)) {
                    return state;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            state = this.stateStore.getLifecycleState(distributionId);
        }
        return state;
    }

    @Override
    public void close() {
        try {
            this.closed = true;
            this.sleep.release(1);
            super.close();
        } finally {
            this.deletedAlreadyRejected.clear();
            this.unregisteredGraceSpent.clear();
            this.metricAttributes.clear();
        }
    }

    /**
     * Gets a builder for a {@link DistributionAcceptanceSink}
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     * @return Builder
     */
    public static <TKey, TValue> Builder<TKey, TValue> create() {
        return new Builder<>();
    }

    /**
     * Builder for a {@link DistributionAcceptanceSink}
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     */
    public static final class Builder<TKey, TValue> extends
            AbstractForwardingSinkBuilder<Event<TKey, TValue>, Event<TKey, TValue>, DistributionAcceptanceSink<TKey, TValue>, Builder<TKey, TValue>> {

        private DistributionLifecycleStateStore stateStore;
        private Duration unregisteredGracePeriod;
        private Duration unregisteredCheckInterval;
        private boolean rejectUnregistered = true;
        private boolean discardAfterRejection = true;

        /**
         * Sets the state store used to check whether a distribution is in an acceptable state
         *
         * @param stateStore State store
         * @return Builder
         */
        public Builder<TKey, TValue> stateStore(DistributionLifecycleStateStore stateStore) {
            this.stateStore = stateStore;
            return this;
        }

        /**
         * Configures the sink to accept events from unregistered distributions
         *
         * @return Builder
         */
        public Builder<TKey, TValue> acceptUnregistered() {
            this.rejectUnregistered = false;
            return this;
        }

        /**
         * Configures the sink to reject events from unregistered distributions, subject to the
         * {@link #unregisteredGracePeriod(Duration, Duration)} configured
         *
         * @return Builder
         */
        public Builder<TKey, TValue> rejectUnregistered() {
            this.rejectUnregistered = true;
            return this;
        }

        /**
         * Configures the sink so that after an event has been rejected for a given distribution then subsequent events
         * for that distribution are discarded rather than rejected.
         * <p>
         * This is the default behaviour as it helps to ensure that a bad distribution doesn't completely fill the DLQ
         * by routing all its events there, rather routing only the first bad event seen per distribution and discarding
         * the rest.  If this is not your desired behaviour, and you'd prefer every bad event to be explicitly rejected
         * then please call {@link #rejectAfterRejection()} instead.
         * </p>
         *
         * @return Builder
         */
        public Builder<TKey, TValue> discardAfterRejection() {
            this.discardAfterRejection = true;
            return this;
        }

        /**
         * Configures the sink so that every event rejected for a distribution is always explicitly rejected and never
         * discarded.
         *
         * @return Builder
         * @see #rejectAfterRejection()
         */
        public Builder<TKey, TValue> rejectAfterRejection() {
            this.discardAfterRejection = false;
            return this;
        }

        /**
         * Sets a grace period for which events pertaining to unregistered distributions are held before rejecting
         * them.
         * <p>
         * This addresses the fact that an application is consuming input events separately from lifecycle events,
         * therefore there may be a small lag between when an event registering a distribution is sent and when that is
         * actually effective.  Therefore, when the state store reports that a distribution is unregistered we wait up
         * to the grace period to see if a registration event has been received before rejecting it.
         * </p>
         * <p>
         * Note that the grace period is applied once per unregistered distribution, after that we will check to see if
         * the state store now knows about that distribution and if so accept later events.  However, if we applied the
         * grace period more than once a large block of events from an unregistered distribution would block processing
         * for a long time.  Therefore, once the grace period has been "spent" for a distribution we don't apply it
         * again for that distribution.
         * </p>
         *
         * @param gracePeriod   Grace period
         * @param checkInterval Check interval during the grace period
         * @return Builder
         */
        public Builder<TKey, TValue> unregisteredGracePeriod(Duration gracePeriod, Duration checkInterval) {
            this.unregisteredGracePeriod = gracePeriod;
            this.unregisteredCheckInterval = checkInterval;
            return this;
        }

        @Override
        public DistributionAcceptanceSink<TKey, TValue> build() {
            return new DistributionAcceptanceSink<>(this.stateStore, this.rejectUnregistered,
                                                    this.unregisteredGracePeriod, this.unregisteredCheckInterval,
                                                    this.discardAfterRejection, this.getDestination());
        }
    }
}
