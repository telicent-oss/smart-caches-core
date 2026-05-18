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
package io.telicent.smart.cache.distribution.lifecycle.store.apps;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.payloads.Envelope;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * An application scoped distribution lifecycle state store that tracks a single applications state of processing
 * distribution lifecycle events focusing only on the information relevant to a specific application
 */
public class AppDistributionLifecycleStoreFile extends AbstractAppDistributionLifecycleStore implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppDistributionLifecycleStoreFile.class);
    public static final String TMP_EXTENSION = ".tmp";
    public static final String BAK_EXTENSION = ".bak";

    private final File stateFile;

    /**
     * Creates a new state store
     *
     * @param app       Application name
     * @param stateFile State file
     */
    protected AppDistributionLifecycleStoreFile(String app, File stateFile) {
        super(app);
        this.stateFile = Objects.requireNonNull(stateFile, "State store file cannot be null");
        if (this.stateFile.isDirectory()) {
            throw new IllegalArgumentException(
                    "State store given as a directory (" + this.stateFile.getAbsolutePath() + ") when a file was expected");
        } else if (!this.stateFile.exists()) {
            File stateDir = this.stateFile.getParentFile();
            if (!stateDir.exists() && !this.stateFile.getParentFile().mkdirs()) {
                throw new IllegalArgumentException(
                        "Failed to create configured state store file parent directory " + this.stateFile.getAbsolutePath());
            }

        }

        this.load();
    }

    private LifecycleStateFile tryRecoverStateFile(IOException e, String extension, boolean throwOnFailure) throws
            IOException {
        File tmpFile = new File(this.stateFile.getAbsolutePath() + extension);
        if (tmpFile.exists()) {
            // Try and load the state from the given temporary or backup file
            try {
                LifecycleStateFile state = Envelope.JSON.readValue(tmpFile, LifecycleStateFile.class);
                Files.move(tmpFile.toPath(), this.stateFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
                return state;
            } catch (IOException e2) {
                // If this was the temporary file we just tried also try and rollback to the backup file
                if (!Objects.equals(BAK_EXTENSION, extension)) {
                    return tryRecoverStateFile(e, BAK_EXTENSION, throwOnFailure);
                }

                // Otherwise throw the original error
                if (throwOnFailure) {
                    throw e;
                } else {
                    return null;
                }
            }
        } else if (!Objects.equals(BAK_EXTENSION, extension)) {
            // If the temporary file did not exist try and rollback to the backup file
            return tryRecoverStateFile(e, BAK_EXTENSION, throwOnFailure);
        } else if (throwOnFailure) {
            throw e;
        } else {
            return null;
        }
    }

    private void load() {
        LifecycleStateFile state = null;
        if (this.stateFile.exists()) {
            try {
                state = Envelope.JSON.readValue(this.stateFile, LifecycleStateFile.class);
            } catch (IOException e) {
                LOGGER.error("Failed to read application distribution lifecycle state file: ", e);
                try {
                    // Try and recover from the temporary file, which also falls back to trying to recover from the
                    // backup file
                    state = tryRecoverStateFile(e, TMP_EXTENSION, true);
                } catch (IOException e2) {
                    // If no state file exists, nor can be recovered, error or null
                    throw new IllegalStateException("Application distribution lifecycle state file unreadable", e2);
                }
            }
        } else {
            // If no state file existed try and recover from a temporary or backup/file but don't fail if this can't
            // be done.
            try {
                state = tryRecoverStateFile(null, TMP_EXTENSION, false);
            } catch (IOException e) {
                // If no state file exists, nor can be recovered
                throw new IllegalArgumentException("Application distribution lifecycle state file unreadable", e);
            }
        }

        if (state != null) {
            if (!Objects.equals(this.application, state.getApplication())) {
                throw new IllegalStateException(
                        "State file " + this.stateFile.getAbsolutePath() + " is from a different application");
            }

            this.events.putAll(state.getActions().getActions());
            this.appStates.putAll(state.getStates().getStates());
            this.distributions.putAll(state.getDistributions().getDistributions());
        }
    }

    private void save() {
        LifecycleStateFile state = LifecycleStateFile.builder()
                                                     .application(this.application)
                                                     .actions(new TrackedActions().setActions(this.events))
                                                     .states(new TrackedAppStates().setStates(this.appStates))
                                                     .distributions(new TrackedDistributions().setDistributions(
                                                             this.distributions))
                                                     .build();

        try {
            // Firstly write the state file to a temporary file
            File tmpStateFile = new File(this.stateFile.getAbsolutePath() + TMP_EXTENSION);
            Envelope.JSON.writeValue(tmpStateFile, state);

            // Second move the pre-existing state file (if any) to the backup location
            File backupStateFile = new File(this.stateFile.getAbsolutePath() + BAK_EXTENSION);
            if (this.stateFile.exists()) {
                Files.move(this.stateFile.toPath(), backupStateFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            }

            // Move the temporary state file to the final location
            Files.move(tmpStateFile.toPath(), this.stateFile.toPath(), StandardCopyOption.ATOMIC_MOVE);

            // Finally remove the backup file (if any)
            if (backupStateFile.exists()) {
                backupStateFile.delete();
            }

        } catch (IOException e) {
            LOGGER.error("Failed to write application distribution lifecycle state file:", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        this.save();
    }


    @NoArgsConstructor
    private static final class TrackedActions {
        private final Map<UUID, LifecycleAction> actions = new HashMap<>();

        @JsonAnyGetter
        public Map<UUID, LifecycleAction> getActions() {
            return this.actions;
        }

        @JsonAnySetter
        public void setAction(String eventId, LifecycleAction action) {
            this.actions.put(UUID.fromString(eventId), action);
        }

        public TrackedActions setActions(Map<UUID, LifecycleAction> actions) {
            this.actions.putAll(actions);
            return this;
        }
    }

    @NoArgsConstructor
    private static final class TrackedAppStates {
        private final Map<UUID, ApplicationState> states = new HashMap<>();

        @JsonAnyGetter
        public Map<UUID, ApplicationState> getStates() {
            return this.states;
        }

        @JsonAnySetter
        public void setState(String eventId, ApplicationState state) {
            this.states.put(UUID.fromString(eventId), state);
        }

        public TrackedAppStates setStates(Map<UUID, ApplicationState> states) {
            this.states.putAll(states);
            return this;
        }
    }

    @NoArgsConstructor
    private static final class TrackedDistributions {
        private final Map<String, DistributionLifecycleState> distributions = new HashMap<>();

        @JsonAnyGetter
        public Map<String, DistributionLifecycleState> getDistributions() {
            return this.distributions;
        }

        @JsonAnySetter
        public void setDistribution(String distributionId, DistributionLifecycleState state) {
            this.distributions.put(distributionId, state);
        }

        public TrackedDistributions setDistributions(Map<String, DistributionLifecycleState> states) {
            this.distributions.putAll(states);
            return this;
        }
    }

    @Builder
    @Jacksonized
    @Getter
    private static final class LifecycleStateFile {
        private final String application;
        private final TrackedActions actions;
        private final TrackedAppStates states;
        private final TrackedDistributions distributions;
    }
}
