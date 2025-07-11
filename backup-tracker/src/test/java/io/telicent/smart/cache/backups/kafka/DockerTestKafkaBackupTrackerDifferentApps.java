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
package io.telicent.smart.cache.backups.kafka;

import io.telicent.smart.cache.backups.BackupTracker;
import io.telicent.smart.cache.backups.BackupTrackerState;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DockerTestKafkaBackupTrackerDifferentApps extends AbstractKafkaBackupTrackerTests {

    @Test(dataProvider = "transitions")
    public void givenKafkaBackupTrackersForDifferentApps_whenTransitioningStates_thenNotSynced(
            List<Consumer<BackupTracker>> transitions, BackupTrackerState expectedFinalState) {
        // Given
        try (KafkaSink<UUID, BackupTransition> sink = createSink()) {
            KafkaEventSource<UUID, BackupTransition> source = createSource(
                    "test-backup-tracker" + GROUP_ID.incrementAndGet());

            // When
            try (BackupTracker primary = KafkaPrimaryBackupTracker.builder().application("app-a").sink(sink).build()) {
                // Then
                verifySecondaryTracker("app-b", source, transitions, primary, BackupTrackerState.STARTING);
                Assert.assertEquals(primary.getState(), expectedFinalState);
            }
        }
    }

}
