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

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.telicent.smart.cache.observability.AttributeNames;
import io.telicent.smart.cache.observability.MetricNames;
import io.telicent.smart.cache.observability.TelicentMetrics;
import io.telicent.smart.cache.projectors.Library;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.atlas.logging.FmtLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A sink that performs duplicate item suppression by using an LRU Cache to avoid forwarding items that have recently
 * been seen <strong>unless</strong> they have been modified.
 * <p>
 * This differs from the basic {@link SuppressDuplicatesSink} in that it provides more customisation over the decision
 * around what items to forward.  In particular this is useful where the preferred way to calculate equality <strong>is
 * not</strong> expressed via the types own equality implementation.
 * </p>
 *
 * @param <T> Item type
 */
@ToString(callSuper = true)
public class SuppressUnmodifiedSink<T, TKey, TValue> extends AbstractTransformingSink<T, T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(SuppressUnmodifiedSink.class);

    @ToString.Exclude
    private final Cache<TKey, TValue> cache;
    @ToString.Exclude
    private final Function<T, TKey> keyFunction;
    @ToString.Exclude
    private final Function<T, TValue> valueFunction;
    @ToString.Exclude
    private final Comparator<TValue> valueComparator;
    @ToString.Exclude
    private long suppressed = 0;
    @ToString.Exclude
    private final LongCounter suppressedMetric;
    @ToString.Exclude
    private final Attributes metricAttributes;
    @ToString.Exclude
    private final Function<T, Boolean> invalidateCache;
    @ToString.Exclude
    private long lastCacheOperationAt = -1;
    private final long expireCacheAfter;
    @ToString.Exclude
    private final Supplier<Boolean> invalidateWholeCache;

    /**
     * Creates a new sink
     *
     * @param destination          Destination sink
     * @param cacheSize            Cache size
     * @param keyFunction          Function that calculates a key for an item
     * @param valueFunction        Function that calculates a value for an item
     * @param valueComparator      Comparator that determines whether an items value has changed versus its previously
     *                             cached value
     * @param invalidateCache      Function that calculates whether to invalidate the cache entry for a specific item
     * @param invalidateWholeCache Supplier that indicates whether to invalidate the whole cache
     * @param expireCacheAfter     Duration after which the whole cache will be considered expired
     */
    SuppressUnmodifiedSink(Sink<T> destination, int cacheSize, String metricsLabel,
                           Function<T, TKey> keyFunction,
                           Function<T, TValue> valueFunction, Comparator<TValue> valueComparator,
                           Function<T, Boolean> invalidateCache, Supplier<Boolean> invalidateWholeCache,
                           Duration expireCacheAfter) {
        super(destination);
        this.invalidateCache = invalidateCache != null ? invalidateCache : x -> false;
        this.invalidateWholeCache = invalidateWholeCache != null ? invalidateWholeCache : () -> false;
        if (expireCacheAfter != null && expireCacheAfter.compareTo(Duration.ZERO) < 1) {
            throw new IllegalArgumentException("Expire cache after duration must be a duration greater than zero");
        }
        this.expireCacheAfter = expireCacheAfter != null ? expireCacheAfter.toMillis() : -1;

        if (cacheSize < 1) {
            throw new IllegalArgumentException("Cache Size must be >= 1");
        }
        Objects.requireNonNull(keyFunction, "Key Function cannot be null");
        Objects.requireNonNull(valueFunction, "Value Function cannot be null");
        Objects.requireNonNull(valueComparator, "Value Comparator cannot be null");

        this.cache = CacheFactory.createCache(cacheSize);
        this.valueFunction = valueFunction;
        this.keyFunction = keyFunction;
        this.valueComparator = valueComparator;
        if (StringUtils.isNotBlank(metricsLabel)) {
            Meter meter = TelicentMetrics.getMeter(Library.NAME);
            //@formatter:off
            this.suppressedMetric = meter.counterBuilder(MetricNames.UNMODIFIED_SUPPRESSED)
                                         .setDescription(MetricNames.UNMODIFIED_SUPPRESSED_DESCRIPTION)
                                         .build();
            //@formatter:on
            this.metricAttributes = Attributes.of(AttributeKey.stringKey(AttributeNames.ITEMS_TYPE), metricsLabel,
                                                  AttributeKey.stringKey(AttributeNames.INSTANCE_ID),
                                                  UUID.randomUUID().toString());
        } else {
            this.suppressedMetric = null;
            this.metricAttributes = null;
        }
    }

    @Override
    protected boolean shouldForward(T item) {
        // Check for whole cache invalidation
        if (this.invalidateWholeCache.get()) {
            LOGGER.info("Invalidated unmodified suppression cache");
            this.cache.clear();
        } else if (this.expireCacheAfter != -1 && this.lastCacheOperationAt > -1) {
            long timeSinceLastCacheOperation = System.currentTimeMillis() - this.lastCacheOperationAt;
            if (timeSinceLastCacheOperation > this.expireCacheAfter) {
                LOGGER.info("Invalidated unmodified suppression cache due to prolonged inactivity");
                this.cache.clear();
            }
        }
        this.lastCacheOperationAt = System.currentTimeMillis();

        TKey key = this.keyFunction.apply(item);
        TValue value = this.valueFunction.apply(item);
        TValue currentValue = this.cache.getIfPresent(key);

        if (this.invalidateCache.apply(item)) {
            this.cache.remove(key);
        } else {
            if (currentValue != null) {
                // Don't forward the item if the item is unchanged relative to its cached value
                if (this.valueComparator.compare(value, currentValue) == 0) {
                    this.suppressed++;
                    if (this.suppressedMetric != null) {
                        this.suppressedMetric.add(1, this.metricAttributes);
                    }
                    return false;
                }
            }
            this.cache.put(key, value);
        }
        return true;
    }

    @Override
    protected T transform(T input) {
        return input;
    }

    @Override
    public void close() {
        super.close();

        if (this.suppressed > 0) {
            FmtLog.info(LOGGER, "Suppressed %,d unmodified duplicates", this.suppressed);
        }

        this.cache.clear();
        this.suppressed = 0;
    }

    /**
     * Gets how many unmodified duplicates were suppressed
     *
     * @return Suppressed duplicates count
     */
    public long getSuppressed() {
        return this.suppressed;
    }

    /**
     * Creates a builder for an unmodified suppressing sink
     *
     * @param <TItem>  Item type
     * @param <TKey>   Key type
     * @param <TValue> Value type
     * @return Unmodified suppressing sink builder
     */
    public static <TItem, TKey, TValue> Builder<TItem, TKey, TValue> create() {
        return new Builder<>();
    }

    /**
     * A builder for an unmodified suppressing sink
     *
     * @param <TItem>  Item type
     * @param <TKey>   Key type
     * @param <TValue> Value type
     */
    public static class Builder<TItem, TKey, TValue>
            extends
            AbstractForwardingSinkBuilder<TItem, TItem, SuppressUnmodifiedSink<TItem, TKey, TValue>, Builder<TItem, TKey, TValue>> {

        private int cacheSize;
        private String metricsLabel;
        private Function<TItem, TKey> keyFunction;
        private Function<TItem, TValue> valueFunction;
        private Comparator<TValue> valueComparator;
        private Function<TItem, Boolean> invalidateCache;
        private Duration expireCacheAfter;
        private Supplier<Boolean> invalidateWholeCache;

        /**
         * Sets a metrics label used to collect metrics on the number of unmodified items suppressed
         *
         * @param metricsLabel Metrics label
         * @return Builder
         */
        public Builder<TItem, TKey, TValue> withMetrics(String metricsLabel) {
            this.metricsLabel = metricsLabel;
            return this;
        }

        /**
         * Sets the cache size used for suppressing duplicates.
         * <p>
         * A higher cache size increases memory usage but may do better at suppressing duplicates.
         * </p>
         *
         * @param size Size
         * @return Builder
         */
        public Builder<TItem, TKey, TValue> cacheSize(int size) {
            this.cacheSize = size;
            return this;
        }

        /**
         * Sets the key function used to determine a key for each item
         *
         * @param f Key extracting function
         * @return Builder
         */
        public Builder<TItem, TKey, TValue> keyFunction(Function<TItem, TKey> f) {
            this.keyFunction = f;
            return this;
        }

        /**
         * Sets the value function used to determine a value for each item that will be used to determine if the item is
         * unmodified in conjunction with the value comparator configured via the {@link #comparator(Comparator)}
         * method.
         *
         * @param f Value extracting function
         * @return Builder
         */
        public Builder<TItem, TKey, TValue> valueFunction(Function<TItem, TValue> f) {
            this.valueFunction = f;
            return this;
        }

        /**
         * Sets the comparator function used to determine if an items value has changed using the values extracted via
         * the configured {@link #valueFunction(Function)} method.
         *
         * @param comparator Comparator
         * @return Builder
         */
        public Builder<TItem, TKey, TValue> comparator(Comparator<TValue> comparator) {
            this.valueComparator = comparator;
            return this;
        }

        /**
         * Sets whether to invalidate the cache.
         *
         * @param invalidate Cache invalidation function
         * @return Builder
         */
        public Builder<TItem, TKey, TValue> invalidateWhen(Function<TItem, Boolean> invalidate) {
            this.invalidateCache = invalidate;
            return this;
        }

        /**
         * Sets a function that calculates when to invalidate the whole cache.
         * <p>
         * If not specified the whole cache is never invalidated.
         * </p>
         *
         * @param invalidateWholeCache Cache invalidation function
         * @return Builder
         */
        public Builder<TItem, TKey, TValue> invalidateWholeWhen(Supplier<Boolean> invalidateWholeCache) {
            this.invalidateWholeCache = invalidateWholeCache;
            return this;
        }

        /**
         * Sets the whole cache to expire after no activity for the given period.
         * <p>
         * If not specified the cache does not expire.
         * </p>
         *
         * @param expiryPeriod Expiry period
         * @return Builder
         */
        public Builder<TItem, TKey, TValue> expireCacheAfter(Duration expiryPeriod) {
            this.expireCacheAfter = expiryPeriod;
            return this;
        }

        /**
         * Builds a new unmodified suppressing sink
         *
         * @return Unmodified suppressing sink
         */
        @Override
        public SuppressUnmodifiedSink<TItem, TKey, TValue> build() {
            return new SuppressUnmodifiedSink<>(this.getDestination(), this.cacheSize, this.metricsLabel,
                                                this.keyFunction, this.valueFunction, this.valueComparator,
                                                this.invalidateCache, this.invalidateWholeCache, this.expireCacheAfter);
        }
    }
}
