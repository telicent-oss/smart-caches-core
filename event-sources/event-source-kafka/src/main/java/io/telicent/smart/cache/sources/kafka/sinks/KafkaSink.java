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
package io.telicent.smart.cache.sources.kafka.sinks;

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.builder.SinkBuilder;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.kafka.KafkaSecurity;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.security.auth.SecurityProtocol;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * A sink that sends the received events to a Kafka topic
 * <p>
 * This uses a {@link KafkaProducer} internally so all the sent events are sent asynchronously.
 * </p>
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public class KafkaSink<TKey, TValue> implements Sink<Event<TKey, TValue>> {

    private final KafkaProducer<TKey, TValue> producer;
    private final String topic;

    /**
     * Creates a new Kafka sink
     *
     * @param bootstrapServers     Bootstrap servers for connecting to the Kafka cluster
     * @param topic                Kafka topic to write events to
     * @param keySerializerClass   Serializer to use for event keys
     * @param valueSerializerClass Serializer to use for event values
     * @param lingerMilliseconds   Linger milliseconds, reduces the number of requests made to Kafka by batching events
     *                             together at the cost of event sending latency
     */
    KafkaSink(final String bootstrapServers, final String topic, final String keySerializerClass,
              final String valueSerializerClass, final Integer lingerMilliseconds, Properties producerProperties) {
        if (StringUtils.isBlank(bootstrapServers)) {
            throw new IllegalArgumentException("Kafka bootstrapServers cannot be null");
        }
        if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("Kafka topic to read cannot be null");
        }
        if (StringUtils.isBlank(keySerializerClass)) {
            throw new IllegalArgumentException("Kafka keySerializerClass cannot be null");
        }
        if (StringUtils.isBlank(valueSerializerClass)) {
            throw new IllegalArgumentException("Kafka valueSerializerClass cannot be null");
        }
        this.topic = topic;

        Properties props = new Properties();
        if (producerProperties != null) {
            props.putAll(producerProperties);
        }
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass);
        if (lingerMilliseconds != null && lingerMilliseconds > 0) {
            props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMilliseconds);
        }
        this.producer = new KafkaProducer<>(props);
    }

    @Override
    public void send(Event<TKey, TValue> event) {
        Objects.requireNonNull(event, "Event cannot be null");
        this.producer.send(new ProducerRecord<>(this.topic, null, null, event.key(), event.value(),
                                                toKafkaHeaders(event.headers())));
    }

    /**
     * Maps headers using our Event API into the Header format used by Kafka
     *
     * @param headers Event headers
     * @return Kafka headers
     */
    public static List<org.apache.kafka.common.header.Header> toKafkaHeaders(Stream<Header> headers) {
        return headers.map(h -> (org.apache.kafka.common.header.Header) new RecordHeader(h.key(), h.value()
                                                                                                   .getBytes(
                                                                                                           StandardCharsets.UTF_8)))
                      .toList();
    }


    @Override
    public void close() {
        this.producer.close();
    }

    /**
     * Gets the metrics of the underlying {@link KafkaProducer}
     *
     * @return Kafka Producer Metrics
     */
    public Map<MetricName, ? extends Metric> metrics() {
        return this.producer.metrics();
    }

    /**
     * Creates a new builder for Kafka Sinks
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     * @return Builder
     */
    public static <TKey, TValue> KafkaSinkBuilder<TKey, TValue> create() {
        return new KafkaSinkBuilder<>();
    }

    /**
     * A builder for Kafka sinks
     *
     * @param <TKey>   Key type
     * @param <TValue> Value type
     */
    public static final class KafkaSinkBuilder<TKey, TValue>
            implements SinkBuilder<Event<TKey, TValue>, KafkaSink<TKey, TValue>> {

        private String bootstrapServers, topic, keySerializerClass, valueSerializerClass;
        private Integer lingerMs;
        private final Properties properties = new Properties();

        /**
         * Sets the bootstrap servers
         *
         * @param servers Servers
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> bootstrapServers(String servers) {
            this.bootstrapServers = servers;
            return this;
        }

        /**
         * Sets the bootstrap servers
         *
         * @param servers Servers
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> bootstrapServers(String... servers) {
            return bootstrapServers(StringUtils.join(servers, ","));
        }

        /**
         * Sets the topic to be written to
         *
         * @param topic Topic
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> topic(String topic) {
            this.topic = topic;
            return this;
        }

        /**
         * Sets the key serializer class
         *
         * @param keySerializerClass Key serializer class
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> keySerializer(String keySerializerClass) {
            this.keySerializerClass = keySerializerClass;
            return this;
        }

        /**
         * Sets the value serializer class
         *
         * @param valueSerializerClass Value serializer class
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> valueSerializer(String valueSerializerClass) {
            this.valueSerializerClass = valueSerializerClass;
            return this;
        }

        /**
         * Sets the key serializer
         *
         * @param cls Class
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> keySerializer(Class<?> cls) {
            Objects.requireNonNull(cls, "Class cannot be null");
            return keySerializer(cls.getCanonicalName());
        }

        /**
         * Sets the value serializer
         *
         * @param cls Class
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> valueSerializer(Class<?> cls) {
            Objects.requireNonNull(cls, "Class cannot be null");
            return valueSerializer(cls.getCanonicalName());
        }

        /**
         * Sets linger milliseconds for the sink
         *
         * @param milliseconds Milliseconds
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> lingerMs(int milliseconds) {
            this.lingerMs = milliseconds;
            return this;
        }

        /**
         * Disables linger for the sink
         *
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> noLinger() {
            this.lingerMs = null;
            return this;
        }

        /**
         * Sets a Kafka Producer configuration property that will be used to configure the underlying
         * {@link KafkaProducer}.  Note that some properties are always overridden by the other sink configuration
         * provided to this builder.
         *
         * @param name  Name
         * @param value Value
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> producerConfig(String name, Object value) {
            this.properties.put(name, value);
            return this;
        }

        /**
         * Sets multiple Kafka Producer configuration properties that will be used to configure the underlying
         * {@link KafkaProducer}. Note that some properties are always overridden by the other sink configuration
         * provided to this builder.
         *
         * @param properties Producer configuration properties
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> producerConfig(Properties properties) {
            this.properties.putAll(properties);
            return this;
        }

        /**
         * Configures the sink to perform a plain SASL login to the Kafka cluster using the provided credentials
         * <p>
         * If an alternative authentication mechanism is needed use {@link #producerConfig(Properties)}} to supply the
         * necessary configuration properties with suitable values.
         * </p>
         *
         * @param username Username
         * @param password Password
         * @return Builder
         */
        public KafkaSinkBuilder<TKey, TValue> plainLogin(String username, String password) {
            this.properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
            this.properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_PLAINTEXT.name);
            this.properties.put(SaslConfigs.SASL_JAAS_CONFIG, KafkaSecurity.plainLogin(username, password));
            return this;
        }

        @Override
        public KafkaSink<TKey, TValue> build() {
            return new KafkaSink<>(this.bootstrapServers, this.topic, this.keySerializerClass,
                                   this.valueSerializerClass, this.lingerMs, this.properties);
        }
    }
}
