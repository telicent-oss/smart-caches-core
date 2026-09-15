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
package io.telicent.smart.cache.security.data.plugins.rdf.abac;

import io.telicent.jena.abac.labels.store.rocksdb.legacy.LegacyLabelsStoreRocksDB;
import io.telicent.smart.cache.storage.BackupConfig;
import io.telicent.smart.cache.storage.BackupException;
import io.telicent.smart.cache.storage.BackupRestoreCapable;
import io.telicent.smart.cache.storage.BackupStatus;
import io.telicent.smart.cache.storage.CompactCapable;
import io.telicent.smart.cache.storage.CompactException;
import io.telicent.smart.cache.storage.CompactStatus;
import io.telicent.smart.cache.storage.RestoreConfig;
import io.telicent.smart.cache.storage.RestoreException;
import io.telicent.smart.cache.storage.RestoreStatus;

import java.time.Instant;
import java.util.Objects;

/**
 * Presents the deprecated legacy RocksDB labels store through the generic storage capability interfaces.
 * <p>
 * The maintenance API on {@link io.telicent.smart.cache.security.data.plugins.DataSecurityPlugin} is expressed purely
 * in terms of {@link BackupRestoreCapable} and {@link CompactCapable}, which the modern
 * {@code DictionaryLabelStoreRocksDB} gets for free by extending {@code RocksDbLabelsStore}. The legacy store predates
 * those interfaces and exposes its own {@code backup(String)}, {@code restore(String)} and {@code compact()} instead,
 * so without this adapter every dataset backed by it would report no maintenance capability at all.
 * <p>
 * That is not a corner case on rdf-abac 3.1.6: {@code Labels} still defaults legacy mode to {@code true} unless a
 * dataset explicitly sets {@code authz:labelsStoreLegacy false}, so the legacy store is what most deployments have.
 * <p>
 * This class exists solely to bridge that gap and should be deleted once rdf-abac 3.1.7 lands, which removes the
 * legacy store from production code.
 */
@SuppressWarnings("deprecation")
final class LegacyLabelsStoreCapability implements BackupRestoreCapable, CompactCapable {

    private final LegacyLabelsStoreRocksDB store;

    /**
     * Creates a new adapter over a legacy labels store
     *
     * @param store Legacy labels store, which remains owned by its dataset and is neither closed nor otherwise
     *              modified by this adapter
     */
    LegacyLabelsStoreCapability(LegacyLabelsStoreRocksDB store) {
        this.store = Objects.requireNonNull(store, "Legacy labels store cannot be null");
    }

    @Override
    public BackupStatus backup(BackupConfig config) {
        final String location = config != null ? config.getBackupLocation() : null;
        if (location == null || location.isBlank()) {
            throw new BackupException("Backup directory must be specified for legacy labels store backups");
        }
        final Instant startTime = Instant.now();
        try {
            this.store.backup(location);
        } catch (Exception e) {
            throw new BackupException("Failed to back up legacy labels store: " + e.getMessage(), e);
        }
        // The legacy API returns void, so there is no backup id and no byte count to report. The location is the only
        // handle the caller has on what was written, and is what the legacy store's own restore() takes back.
        return BackupStatus.success(location, 0L, startTime, Instant.now());
    }

    @Override
    public RestoreStatus restore(RestoreConfig config) {
        final String location = config != null ? config.getBackupLocation() : null;
        if (location == null || location.isBlank()) {
            throw new RestoreException("Backup directory must be specified for legacy labels store restores");
        }
        try {
            this.store.restore(location);
        } catch (Exception e) {
            throw new RestoreException("Failed to restore legacy labels store: " + e.getMessage(), e);
        }
        return RestoreStatus.success(location, 0L);
    }

    @Override
    public CompactStatus compact() {
        final Instant startTime = Instant.now();
        try {
            this.store.compact();
        } catch (Exception e) {
            throw new CompactException("Failed to compact legacy labels store: " + e.getMessage(), e);
        }
        // The legacy store reports neither the size before nor after, so no space reclamation can be claimed
        return new CompactStatus(0L, 0L, startTime, Instant.now());
    }
}
