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
import com.github.rvesse.airline.annotations.restrictions.NotBlank;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.AcknowledgingListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.DistributionLifecycleListener;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTracker;
import io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTrackerRegistry;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeDeserializer;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeSerializer;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Options pertaining to creating a distribution lifecycle tracker for lifecycle aware services
 */
public class DistributionLifecycleTrackerOptions {

    /**
     * The default distribution lifecycle topic
     */
    public static final String DEFAULT_LIFECYCLE_TOPIC = "distribution-lifecycle";
    /**
     * The default distribution lifecycle DLQ topic
     */
    public static final String DEFAULT_LIFECYCLE_DLQ_TOPIC = "distribution-lifecycle.dlq";

    @Option(name = {
            "--dist-lifecycle-bootstrap-server", "--dist-lifecycle-bootstrap-servers"
    }, title = "ActionBootstrapServers", description = "Provides a comma separated list of bootstrap servers to use for creating the initial connection to Kafka.  For commands that connect to Kafka anyway this option is unnecessary provided the Kafka source is configured via the --bootstrap-servers option, however for commands that don't require a Kafka connection normally this option is required for the Distribution Lifecycle Tracker to work correctly.")
    String distLifecycleBootstrapServers = Configurator.get(new String[] {
            CliEnvironmentVariables.DISTRIBUTION_LIFECYCLE_BOOTSTRAP_SERVERS, KafkaConfiguration.BOOTSTRAP_SERVERS
    });

    @Option(name = {
            "--dist-lifecycle-topic"
    }, description = "Specifies a Kafka topic used to manage distribution lifecycle")
    @NotBlank
    String distLifecycleTopic = Configurator.get(new String[] { CliEnvironmentVariables.DISTRIBUTION_LIFECYCLE_TOPIC },
                                                 DEFAULT_LIFECYCLE_TOPIC);

    @Option(name = {
            "--dist-lifecycle-dlq-topic"
    }, description = "Specifies a Kafka Dead Letter Queue (DLQ) topic to which any malformed distribution lifecycle events should be forwarded")
    @NotBlank
    String distLifecycleDlqTopic =
            Configurator.get(new String[] { CliEnvironmentVariables.DISTRIBUTION_LIFECYCLE_DLQ_TOPIC },
                             DEFAULT_LIFECYCLE_DLQ_TOPIC);

    @Option(name = "--no-singleton", arity = 0, hidden = true, description = "Disables use of singleton tracker registration which is useful when test commands are running in the same process")
    private boolean singleton = true;

