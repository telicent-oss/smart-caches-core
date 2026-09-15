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

import io.telicent.jena.abac.core.DatasetGraphABAC;
import io.telicent.jena.abac.labels.store.rocksdb.legacy.LegacyLabelsStoreRocksDB;
import io.telicent.smart.cache.storage.BackupConfig;
import io.telicent.smart.cache.storage.BackupException;
import io.telicent.smart.cache.storage.BackupStatus;
import io.telicent.smart.cache.storage.CompactException;
import io.telicent.smart.cache.storage.CompactStatus;
import io.telicent.smart.cache.storage.RestoreConfig;
import io.telicent.smart.cache.storage.RestoreException;
import io.telicent.smart.cache.storage.RestoreStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the adapter that lets the deprecated legacy RocksDB labels store satisfy the generic storage maintenance
 * capabilities, which is what keeps labels backup, restore and compaction working on rdf-abac 3.1.6 where that store
 * is still the default.
 */
@SuppressWarnings("deprecation")
public class TestLegacyLabelsStoreCapability {

    private static final String LOCATION = "/tmp/labels-backup";

    private LegacyLabelsStoreRocksDB store;
    private LegacyLabelsStoreCapability capability;

    @BeforeMethod
    public void setup() {
        this.store = mock(LegacyLabelsStoreRocksDB.class);
        this.capability = new LegacyLabelsStoreCapability(this.store);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoStore_whenCreatingCapability_thenRejected() {
        new LegacyLabelsStoreCapability(null);
    }

    // ------------------------------------------------------------------ backup

    @Test
    public void givenLocation_whenBackingUp_thenDelegatedAndSuccessful() {
        // When
        BackupStatus status = this.capability.backup(BackupConfig.builder().backupLocation(LOCATION).build());

        // Then
        verify(this.store).backup(LOCATION);
        verify(this.store, never()).close();
        Assert.assertTrue(status.isSuccess());
        Assert.assertEquals(status.getBackupId(), LOCATION);
        Assert.assertTrue(status.getErrorMessage().isEmpty());
        Assert.assertNotNull(status.getStartTime());
        Assert.assertNotNull(status.getEndTime());
    }

    @Test(expectedExceptions = BackupException.class)
    public void givenNoConfig_whenBackingUp_thenRejected() {
        this.capability.backup(null);
    }

    @Test(expectedExceptions = BackupException.class)
    public void givenBlankLocation_whenBackingUp_thenRejected() {
        this.capability.backup(BackupConfig.builder().backupLocation("   ").build());
    }

    @Test(expectedExceptions = BackupException.class)
    public void givenNoLocation_whenBackingUp_thenRejected() {
        this.capability.backup(BackupConfig.builder().build());
    }

    @Test
    public void givenFailingStore_whenBackingUp_thenWrapped() {
        // Given
        doThrow(new IllegalStateException("rocks says no")).when(this.store).backup(anyString());

        // When and Then
        BackupException e = Assert.expectThrows(BackupException.class, () -> this.capability.backup(
                BackupConfig.builder().backupLocation(LOCATION).build()));
        Assert.assertTrue(e.getMessage().contains("rocks says no"));
        Assert.assertNotNull(e.getCause());
    }

    // ------------------------------------------------------------------ restore

    @Test
    public void givenLocation_whenRestoring_thenDelegatedAndSuccessful() {
        // When
        RestoreStatus status = this.capability.restore(RestoreConfig.builder().backupLocation(LOCATION).build());

        // Then
        verify(this.store).restore(LOCATION);
        verify(this.store, never()).close();
        Assert.assertTrue(status.isSuccess());
        Assert.assertEquals(status.getBackupId(), LOCATION);
        Assert.assertTrue(status.getErrorMessage().isEmpty());
    }

    @Test(expectedExceptions = RestoreException.class)
    public void givenNoConfig_whenRestoring_thenRejected() {
        this.capability.restore(null);
    }

    @Test(expectedExceptions = RestoreException.class)
    public void givenBlankLocation_whenRestoring_thenRejected() {
        this.capability.restore(RestoreConfig.builder().backupLocation(" ").build());
    }

    @Test(expectedExceptions = RestoreException.class)
    public void givenNoLocation_whenRestoring_thenRejected() {
        this.capability.restore(RestoreConfig.builder().build());
    }

    @Test
    public void givenFailingStore_whenRestoring_thenWrapped() {
        // Given
        doThrow(new IllegalStateException("no such backup")).when(this.store).restore(anyString());

        // When and Then
        RestoreException e = Assert.expectThrows(RestoreException.class, () -> this.capability.restore(
                RestoreConfig.builder().backupLocation(LOCATION).build()));
        Assert.assertTrue(e.getMessage().contains("no such backup"));
        Assert.assertNotNull(e.getCause());
    }

    // ------------------------------------------------------------------ compact

    @Test
    public void givenStore_whenCompacting_thenDelegated() {
        // When
        CompactStatus status = this.capability.compact();

        // Then
        verify(this.store).compact();
        verify(this.store, never()).close();
        Assert.assertNotNull(status.getStartTime());
        Assert.assertNotNull(status.getEndTime());
        // The legacy store reports no sizes, so nothing can be claimed as reclaimed
        Assert.assertEquals(status.getReclaimedBytes(), 0L);
    }

    @Test
    public void givenFailingStore_whenCompacting_thenWrapped() {
        // Given
        doThrow(new IllegalStateException("compaction failed")).when(this.store).compact();

        // When and Then
        CompactException e = Assert.expectThrows(CompactException.class, () -> this.capability.compact());
        Assert.assertTrue(e.getMessage().contains("compaction failed"));
        Assert.assertNotNull(e.getCause());
    }

    // ------------------------------------------------------------------ plugin wiring

    @Test
    public void givenLegacyLabelsStore_whenPreparingMaintenance_thenAdapterReturned() {
        // Given
        DatasetGraphABAC dataset = mock(DatasetGraphABAC.class);
        when(dataset.labelsStore()).thenReturn(this.store);
        RdfAbacPlugin plugin = new RdfAbacPlugin();

        // When and Then
        Assert.assertTrue(plugin.prepareLabelsBackup(dataset).orElseThrow() instanceof LegacyLabelsStoreCapability);
        Assert.assertTrue(plugin.prepareLabelsRestore(dataset).orElseThrow() instanceof LegacyLabelsStoreCapability);
        Assert.assertTrue(plugin.prepareLabelsCompact(dataset).orElseThrow() instanceof LegacyLabelsStoreCapability);
        verify(this.store, never()).close();
    }
}
