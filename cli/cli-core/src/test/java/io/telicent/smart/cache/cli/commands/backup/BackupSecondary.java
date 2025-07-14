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
import io.telicent.smart.cache.actions.tracker.ActionTracker;
import io.telicent.smart.cache.actions.tracker.model.ActionState;
import io.telicent.smart.cache.actions.tracker.listeners.ActionTransitionListener;
import io.telicent.smart.cache.actions.tracker.model.ActionTransition;
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
        try (ActionTracker secondary = this.actionTrackerOptions.getSecondary(null, BackupPrimary.APP_ID,
                                                                              this.kafkaOptions,
                                                                              BackupPrimary.APP_ID,
                                                                              List.of(new PauseListener(paused)))) {
            print("Secondary started...");
            while (true) {
                if (paused.get()) {
                    print(
                            "Paused while primary carries out a '" + secondary.getAction() + "' action (state " + secondary.getState() + ")...");
                } else {
                    print("Working (state " + secondary.getState() + ")...");
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
    protected String tag() {
        return "SECONDARY";
    }

    @AllArgsConstructor
    @ToString
    private static final class PauseListener implements ActionTransitionListener {

        @NonNull
        private final AtomicBoolean paused;

        @Override
        public void accept(ActionTracker tracker, ActionTransition transition) {
            this.paused.set(transition.getTo() == ActionState.PROCESSING);
        }
    }
}
