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

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.KafkaContainer;

import java.util.Map;
import java.util.Properties;

/**
 * An attempt to create a Kafka test cluster that encrypts its traffic
 * <p>
 * <strong>WARNING:</strong> This is not yet functional as I haven't found the right combination of Kafka options to
 * make it work correctly, currently the container fails to start so the test cluster is unusable!
 * </p>
 */
public class EncryptedKafkaTestCluster extends SecureKafkaTestCluster {

    public EncryptedKafkaTestCluster(String adminUsername, String adminPassword, Map<String, String> additionalUsers) {
        super(adminUsername, adminPassword, additionalUsers);
    }

    @Override
    protected KafkaContainer createKafkaContainer() {
        throw new RuntimeException("EncryptedKafkaTestCluster is not yet functional - DO NOT USE!");

        //@formatter:off
        /*return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
                .withKraft()
                .withFileSystemBind(new File("certs/").getAbsolutePath(), "/var/private/ssl/")
                .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:SASL_SSL,BROKER:SASL_SSL")
                .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "BROKER")
                .withEnv("KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL", "SCRAM-SHA-256")
                .withEnv("KAFKA_LISTENER_NAME_PLAINTEXT_SASL_ENABLED_MECHANISMS", "SCRAM-SHA-256")
                .withEnv("KAFKA_LISTENER_NAME_BROKER_SASL_ENABLED_MECHANISMS", "SCRAM-SHA-256")
                .withEnv("KAFKA_LISTENER_NAME_BROKER_SASL_SSL_SASL_JAAS_CONFIG", buildBrokerJaasConfig())
                .withEnv("KAFKA_SASL_JAAS_CONFIG", KafkaSecurity.scramLogin(this.adminUsername, this.adminPassword))
                .withEnv("KAFKA_LISTENER_NAME_PLAINTEXT_SASL_SSL_SASL_JAAS_CONFIG", buildBrokerJaasConfig())
                .withEnv("KAFKA_SSL_KEYSTORE_LOCATION", "/var/private/ssl/broker-keystore")
                .withEnv("KAFKA_SSL_KEYSTORE_PASSWORD", "squirrel")
                .withEnv("KAFKA_SSL_KEY_PASSWORD", "squirrel")
                .withEnv("KAFKA_SSL_TRUSTSTORE_LOCATION", "/var/private/ssl/client-truststore")
                .withEnv("KAFKA_SSL_TRUSTSTORE_PASSWORD", "squirrel")
                .withEnv("KAFKA_SUPER_USERS", "User:" + this.adminUsername)
                .withStartupTimeout(Duration.ofSeconds(180));*/
        //@formatter:on
    }

    @Override
    protected @NotNull String buildBrokerJaasConfig() {
        StringBuilder builder = new StringBuilder();
        builder.append("org.apache.kafka.common.security.scram.ScramLoginModule required ")
               .append("username=\"")
               .append(this.adminUsername)
               .append("\" password=\"")
               .append(this.adminPassword)
               .append("\" ")
               .append("user_")
               .append(this.adminUsername)
               .append("=\"")
               .append(this.adminPassword)
               .append("\" ");
        for (Map.Entry<String, String> user : this.additionalUsers.entrySet()) {
            builder.append("user_").append(user.getKey()).append("=\"").append(user.getValue()).append("\" ");
        }
        builder.append(";");
        return builder.toString();
    }

    @Override
    protected void addAdminClientProperties(Properties properties) {
        super.addAdminClientProperties(properties);
        properties.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
        properties.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, KafkaSecurity.scramLogin(this.adminUsername, this.adminPassword));
    }

    @Override
    public Properties getClientProperties(String username, String password) {
        Properties properties = new Properties();
        properties.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, KafkaSecurity.scramLogin(username, password));
        return properties;
    }
}
