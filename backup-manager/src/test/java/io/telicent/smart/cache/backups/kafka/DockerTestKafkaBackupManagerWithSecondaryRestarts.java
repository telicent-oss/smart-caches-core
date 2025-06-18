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

import io.telicent.smart.cache.backups.BackupManager;
import io.telicent.smart.cache.backups.BackupManagerState;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DockerTestKafkaBackupManagerWithSecondaryRestarts extends AbstractKafkaBackupManagerTests {

    @Test(dataProvider = "splitTransitions")
    public void givenKafkaBackupManagers_whenTransitioningStatesAndSecondaryRestarts_thenStateManagersAreSynced(
            List<Consumer<BackupManager>> preTransitions, List<Consumer<BackupManager>> postTransitions,
            BackupManagerState expectedInterimState, BackupManagerState expectedFinalState) {
        // Given
        String groupId = "test-backup-manager" + GROUP_ID.incrementAndGet();
        try (KafkaSink<UUID, BackupTransition> sink = createSink()) {
            try (BackupManager primary = KafkaPrimaryBackupManager.builder().application("test").sink(sink).build()) {
                // When
                verifySecondaryManager("test", createSource(groupId), preTransitions, primary, expectedInterimState);

                // Then
                // We close and restart the secondary applying further transitions
                // NB - We reuse the same groupId so should resume consumption from previously committed offsets, if any
                verifySecondaryManager("test", createSource(groupId), postTransitions, primary, expectedFinalState);
            }
        }
    }

}
