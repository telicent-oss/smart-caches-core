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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.telicent.jena.abac.ABAC;
import io.telicent.jena.abac.SysABAC;
import io.telicent.jena.abac.attributes.syntax.AEX;
import io.telicent.jena.abac.core.AttributesStoreLocal;
import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.labels.Labels;
import io.telicent.jena.abac.labels.LabelsStore;
import io.telicent.jena.abac.labels.store.rocksdb.legacy.LegacyLabelsStoreRocksDB;
import io.telicent.smart.cache.storage.BackupRestoreCapable;
import io.telicent.smart.cache.storage.BackupStatus;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestRdfAbacLabelsBackup {

    private static final String BACKUP_PATH = "/tmp/test-backup";
    private RdfAbacLabelsBackup backup;
    private ObjectNode node;

    @BeforeMethod
    public void setUp() {
        backup = new RdfAbacLabelsBackup();
        node = new ObjectMapper().createObjectNode();
    }

    @Test
    public void givenNonAbacDataset_whenBackingUp_thenSuccessFalse() {
        final DatasetGraph plainDsg = DatasetGraphFactory.createTxnMem();
        backup.backup(plainDsg, BACKUP_PATH, node);

        Assert.assertFalse(node.get(RdfAbacLabelsBackup.SUCCESS).asBoolean());
        Assert.assertTrue(node.get(RdfAbacLabelsBackup.REASON).asText().contains("not ABAC"));
    }

    @Test
    public void givenAbacDatasetWithPlainLabelsStore_whenBackingUp_thenSuccessFalse() {
        final DatasetGraphABAC abac =
                ABAC.authzDataset(DatasetGraphFactory.createTxnMem(), AEX.strALLOW, Labels.createLabelsStoreMem(),
                                  SysABAC.denyLabel, new AttributesStoreLocal());

        backup.backup(abac, BACKUP_PATH, node);

        Assert.assertFalse(node.get(RdfAbacLabelsBackup.SUCCESS).asBoolean());
        Assert.assertTrue(node.get(RdfAbacLabelsBackup.REASON).asText().contains("not RocksDB"));
    }

    @Test
    public void givenAbacDatasetWithBackupCapableStore_whenBackingUp_thenSuccessTrue() {
        final LabelsStore backupableStore =
                mock(LabelsStore.class, withSettings().extraInterfaces(BackupRestoreCapable.class));
        final BackupStatus successStatus = BackupStatus.builder().success(true).build();
        when(((BackupRestoreCapable) backupableStore).backup(any())).thenReturn(successStatus);

        final DatasetGraphABAC abac =
                ABAC.authzDataset(DatasetGraphFactory.createTxnMem(), AEX.strALLOW, backupableStore, SysABAC.denyLabel,
                                  new AttributesStoreLocal());

        backup.backup(abac, BACKUP_PATH, node);

        Assert.assertTrue(node.get(RdfAbacLabelsBackup.SUCCESS).asBoolean());
    }

    @Test
    public void givenAbacDatasetWithBackupCapableStoreThatFails_whenBackingUp_thenSuccessFalse() {
        final LabelsStore backupableStore =
                mock(LabelsStore.class, withSettings().extraInterfaces(BackupRestoreCapable.class));
        final BackupStatus failStatus = BackupStatus.builder().success(false).errorMessage("disk full").build();
        when(((BackupRestoreCapable) backupableStore).backup(any())).thenReturn(failStatus);

        final DatasetGraphABAC abac =
                ABAC.authzDataset(DatasetGraphFactory.createTxnMem(), AEX.strALLOW, backupableStore, SysABAC.denyLabel,
                                  new AttributesStoreLocal());

        backup.backup(abac, BACKUP_PATH, node);

        Assert.assertFalse(node.get(RdfAbacLabelsBackup.SUCCESS).asBoolean());
        Assert.assertEquals(node.get(RdfAbacLabelsBackup.REASON).asText(), "disk full");
    }

    @Test
    public void givenAbacDatasetWithBackupCapableStoreThatThrows_whenBackingUp_thenSuccessFalse() {
        final LabelsStore backupableStore =
                mock(LabelsStore.class, withSettings().extraInterfaces(BackupRestoreCapable.class));
        when(((BackupRestoreCapable) backupableStore).backup(any())).thenThrow(
                new RuntimeException("unexpected error"));

        final DatasetGraphABAC abac =
                ABAC.authzDataset(DatasetGraphFactory.createTxnMem(), AEX.strALLOW, backupableStore, SysABAC.denyLabel,
                                  new AttributesStoreLocal());

        backup.backup(abac, BACKUP_PATH, node);

        Assert.assertFalse(node.get(RdfAbacLabelsBackup.SUCCESS).asBoolean());
        Assert.assertEquals(node.get(RdfAbacLabelsBackup.REASON).asText(), "unexpected error");
    }

    @Test
    public void givenBackupCapableStore_whenExecuteBackup_thenNodePopulated() {
        final BackupRestoreCapable backupCapable = mock(BackupRestoreCapable.class);
        final BackupStatus successStatus = BackupStatus.builder().success(true).build();
        when(backupCapable.backup(any())).thenReturn(successStatus);

        backup.executeBackup(backupCapable, BACKUP_PATH, node);

        Assert.assertTrue(node.get(RdfAbacLabelsBackup.SUCCESS).asBoolean());
    }

    @Test
    public void givenAbacDatasetWithBackupCapableStore_whenExecuteBackup_thenLiveStoreIsNotClosed() throws Exception {
        // The labels store is owned by the DatasetGraphABAC — backup must leave it open,
        // otherwise every subsequent request fails with "Storage already closed" until restart
        final LabelsStore backupableStore =
                mock(LabelsStore.class, withSettings().extraInterfaces(BackupRestoreCapable.class));
        final DatasetGraphABAC abac =
                ABAC.authzDataset(DatasetGraphFactory.createTxnMem(), AEX.strALLOW, backupableStore, SysABAC.denyLabel,
                                  new AttributesStoreLocal());

        backup.backup(abac, BACKUP_PATH, node);
        verify(backupableStore, never()).close();
    }

    @Test
    public void givenBackupCapableStoreWithError_whenExecuteBackup_thenReasonPopulated() {
        final BackupRestoreCapable backupCapable = mock(BackupRestoreCapable.class);
        final BackupStatus failStatus = BackupStatus.builder().success(false).errorMessage("io error").build();
        when(backupCapable.backup(any())).thenReturn(failStatus);

        backup.executeBackup(backupCapable, BACKUP_PATH, node);

        Assert.assertFalse(node.get(RdfAbacLabelsBackup.SUCCESS).asBoolean());
        Assert.assertEquals(node.get(RdfAbacLabelsBackup.REASON).asText(), "io error");
    }

    @Test
    @SuppressWarnings("deprecation")
    public void givenAbacDatasetWithLegacyStore_whenBackingUp_thenSuccessTrue() {
        final LegacyLabelsStoreRocksDB legacyStore = mock(LegacyLabelsStoreRocksDB.class);

        final DatasetGraphABAC abac =
                ABAC.authzDataset(DatasetGraphFactory.createTxnMem(), AEX.strALLOW, legacyStore, SysABAC.denyLabel,
                                  new AttributesStoreLocal());

        backup.backup(abac, BACKUP_PATH, node);

        Assert.assertTrue(node.get(RdfAbacLabelsBackup.SUCCESS).asBoolean());
    }

}
