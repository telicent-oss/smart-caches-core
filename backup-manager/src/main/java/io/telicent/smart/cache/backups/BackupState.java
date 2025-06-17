package io.telicent.smart.cache.backups;

public enum BackupState {
    READY,
    REQUESTED,
    PREPARING,
    SUCCESS,
    FAILURE;

    public static boolean canTransition(BackupState current, BackupState target) {
        return switch (current) {
            // From READY can move to REQUESTED
            case READY -> target == REQUESTED;
            // From REQUESTED can move to PREPARING/FAILURE
            case REQUESTED -> PREPARING == target || FAILURE == target;
            // FROM PREPARING can move to SUCCESS/FAILURE
            case PREPARING -> SUCCESS == target || FAILURE == target;
            default -> false;
        };
    }
}
