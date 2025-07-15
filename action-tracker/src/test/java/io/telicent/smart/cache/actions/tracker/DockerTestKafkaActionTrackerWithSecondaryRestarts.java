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
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DockerTestKafkaActionTrackerWithSecondaryRestarts extends AbstractKafkaActionTrackerTests {

    @Test(dataProvider = "splitTransitions")
    public void givenKafkaBackupTrackers_whenTransitioningStatesAndSecondaryRestarts_thenStateTrackersAreSynced(
            List<Consumer<ActionTracker>> preTransitions, List<Consumer<ActionTracker>> postTransitions,
            ActionState expectedInterimState, String expectedInterimAction, ActionState expectedFinalState, String expectedFinalAction) {
        // Given
        String groupId = "test-action-tracker" + GROUP_ID.incrementAndGet();
        try (KafkaSink<UUID, ActionTransition> sink = createSink()) {
            try (ActionTracker primary = PrimaryActionTracker.builder().application("test").sink(sink).build()) {
                // When
                verifySecondaryTracker("test", createSource(groupId), preTransitions, primary, expectedInterimState, expectedInterimAction);

                // Then
                // We close and restart the secondary applying further transitions
                // NB - We reuse the same groupId so should resume consumption from previously committed offsets, if any
                verifySecondaryTracker("test", createSource(groupId), postTransitions, primary, expectedFinalState, expectedFinalAction);
            }
        }
    }

}
