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
package io.telicent.smart.cache.cli.commands.distributions;

import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.cli.options.DistributionLifecycleTrackerOptions;
import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.Util;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTracker;
import io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTrackerRegistry;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeSerializer;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.telicent.smart.cache.cli.commands.backup.DockerTestActionTracker.hasLine;

public class DockerTestDistributionLifecycleTracker extends AbstractCommandTests {

    public static final String APP_ID = "dist-lifecycle-tracker";
    static DistributionLifecycleTracker TRACKER = null;

    /**
     * Intentionally protected so we can extend this test class and run it against different test clusters
     */
    protected KafkaTestCluster kafka = new BasicKafkaTestCluster();

    @BeforeClass
    @Override
    public void setup() {
        this.kafka.setup();
        this.kafka.resetTopic(DistributionLifecycleTrackerOptions.DEFAULT_LIFECYCLE_DLQ_TOPIC);
        TRACKER = null;
        DistributionLifecycleTrackerRegistry.reset();
        // Uncomment for easier debugging
        //SmartCacheCommandTester.TEE_TO_ORIGINAL_STREAMS = true;
        super.setup();
    }

    @AfterMethod
    @Override
    public void testCleanup() {
        super.testCleanup();

        this.kafka.resetTopic(DistributionLifecycleTrackerOptions.DEFAULT_LIFECYCLE_DLQ_TOPIC);
        if (TRACKER != null) {
            TRACKER.close();
        }
        TRACKER = null;
        DistributionLifecycleTrackerRegistry.reset();
    }

    @AfterClass
    @Override
    public void teardown() {
        this.kafka.teardown();
        TRACKER = null;
        super.teardown();
    }

    @Test
    public void givenCommand_whenRunning_thenADistributionTrackerIsAvailable() {
        // Given
        //@formatter:off
        String[] args = List.of("--dist-lifecycle-bootstrap-servers",
                                this.kafka.getBootstrapServers(),
                                "--dist-lifecycle-topic",
                                KafkaTestCluster.DEFAULT_TOPIC,
                                "--no-singleton"
                                // Uncomment the following if you need to debug and want the test command to wait
                                // longer than normal
                                //, "--delay", "60"
        ).toArray(new String[0]);
        //@formatter:on
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // When
            Future<?> future =
                    executor.submit(() -> SmartCacheCommand.runAsSingleCommand(DistLifecycleTracker.class, args));

            // Then
            Awaitility.await("Wait for command parsing to complete")
                      .pollInterval(Duration.ofMillis(10))
                      .atMost(Duration.ofSeconds(1))
                      .until(() -> SmartCacheCommandTester.getLastParseResult() != null);
            Assert.assertNotNull(SmartCacheCommandTester.getLastParseResult());
            Assert.assertTrue(SmartCacheCommandTester.getLastParseResult().wasSuccessful());
            Awaitility.await("Distribution Lifecycle Tracker to be available and running")
                      .pollInterval(Duration.ofMillis(100))
                      .atMost(Duration.ofSeconds(30))
                      .until(() -> TRACKER != null && TRACKER.isRunning());
            Assert.assertNull(DistributionLifecycleTrackerRegistry.getInstance());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void givenCommand_whenRunningWithSingleton_thenADistributionTrackerIsAvailable() {
        // Given
        //@formatter:off
        String[] args = List.of("--dist-lifecycle-bootstrap-servers",
                                this.kafka.getBootstrapServers(),
                                "--dist-lifecycle-topic",
                                KafkaTestCluster.DEFAULT_TOPIC
                                // Uncomment the following if you need to debug and want the test command to wait
                                // longer than normal
                                //, "--delay", "60"
        ).toArray(new String[0]);
        //@formatter:on
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // When
            Future<?> future =
                    executor.submit(() -> SmartCacheCommand.runAsSingleCommand(DistLifecycleTracker.class, args));

            // Then
            Awaitility.await("Wait for command parsing to complete")
                      .pollInterval(Duration.ofMillis(10))
                      .atMost(Duration.ofSeconds(1))
                      .until(() -> SmartCacheCommandTester.getLastParseResult() != null);
            Assert.assertNotNull(SmartCacheCommandTester.getLastParseResult());
            Assert.assertTrue(SmartCacheCommandTester.getLastParseResult().wasSuccessful());
            Awaitility.await("Distribution Lifecycle Tracker to be available and running")
                      .pollInterval(Duration.ofMillis(100))
                      .atMost(Duration.ofSeconds(30))
                      .until(() -> DistributionLifecycleTrackerRegistry.getInstance() != null && DistributionLifecycleTrackerRegistry.getInstance()
                                                                                                                                     .isRunning());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void givenCommandAndLifecycleEventsOnKafka_whenRunning_thenStateStoreIsUpdated_andLifecycleEventsAreLogged() {
        // Given
        UUID regEvent = UUID.randomUUID(), activateEvent = UUID.randomUUID();
        try (KafkaSink<UUID, LazyEnvelope> sink = KafkaSink.<UUID, LazyEnvelope>create()
                                                           .bootstrapServers(this.kafka.getBootstrapServers())
                                                           .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                           .producerConfig(this.kafka.getClientProperties())
                                                           .keySerializer(UUIDSerializer.class)
                                                           .valueSerializer(LazyEnvelopeSerializer.class)
                                                           .noAsync()
                                                           .noLinger()
                                                           .build()) {
            sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                                 Util.action(regEvent, "test", DistributionLifecycleState.Unregistered,
                                             DistributionLifecycleState.Registered)));
            sink.send(Util.event(LifecycleAction.DOCUMENT_FORMAT,
                                 Util.action(activateEvent, "test", DistributionLifecycleState.Registered,
                                             DistributionLifecycleState.Active)));
        }
        //@formatter:off
        String[] args = List.of("--dist-lifecycle-bootstrap-servers",
                                this.kafka.getBootstrapServers(),
                                "--dist-lifecycle-topic",
                                KafkaTestCluster.DEFAULT_TOPIC,
                                "--no-singleton"
                // Uncomment the following if you need to debug and want the test command to wait
                // longer than normal
                //, "--delay", "60"
        ).toArray(new String[0]);
        //@formatter:on
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // When
            executor.submit(() -> SmartCacheCommand.runAsSingleCommand(DistLifecycleTracker.class, args));

            // Then
            Awaitility.await("Distribution Lifecycle Tracker to be available and running")
                      .pollInterval(Duration.ofMillis(100))
                      .atMost(Duration.ofSeconds(30))
                      .until(() -> TRACKER != null && TRACKER.isRunning());
            DistributionLifecycleStateStore stateStore = TRACKER.getStateStore();
            Assert.assertNotNull(stateStore);
            Util.verifyDistributionState("test", stateStore, DistributionLifecycleState.Active);
            Util.verifyApplicationState(stateStore, regEvent, APP_ID, ApplicationState.Completed);
            Util.verifyApplicationState(stateStore, activateEvent, APP_ID, ApplicationState.Completed);

            // And
            String logs = SmartCacheCommandTester.getLastStdErr();
            List<String> lines = Arrays.stream(StringUtils.split(logs, '\n')).toList();
            hasLine(lines, "transitioned from Unregistered to Registered");
            hasLine(lines, "transitioned from Registered to Active");
        } finally {
            executor.shutdownNow();
        }
    }
}
