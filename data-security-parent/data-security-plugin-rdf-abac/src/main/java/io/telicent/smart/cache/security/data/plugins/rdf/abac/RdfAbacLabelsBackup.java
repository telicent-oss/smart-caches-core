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
import io.telicent.smart.cache.security.data.DataSecurityException;
import io.telicent.smart.cache.security.data.labels.SecurityLabelsBackup;
import io.telicent.smart.cache.storage.BackupConfig;
import io.telicent.smart.cache.storage.BackupRestoreCapable;
import io.telicent.smart.cache.storage.BackupStatus;
import org.apache.jena.sparql.core.DatasetGraph;

public class RdfAbacLabelsBackup implements SecurityLabelsBackup {

    @Override
    public void backup(DatasetGraph dsg, String backupPath, ObjectNode node) throws DataSecurityException {
        if (dsg instanceof DatasetGraphABAC abac) {
            try(LabelsStore labelsStore = abac.labelsStore()) {
                if (labelsStore instanceof LegacyLabelsStoreRocksDB rocksDB) {
                    try {
                        executeBackupLabelStore(rocksDB, backupPath, node);
                    } catch (RuntimeException e) {
                        node.put("reason", e.getMessage());
                        node.put("success", false);
                    }
                } else if (labelsStore instanceof BackupRestoreCapable backupCapable) {
                    try {
                        executeBackup(backupCapable, backupPath, node);
                    } catch (RuntimeException e) {
                        node.put("reason", e.getMessage());
                        node.put("success", false);
                    }
                } else {
                    node.put("reason", "No Label Store to back up (not RocksDB)");
                    node.put("success", false);
                }
            } catch (Exception e){
                throw new DataSecurityException(e.getMessage(),e);
            }
        } else {
            node.put("reason", "No Label Store to back up (not ABAC)");
            node.put("success", false);
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
        node.put("success", true);
    }

    void executeBackup(BackupRestoreCapable backupCapable, String backupPath, ObjectNode node) {
        BackupStatus status = backupCapable.backup(BackupConfig.builder().backupLocation(backupPath).build());
        node.put("success", status.isSuccess());
        if (status.getErrorMessage().isPresent()) {
            node.put("reason", status.getErrorMessage().get());
        }
    }
}
