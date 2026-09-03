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
package io.telicent.smart.cache.sources.kafka;

import io.telicent.smart.cache.projectors.sinks.events.DistributionKeySink;
import io.telicent.smart.cache.sources.DistributionIds;
import io.telicent.smart.cache.sources.DistributionKeyStrategy;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.TelicentHeaders;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.utils.Bytes;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Kafka aware helpers for reading and writing the Distribution ID message key.
 * <p>
 * {@link DistributionIds} lives in {@code event-sources-core} which has no Kafka dependency, so it cannot understand
 * Kafka's {@link Bytes} type.  Since the overwhelming majority of our pipelines are {@code Event<Bytes, ?>} this class
 * fills that gap and is the entry point that services should use.
 * </p>
 * <p>
 * Note that a Distribution ID message key is always the UTF-8 encoding of a string, so a {@link Bytes} key written by
 * a producer configured with {@code BytesSerializer} is byte for byte identical to one written with
 * {@code StringSerializer}.  Adopting message keys therefore requires no serializer changes in any existing pipeline.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaDistributionKeys {

    /**
     * Key encoder for pipelines whose key type is {@link Bytes}, i.e. most of them
     */
    public static final Function<String, Bytes> BYTES_ENCODER =
            key -> key != null ? Bytes.wrap(key.getBytes(StandardCharsets.UTF_8)) : null;

    /**
     * Key encoder for pipelines whose key type is {@link String}
     */
    public static final Function<String, String> STRING_ENCODER = key -> key;

    /**
     * Decodes a message key into the Distribution ID it conveys, if any.
     * <p>
     * Extends {@link DistributionIds#fromKey(Object)} with support for Kafka's {@link Bytes} type.
     * </p>
     *
     * @param key Message key, may be {@code null}
     * @return Distribution ID, or {@code null} if the key does not convey one
     */
    public static String fromKey(Object key) {
        if (key instanceof Bytes bytes) {
            // NB - MUST use get() rather than toString(), the latter produces a debug representation
            return DistributionIds.fromKeyBytes(bytes.get());
        }
        return DistributionIds.fromKey(key);
    }

    /**
     * Resolves the Distribution ID for an event, preferring the message key and falling back to the
     * {@value TelicentHeaders#DISTRIBUTION_ID} header.
     * <p>
     * This is the resolution order mandated by the Core Data Management design and <strong>SHOULD</strong> be used in
     * place of a direct {@code event.lastHeader(TelicentHeaders.DISTRIBUTION_ID)} call everywhere a service needs the
     * Distribution ID for an event.
     * </p>
     *
     * @param event Event, may be {@code null}
     * @return Distribution ID, or {@code null} if the event does not carry one
     */
    public static String resolve(Event<?, ?> event) {
        if (event == null) {
            return null;
        }
        String fromKey = fromKey(event.key());
        return fromKey != null ? fromKey : DistributionIds.fromHeader(event);
    }

    /**
     * Resolves the Distribution ID for a raw Kafka record, preferring the message key and falling back to the
     * {@value TelicentHeaders#DISTRIBUTION_ID} header.
     * <p>
     * This is the equivalent of {@link #resolve(Event)} for code that works with the Kafka client API directly rather
     * than with our {@link Event} abstraction.
     * </p>
     *
     * @param record Kafka record, may be {@code null}
     * @return Distribution ID, or {@code null} if the record does not carry one
     */
    public static String resolve(ConsumerRecord<?, ?> record) {
        if (record == null) {
            return null;
        }
        String fromKey = fromKey(record.key());
        if (fromKey != null) {
            return fromKey;
        }
        Header header = record.headers().lastHeader(TelicentHeaders.DISTRIBUTION_ID);
        if (header == null || header.value() == null) {
            return null;
        }
        return StringUtils.trimToNull(new String(header.value(), StandardCharsets.UTF_8));
    }

    /**
     * Creates a {@link DistributionKeySink} builder pre-wired for a {@link Bytes} keyed pipeline
     *
     * @param <TValue> Value type
     * @return Builder
     */
    public static <TValue> DistributionKeySink.Builder<Bytes, TValue> bytesKeySink() {
        DistributionKeySink.Builder<Bytes, TValue> builder = DistributionKeySink.create();
        return builder.keyEncoder(BYTES_ENCODER).resolver(KafkaDistributionKeys::resolve);
    }

    /**
     * Creates a {@link DistributionKeySink} builder pre-wired for a {@link String} keyed pipeline
     *
     * @param <TValue> Value type
     * @return Builder
     */
    public static <TValue> DistributionKeySink.Builder<String, TValue> stringKeySink() {
        DistributionKeySink.Builder<String, TValue> builder = DistributionKeySink.create();
        return builder.keyEncoder(STRING_ENCODER).resolver(KafkaDistributionKeys::resolve);
    }

    /**
     * Generates a {@link Bytes} message key for a Distribution ID using the given strategy
     *
     * @param distributionId Distribution ID
     * @param strategy       Key strategy, if {@code null} then {@link DistributionKeyStrategy#DEFAULT} is used
     * @return Message key, or {@code null} if no Distribution ID was supplied
     */
    public static Bytes toBytesKey(String distributionId, DistributionKeyStrategy strategy) {
        return BYTES_ENCODER.apply(DistributionIds.toKey(distributionId, strategy));
    }
}
