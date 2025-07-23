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
import com.github.rvesse.airline.annotations.restrictions.RequiredUnlessEnvironment;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;

/**
 * Options related to writing output to Kafka
 */
public class KafkaOutputOptions extends KafkaConfigurationOptions {

    /**
     * Environment variable used to specify an output topic
     */
    public static final String OUTPUT_TOPIC = "OUTPUT_TOPIC";

    /**
     * Kafka bootstrap servers
     */
    @Option(name = { "--bootstrap-server", "--bootstrap-servers" }, title = "BootstrapServers",
            description = "Provides a comma separated list of bootstrap servers to use for creating the initial connection to Kafka.")
    @RequiredUnlessEnvironment(variables = { KafkaConfiguration.BOOTSTRAP_SERVERS })
    public String bootstrapServers = Configurator.get(KafkaConfiguration.BOOTSTRAP_SERVERS);

    /**
     * Kafka input topic
     */
    @Option(name = { "-t", "--topic" }, title = "KafkaTopic",
            description = "Provides the name of the Kafka topic to write events to.")
    @RequiredUnlessEnvironment(variables = { KafkaConfiguration.TOPIC, OUTPUT_TOPIC })
    public String topic = Configurator.get(new String[] { KafkaConfiguration.TOPIC, OUTPUT_TOPIC });
}
