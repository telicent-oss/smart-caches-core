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

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Convenience wrapper around a {@link org.testcontainers.kafka.ConfluentKafkaContainer} instance for use in testing
 */
public abstract class KafkaTestCluster {

    public static final String DEFAULT_TOPIC = "tests";
    private static final int DEFAULT_TIMEOUT = 3;

    protected GenericContainer kafka;
    protected AdminClient adminClient;

    /**
     * Creates a new test cluster
     */
    public KafkaTestCluster() {

    }

    /**
     * Creates the actual Kafka container that forms the test cluster
     *
     * @return Kafka container
     */
    protected abstract GenericContainer createKafkaContainer();

    /**
     * Gets the default set of additional Kafka Client properties necessary to connect to the Kafka cluster
     *
     * @return Client Properties
     */
    public Properties getClientProperties() {
        return new Properties();
    }

    /**
     * Gets the bootstrap server for connecting to the Kafka test cluster
     *
     * @return Bootstrap servers
     */
    public abstract String getBootstrapServers();

    /**
     * Gets a Kafka Admin Client for the cluster
     *
     * @return Admin Client
     */
    public AdminClient getAdminClient() {
        return this.adminClient;
    }

    /**
     * Adds properties necessary to create an Admin Client.
     * <p>
     * The default implementation just sets the Bootstrap servers.
     * </p>
     *
     * @param properties Admin client properties
     */
    protected void addAdminClientProperties(Properties properties) {
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.getBootstrapServers());
    }

    /**
     * Setups and starts the test cluster
     */
    public void setup() {
        if (this.kafka == null) {
            this.kafka = this.createKafkaContainer();
        }

        try {
            Utils.logStdOut("Starting Kafka Test Cluster...");
            long start = System.currentTimeMillis();
            this.kafka.start();
            Duration elapsed = Duration.ofMillis(System.currentTimeMillis() - start);
            Utils.logStdOut("Kafka Test Cluster ready in %,d seconds!", elapsed.toSeconds());
        } catch (ContainerLaunchException e) {
            System.err.println("Failed to launch Kafka container, see logs below:");
            System.err.println();
            System.err.println(this.kafka.getLogs());
            System.err.println();

            throw e;
        }
        Properties properties = new Properties();
        addAdminClientProperties(properties);
        this.adminClient = KafkaAdminClient.create(properties);
        createTopic(DEFAULT_TOPIC);
    }

    /**
     * Resets the test topic
     */
    public void resetTestTopic() {
        resetTopic(DEFAULT_TOPIC);
    }

    /**
     * Resets the given topic i.e. deletes it then recreates it
     *
     * @param topic Topic
     */
    public void resetTopic(String topic) {
        // If the admin client is null then the test cluster never came up so nothing to reset
        if (this.adminClient != null) {
            deleteTopic(topic);
            createTopic(topic);
        }
    }

    /**
     * Creates a new topic with 1 partition and replication factor of 1
     * <p>
     * Generally tests don't need to call this directly but should rather call {@link #resetTopic(String)} instead.
     * </p>
     *
     * @param topic Topic name
     */
    public void createTopic(String topic) {
        long start = System.currentTimeMillis();
        CreateTopicsResult created = this.adminClient.createTopics(List.of(new NewTopic(topic, 1, (short) 1)));
        try {
            created.all().get(getDefaultTimeout(), TimeUnit.SECONDS);
            Utils.logStdOut("Created Kafka test topic '%s' in %,d milliseconds", topic,
                            Duration.ofMillis(System.currentTimeMillis() - start).toMillis());
        } catch (Throwable e) {
            Utils.logStdOut("Failed to create Kafka test topic '%s': %s", topic, e.getMessage());
            throw new RuntimeException("Failed to create Kafka test topic '" + topic + "'", e);
        }
    }

    /**
     * Deletes the given topic
     *
     * @param topic Topic
     */
    public void deleteTopic(String topic) {
        long start = System.currentTimeMillis();
        DeleteTopicsResult deleted = this.adminClient.deleteTopics(List.of(topic));
        try {
            deleted.all().get(getDefaultTimeout(), TimeUnit.SECONDS);
            Utils.logStdOut("Deleted Kafka test topic '%s' in %,d milliseconds", topic,
                            Duration.ofMillis(System.currentTimeMillis() - start).toMillis());
        } catch (Throwable e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                // Ignore and continue
                Utils.logStdOut("Test asked to delete Kafka test topic '%s' that does not exist", topic);
            } else {
                Utils.logStdOut("Failed to delete Kafka test topic '%s': %s", topic, e.getMessage());
                throw new RuntimeException("Failed to delete Kafka test topic '" + topic + "'", e);
            }
        }
    }

    /**
     * Gets the default timeout in seconds to wait for Kafka admin operations to complete
     * <p>
     * This is {@value #DEFAULT_TIMEOUT} seconds by default.  Derived classes may wish to increase this e.g. because of
     * security overheads in establishing connections
     * </p>
     *
     * @return Default timeout
     */
    protected int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * Tears down the test cluster
     */
    public void teardown() {
        if (this.kafka != null) {
            Utils.logStdOut("Stopping Kafka Test Cluster...");
            long start = System.currentTimeMillis();
            this.adminClient.close();
            this.kafka.stop();
            Duration elapsed = Duration.ofMillis(System.currentTimeMillis() - start);
            Utils.logStdOut("Kafka Test Cluster stopped in %,d seconds!", elapsed.toSeconds());
        }
        this.adminClient = null;
    }
}
