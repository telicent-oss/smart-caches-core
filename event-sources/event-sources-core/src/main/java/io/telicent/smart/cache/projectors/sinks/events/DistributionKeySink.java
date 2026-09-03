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
package io.telicent.smart.cache.projectors.sinks.events;

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.AbstractTransformingSink;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import io.telicent.smart.cache.sources.DistributionIds;
import io.telicent.smart.cache.sources.DistributionKeyStrategy;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.TelicentHeaders;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A forwarding sink that sets the Distribution ID as the event's message key, per the Core Data Management design.
 * <p>
 * The Distribution ID is resolved from the event, by default preferring the existing message key and falling back to
 * the {@value TelicentHeaders#DISTRIBUTION_ID} header, then re-encoded as a message key using the configured
 * {@link DistributionKeyStrategy}.  Where the event does not carry a Distribution ID at all it is forwarded
 * <strong>unmodified</strong>, unless the sink is configured via
 * {@link Builder#requireDistributionId()} to require one in which case an {@link IllegalStateException} is thrown.
 * </p>
 * <p>
 * By default the sink also backfills the {@value TelicentHeaders#DISTRIBUTION_ID} header when it is missing.  The
 * design retains that header for backwards compatibility with existing pipelines and services, so an event that gains
 * a key should carry the header too.
 * </p>
 * <p>
 * The key type is not constrained.  Callers supply a {@code keyEncoder} that converts the generated key string into
 * their pipeline's key type, which keeps existing serializer configuration untouched.  For {@code Bytes} keyed Kafka
 * pipelines, i.e. most of them, use the pre-wired builder from
 * {@code io.telicent.smart.cache.sources.kafka.KafkaDistributionKeys} in the {@code event-source-kafka} module rather
 * than assembling this sink by hand.
 * </p>
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
@ToString(callSuper = true)
// java:S119 - TKey/TValue/TRequest generic naming convention is used across the codebase
@SuppressWarnings("java:S119")
public class DistributionKeySink<TKey, TValue> extends
        AbstractTransformingSink<Event<TKey, TValue>, Event<TKey, TValue>> {

    /**
     * Error thrown when an event has no Distribution ID but the sink was configured to require one
     */
    public static final String MISSING_DISTRIBUTION_ID_ERROR =
            "Cannot set the Distribution ID message key because the event has neither a Distribution ID message key nor a " + TelicentHeaders.DISTRIBUTION_ID + " header";

    @ToString.Exclude
    private final Function<String, TKey> keyEncoder;
    @ToString.Exclude
    private final Function<Event<TKey, TValue>, String> resolver;
    private final DistributionKeyStrategy strategy;
    private final boolean enabled;
    private final boolean requireDistributionId;
    private final boolean backfillHeader;

    /**
     * Creates a new sink
     *
     * @param destination           Destination sink
     * @param keyEncoder            Function converting the generated key string into the pipeline's key type
     * @param resolver              Function resolving the Distribution ID from an event, if {@code null} then
     *                              {@link DistributionIds#resolve(Event)} is used
     * @param strategy              Key strategy, if {@code null} then {@link DistributionKeyStrategy#DEFAULT} is used
     * @param enabled               Whether message keys are actually set, when {@code false} events are forwarded
     *                              unmodified which allows the behaviour to be disabled without restructuring a
     *                              pipeline
     * @param requireDistributionId Whether an event without a Distribution ID is an error
     * @param backfillHeader        Whether to add the {@value TelicentHeaders#DISTRIBUTION_ID} header when missing
     */
    DistributionKeySink(Sink<Event<TKey, TValue>> destination, Function<String, TKey> keyEncoder,
                        Function<Event<TKey, TValue>, String> resolver, DistributionKeyStrategy strategy,
                        boolean enabled, boolean requireDistributionId, boolean backfillHeader) {
        super(destination);
        this.keyEncoder = Objects.requireNonNull(keyEncoder, "Key Encoder cannot be null");
        this.resolver = resolver != null ? resolver : DistributionIds::resolve;
        this.strategy = strategy != null ? strategy : DistributionKeyStrategy.DEFAULT;
        this.enabled = enabled;
        this.requireDistributionId = requireDistributionId;
        this.backfillHeader = backfillHeader;
    }

    @Override
    protected Event<TKey, TValue> transform(Event<TKey, TValue> event) {
        if (!this.enabled) {
            return event;
        }

        String distributionId = this.resolver.apply(event);
        if (StringUtils.isBlank(distributionId)) {
            if (this.requireDistributionId) {
                throw new IllegalStateException(MISSING_DISTRIBUTION_ID_ERROR);
            }
            return event;
        }

        // NB - replaceKey() is generic in the new key type, but as we re-key with the pipeline's own key type it
        //      infers back to Event<TKey, TValue> so no cast is needed here.
        String key = this.strategy.key(distributionId);
        Event<TKey, TValue> keyed = event.replaceKey(this.keyEncoder.apply(key));

        if (this.backfillHeader && DistributionIds.fromHeader(event) == null) {
            return keyed.addHeaders(Stream.of(new Header(TelicentHeaders.DISTRIBUTION_ID, distributionId)));
        }
        return keyed;
    }

    /**
     * Creates a builder for a Distribution ID key sink
     *
     * @param <TKey>   Event key type
     * @param <TValue> Event value type
     * @return Builder
     */
    public static <TKey, TValue> Builder<TKey, TValue> create() {
        return new Builder<>();
    }

    /**
     * A builder for Distribution ID key sinks
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     */
    public static class Builder<TKey, TValue> extends
            AbstractForwardingSinkBuilder<Event<TKey, TValue>, Event<TKey, TValue>, DistributionKeySink<TKey, TValue>, Builder<TKey, TValue>> {

        private Function<String, TKey> keyEncoder;
        private Function<Event<TKey, TValue>, String> resolver;
        private DistributionKeyStrategy strategy = DistributionKeyStrategy.DEFAULT;
        private boolean enabled = true;
        private boolean requireDistributionId = false;
        private boolean backfillHeader = true;

        /**
         * Sets the function used to convert the generated key string into the pipeline's key type.  This is required.
         *
         * @param keyEncoder Key encoder
         * @return Builder
         */
        public Builder<TKey, TValue> keyEncoder(Function<String, TKey> keyEncoder) {
            this.keyEncoder = keyEncoder;
            return this;
        }

        /**
         * Sets the function used to resolve the Distribution ID from an event, defaults to
         * {@link DistributionIds#resolve(Event)} i.e. message key first, then the
         * {@value TelicentHeaders#DISTRIBUTION_ID} header
         *
         * @param resolver Distribution ID resolver
         * @return Builder
         */
        public Builder<TKey, TValue> resolver(Function<Event<TKey, TValue>, String> resolver) {
            this.resolver = resolver;
            return this;
        }

        /**
         * Sets the key strategy, defaults to {@link DistributionKeyStrategy#DEFAULT}
         *
         * @param strategy Key strategy
         * @return Builder
         */
        public Builder<TKey, TValue> strategy(DistributionKeyStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        /**
         * Sets whether message keys are set at all, defaults to {@code true}.  When disabled events are forwarded
         * unmodified, which allows a deployment to turn the behaviour off without restructuring its pipeline.
         *
         * @param enabled Whether enabled
         * @return Builder
         */
        public Builder<TKey, TValue> enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Requires that every event carries a Distribution ID, events that do not cause an
         * {@link IllegalStateException} rather than being forwarded unmodified
         *
         * @return Builder
         */
        public Builder<TKey, TValue> requireDistributionId() {
            this.requireDistributionId = true;
            return this;
        }

        /**
         * Sets whether the {@value TelicentHeaders#DISTRIBUTION_ID} header is added when missing, defaults to
         * {@code true}
         *
         * @param backfillHeader Whether to backfill the header
         * @return Builder
         */
        public Builder<TKey, TValue> backfillHeader(boolean backfillHeader) {
            this.backfillHeader = backfillHeader;
            return this;
        }

        @Override
        public DistributionKeySink<TKey, TValue> build() {
            return new DistributionKeySink<>(this.getDestination(), this.keyEncoder, this.resolver, this.strategy,
                                             this.enabled, this.requireDistributionId, this.backfillHeader);
        }
    }
}
