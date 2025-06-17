package io.telicent.smart.cache.backups;


/**
 * Possible Backup Manager States
 */
public enum BackupManagerState {
    STARTING, READY, BACKING_UP, RESTORING;

    public static boolean canTransition(BackupManagerState current, BackupManagerState target) {
        return switch (current) {
            // From STARTING state we can transition to any other state, including ourselves if we're still starting up
            case STARTING -> true;
            // From READY state we can transition to anything other than STARTING, including ourselves
            case READY -> target != BackupManagerState.STARTING;
            // From BACKING_UP/RESTORING we can only transition back to READY
            case BACKING_UP, RESTORING -> target == READY;
        };
    }
}
