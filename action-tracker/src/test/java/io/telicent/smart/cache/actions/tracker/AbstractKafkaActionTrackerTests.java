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
package io.telicent.smart.cache.actions.tracker;

import io.telicent.smart.cache.actions.tracker.model.ActionState;
import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
import io.telicent.smart.cache.actions.tracker.model.ActionTransitionDeserializer;
import io.telicent.smart.cache.actions.tracker.model.ActionTransitionSerializer;
import io.telicent.smart.cache.sources.kafka.BasicKafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AbstractKafkaActionTrackerTests {
    protected static final AtomicInteger GROUP_ID = new AtomicInteger(0);

    /**
     * The Kafka test cluster to use, protected so derived tests can change to a different implementation if desired
     */
    protected KafkaTestCluster kafka = new BasicKafkaTestCluster();

    private static void verifySecondaryState(ActionTracker tracker, ActionState expected, String expectedAction) {
        Awaitility.await()
                  .alias("Secondary State Sync")
                  .atMost(Duration.ofSeconds(10))
                  .pollDelay(Duration.ofSeconds(3))
                  .pollInterval(Duration.ofSeconds(1))
                  .until(() -> tracker.getState() == expected && Objects.equals(tracker.getAction(), expectedAction));
    }

    @BeforeClass
    public void setup() {
        this.kafka.setup();
    }

    @AfterMethod
    public void cleanup() {
        this.kafka.resetTestTopic();
    }

    @AfterClass
    public void teardown() {
        this.kafka.teardown();
    }

    private Consumer<ActionTracker> make(Consumer<ActionTracker> consumer) {
        return consumer;
    }

    @DataProvider(name = "transitions")
    protected Object[][] transitions() {
        //@formatter:off
        return new Object[][] {
            {
                Collections.<Consumer<ActionTracker>>emptyList(), ActionState.STARTING, null
            },
            {
                List.of(make(ActionTracker::startupComplete)), ActionState.READY, null
            },
            {
                List.of(make(ActionTracker::startupComplete),
                        start("backup")),
                ActionState.PROCESSING, "backup"
            },
            {
                List.of(make(ActionTracker::startupComplete),
                        start("backup"),
                        finish("backup")),
                ActionState.READY, null
            },
            {
                List.of(make(ActionTracker::startupComplete),
                        start("backup"),
                        finish("backup"),
                        start("restore")),
                ActionState.PROCESSING, "restore"
            },
            {
                List.of(start("restore"),
                        finish("restore")),
                ActionState.READY, null
            }
        };
        //@formatter:on
    }

    private @NotNull Consumer<ActionTracker> finish(String backup) {
        return make(t -> t.finish(backup));
    }

    protected KafkaEventSource<UUID, ActionTransition> createSource(String groupId) {
        return KafkaEventSource.<UUID, ActionTransition>create()
                               .bootstrapServers(this.kafka.getBootstrapServers())
                               .consumerConfig(this.kafka.getClientProperties())
                               .topic(KafkaTestCluster.DEFAULT_TOPIC)
                               .consumerGroup(groupId)
                               .readPolicy(KafkaReadPolicies.fromEarliest())
                               .commitOnProcessed()
                               .keyDeserializer(UUIDDeserializer.class)
                               .valueDeserializer(ActionTransitionDeserializer.class)
                               .build();
    }

    @NotNull
    protected KafkaSink<UUID, ActionTransition> createSink() {
        return KafkaSink.<UUID, ActionTransition>create()
                        .bootstrapServers(this.kafka.getBootstrapServers())
                        .producerConfig(this.kafka.getClientProperties())
                        .topic(KafkaTestCluster.DEFAULT_TOPIC)
                        .keySerializer(UUIDSerializer.class)
                        .valueSerializer(ActionTransitionSerializer.class)
                        .noLinger()
                        .noAsync()
                        .build();
    }

    @DataProvider(name = "splitTransitions")
    protected Object[][] splitTransitions() {
        //@formatter:off
        return new Object[][] {
            {
                List.of(make(ActionTracker::startupComplete),
                        start("backup")),
                List.of(finish("backup")),
                ActionState.PROCESSING,
                "backup",
                ActionState.READY,
                null
            },
            {
                List.of(make(ActionTracker::startupComplete),
                        start("backup")),
                Collections.emptyList(),
                ActionState.PROCESSING,
                "backup",
                ActionState.PROCESSING,
                "backup"
            },
            {
                List.of(make(ActionTracker::startupComplete),
                        start("backup")),
                List.of(finish("backup"),
                        start("restore")),
                ActionState.PROCESSING,
                "backup",
                ActionState.PROCESSING,
                "restore"
            },
            {
                List.of(make(ActionTracker::startupComplete),
                        start("backup"),
                        finish("backup")),
                List.of(start("restore"),
                        finish("restore")),
                ActionState.READY,
                null,
                ActionState.READY,
                null
            },
        };
        //@formatter:on
    }

    private @NotNull Consumer<ActionTracker> start(String backup) {
        return make(t -> t.start(backup));
    }

    protected void verifySecondaryTracker(String application, KafkaEventSource<UUID, ActionTransition> source,
                                          List<Consumer<ActionTracker>> transitions, ActionTracker primary,
                                          ActionState expectedState, String expectedAction) {
        // Given
        try (ActionTracker secondary = SecondaryActionTracker.builder()
                                                             .application(application)
                                                             .eventSource(source)
                                                             .build()) {

            // When
            for (Consumer<ActionTracker> transition : transitions) {
                transition.accept(primary);
            }

            // Then
            AbstractKafkaActionTrackerTests.verifySecondaryState(secondary, expectedState, expectedAction);
        }
    }
}
