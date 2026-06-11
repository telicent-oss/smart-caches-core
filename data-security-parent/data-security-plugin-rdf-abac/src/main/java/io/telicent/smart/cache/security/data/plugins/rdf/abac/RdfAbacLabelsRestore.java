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
import io.telicent.smart.cache.security.data.labels.SecurityLabelsRestore;
import io.telicent.smart.cache.storage.BackupRestoreCapable;
import io.telicent.smart.cache.storage.RestoreConfig;
import io.telicent.smart.cache.storage.RestoreStatus;
import org.apache.jena.sparql.core.DatasetGraph;

import java.io.File;

public class RdfAbacLabelsRestore implements SecurityLabelsRestore {

    public void restore(DatasetGraph dsg, String restorePath, ObjectNode node) throws DataSecurityException {
        if (dsg instanceof DatasetGraphABAC abac) {
            try(final LabelsStore labelsStore = abac.labelsStore()) {
                if (labelsStore instanceof LegacyLabelsStoreRocksDB rocksDB) {
                    if (!checkPathExistsAndIsDir(restorePath)) {
                        node.put("reason", "Restore directory not found: " + restorePath);
                        node.put("success", false);
                    } else {
                        try {
                            executeRestoreLabelStore(rocksDB, restorePath, node);
                        } catch (RuntimeException e) {
                            node.put("reason", e.getMessage());
                            node.put("success", false);
                        }
                    }
                } else if (labelsStore instanceof BackupRestoreCapable restoreCapable) {
                    try {
                        executeRestore(restoreCapable, restorePath, node);
                    } catch (RuntimeException e) {
                        node.put("reason", e.getMessage());
                        node.put("success", false);
                    }
                } else {
                    node.put("reason", "No Label Store to restore (not RocksDB)");
                    node.put("success", false);
                }
            } catch (Exception e){
                throw new DataSecurityException(e.getMessage(),e);
            }
        } else {
            node.put("reason", "No Label Store to restore (not ABAC)");
            node.put("success", false);
        }
    }

    /**
     * Takes a string path, checks it exists and is a directory
     *
     * @param pathString the path to check
     * @return whether the operation was successful or not
     */
    public static boolean checkPathExistsAndIsDir(String pathString) {
        if (requestIsEmpty(pathString)) {
            return false;
        }
        final File path = new File(pathString);
        return path.exists() && path.isDirectory();
    }

    /**
     * Checks to see if the requested parameter is empty or just a '/'
     * which we treat as equivalent
     *
     * @param requestName the requested dataset (if provided)
     * @return true if empty, false if set
     */
    public static boolean requestIsEmpty(String requestName) {
        if (requestName == null) {
            return true;
        } else if (requestName.trim().isEmpty()) {
            return true;
        } else {
            return requestName.trim().equals("/");
        }
    }

    /**
     * Calls a RocksDB to restore itself
     *
     * @param rocksDB          the rocks db label store
     * @param labelRestorePath the location of the recovery files
     * @param node             the results of the operation
     */
    void executeRestoreLabelStore(LegacyLabelsStoreRocksDB rocksDB, String labelRestorePath, ObjectNode node) {
        rocksDB.restore(labelRestorePath);
        node.put("success", true);
    }

    void executeRestore(BackupRestoreCapable restoreCapable, String labelRestorePath, ObjectNode node) {
        final RestoreStatus status = restoreCapable.restore(RestoreConfig.builder().backupLocation(labelRestorePath).build());
        node.put("success", status.isSuccess());
        if (status.getErrorMessage().isPresent()) {
            node.put("reason", status.getErrorMessage().get());
        }
    }
}
