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
package io.telicent.smart.cache.sources;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * Strategies for deriving a Kafka message key from a Distribution ID.
 * <p>
 * The Core Data Management design requires that events carrying data for a distribution use the Distribution ID as
 * their message key, so that all events for a distribution hash to the same Kafka partition and are therefore
 * processed in-order.  See the <em>Data Partitioning in Kafka</em> section of that design.
 * </p>
 * <p>
 * Regardless of the strategy the key is always the UTF-8 encoding of a string.  This means a producer using
 * {@code BytesSerializer} and a consumer using {@code StringDeserializer} (or vice versa) remain wire compatible, so
 * adopting message keys does not require any pipeline to change its configured serializers.
 * </p>
 */
public enum DistributionKeyStrategy {
    /**
     * Uses the Distribution ID verbatim as the message key.
     * <p>
     * This gives the strongest ordering guarantee, every event for a distribution lands on a single partition, but it
     * is <strong>not</strong> compatible with Kafka's log compaction.  A compacted topic would eventually reduce each
     * distribution down to its single most recent event.
     * </p>
     */
    DISTRIBUTION_ID("distribution-id") {
        @Override
        public String key(String distributionId) {
            return StringUtils.isBlank(distributionId) ? null : distributionId;
        }
    },
    /**
     * Uses {@code <distributionId>/<uuid>} as the message key, i.e. the Distribution ID with a freshly generated UUID
     * appended.
     * <p>
     * Every event therefore has a unique key which keeps log compaction, and thus deletion of a distribution's events
     * from the topic by tombstoning, viable.
     * </p>
     * <p>
     * <strong>NB</strong> With Kafka's default partitioner each event hashes independently, so this strategy does
     * <em>not</em> on its own give the per-distribution ordering guarantee that {@link #DISTRIBUTION_ID} does.  Where
     * both properties are needed configure the
     * {@code io.telicent.smart.cache.sources.kafka.DistributionIdPartitioner} from the {@code event-source-kafka}
     * module, which partitions on the Distribution ID portion of the key only.
     * </p>
     */
    DISTRIBUTION_ID_AND_UUID("distribution-id-and-uuid") {
        @Override
        public String key(String distributionId) {
            return StringUtils.isBlank(distributionId) ? null :
                   distributionId + DistributionIds.KEY_SEPARATOR + UUID.randomUUID();
        }
    };

    /**
     * Environment variable/configuration key used to select the strategy
     */
    public static final String CONFIG_KEY = "DISTRIBUTION_KEY_STRATEGY";

    /**
     * Environment variable/configuration key used to enable/disable writing Distribution ID message keys at all,
     * defaults to enabled
     */
    public static final String ENABLED_CONFIG_KEY = "DISTRIBUTION_KEY_ENABLED";

    /**
     * The default strategy, used when nothing is configured
     */
    public static final DistributionKeyStrategy DEFAULT = DISTRIBUTION_ID;

    private final String configValue;

    DistributionKeyStrategy(String configValue) {
        this.configValue = configValue;
    }

    /**
     * Gets the value used to select this strategy in configuration
     *
     * @return Configuration value
     */
    public String configValue() {
        return this.configValue;
    }

    /**
     * Generates the message key for the given Distribution ID
     *
     * @param distributionId Distribution ID
     * @return Message key, or {@code null} if the Distribution ID was {@code null}/blank in which case callers
     * <strong>MUST</strong> leave the event's existing key untouched
     */
    public abstract String key(String distributionId);

    /**
     * Parses a strategy from its configuration value.
     * <p>
     * Both the {@link #configValue()} form, e.g. {@code distribution-id-and-uuid}, and the enum member name, e.g.
     * {@code DISTRIBUTION_ID_AND_UUID}, are accepted and matching is case-insensitive.  A blank value yields
     * {@link #DEFAULT}.
     * </p>
     *
     * @param value Configuration value
     * @return Strategy
     * @throws IllegalArgumentException Thrown if the value does not identify a strategy
     */
    public static DistributionKeyStrategy parse(String value) {
        if (StringUtils.isBlank(value)) {
            return DEFAULT;
        }
        String normalised = value.trim();
        for (DistributionKeyStrategy strategy : values()) {
            if (StringUtils.equalsIgnoreCase(strategy.configValue, normalised) || StringUtils.equalsIgnoreCase(
                    strategy.name(), normalised)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException(
                "'" + value + "' is not a valid Distribution ID key strategy, expected one of " + DISTRIBUTION_ID.configValue + " or " + DISTRIBUTION_ID_AND_UUID.configValue);
    }
}
