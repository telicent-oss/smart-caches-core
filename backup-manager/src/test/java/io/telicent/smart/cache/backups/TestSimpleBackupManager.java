package io.telicent.smart.cache.backups;

import lombok.ToString;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSimpleBackupManager {

    @Test
    public void givenSimpleManager_whenCheckingState_thenStartingUp() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // Then
        BackupManagerState state = manager.getState();

        // When
        Assert.assertEquals(state, BackupManagerState.STARTING);
    }

    @Test
    public void givenSimpleManager_whenStartupComplete_thenReady() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When
        manager.startupComplete();

        // Then
        Assert.assertEquals(manager.getState(), BackupManagerState.READY);
    }

    @Test
    public void givenSimpleManager_whenStartupCompleteTwice_thenReady() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When
        manager.startupComplete();
        manager.startupComplete();

        // Then
        Assert.assertEquals(manager.getState(), BackupManagerState.READY);
    }

    @Test
    public void givenSimpleManager_whenStartingBackup_thenBackingUp_andReturnsToReadyWhenFinished() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When
        manager.startBackup();

        // Then
        Assert.assertEquals(manager.getState(), BackupManagerState.BACKING_UP);

        // And
        manager.finishBackup();
        Assert.assertEquals(manager.getState(), BackupManagerState.READY);
    }

    @Test
    public void givenSimpleManager_whenStartingRestore_thenRestoring_andReturnsToReadyWhenFinished() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When
        manager.startRestore();

        // Then
        Assert.assertEquals(manager.getState(), BackupManagerState.RESTORING);

        // And
        manager.finishRestore();
        Assert.assertEquals(manager.getState(), BackupManagerState.READY);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*transition.*not currently permitted")
    public void givenSimpleManager_whenBackingUp_thenStartingRestoreFails() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When
        manager.startBackup();

        // Then
        manager.startRestore();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = ".*transition.*not currently permitted")
    public void givenSimpleManager_whenRestoring_thenStartingBackupFails() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When
        manager.startRestore();

        // Then
        manager.startBackup();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleManager_whenStartingBackupTwice_thenIllegalState() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When and Then
        manager.startBackup();
        manager.startBackup();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSimpleManager_whenStartingRestoreTwice_thenIllegalState() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When and Then
        manager.startRestore();
        manager.startRestore();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Can't finish.*")
    public void givenSimpleManager_whenStartingBackup_thenFinishingRestoreIsIllegal() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When
        manager.startBackup();

        // Then
        manager.finishRestore();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Can't finish.*")
    public void givenSimpleManager_whenStartingRestore_thenFinishingBackupIsIllegal() {
        // Given
        BackupManager manager = new SimpleBackupManager();

        // When
        manager.startRestore();

        // Then
        manager.finishBackup();
    }
}
