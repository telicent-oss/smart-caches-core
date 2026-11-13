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
import java.util.function.LongSupplier;

/**
 * A sink which decides whether to accept events based on distribution status, throwing a
 * {@link io.telicent.smart.cache.projectors.SinkException} for rejected events to allow higher level logic to deal with
 * DLQ'ing as appropriate.
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
@ToString(onlyExplicitlyIncluded = true)
public class DistributionAcceptanceSink<TKey, TValue>
        extends AbstractTransformingSink<Event<TKey, TValue>, Event<TKey, TValue>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionAcceptanceSink.class);

    public static final String MISSING_DISTRIBUTION_ID_ERROR =
            "Rejected event because ROUTE_TO_NAMED_GRAPHS=true and the required Distribution-Id header was missing";
    public static final String DELETED_DISTRIBUTION_ERROR =
            "Rejected event because its distribution has been deleted: ";
    public static final String UNREGISTERED_DISTRIBUTION_ERROR =
            "Rejected event because its distribution is not registered with the distribution lifecycle: ";

    public static final Duration DEFAULT_UNREGISTERED_GRACE_PERIOD = Duration.ofSeconds(30);
    public static final Duration DEFAULT_UNREGISTERED_RECHECK_INTERVAL = Duration.ofMillis(500);
    protected static final String REJECTED_METRIC_NAME = "distribution.lifecycle.ingest.filtered";
    protected static final String REJECTED_METRIC_DESCRIPTION =
            "Counter that tracks how many events were rejected by the distribution lifecycle ingest filtering per Distribution ID";
    protected static final String ACCEPTED_METRIC_NAME = "distribution.lifecycle.ingest.accepted";
    protected static final String ACCEPTED_METRIC_DESCRIPTION =
            "Counter that tracks how many events were accepted by the distribution lifecycle ingest filtering per Distribution ID";

    /**
     * Sleeps for the given number of milliseconds
     */
    @FunctionalInterface
    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    @ToString.Include
    private final DistributionLifecycleStateStore stateStore;
    @ToString.Include
    private final boolean rejectUnregistered;
    @ToString.Include
    private final Duration unregisteredGracePeriod;
    @ToString.Include
    private final Duration unregisteredCheckInterval;
    private final Sleeper sleeper;
    private final LongSupplier clock;
    // NB - in-memory only, so after a restart the first event for a still-deleted distribution is dead-lettered again,
    //      and an unregistered distribution gets its grace period again
    private final Set<String> deletedAlreadyRejected = ConcurrentHashMap.newKeySet();
    private final Set<String> unregisteredGraceSpent = ConcurrentHashMap.newKeySet();
    private volatile boolean closed = false;
    private final LongCounter rejectedCounter;
    private final LongCounter acceptedCounter;
    private final Map<String, Attributes> metricAttributes = new HashMap<>();
    // Semaphore, used for the default sleep function and allows close() to immediately interrupt a sleep by releasing a
    // permit
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
     * @param sleeper                   Sleeper to wait between rechecks
     * @param clock                     Clock used to determine how much time has elapsed
     * @param destination               Forwarding destination
     */
    protected DistributionAcceptanceSink(DistributionLifecycleStateStore stateStore, boolean rejectUnregistered,
                                         Duration unregisteredGracePeriod, Duration unregisteredCheckInterval,
                                         Sleeper sleeper, LongSupplier clock, Sink<Event<TKey, TValue>> destination) {
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
        this.sleeper =
                Objects.requireNonNullElse(sleeper, millis -> this.sleep.tryAcquire(1, millis, TimeUnit.MILLISECONDS));
        this.clock = Objects.requireNonNullElse(clock, System::currentTimeMillis);

        Meter meter = TelicentMetrics.getMeter("distribution-lifecycle");
        this.rejectedCounter =
                meter.counterBuilder(REJECTED_METRIC_NAME).setDescription(REJECTED_METRIC_DESCRIPTION).build();
        this.acceptedCounter =
                meter.counterBuilder(ACCEPTED_METRIC_NAME).setDescription(ACCEPTED_METRIC_DESCRIPTION).build();
    }

    @Override
    protected Event<TKey, TValue> transform(Event<TKey, TValue> event) {
        return event;
    }

    @Override
    protected boolean shouldForward(Event<TKey, TValue> event) {
        if (this.closed) {
            throw new IllegalStateException("Sink already closed");
        }

        // A missing Distribution ID is always an error
        String distributionId = event.lastHeader(TelicentHeaders.DISTRIBUTION_ID);
        if (StringUtils.isBlank(distributionId)) {
            this.rejectedCounter.add(1, distributionAttributes("<missing>"));
            throw new SinkException(MISSING_DISTRIBUTION_ID_ERROR);
        }

        DistributionLifecycleState state = this.stateStore.getLifecycleState(distributionId);

        // If the distribution is unregistered, and we haven't previously waited the grace period for that distribution,
        // then wait to see if we receive a registration event
        if (state == DistributionLifecycleState.Unregistered && this.rejectUnregistered && !this.unregisteredGraceSpent.contains(
                distributionId)) {
            state = awaitRegistration(distributionId);
        }

        // If the distribution is deleted explicitly reject the first such event seen and discard the subsequent ones
        if (state == DistributionLifecycleState.Deleted) {
            this.unregisteredGraceSpent.remove(distributionId);
            this.rejectedCounter.add(1, distributionAttributes(distributionId));
            if (this.deletedAlreadyRejected.add(distributionId)) {
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
            if (this.unregisteredGraceSpent.add(distributionId)) {
                throw new SinkException(UNREGISTERED_DISTRIBUTION_ERROR + distributionId);
            } else {
                LOGGER.debug("Discarded event for previously rejected unregistered distribution {}", distributionId);
                return false;
            }
        }

        // Viable - forget any earlier rejection so it is re-evaluated if the state later changes again
        this.deletedAlreadyRejected.remove(distributionId);
        this.unregisteredGraceSpent.remove(distributionId);
        this.acceptedCounter.add(1, distributionAttributes(distributionId));
        return true;
    }

    /**
     * Gets the metric attributes used for reporting accepted/rejected counts for different distributions
     *
     * @param distributionId Distribution ID
     * @return Distribution Attributes
     */
    private Attributes distributionAttributes(String distributionId) {
        return metricAttributes.computeIfAbsent(distributionId,
                                                id -> Attributes.of(AttributeKey.stringKey("distributionId"), id));
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
        final long deadline = this.clock.getAsLong() + this.unregisteredGracePeriod.toMillis();
        DistributionLifecycleState state = this.stateStore.getLifecycleState(distributionId);
        while (!this.closed && state == DistributionLifecycleState.Unregistered && this.clock.getAsLong() - deadline < 0) {
            try {
                this.sleeper.sleep(this.unregisteredCheckInterval.toMillis());
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
        private Sleeper sleeper;
        private LongSupplier clock;

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
         * Sets a grace period for which events pertaining to unregistered distributions are held before rejecting
         * them.
         * <p>
         * This addresses the fact that an application is consuming input events separately from lifecycle events,
         * therefore there may be a small lag between when an event registered a distribution is sent and when that is
         * actually effective.  Therefore, when the state store reports that a distribution is unregistered we wait up
         * to the grace period to see if a registration event has been received before rejecting it.
         * </p>
         * <p>
         * Note that the grace period is applied once per unregistered distribution, after that we will check to see if
         * the state store now knows about that distribution and if so accept later events.  However, if we applied the
         * grace period more than once a large block of events from an unregistered distribution would block processing
         * for a long time.
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

        /**
         * Sets the sleep function to use
         * <p>
         * If not explicitly specified then an internal sleep function based on {@link Semaphore#tryAcquire()} is used
         * as that allows sleep to be immediately interrupted by releasing permits when the sink is closed.
         * </p>
         *
         * @param sleeper Sleep function
         * @return Builder
         */
        public Builder<TKey, TValue> sleeper(Sleeper sleeper) {
            this.sleeper = sleeper;
            return this;
        }

        /**
         * Sets the clock to use that returns current time in milliseconds
         * <p>
         * If not specified then {@link System#currentTimeMillis()} is used
         * </p>
         *
         * @param clock Clock
         * @return Builder
         */
        public Builder<TKey, TValue> clock(LongSupplier clock) {
            this.clock = clock;
            return this;
        }

        @Override
        public DistributionAcceptanceSink<TKey, TValue> build() {
            return new DistributionAcceptanceSink<>(this.stateStore, this.rejectUnregistered,
                                                    this.unregisteredGracePeriod, this.unregisteredCheckInterval,
                                                    this.sleeper, this.clock, this.getDestination());
        }
    }
}
