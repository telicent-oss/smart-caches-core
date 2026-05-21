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
package io.telicent.smart.cache.cli.commands.projection;

import com.github.rvesse.airline.annotations.AirlineModule;
import io.telicent.smart.cache.cli.options.KafkaOptions;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Abstract base class for commands that run a Projector with a Kafka event source
 *
 * @param <TKey>    Event key type
 * @param <TValue>  Event value type
 * @param <TOutput> Output type
 */
public abstract class AbstractKafkaProjectorCommand<TKey, TValue, TOutput>
        extends AbstractProjectorCommand<TKey, TValue, TOutput> {
    /**
     * Provides options for Kafka based event sources
     */
    @AirlineModule
    protected KafkaOptions kafka = new KafkaOptions();

    /**
     * Indicates whether the {@link io.telicent.smart.cache.sources.kafka.KafkaEventSource} being used by this command
     * should use auto-commits or not
     *
     * @return True if auto-commit should be used, false otherwise
     */
    protected boolean useAutoCommit() {
        return true;
    }

    /**
     * Gets the key serializer class, needed for event capture.
     *
     * @return Key serializer type.
     */
    @SuppressWarnings("unchecked")
    protected final Class<Serializer<TKey>> keySerializerClass() {
        try (Serializer<TKey> serdes = keySerializer()) {
            return (Class<Serializer<TKey>>) serdes.getClass();
        }
    }

    /**
     * Gets the value serializer class, needed for event capture.
     *
     * @return Value serializer type.
     */
    @SuppressWarnings("unchecked")
    protected final Class<Serializer<TValue>> valueSerializerClass() {
        try (Serializer<TValue> serdes = valueSerializer()) {
            return (Class<Serializer<TValue>>) serdes.getClass();
        }
    }

    /**
     * Gets the key deserializer class, needed for event propagation.
     *
     * @return Key deserializer type.
     */
    @SuppressWarnings("unchecked")
    protected final Class<Deserializer<TKey>> keyDeserializerClass() {
        try (Deserializer<TKey> serdes = keyDeserializer()) {
            return (Class<Deserializer<TKey>>) serdes.getClass();
        }
    }

    /**
     * Gets the value deserializer class, needed for event propagation.
     *
     * @return Value deserializer.
     */
    @SuppressWarnings("unchecked")
    protected final Class<Deserializer<TValue>> valueDeserializerClass() {
        try (Deserializer<TValue> serdes = valueDeserializer()) {
            return (Class<Deserializer<TValue>>) serdes.getClass();
        }
    }

    /**
     * Gets the event source that is in use which, in this implementation, is a {@link KafkaEventSource} configured from
     * the Kafka options provided.
     *
     * @return a Kafka event source, configured with the Kafka options provided.
     */
    protected EventSource<TKey, TValue> getSource() {
        return KafkaEventSource.<TKey, TValue>create()
                               .bootstrapServers(this.kafka.bootstrapServers)
                               .topics(this.kafka.topics)
                               .consumerGroup(this.kafka.getConsumerGroup(null))
                               .keyDeserializer(keyDeserializerClass())
                               .valueDeserializer(valueDeserializerClass())
                               .maxPollRecords(this.kafka.getMaxPollRecords())
                               .readPolicy(this.kafka.readPolicy.toReadPolicy())
                               .consumerConfig(this.kafka.getAdditionalProperties())
                               .autoCommit(this.useAutoCommit())
                               .build();
    }

    /**
     * Prepares a kafka dead letter sink, if configured, where events with processing errors are written. This
     * implementation provides a basic implementation, based on the key and value de/serialiser classes.
     * <p>
     * This <strong>MUST</strong> only be used if the projection does not mutate the event types prior to the point
     * where the event could be dead lettered.  If that is not the case then please call
     * {@link #prepareDeadLetterSink(String, Class, Class)} instead supplying appropriate serializer classes for the
     * types at the point where the dead lettering will occur.
     * </p>
     *
     * @return a dead letter topic sink, provided in this implementation if a DLQ topic name has been configured, null
     * if not configured.
     * @see KafkaConfiguration#DLQ_TOPIC
     */
    @Override
    protected <K, V> Sink<Event<K, V>> prepareDeadLetterSink() {
        return prepareDeadLetterSink(this.kafka.dlqTopic, keySerializerClass(), valueSerializerClass());
    }

    /**
     * Prepares a Kafka dead letter sink, if configured, where events with processing errors are written.
     * <p>
     * Since there may be multiple points in a projection pipeline where we may want to dead letter events this method
     * allows providing the appropriate serializers for the key and value type as they will be at the point where an
     * event may be dead lettered.  Thus some commands may actually call this method multiple times to create multiple
     * dead letter sinks that accept dead letters with different type signatures as they exist at different points in a
     * projection pipeline.
     * </p>
     *
     * @param dlqTopic        Dead Letter topic, if blank nothing is configured
     * @param keySerializer   Key Serializer class
     * @param valueSerializer Value Serializer class
     * @param <K>             Key type
     * @param <V>             Value type
     * @return Dead letter sink, {@code null} if none configured
     */
    protected <K, V> Sink<Event<K, V>> prepareDeadLetterSink(String dlqTopic, Class<?> keySerializer,
                                                             Class<?> valueSerializer) {
        if (StringUtils.isBlank(dlqTopic)) return null;

        return KafkaSink.<K, V>create()
                        .bootstrapServers(this.kafka.bootstrapServers)
                        .topic(dlqTopic)
                        .keySerializer(keySerializer)
                        .valueSerializer(valueSerializer)
                        .producerConfig(this.kafka.getAdditionalProperties())
                        .lingerMs(5)
                        .build();
    }
}