    /**
     * Creates the distribution lifecycle tracker
     *
     * @param bootstrapServers Kafka Bootstrap Servers
     * @param consumerGroup    Kafka Consumer Group
     * @param kafkaOptions     Kafka Options
     * @param application      Application ID
     * @param stateStore       Distribution Lifecycle State Store
     * @param listenerThreads  Number of background threads to use for processing lifecycle listeners
     * @param listeners        Distribution lifecycle listeners
     * @return Lifecycle tracker
     */
    public DistributionLifecycleTracker create(String bootstrapServers, String consumerGroup,
                                               KafkaConfigurationOptions kafkaOptions, String application,
                                               DistributionLifecycleStateStore stateStore, int listenerThreads,
                                               List<DistributionLifecycleListener> listeners) {
        // If using a singleton return the existing instance if previously created since applications should only
        // have a single lifecycle tracker running
        if (this.singleton) {
            if (DistributionLifecycleTrackerRegistry.getInstance() != null) {
                return DistributionLifecycleTrackerRegistry.getInstance();
            }
        }

        //@formatter:off
        KafkaEventSource<UUID, LazyEnvelope> source
                = KafkaEventSource.<UUID, LazyEnvelope>create()
                                  .bootstrapServers(selectBootstrapServers(bootstrapServers,
                                                                           this.distLifecycleBootstrapServers))
                                  .consumerConfig(kafkaOptions.getAdditionalProperties())
                                  .topic(this.distLifecycleTopic)
                                  .consumerGroup(consumerGroup)
                                  .readPolicy(KafkaReadPolicies.fromEarliest())
                                  .commitOnProcessed()
                                  .keyDeserializer(UUIDDeserializer.class)
                                  .valueDeserializer(LazyEnvelopeDeserializer.class)
                                  .build();
        KafkaSink<UUID, LazyEnvelope> dlq
                = KafkaSink.<UUID, LazyEnvelope>create()
                           .bootstrapServers(selectBootstrapServers(bootstrapServers,
                                                                    this.distLifecycleBootstrapServers))
                           .topic(this.distLifecycleDlqTopic)
                           .producerConfig(kafkaOptions.getAdditionalProperties())
                           .keySerializer(UUIDSerializer.class)
                           .valueSerializer(LazyEnvelopeSerializer.class)
                           .async()
                           .lingerMs(50)
                           .build();
        //@formatter:on
        DistributionLifecycleTracker tracker = DistributionLifecycleTracker.builder()
                                                                           .application(application)
                                                                           .eventSource(source)
                                                                           .dlq(dlq)
                                                                           .listenerThreads(listenerThreads)
                                                                           .listeners(listeners)
                                                                           .stateStore(stateStore)
                                                                           .flushFrequency(stateStore.requiresFlush() ?
                                                                                           Duration.ofSeconds(20) :
                                                                                           Duration.ZERO)
                                                                           .pollTimeout(Duration.ofSeconds(5))
                                                                           .trackerStartupTimeout(
                                                                                   Duration.ofSeconds(15))
                                                                           .build();
        if (this.singleton) {
            DistributionLifecycleTrackerRegistry.setInstance(tracker);
        }
        return tracker;
    }

    /**
     * Selects the first valid bootstrap servers value from the given values
     *
     * @param bootstrapServers Bootstrap server values
     * @return First valid value, or {@code null} if no valid value
     */
    private String selectBootstrapServers(String... bootstrapServers) {
        for (String bootstrapServer : bootstrapServers) {
            if (StringUtils.isNotBlank(bootstrapServer)) {
                return bootstrapServer;
            }
        }
        return null;
    }

    /**
     * Creates an {@link AcknowledgingListener} decorator around the applications actual distribution lifecycle listener
     * that handles generating the
     * {@link io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement} events back to the
     * Distribution Lifecycle topic
     *
     * @param bootstrapServers Kafka Bootstrap Servers
     * @param kafkaOptions     Kafka Configuration Options
     * @param application      Application ID
     * @param appVersion       Application Version
     * @param stateStore       Distribution Lifecycle state store
     * @param listener         Distribution Lifecycle listener that encapsulates the applications actual lifecycle event
     *                         handling logic
     * @return Decorated listener that generates acknowledgements during listener processing
     */
    public AcknowledgingListener createAckListener(String bootstrapServers, KafkaConfigurationOptions kafkaOptions,
                                                   String application, String appVersion,
                                                   DistributionLifecycleStateStore stateStore,
                                                   DistributionLifecycleListener listener) {
        return AcknowledgingListener.builder()
                                    .sink(KafkaSink.<UUID, LazyEnvelope>create()
                                                   .bootstrapServers(selectBootstrapServers(bootstrapServers,
                                                                                            this.distLifecycleBootstrapServers))
                                                   .topic(this.distLifecycleTopic)
                                                   .producerConfig(kafkaOptions.getAdditionalProperties())
                                                   .keySerializer(UUIDSerializer.class)
                                                   .valueSerializer(LazyEnvelopeSerializer.class)
                                                   .async()
                                                   .lingerMs(50)
                                                   .build())
                                    .application(application)
                                    .version(appVersion)
                                    .listener(listener)
                                    .stateStore(stateStore)
                                    .build();
    }

}
