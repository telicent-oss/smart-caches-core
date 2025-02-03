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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.time.Duration;

/**
 * Convenience wrapper around a {@link ConfluentKafkaContainer} instance for use in testing, this implementation creates a
 * completely insecure cluster.  See {@link SecureKafkaTestCluster} or {@link MutualTlsKafkaTestCluster} for secure
 * clusters.
 */
public class BasicKafkaTestCluster extends KafkaTestCluster {

    public static final String DEFAULT_TOPIC = "tests";

    /**
     * Creates a new test cluster
     */
    public BasicKafkaTestCluster() {

    }

    /**
     * Creates the actual Kafka container that forms the test cluster
     *
     * @return Kafka container
     */
    @SuppressWarnings("resource")
    protected GenericContainer createKafkaContainer() {
        //@formatter:off
        return new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"))
                    .withStartupTimeout(Duration.ofSeconds(30));

        //@formatter:on
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getBootstrapServers() {
        if (this.kafka != null) {
            if (this.kafka instanceof ConfluentKafkaContainer confluentKafkaContainer) {
                return confluentKafkaContainer.getBootstrapServers();
            } else if (this.kafka instanceof KafkaContainer kafkaContainer) {
                return kafkaContainer.getBootstrapServers();
            } else if (this.kafka instanceof org.testcontainers.containers.KafkaContainer deprecatedKafkaContainer) {
                return deprecatedKafkaContainer.getBootstrapServers();
            }
        }
        throw new RuntimeException("Unexpected Kafka Container in use");
    }
}
