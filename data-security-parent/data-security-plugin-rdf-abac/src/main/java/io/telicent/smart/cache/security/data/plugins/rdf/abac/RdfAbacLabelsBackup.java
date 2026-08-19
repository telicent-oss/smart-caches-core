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

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.labels.LabelsStore;
import io.telicent.jena.abac.labels.store.rocksdb.legacy.LegacyLabelsStoreRocksDB;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsBackup;
import io.telicent.smart.cache.storage.BackupConfig;
import io.telicent.smart.cache.storage.BackupRestoreCapable;
import io.telicent.smart.cache.storage.BackupStatus;
import org.apache.jena.sparql.core.DatasetGraph;

public class RdfAbacLabelsBackup implements SecurityLabelsBackup {

    public static final String REASON = "reason";
    public static final String SUCCESS = "success";

    @Override
    @SuppressWarnings("deprecation")
    public void backup(DatasetGraph datasetGraph, String backupPath, ObjectNode node) {
        if (datasetGraph instanceof DatasetGraphABAC datasetGraphABAC) {
            // The labels store is owned by the DatasetGraphABAC and must stay open after backup
            final LabelsStore labelsStore = datasetGraphABAC.labelsStore();
            try {
                if (labelsStore instanceof LegacyLabelsStoreRocksDB rocksDB) {
                    executeBackupLabelStore(rocksDB, backupPath, node);
                } else if (labelsStore instanceof BackupRestoreCapable backupCapable) {
                    executeBackup(backupCapable, backupPath, node);
                } else {
                    node.put(REASON, "No Label Store to back up (not RocksDB)");
                    node.put(SUCCESS, false);
                }
            } catch (Exception e) {
                node.put(REASON, e.getMessage());
                node.put(SUCCESS, false);
            }
        } else {
            node.put(REASON, "No Label Store to back up (not ABAC)");
            node.put(SUCCESS, false);
        }
    }

    /**
     * Call Rocks DB to back up itself.
     *
     * @param rocksDB         instance to call
     * @param labelBackupPath path to use
     * @param node            to collect the results
     */
    void executeBackupLabelStore(LegacyLabelsStoreRocksDB rocksDB, String labelBackupPath, ObjectNode node) {
        rocksDB.backup(labelBackupPath);
        node.put(SUCCESS, true);
    }

    void executeBackup(BackupRestoreCapable backupCapable, String backupPath, ObjectNode node) {
        final BackupStatus status = backupCapable.backup(BackupConfig.builder().backupLocation(backupPath).build());
        node.put(SUCCESS, status.isSuccess());
        if (status.getErrorMessage().isPresent()) {
            node.put(REASON, status.getErrorMessage().get());
        }
    }
}
