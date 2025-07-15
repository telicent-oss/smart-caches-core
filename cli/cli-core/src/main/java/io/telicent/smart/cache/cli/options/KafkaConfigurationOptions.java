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
package io.telicent.smart.cache.cli.options;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.AllowedRawValues;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Options related to provided additional Kafka configuration, especially around authentication
 */
public class KafkaConfigurationOptions {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfigurationOptions.class);

    @Option(name = {
            "--kafka-user", "--kafka-username"
    }, title = "KafkaUser", description = "Specifies the username used to connect to Kafka.  May also be specified via the KAFKA_USER environment variable.")
    private String username = Configurator.get(KafkaConfiguration.KAFKA_USERNAME);

    @Option(name = "--kafka-password", title = "KafkaPassword", description = "Specifies the password used to connect to Kafka.  Generally it is better to use the KAFKA_PASSWORD environment variable to supply this instead of supplying it directly at the command line.")
    private String password = Configurator.get(KafkaConfiguration.KAFKA_PASSWORD);

    @Option(name = "--kafka-login-type", title = "LoginType", description = "Specifies the Kafka Login Type to use in conjunction with the --kafka-user and --kafka-password arguments for SASL authentication, if you use an alternative Kafka authentication mechanism, or a variant of SASL not listed as supported here, then use --kafka-properties to supply a suitably configured properties file instead.")
    @AllowedRawValues(allowedValues = {
            KafkaConfiguration.LOGIN_PLAIN,
            KafkaConfiguration.LOGIN_SCRAM_SHA_256,
            KafkaConfiguration.LOGIN_SCRAM_SHA_512
    })
    private String loginType =
            Configurator.get(new String[] { KafkaConfiguration.KAFKA_LOGIN_TYPE }, KafkaConfiguration.LOGIN_PLAIN);

    @Option(name = "--kafka-properties", title = "KafkaPropertiesFile", description = "Specifies a properties file containing Kafka configuration properties to use with Kafka.")
    @com.github.rvesse.airline.annotations.restrictions.File(mustExist = true)
    private File propertiesFile = Configurator.get(new String[] {
            KafkaConfiguration.KAFKA_CONFIG_FILE_PATH, KafkaConfiguration.KAFKA_PROPERTIES_FILE
    }, File::new, null);

    @Option(name = "--kafka-property", title = "KafkaProperty", arity = 2, description = "Specifies a Kafka configuration property to use with Kafka.  These are loaded prior to any properties from a file specified via the --kafka-properties option.")
    private List<String> extraConfiguration = new ArrayList<>();

    private Properties properties = null;

    /**
     * Gets the additional configuration properties to pass to Kafka
     *
     * @return Configuration properties
     */
    public synchronized Properties getAdditionalProperties() {
        // Use cached properties if present
        if (properties != null) {
            return this.properties;
        }
        Properties properties = new Properties();

        // If a Username and Password are provided then configure Kafka properties for login based on those
        if (StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password)) {
            KafkaConfiguration.addLoginProperties(properties, this.loginType, this.username, this.password);
        }

        // Load in any command line provided properties
        for (int i = 0; i <= this.extraConfiguration.size() - 2; i += 2) {
            properties.put(this.extraConfiguration.get(i), this.extraConfiguration.get(i + 1));
        }

        // Load in the properties file (if specified)
        if (this.propertiesFile != null) {
            try (FileInputStream input = new FileInputStream(this.propertiesFile)) {
                properties.load(input);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to read user supplied Kafka properties file %s",
                                                         this.propertiesFile.getAbsolutePath()));
            }
        }

        LOGGER.info("Gathered/generated {} Kafka properties based on supplied options", properties.size());
        // Cache properties for later reuse because some option modules and/or commands may call this method multiple
        // times and this avoids repeating the property loading unnecessary
        this.properties = properties;
        return properties;
    }
}
