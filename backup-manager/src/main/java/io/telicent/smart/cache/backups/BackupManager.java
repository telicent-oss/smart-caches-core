package io.telicent.smart.cache.backups;

public interface BackupManager {

    /**
     * Gets the current state
     * @return Current state
     */
    BackupManagerState getState();

    void startupComplete();

    void startBackup();

    void finishBackup();

    void startRestore();

    void finishRestore();
}
