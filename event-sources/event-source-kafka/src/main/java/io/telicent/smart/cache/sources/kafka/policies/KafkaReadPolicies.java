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
package io.telicent.smart.cache.sources.kafka.policies;

import io.telicent.smart.cache.sources.kafka.policies.automatic.*;
import io.telicent.smart.cache.sources.kafka.policies.manual.ManualFromBeginning;
import io.telicent.smart.cache.sources.offsets.OffsetStore;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

/**
 * Provides access to predefined {@link KafkaReadPolicy} instances to control how you want a
 * {@link io.telicent.smart.cache.sources.kafka.KafkaEventSource} to read from Kafka topic(s)
 */
public class KafkaReadPolicies {

    /**
     * Private constructor to prevent instantiation
     */
    private KafkaReadPolicies() {
    }

    /**
     * Read all events from the beginning, possibly reading events multiple times
     *
     * @param <TKey>   Key Type
     * @param <TValue> Value Type
     * @return Read Policy
     */
    public static <TKey, TValue> KafkaReadPolicy<TKey, TValue> fromBeginning() {
        return new AutoFromBeginning<>();
    }

    /**
     * Read all events from the beginning, possibly reading events multiple times
     *
     * @param <TKey>   Key Type
     * @param <TValue> Value Type
     * @return Read Policy
     */
    public static <TKey, TValue> KafkaReadPolicy<TKey, TValue> manualFromBeginning() {
        return new ManualFromBeginning<>();
    }

    /**
     * Read all events from the beginning, possibly ignoring pre-existing events
     *
     * @param <TKey>   Key Type
     * @param <TValue> Value Type
     * @return Read Policy
     */
    public static <TKey, TValue> KafkaReadPolicy<TKey, TValue> fromEnd() {
        return new AutoFromEnd<>();
    }

    /**
     * Read all events starting from the earliest unread
     * <p>
     * If the topic has not previously been read by the Consumer Group then this is equivalent to
     * {@link #fromBeginning()} since it starts from the earliest available event.  If the topic has previously been
     * read by the Consumer Group then reading resumes from the most recent event read.
     * </p>
     *
     * @param <TKey>   Key Type
     * @param <TValue> Value Type
     * @return Read Policy
     */
    public static <TKey, TValue> KafkaReadPolicy<TKey, TValue> fromEarliest() {
        return new AutoFromEarliest<>();
    }

    /**
     * Reads events starting from the latest
     * <p>
     * If the topic has not previously been read by the Consumer Group then starts from the latest offset i.e. only
     * reads new events.  If the topic has previously been read by the Consumer Group then reading resumes from the most
     * recent event read.
     * </p>
     *
     * @param <TKey>   Key Type
     * @param <TValue> Value Type
     * @return Read Policy
     */
    public static <TKey, TValue> KafkaReadPolicy<TKey, TValue> fromLatest() {
        return new AutoFromLatest<>();
    }

    /**
     * Reads events starting from specific offsets
     *
     * @param offsets       Offsets
     * @param defaultOffset Default offset to use for any partition whose desired offset is not explicitly specified
     * @param <TKey>        Key Type
     * @param <TValue>      Value Type
     * @return Read Policy
     */
    public static <TKey, TValue> KafkaReadPolicy<TKey, TValue> fromOffsets(Map<TopicPartition, Long> offsets,
                                                                           long defaultOffset) {
        return new AutoFromOffset<>(offsets, defaultOffset);
    }

    /**
     * Reads event starting from specific offsets as managed by an external offsets store
     *
     * @param offsets       Offsets store
     * @param defaultOffset Default offset to use for any partition whose desired offset is not explicitly specified
     * @param <TKey>        Key Type
     * @param <TValue>      Value Type
     * @return Read Policy
     */
    public static <TKey, TValue> KafkaReadPolicy<TKey, TValue> fromExternalOffsets(OffsetStore offsets,
                                                                                   long defaultOffset) {
        return new AutoFromExternalOffsetsStore<>(offsets, defaultOffset);
    }
}
