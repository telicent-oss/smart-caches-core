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
import io.telicent.smart.cache.configuration.sources.NullSource;
import io.telicent.smart.cache.configuration.sources.PropertiesSource;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.Util;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.AcknowledgingListener;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.LoggingListener;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.store.global.GlobalDistributionLifecycleStoreMemory;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.LazyUUID;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.TopicExistenceChecker;
import io.telicent.smart.cache.sources.kafka.config.KafkaConfiguration;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import io.telicent.smart.cache.sources.kafka.policies.automatic.AutoFromBeginning;
import io.telicent.smart.cache.sources.kafka.policies.automatic.AutoFromEarliest;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Uuid;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.jspecify.annotations.NonNull;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDistributionLifecycleConfiguration {

    @BeforeClass
    public void setup() {
        Configurator.reset();
    }

    @AfterMethod
    public void cleanup() {
        Configurator.reset();
    }

    @AfterClass
    public void teardown() {
        Configurator.reset();
    }

    @Test
    public void givenFeatureDisabled_whenCreatingComponents_thenNullReturned() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, false);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        Assert.assertNull(DistributionLifecycleConfiguration.createStateStore("test"));
        Assert.assertNull(
                DistributionLifecycleConfiguration.createAcknowledgingListener(null, "test", "1.2.3", null, null));
        Assert.assertNull(
                DistributionLifecycleConfiguration.createTracker(null, "test", null, 1, Collections.emptyList()));
    }

    @Test
    public void givenNoConfiguration_whenCheckingDistributionLifecycleConfig_thenDisabled() {
        // Given
        Configurator.setSingleSource(NullSource.INSTANCE);

        // When and Then
        Assert.assertFalse(DistributionLifecycleConfiguration.isEnabled());
        Assert.assertNull(DistributionLifecycleConfiguration.createStateStore("test"));
        Assert.assertNull(DistributionLifecycleConfiguration.resolveStateFile());
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveListenerThreads(),
                            DistributionLifecycleConfiguration.DEFAULT_LISTENER_THREADS);
        Assert.assertNull(
                DistributionLifecycleConfiguration.createTracker(KafkaConfiguration.builder().build(), "test", null, 1,
                                                                 Collections.emptyList()));
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*Missing configuration.*")
    public void givenPartialConfiguration_whenCheckingDistributionLifecycleConfig_thenEnabled_andNoStoreCanBeCreated() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, "true");
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        Assert.assertTrue(DistributionLifecycleConfiguration.isEnabled());

        // And
        DistributionLifecycleConfiguration.createStateStore("test");
    }

    @Test
    public void givenFullConfiguration_whenCheckingDistributionLifecycleConfig_thenEnabled_andComponentsCanBeCreated() throws
            IOException {
        // Given
        File stateFile = Files.createTempFile("distribution-lifecycle-state", ".json").toFile();
        stateFile.delete();
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, "true");
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_STATE_FILE,
                       stateFile.getAbsolutePath());
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_LISTENER_THREADS, 4);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        Assert.assertTrue(DistributionLifecycleConfiguration.isEnabled());
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveStateFile(), stateFile);
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveListenerThreads(), 4);

        // And
        try (DistributionLifecycleStateStore store = DistributionLifecycleConfiguration.createStateStore(
                "test")) {
            Assert.assertTrue(store.activeEvents().isEmpty());

            try (AcknowledgingListener listener = DistributionLifecycleConfiguration.createAcknowledgingListener(
                    KafkaConfiguration.builder().bootstrapServers("localhost:9092").outputTopic("tests").build(),
                    "test",
                    "1.2.3", store, new LoggingListener())) {
                Assert.assertNotNull(listener);
            }
        }
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*state file unreadable.*")
    public void givenBadConfiguration_whenCheckingDistributionLifecycleConfig_thenEnabled_andStoreFailsToCreate() throws
            IOException {
        // Given
        File stateFile = Files.createTempFile("distribution-lifecycle-state", ".json").toFile();
        try (FileWriter writer = new FileWriter(stateFile)) {
            writer.write("junk");
        }
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, "true");
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_STATE_FILE,
                       stateFile.getAbsolutePath());
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_LISTENER_THREADS, 4);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        Assert.assertTrue(DistributionLifecycleConfiguration.isEnabled());
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveStateFile(), stateFile);
        Assert.assertEquals(DistributionLifecycleConfiguration.resolveListenerThreads(), 4);

        // And
        DistributionLifecycleConfiguration.createStateStore("test");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Kafka Configuration.*")
    public void givenNullKafkaConfiguration_whenCreatingListener_thenNPE() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, true);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        DistributionLifecycleConfiguration.createAcknowledgingListener(null, "test", "1.2.3", null, null);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Invalid configuration for output.*")
    public void givenEmptyKafkaConfiguration_whenCreatingListener_thenIllegalState() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, true);
        Configurator.setSingleSource(new PropertiesSource(properties));
        KafkaConfiguration kafkaConfiguration = KafkaConfiguration.builder().build();

        // When and Then
        DistributionLifecycleConfiguration.createAcknowledgingListener(kafkaConfiguration, "test", "1.2.3", null, null);
    }

    @Test
    public void givenNoStateStore_whenResolvingReadPolicy_thenFromBeginning() {
        // Given
        DistributionLifecycleStateStore store = null;

        // When
        KafkaReadPolicy<UUID, String> policy = DistributionLifecycleConfiguration.resolveReadPolicy(store);

        // Then
        Assert.assertTrue(policy instanceof AutoFromBeginning,
                          "Expected to read from the beginning when there is no state store");
    }

    @Test
    public void givenEmptyStateStore_whenResolvingReadPolicy_thenFromBeginning() {
        // Given
        try (DistributionLifecycleStateStore store = new GlobalDistributionLifecycleStoreMemory()) {
            Assert.assertTrue(store.isEmpty());

            // When
            KafkaReadPolicy<UUID, String> policy = DistributionLifecycleConfiguration.resolveReadPolicy(store);

            // Then
            Assert.assertTrue(policy instanceof AutoFromBeginning,
                              "Expected to read from the beginning so that an empty state store is rebuilt");
        }
    }

    @Test
    public void givenPopulatedStateStore_whenResolvingReadPolicy_thenFromEarliest() {
        // Given
        try (DistributionLifecycleStateStore store = new GlobalDistributionLifecycleStoreMemory()) {
            store.add(Util.action(UUID.randomUUID(), "distro", DistributionLifecycleState.Unregistered,
                                  DistributionLifecycleState.Registered));
            Assert.assertFalse(store.isEmpty());

            // When
            KafkaReadPolicy<UUID, String> policy = DistributionLifecycleConfiguration.resolveReadPolicy(store);

            // Then
            Assert.assertTrue(policy instanceof AutoFromEarliest,
                              "Expected to resume from the committed offsets when the state store is populated");
        }
    }

    @DataProvider(name = "timeouts")
    public static Object[][] timeouts() {
        return new Object[][] {
                { "PT30S", Duration.ofSeconds(30) },
                { "PT1M", Duration.ofMinutes(1) },
                { "PT5S", Duration.ofSeconds(5) },
                { "PT1H", Duration.ofHours(1) },
                { "", DistributionLifecycleConfiguration.DEFAULT_STARTUP_TIMEOUT },
                { "bad", DistributionLifecycleConfiguration.DEFAULT_STARTUP_TIMEOUT },
                { "PT-1S", DistributionLifecycleConfiguration.DEFAULT_STARTUP_TIMEOUT },
                { "PT0S", DistributionLifecycleConfiguration.DEFAULT_STARTUP_TIMEOUT }
        };
    }

    @Test(dataProvider = "timeouts")
    public void givenTimeoutConfiguration_whenResolving_thenAsExpected(String rawTimeout, Duration expected) {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_STARTUP_TIMEOUT, rawTimeout);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When
        Duration resolvedTimeout = DistributionLifecycleConfiguration.resolveTrackerStartupTimeout();

        // Then
        Assert.assertEquals(resolvedTimeout, expected);
    }

    @Test
    public void givenInMemorySource_whenPreparingSourceIdentity_thenToStringUsed() {
        // Given
        InMemoryEventSource<LazyUUID, LazyEnvelope> source = new InMemoryEventSource<>(Collections.emptyList());

        // When
        String sourceId = DistributionLifecycleConfiguration.determineSourceIdentity(source, Duration.ofSeconds(1));

        // Then
        Assert.assertEquals(sourceId, source.toString());
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void givenKafkaEventSource_whenPreparingSourceIdentity_thenKafkaTopicIdsUsed() {
        // Given
        AdminClient client = mock(AdminClient.class);
        Uuid topicId = Uuid.randomUuid();
        DescribeTopicsResult result = mockDescribeResult(topicId);
        when(client.describeTopics(any(Collection.class))).thenReturn(result);
        TopicExistenceChecker checker =
                new TopicExistenceChecker(client, "localhost:9092", List.of(KafkaTestCluster.DEFAULT_TOPIC), null);
        KafkaEventSource<LazyUUID, LazyEnvelope> source = mock(KafkaEventSource.class);
        when(source.getTopicExistenceChecker()).thenReturn(checker);

        // When
        String sourceId = DistributionLifecycleConfiguration.determineSourceIdentity(source, Duration.ofSeconds(1));

        // Then
        Assert.assertNotEquals(sourceId, source.toString());
        Assert.assertEquals(sourceId, topicId.toString());
    }

    private static DescribeTopicsResult mockDescribeResult(Uuid topicId) {
        DescribeTopicsResult result = mock(DescribeTopicsResult.class);
        when(result.allTopicNames()).thenReturn(KafkaFuture.completedFuture(Map.of(KafkaTestCluster.DEFAULT_TOPIC,
                                                                                   new TopicDescription(
                                                                                           KafkaTestCluster.DEFAULT_TOPIC,
                                                                                           false,
                                                                                           Collections.emptyList(),
                                                                                           Collections.emptySet(),
                                                                                           topicId))));
        return result;
    }

    @Test(expectedExceptions = IllegalStateException.class)
    @SuppressWarnings({ "unchecked" })
    public void givenKafkaEventSourceWhereNoTopicsExist_whenPreparingSourceIdentity_thenIllegalState() {
        // Given
        AdminClient client = mock(AdminClient.class);
        when(client.describeTopics(any(Collection.class))).thenThrow(new UnknownTopicOrPartitionException());
        TopicExistenceChecker checker =
                new TopicExistenceChecker(client, "localhost:9092", List.of(KafkaTestCluster.DEFAULT_TOPIC), null);
        KafkaEventSource<LazyUUID, LazyEnvelope> source = mock(KafkaEventSource.class);
        when(source.getTopicExistenceChecker()).thenReturn(checker);

        // When and Then
        DistributionLifecycleConfiguration.determineSourceIdentity(source, Duration.ofSeconds(1));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    @SuppressWarnings({ "unchecked" })
    public void givenKafkaEventSourceWhichGetsInterrupted_whenPreparingSourceIdentity_thenIllegalState() {
        // Given
        AdminClient client = mock(AdminClient.class);
        Uuid topicId = Uuid.randomUuid();
        DescribeTopicsResult result = mockDescribeResult(topicId);
        when(client.describeTopics(any(Collection.class))).thenReturn(result)
                                                          .thenAnswer(invocation -> {
                                                              throw new InterruptedException();
                                                          });
        TopicExistenceChecker checker =
                new TopicExistenceChecker(client, "localhost:9092", List.of(KafkaTestCluster.DEFAULT_TOPIC), null);
        KafkaEventSource<LazyUUID, LazyEnvelope> source = mock(KafkaEventSource.class);
        when(source.getTopicExistenceChecker()).thenReturn(checker);

        // When and Then
        DistributionLifecycleConfiguration.determineSourceIdentity(source, Duration.ofSeconds(1));
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "State store cannot be null")
    public void givenNoStateStore_whenCreatingTracker_thenNPE() {
        // Given
        Properties properties = new Properties();
        properties.put(DistributionLifecycleConfiguration.DISTRIBUTION_LIFECYCLE_ENABLED, true);
        Configurator.setSingleSource(new PropertiesSource(properties));

        // When and Then
        DistributionLifecycleConfiguration.createTracker(KafkaConfiguration.builder()
                                                                           .bootstrapServers("localhost:9092")
                                                                           .inputTopic(
                                                                                   DistributionLifecycleConfiguration.DEFAULT_LIFECYCLE_TOPIC)
                                                                           .consumerGroup("test")
                                                                           .build(), "app", null, 1,
                                                         Collections.emptyList());
    }
}
