package io.telicent.smart.cache.backups;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a basic state machine for tracking the state of ongoing backup/restore operations
 * <p>
 * Derived implementations can implement the {@link #onTransition(BackupManagerState, BackupManagerState)} method to
 * take additional application specific actions when these things occur.
 * </p>
 */
public class SimpleBackupManager implements BackupManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBackupManager.class);

    protected volatile BackupManagerState managerState = BackupManagerState.STARTING;
    protected final Object transitionLock = new Object();

    @Override
    public BackupManagerState getState() {
        return this.managerState;
    }

    /**
     * Tells the manager that application start up is complete
     * <p>
     * <strong>MUST</strong> only be called once by an application
     * </p>
     */
    @Override
    public final void startupComplete() {
        attemptTransition(BackupManagerState.READY);
    }

    private void attemptTransition(BackupManagerState newState) {
        synchronized (transitionLock) {
            if (BackupManagerState.canTransition(managerState, newState)) {
                updateManagerState(newState);
            } else {
                illegalTransition(newState);
            }
        }
    }

    private void illegalTransition(BackupManagerState target) {
        throw new IllegalStateException(
                "Backup Manager in state " + this.managerState + ", transition to state " + target + " not currently permitted");
    }

    private void updateManagerState(BackupManagerState newState) {
        BackupManagerState oldState = managerState;
        this.managerState = newState;
        try {
            this.onTransition(oldState, this.managerState);
        } catch (Throwable e) {
            LOGGER.warn("onTransition from {} to {} failed: {}", oldState, newState, e.getMessage());
        }
    }

    /**
     * Method that may be overridden by derived implementations to take additional actions when a state transition
     * occurs
     *
     * @param from From state i.e. the old state we transitioned from
     * @param to   To state i.e. the new state to which we have transitioned
     */
    protected void onTransition(BackupManagerState from, BackupManagerState to) {
    }

    @Override
    public final void startBackup() {
        attemptTransition(BackupManagerState.BACKING_UP);
    }

    @Override
    public final void finishBackup() {
        if (this.managerState != BackupManagerState.BACKING_UP) {
            throw new IllegalStateException("Can't finish restore from state " + this.managerState);
        }
        attemptTransition(BackupManagerState.READY);
    }

    @Override
    public final void startRestore() {
        attemptTransition(BackupManagerState.RESTORING);
    }

    @Override
    public final void finishRestore() {
        if (this.managerState != BackupManagerState.RESTORING) {
            throw new IllegalStateException("Can't finish restore from state " + this.managerState);
        }
        attemptTransition(BackupManagerState.READY);
    }

}
