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

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSourceException;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DockerTestSecureKafkaCluster {

    public static final String RECORD_ERROR_TOTAL = "record-error-total";
    public static final String NO_SUCH_USER = "no-such-user";
    public static final String NO_SUCH_SECRET = "no-such-secret";
    public SecureKafkaTestCluster kafka;

    @BeforeClass
    public void setup() {
        Utils.logTestClassStarted(this.getClass());
        this.kafka = new SecureKafkaTestCluster(SecureKafkaTestCluster.DEFAULT_ADMIN_USERNAME,
                                                SecureKafkaTestCluster.DEFAULT_ADMIN_PASSWORD,
                                                Map.of(SecureKafkaTestCluster.DEFAULT_CLIENT_USERNAME,
                                                       SecureKafkaTestCluster.DEFAULT_CLIENT_PASSWORD, "extra",
                                                       "secret-squirrel"));
        this.kafka.setup();
    }

    @AfterClass
    public void teardown() {
        this.kafka.teardown();
        Utils.logTestClassFinished(this.getClass());
    }

    @Test
    public void secure_cluster_admin_01() throws ExecutionException, InterruptedException, TimeoutException {
        AdminClient client = this.kafka.getAdminClient();
        Assert.assertNotNull(client);

        // Verify that the admin client is supplied with suitable credentials by default
        ListTopicsResult topics = client.listTopics();
        Assert.assertNotNull(topics);
        Set<String> topicNames = topics.names().get(10, TimeUnit.SECONDS);
        Assert.assertNotNull(topicNames);
        Assert.assertFalse(topicNames.isEmpty());
        Assert.assertTrue(topicNames.contains(KafkaTestCluster.DEFAULT_TOPIC));
    }

    @SuppressWarnings("resource")
    @Test(expectedExceptions = KafkaException.class, expectedExceptionsMessageRegExp = "Failed to create.*")
    public void secure_cluster_admin_02() {
        // Try to create an admin client with no credentials
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafka.getBootstrapServers());
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 3000);
        KafkaAdminClient.create(props);
    }

    @SuppressWarnings("resource")
    @Test(expectedExceptions = KafkaException.class, expectedExceptionsMessageRegExp = "Failed to create.*")
    public void secure_cluster_admin_03() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafka.getBootstrapServers());
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 3000);
        props.putAll(this.kafka.getClientProperties(NO_SUCH_USER, NO_SUCH_SECRET));
        KafkaAdminClient.create(props);
    }

    @Test
    public void secure_cluster_send_01() {
        try (KafkaSink<Bytes, String> sink = KafkaSink.<Bytes, String>create()
                                                      .bootstrapServers(this.kafka.getBootstrapServers())
                                                      .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                      .keySerializer(BytesSerializer.class)
                                                      .valueSerializer(StringSerializer.class)
                                                      .producerConfig(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000L)
                                                      .build()) {
            sink.send(new SimpleEvent<>(Collections.emptyList(), null, "No authentication"));
            // This doesn't fail in of itself because the underlying KafkaProducer.send() is async and we don't supply
            // a callback
            // However, the error should have been recorded by the producer
            Map<MetricName, ? extends Metric> metrics = sink.metrics();
            Assert.assertNotNull(metrics);
            Double totalErrors = findKafkaMetric(metrics, RECORD_ERROR_TOTAL);
            Assert.assertNotNull(totalErrors);
            Assert.assertTrue(Double.compare(totalErrors, 0.0) > 0);
        }
    }

    @Test
    public void secure_cluster_send_02() {
        try (KafkaSink<Bytes, String> sink = KafkaSink.<Bytes, String>create()
                                                      .bootstrapServers(this.kafka.getBootstrapServers())
                                                      .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                      .keySerializer(BytesSerializer.class)
                                                      .valueSerializer(StringSerializer.class)
                                                      .producerConfig(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000L)
                                                      .producerConfig(this.kafka.getClientProperties(NO_SUCH_USER,
                                                                                                     NO_SUCH_SECRET))
                                                      .build()) {
            sink.send(new SimpleEvent<>(Collections.emptyList(), null, "Bad authentication"));
            // This doesn't fail in of itself because the underlying KafkaProducer.send() is async and we don't supply
            // a callback
            // However, the error should have been recorded by the producer
            Map<MetricName, ? extends Metric> metrics = sink.metrics();
            Assert.assertNotNull(metrics);
            Double totalErrors = findKafkaMetric(metrics, RECORD_ERROR_TOTAL);
            Assert.assertNotNull(totalErrors);
            Assert.assertTrue(Double.compare(totalErrors, 0.0) > 0);
        }
    }

    @Nullable
    private static Double findKafkaMetric(Map<MetricName, ? extends Metric> metrics, String name) {
        return metrics.entrySet()
                      .stream()
                      .filter(m -> m.getKey().name().equals(name))
                      .map(m -> (Double) m.getValue().metricValue())
                      .findFirst()
                      .orElse(null);
    }

    @Test
    public void secure_cluster_send_03() {
        try (KafkaSink<Bytes, String> sink = KafkaSink.<Bytes, String>create()
                                                      .bootstrapServers(this.kafka.getBootstrapServers())
                                                      .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                      .keySerializer(BytesSerializer.class)
                                                      .valueSerializer(StringSerializer.class)
                                                      .producerConfig(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000L)
                                                      .producerConfig(this.kafka.getClientProperties(
                                                              this.kafka.getAdminUsername(),
                                                              this.kafka.getAdminPassword()))
                                                      .build()) {
            sink.send(new SimpleEvent<>(Collections.emptyList(), null, "Has authentication"));
            Map<MetricName, ? extends Metric> metrics = sink.metrics();
            Assert.assertNotNull(metrics);
            Double totalErrors = findKafkaMetric(metrics, RECORD_ERROR_TOTAL);
            Assert.assertEquals(totalErrors, 0.0);
        }

        verifyUnableToPollEventsWithoutCredentials();
        Event<Bytes, String> event;

        // Verify we can poll for events with valid credentials
        verifyPollEventsWithCredentials(this.kafka.getAdminUsername(), this.kafka.getAdminPassword(), true);
        for (Map.Entry<String, String> user : this.kafka.getAdditionalUsers().entrySet()) {
            verifyPollEventsWithCredentials(user.getKey(), user.getValue(), true);
        }

        // Verify we cannot poll for events with bad credentials
        verifyPollEventsWithCredentials(NO_SUCH_USER, NO_SUCH_SECRET, false);
    }

    @Test
    public void secure_cluster_send_04() {
        try (KafkaSink<Bytes, String> sink = KafkaSink.<Bytes, String>create()
                                                      .bootstrapServers(this.kafka.getBootstrapServers())
                                                      .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                      .keySerializer(BytesSerializer.class)
                                                      .valueSerializer(StringSerializer.class)
                                                      .producerConfig(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000L)
                                                      .plainLogin(this.kafka.getAdminUsername(),
                                                                  this.kafka.getAdminPassword())
                                                      .build()) {
            sink.send(new SimpleEvent<>(Collections.emptyList(), null, "Has authentication"));
            Map<MetricName, ? extends Metric> metrics = sink.metrics();
            Assert.assertNotNull(metrics);
            Double totalErrors = findKafkaMetric(metrics, RECORD_ERROR_TOTAL);
            Assert.assertEquals(totalErrors, 0.0);
        }

        verifyUnableToPollEventsWithoutCredentials();
        Event<Bytes, String> event;

        // Verify we can poll for events with valid credentials
        verifyPollEventsWithCredentials(this.kafka.getAdminUsername(), this.kafka.getAdminPassword(), true);
        for (Map.Entry<String, String> user : this.kafka.getAdditionalUsers().entrySet()) {
            verifyPollEventsWithCredentials(user.getKey(), user.getValue(), true);
        }

        // Verify we cannot poll for events with bad credentials
        verifyPollEventsWithCredentials(NO_SUCH_USER, NO_SUCH_SECRET, false);
    }

    private void verifyPollEventsWithCredentials(String username, String password, boolean areCredentialsValid) {
        Event<Bytes, String> event = null;
        KafkaEventSource<Bytes, String> goodSource = KafkaEventSource.<Bytes, String>create()
                                                                     .bootstrapServers(this.kafka.getBootstrapServers())
                                                                     .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                                     .keyDeserializer(BytesDeserializer.class)
                                                                     .valueDeserializer(StringDeserializer.class)
                                                                     .consumerGroup("secure-cluster-03")
                                                                     .fromBeginning()
                                                                     .consumerConfig(
                                                                             this.kafka.getClientProperties(username,
                                                                                                            password))
                                                                     .plainLogin(username, password)
                                                                     .build();
        try {
            event = goodSource.poll(Duration.ofSeconds(3));
        } catch (EventSourceException e) {
            if (!areCredentialsValid) {
                Assert.assertTrue(StringUtils.contains(e.getMessage(), "Security"));
            } else {
                Assert.fail("Event Source threw an error when credentials were expected to be valid");
            }
        }
        if (areCredentialsValid) {
            Assert.assertNotNull(event);
            Assert.assertEquals(event.value(), "Has authentication");
        } else {
            Assert.assertNull(event);
        }
        goodSource.close();
    }

    private void verifyUnableToPollEventsWithoutCredentials() {
        KafkaEventSource<Bytes, String> badSource = KafkaEventSource.<Bytes, String>create()
                                                                    .bootstrapServers(this.kafka.getBootstrapServers())
                                                                    .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                                    .keyDeserializer(BytesDeserializer.class)
                                                                    .valueDeserializer(StringDeserializer.class)
                                                                    .consumerGroup("secure-cluster-03")
                                                                    .fromBeginning()
                                                                    .build();
        Event<Bytes, String> event = badSource.poll(Duration.ofSeconds(3));
        Assert.assertNull(event);
        badSource.close();
    }
}
