/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.cache.backups;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Provides a basic state machine for tracking the state of ongoing backup/restore operations
 * <p>
 * Derived implementations can either register one/more listeners that are triggered when a state transition happens, or
 * override the {@link #onTransition(BackupTrackerState, BackupTrackerState)} method, in order to take additional
 * application specific actions when these things occur.
 * </p>
 */
@ToString
public class SimpleBackupTracker implements BackupTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBackupTracker.class);

    protected final List<BackupTransitionListener> listeners;
    protected volatile BackupTrackerState state = BackupTrackerState.STARTING;
    @ToString.Exclude
    protected final Object transitionLock = new Object();

    /**
     * Creates a new simple tracker with no listeners
     */
    public SimpleBackupTracker() {
        this(null);
    }

    /**
     * Creates a new simple tracker with the supplied listeners
     *
     * @param listeners Listeners
     */
    public SimpleBackupTracker(List<BackupTransitionListener> listeners) {
        this.listeners = Objects.requireNonNullElse(listeners, Collections.emptyList());
    }

    @Override
    public BackupTrackerState getState() {
        return this.state;
    }

    /**
     * Tells the tracker that application start up is complete
     * <p>
     * <strong>MUST</strong> only be called once by an application
     * </p>
     */
    @Override
    public final void startupComplete() {
        attemptTransition(BackupTrackerState.READY);
    }

    private void attemptTransition(BackupTrackerState newState) {
        synchronized (transitionLock) {
            if (BackupTrackerState.canTransition(state, newState)) {
                updateState(newState);
            } else {
                throw illegalTransition(newState);
            }
        }
    }

    private IllegalStateException illegalTransition(BackupTrackerState target) {
        return new IllegalStateException(
                "Backup Tracker in state " + this.state + ", transition to state " + target + " not currently permitted");
    }

    private void updateState(BackupTrackerState newState) {
        BackupTrackerState oldState = state;
        this.state = newState;
        this.onTransition(oldState, this.state);

    }

    @Override
    public final void startBackup() {
        attemptTransition(BackupTrackerState.BACKING_UP);
    }

    @Override
    public final void finishBackup() {
        if (this.state != BackupTrackerState.BACKING_UP) {
            throw new IllegalStateException("Can't finish backup from state " + this.state);
        }
        attemptTransition(BackupTrackerState.READY);
    }

    @Override
    public final void startRestore() {
        attemptTransition(BackupTrackerState.RESTORING);
    }

    @Override
    public final void finishRestore() {
        if (this.state != BackupTrackerState.RESTORING) {
            throw new IllegalStateException("Can't finish restore from state " + this.state);
        }
        attemptTransition(BackupTrackerState.READY);
    }

    /**
     * Called as part of {@link #updateState(BackupTrackerState)} and handles calling the registered listeners to allow
     * them to respond to the transition appropriately
     *
     * @param from Previous state
     * @param to   New state
     */
    private void onTransition(BackupTrackerState from, BackupTrackerState to) {
        // Apply all registered listeners
        for (BackupTransitionListener listener : this.listeners) {
            // Ignore any null listeners
            if (listener == null) {
                continue;
            }
            try {
                listener.accept(this, from, to);
            } catch (Throwable e) {
                LOGGER.warn("onTransition from {} to {} listener {} failed: {}", from, to, listener, e.getMessage());
            }
        }
    }

    /**
     * Closes the backup tracker
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
