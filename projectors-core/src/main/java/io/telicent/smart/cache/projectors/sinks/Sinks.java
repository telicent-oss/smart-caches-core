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

import io.telicent.smart.cache.projectors.RejectSink;
import io.telicent.smart.cache.projectors.sinks.builder.SinkBuilder;

/**
 * Provides helper methods for building sink instances
 */
public class Sinks {

    /**
     * Private constructor prevents instantiation
     */
    private Sinks() {
    }

    /**
     * Creates a new collecting sink builder
     *
     * @param <T> Item type
     * @return Collecting sink builder
     */
    public static <T> SinkBuilder<T, CollectorSink<T>> collect() {
        return CollectorSink.create();
    }

    /**
     * Creates a new discarding sink builder
     *
     * @param <T> Item type
     * @return Discarding sink builder
     */
    public static <T> SinkBuilder<T, NullSink<T>> discard() {
        return NullSink.create();
    }

    /**
     * Creates a new filtering sink builder
     *
     * @param <T> Item type
     * @return Filtering sink builder
     */
    public static <T> FilterSink.Builder<T> filter() {
        return FilterSink.create();
    }

    /**
     * Creates a new rejecting sink builder
     *
     * @param <T> Item type
     * @return Rejecting sink builder
     */
    public static <T> RejectSink.Builder<T> reject() {
        return RejectSink.createRejecting();
    }

    /**
     * Creates a new cleanup sink builder
     *
     * @param <T> Item type
     * @return Cleanup sink builder
     */
    public static <T> CleanupSink.Builder<T> cleanup() {
        return CleanupSink.create();
    }

    /**
     * Creates a new circuit breaker sink builder
     *
     * @param <T> Item type
     * @return Circuit breaker sink builder
     */
    public static <T> CircuitBreakerSink.Builder<T> circuitBreaker() {
        return CircuitBreakerSink.create();
    }

    /**
     * Creates a new JSON sink builder
     *
     * @param <T> Item type
     * @return JSON sink builder
     */
    public static <T> JacksonJsonSink.Builder<T> toJson() {
        return JacksonJsonSink.create();
    }

    /**
     * Creates a new duplicate suppressing sink builder
     *
     * @param <T> Item type
     * @return Duplicate suppressing sink builder
     */
    public static <T> SuppressDuplicatesSink.Builder<T> suppressDuplicates() {
        return SuppressDuplicatesSink.create();
    }

    /**
     * Creates a new unmodified suppressing sink builder
     *
     * @param <T>      Item type
     * @param <TKey>   Key type
     * @param <TValue> Value type
     * @return Unmodified suppressing sink builder
     */
    public static <T, TKey, TValue> SuppressUnmodifiedSink.Builder<T, TKey, TValue> suppressUnmodified() {
        return SuppressUnmodifiedSink.create();
    }

    /**
     * Creates a new throughput tracking sink builder
     *
     * @param <T> Item type
     * @return Throughput tracking sink builder
     */
    public static <T> ThroughputSink.Builder<T> throughput() {
        return ThroughputSink.create();
    }
}
