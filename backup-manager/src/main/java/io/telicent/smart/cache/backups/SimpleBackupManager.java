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
package io.telicent.smart.cache.backups;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Provides a basic state machine for tracking the state of ongoing backup/restore operations
 * <p>
 * Derived implementations can implement the {@link #onTransition(BackupManagerState, BackupManagerState)} method to
 * take additional application specific actions when these things occur.
 * </p>
 */
public class SimpleBackupManager implements BackupManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBackupManager.class);
    protected final List<BiConsumer<BackupManagerState, BackupManagerState>> listeners;

    protected volatile BackupManagerState managerState = BackupManagerState.STARTING;
    protected final Object transitionLock = new Object();

    public SimpleBackupManager() {
        this(null);
    }

    public SimpleBackupManager(List<BiConsumer<BackupManagerState, BackupManagerState>> listeners) {
        this.listeners = Objects.requireNonNullElse(listeners, Collections.emptyList());
    }

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
                throw illegalTransition(newState);
            }
        }
    }

    private IllegalStateException illegalTransition(BackupManagerState target) {
        return new IllegalStateException(
                "Backup Manager in state " + this.managerState + ", transition to state " + target + " not currently permitted");
    }

    private void updateManagerState(BackupManagerState newState) {
        BackupManagerState oldState = managerState;
        this.managerState = newState;
        this.onTransition(oldState, this.managerState);

    }

    @Override
    public final void startBackup() {
        attemptTransition(BackupManagerState.BACKING_UP);
    }

    @Override
    public final void finishBackup() {
        if (this.managerState != BackupManagerState.BACKING_UP) {
            throw new IllegalStateException("Can't finish backup from state " + this.managerState);
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

    /**
     * Called as part of {@link #updateManagerState(BackupManagerState)} and handles calling the registered listeners to
     * allow them to respond to the transition appropriately
     *
     * @param from Previous state
     * @param to New state
     */
    private void onTransition(BackupManagerState from, BackupManagerState to) {
        // Apply all registered listeners
        for (BiConsumer<BackupManagerState, BackupManagerState> listener : this.listeners) {
            // Ignore any null listeners
            if (listener == null) {
                continue;
            }
            try {
                listener.accept(from, to);
            } catch (Throwable e) {
                LOGGER.warn("onTransition from {} to {} listener {} failed: {}", from, to, listener, e.getMessage());
            }
        }
    }

    /**
     * Closes the backup manager
     * <p>
     * This implementation is a no-op as it holds only in-memory state, derived implementations may have external
     * resources they need to close so should override this method and clean up accordingly.
     * </p>
     */
    @Override
    public void close() {
        // Does nothing by default
    }

}
