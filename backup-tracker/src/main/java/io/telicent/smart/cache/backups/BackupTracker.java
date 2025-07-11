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
package io.telicent.smart.cache.backups;

import io.telicent.smart.cache.backups.kafka.KafkaPrimaryBackupTracker;
import io.telicent.smart.cache.backups.kafka.KafkaSecondaryBackupTracker;

/**
 * A Backup Tracker allows applications composed of multiple microservices to track and respond to backup/restore
 * operations in appropriate ways
 * <p>
 * As an example consider an application composed of two microservices - a projector that loads data into the
 * application, and an API server that provides access to that data.  The API server would provide the ability for a
 * user to trigger a backup/restore operation and the projector may wish to respond to that e.g. after a restore
 * operation it likely wants to re-seek to the correct Kafka offsets for the restored application state.
 * </p>
 * <p>
 * By using a {@link KafkaPrimaryBackupTracker} and a
 * {@link KafkaSecondaryBackupTracker} in the API server and Projector
 * respectively the projector can be informed of backup/restore operations happening and react accordingly.
 * </p>
 */
public interface BackupTracker extends AutoCloseable {

    /**
     * Gets the current state
     *
     * @return Current state
     */
    BackupTrackerState getState();

    /**
     * Informs the tracker that application startup has completed
     */
    void startupComplete();

    /**
     * Informs the tracker that the application is starting a backup operation
     */
    void startBackup();

    /**
     * Informs the tracker that the application finished a backup operation
     */
    void finishBackup();

    /**
     * Informs the tracker that the application started a restore operation
     */
    void startRestore();

    /**
     * Informs the tracker that the application finished a restore operation
     */
    void finishRestore();

    /**
     * Closes the backup tracker
     */
    void close();
}
