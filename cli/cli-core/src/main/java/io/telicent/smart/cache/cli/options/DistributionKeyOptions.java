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
import io.telicent.smart.cache.projectors.sinks.events.DistributionKeySink;
import io.telicent.smart.cache.sources.DistributionKeyStrategy;
import io.telicent.smart.cache.sources.kafka.KafkaDistributionKeys;
import org.apache.kafka.common.utils.Bytes;

/**
 * Options pertaining to writing the Distribution ID as the Kafka message key.
 * <p>
 * The Core Data Management design requires that events carrying a distribution's data use its Distribution ID as
 * their message key, so that all of a distribution's events hash to a single Kafka partition and are therefore
 * processed in-order.  The {@value io.telicent.smart.cache.sources.TelicentHeaders#DISTRIBUTION_ID} header continues
 * to be written alongside the key for backwards compatibility with pipelines and services that predate this.
 * </p>
 */
public class DistributionKeyOptions {

    @Option(name = {
            "--distribution-key-strategy"
    }, title = "DistributionKeyStrategy", description = "Specifies how the Kafka message key is derived from the Distribution ID.  'distribution-id' (the default) uses the Distribution ID verbatim which guarantees that a distribution's events are processed in-order.  'distribution-id-and-uuid' appends a UUID so that every event has a unique key, which keeps Kafka log compaction viable, but note that this only preserves in-order processing when producers are also configured to use the DistributionIdPartitioner.")
    @AllowedRawValues(allowedValues = { "distribution-id", "distribution-id-and-uuid" })
    String distributionKeyStrategy =
            Configurator.get(new String[] { DistributionKeyStrategy.CONFIG_KEY },
                             DistributionKeyStrategy.DEFAULT.configValue());

    @Option(name = {
            "--no-distribution-key"
    }, arity = 0, description = "Disables writing the Distribution ID as the Kafka message key, events are produced with whatever key they already had.  Provided for staged rollout, the Distribution ID header is unaffected.")
    private boolean noDistributionKey = false;

    /**
     * Gets the configured key strategy
     *
     * @return Key strategy
     * @throws IllegalArgumentException Thrown if the configured strategy is not recognised
     */
    public DistributionKeyStrategy getStrategy() {
        return DistributionKeyStrategy.parse(this.distributionKeyStrategy);
    }

    /**
     * Gets whether the Distribution ID is written as the Kafka message key.
     * <p>
     * Disabled either by the {@code --no-distribution-key} option or by setting the
     * {@value DistributionKeyStrategy#ENABLED_CONFIG_KEY} environment variable to {@code false}.
     * </p>
     *
     * @return True if message keys are written, false otherwise
     */
    public boolean isEnabled() {
        if (this.noDistributionKey) {
            return false;
        }
        return Configurator.get(new String[] { DistributionKeyStrategy.ENABLED_CONFIG_KEY }, Boolean::parseBoolean,
                                Boolean.TRUE);
    }

    /**
     * Creates a {@link DistributionKeySink} builder for a {@link Bytes} keyed pipeline, configured from these options
     *
     * @param <TValue> Value type
     * @return Builder
     */
    public <TValue> DistributionKeySink.Builder<Bytes, TValue> bytesKeySink() {
        DistributionKeySink.Builder<Bytes, TValue> builder = KafkaDistributionKeys.bytesKeySink();
        return builder.strategy(this.getStrategy()).enabled(this.isEnabled());
    }

    /**
     * Creates a {@link DistributionKeySink} builder for a {@link String} keyed pipeline, configured from these options
     *
     * @param <TValue> Value type
     * @return Builder
     */
    public <TValue> DistributionKeySink.Builder<String, TValue> stringKeySink() {
        DistributionKeySink.Builder<String, TValue> builder = KafkaDistributionKeys.stringKeySink();
        return builder.strategy(this.getStrategy()).enabled(this.isEnabled());
    }
}
