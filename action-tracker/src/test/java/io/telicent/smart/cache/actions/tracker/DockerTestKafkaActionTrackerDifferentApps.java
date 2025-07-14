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
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DockerTestKafkaActionTrackerDifferentApps extends AbstractKafkaActionTrackerTests {

    @Test(dataProvider = "transitions")
    public void givenKafkaBackupTrackersForDifferentApps_whenTransitioningStates_thenNotSynced(
            List<Consumer<ActionTracker>> transitions, ActionState expectedFinalState, String expectedFinalAction) {
        // Given
        try (KafkaSink<UUID, ActionTransition> sink = createSink()) {
            KafkaEventSource<UUID, ActionTransition> source = createSource(
                    "test-action-tracker" + GROUP_ID.incrementAndGet());

            // When
            try (ActionTracker primary = PrimaryActionTracker.builder().application("app-a").sink(sink).build()) {
                // Then
                verifySecondaryTracker("app-b", source, transitions, primary, ActionState.STARTING, null);
                Assert.assertEquals(primary.getState(), expectedFinalState);
            }
        }
    }

}
