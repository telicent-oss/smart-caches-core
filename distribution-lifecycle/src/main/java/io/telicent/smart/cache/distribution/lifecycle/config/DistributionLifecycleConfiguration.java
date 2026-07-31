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
package io.telicent.smart.cache.distribution.lifecycle.config;

import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.AcknowledgingListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.DistributionLifecycleListener;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.store.apps.AppDistributionLifecycleStoreFile;
import io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTracker;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeDeserializer;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeSerializer;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Provides the configuration variables and helper methods shared by the components that produce and consume
 * distribution lifecycle state.  This is the starting point for services that need to support configurable distribution
 * lifecycle.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistributionLifecycleConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleConfiguration.class);

    /**
     * The default distribution lifecycle topic
     */
    public static final String DEFAULT_LIFECYCLE_TOPIC = "distribution-lifecycle";
    /**
     * The default distribution lifecycle DLQ topic
     */
    public static final String DEFAULT_LIFECYCLE_DLQ_TOPIC = "distribution-lifecycle.dlq";

    /**
     * Environment variable controlling whether distribution lifecycle processing is enabled.
     *
     * @see #isEnabled()
     */
    public static final String DISTRIBUTION_LIFECYCLE_ENABLED = "DISTRIBUTION_LIFECYCLE_ENABLED";

    /**
     * Environment variable specifying the file used to persist/read distribution lifecycle tracking state.
     */
    public static final String DISTRIBUTION_LIFECYCLE_STATE_FILE = "DISTRIBUTION_LIFECYCLE_STATE_FILE";

    /**
     * Environment variable specifying the number of threads used to dispatch distribution lifecycle events to
     * listeners.  Default is {@value #DEFAULT_LISTENER_THREADS}.
     */
    public static final String DISTRIBUTION_LIFECYCLE_LISTENER_THREADS = "DISTRIBUTION_LIFECYCLE_LISTENER_THREADS";

    /**
     * Default number of listener threads used to dispatch distribution lifecycle events if not explicitly configured.
     */
    public static final int DEFAULT_LISTENER_THREADS = 1;

    /**
     * Resolves whether distribution lifecycle processing is enabled.
     * <p>
     * For now this defaults to {@code false}, once sufficiently validated this feature will be updated to default to
     * {@code true}.
     * </p>
     *
     * @return True if enabled, false otherwise
     */
    public static boolean isEnabled() {
        return Configurator.get(new String[] { DISTRIBUTION_LIFECYCLE_ENABLED }, Boolean::parseBoolean, false);
    }

    /**
     * Resolves the configured distribution lifecycle listener thread count.
     *
     * @return Listener thread count, will be {@link #DEFAULT_LISTENER_THREADS} it not configured or invalid
     * configuration
     */
    public static int resolveListenerThreads() {
        return Configurator.get(new String[] { DISTRIBUTION_LIFECYCLE_LISTENER_THREADS }, Integer::parseInt,
                                DEFAULT_LISTENER_THREADS);
    }

    /**
     * Resolves the distribution lifecycle state file.
     * <p>
     * This <strong>MUST</strong> be explicitly configured as while we could generate a default location in practise we
     * have no guarantee that the default location would be persistent.  If the location isn't persistent then the
     * applications view of distribution lifecycle states and the event driven view will get out of sync.  Thus, we
     * won't be able to reliably enforce distribution lifecycle.  Therefore, other methods like
     * {@link #createStateStore(String)} which call this method <strong>MUST</strong> throw errors
     * upwards and/or refuse to start applications when this isn't configured when it should be.
     * </p>
     *
     * @return State file, or {@code null} if no relevant configuration
     */
    public static File resolveStateFile() {
        return Configurator.get(new String[] { DISTRIBUTION_LIFECYCLE_STATE_FILE }, File::new, null);
    }

    /**
     * Attempts to create a distribution lifecycle state store based on available configuration, may return {@code null}
     * if this feature is disabled (see {@link #isEnabled()}).
     *
     * @return Distribution Lifecycle State Store, or {@code null} if feature disabled
     * @throws IllegalStateException If sufficient configuration is present to attempt state store creation but the
     *                               creation fails
     */
    public static DistributionLifecycleStateStore createStateStore(String application) {
        boolean enabled = DistributionLifecycleConfiguration.isEnabled();
        if (!enabled) {
            LOGGER.info(
                    "Distribution lifecycle is disabled ({}=false), service will not react to any distribution events",
                    DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED);
            return null;
        }

        try {
            File stateFile = DistributionLifecycleConfiguration.resolveStateFile();
            if (stateFile == null) {
                throw new IllegalStateException(String.format("Missing configuration %s",
                                                              DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_STATE_FILE));
            }
            return AppDistributionLifecycleStoreFile.builder().app(application).stateFile(stateFile).build();
        } catch (Throwable t) {
            LOGGER.warn(
                    "Unable to create the Distribution Lifecycle State Store - {} - service will refuse to start as a result",
                    t.getMessage());
            throw new IllegalStateException("Failed to create distribution lifecycle state store: " + t.getMessage(),
                                            t);
        }
    }

    /**
     * Attempts to create an {@link AcknowledgingListener} wrapped around the given listener
     *
     * @param kafkaConfig Kafka Configuration
     * @param application Application ID
     * @param appVersion  Application Version
     * @param stateStore  State Store
     * @param listener    Lifecycle Listener
     * @throws NullPointerException     If any required argument is {@code null}
     *      * @throws IllegalArgumentException If any provided argument is invalid
     */
    public static AcknowledgingListener createAcknowledgingListener(KafkaConfiguration kafkaConfig, String application,
                                                                    String appVersion,
                                                                    DistributionLifecycleStateStore stateStore,
                                                                    DistributionLifecycleListener listener) {
        Objects.requireNonNull(kafkaConfig, "Kafka Configuration cannot be null");
        return AcknowledgingListener.builder()
                                    .sink(kafkaConfig.outputBuilder(UUIDSerializer.class, LazyEnvelopeSerializer.class)
                                                     .async()
                                                     .lingerMs(50)
                                                     .build())
                                    .application(application)
                                    .version(appVersion)
                                    .listener(listener)
                                    .stateStore(stateStore)
                                    .build();
    }

    /**
     * Attempts to create a {@link DistributionLifecycleTracker}
     * <p>
     * The application is responsible for installing this as the singleton via
     * {@link
     * io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTrackerRegistry#setInstance(DistributionLifecycleTracker)}
     * if that's appropriate to the application.
     * </p>
     *
     * @param kafkaConfig     Kafka Configuration
     * @param application     Application ID
     * @param stateStore      State Store
     * @param listenerThreads Listener Threads
     * @param listeners       Lifecycle listeners
     * @return Tracker, or {@code null} if distribution lifecycle feature disabled as reported by {@link #isEnabled()}
     * @throws NullPointerException     If any required argument is {@code null}
     * @throws IllegalArgumentException If any provided argument is invalid
     * @throws IllegalStateException    If the tracker cannot be created for any reason
     */
    public static DistributionLifecycleTracker createTracker(KafkaConfiguration kafkaConfig, String application,
                                                             DistributionLifecycleStateStore stateStore,
                                                             int listenerThreads,
                                                             List<DistributionLifecycleListener> listeners) {

        if (!DistributionLifecycleConfiguration.isEnabled()) {
            return null;
        }

        Objects.requireNonNull(kafkaConfig, "Kafka Configuration cannot be null");

        //@formatter:off
        KafkaEventSource<UUID, LazyEnvelope> source
                = kafkaConfig.inputBuilder(UUIDDeserializer.class, LazyEnvelopeDeserializer.class)
                             .fromEarliest()
                             .commitOnProcessed()
                             .build();
        KafkaSink<UUID, LazyEnvelope> dlq = null;
        if (kafkaConfig.isValidForDlq()) {
            dlq = kafkaConfig.dlqBuilder(UUIDSerializer.class, LazyEnvelopeSerializer.class)
                             .async()
                             .lingerMs(50)
                             .build();
        }
        //@formatter:on
        return DistributionLifecycleTracker.builder()
                                           .application(application)
                                           .eventSource(source)
                                           .dlq(dlq)
                                           .listenerThreads(listenerThreads)
                                           .listeners(listeners)
                                           .stateStore(stateStore)
                                           .flushFrequency(
                                                   stateStore.requiresFlush() ? Duration.ofSeconds(20) : Duration.ZERO)
                                           .pollTimeout(Duration.ofSeconds(5))
                                           .trackerStartupTimeout(Duration.ofSeconds(15))
                                           .build();
    }
}
