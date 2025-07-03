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
package io.telicent.smart.cache.cli.commands.backup;

import com.github.rvesse.airline.annotations.Command;
import io.telicent.smart.cache.backups.BackupTracker;
import io.telicent.smart.cache.backups.BackupTrackerState;
import io.telicent.smart.cache.backups.BackupTransitionListener;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Command(name = "secondary", description = "Runs the secondary backup service")
public class BackupSecondary extends BackupPrimary {

    @Override
    public int run() {
        AtomicBoolean paused = new AtomicBoolean(false);
        long start = System.currentTimeMillis();
        try (BackupTracker secondary = this.backupTrackerOptions.getSecondary(null, BackupPrimary.APP_ID,
                                                                              this.kafkaOptions,
                                                                              BackupPrimary.APP_ID,
                                                                              List.of(new PauseListener(paused)))) {
            print("Secondary started...");
            while (true) {
                if (paused.get()) {
                    print(
                            "Paused while primary carries out a backup/restore operation (backup state " + secondary.getState() + ")...");
                } else {
                    print("Working (backup state " + secondary.getState() + ")...");
                }
                BackupPrimary.waitBriefly(1);

                long elapsed = System.currentTimeMillis() - start;
                if (elapsed > Duration.ofSeconds(30).toMillis()) {
                    print("Exiting...");
                    return 0;
                }
            }
        }
    }

    @Override
    protected void print(String line) {
        System.out.println("[SECONDARY] " + line);
    }

    @AllArgsConstructor
    @ToString
    private static final class PauseListener implements BackupTransitionListener {

        @NonNull
        private final AtomicBoolean paused;

        @Override
        public void accept(BackupTracker tracker, BackupTrackerState from, BackupTrackerState to) {
            switch (to) {
                case BACKING_UP, RESTORING:
                    this.paused.set(true);
                    break;
                default:
                    this.paused.set(false);
                    break;
            }
        }
    }
}
