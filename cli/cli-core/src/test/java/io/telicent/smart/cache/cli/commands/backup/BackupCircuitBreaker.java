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
import io.telicent.smart.cache.projectors.sinks.CircuitBreakerSink;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.projectors.sinks.Sinks;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.time.Duration;
import java.util.List;

@Command(name = "circuit-breaker", description = "Runs the secondary backup service with circuit-breaker")
public class BackupCircuitBreaker extends BackupPrimary {
    @Override
    public int run() {
        try (NullSink<String> counter = NullSink.of()) {
            try (CircuitBreakerSink<String> circuitBreaker = Sinks.<String>circuitBreaker()
                                                                  .closed()
                                                                  .queueSize(100)
                                                                  .destination(counter)
                                                                  .build()) {
                long start = System.currentTimeMillis();
                try (BackupTracker secondary = this.backupTrackerOptions.getSecondary(null, BackupPrimary.APP_ID,
                                                                                      this.kafkaOptions,
                                                                                      BackupPrimary.APP_ID,
                                                                                      List.of(new CircuitBreakerListener(
                                                                                              circuitBreaker)))) {
                    print("Secondary with circuit breaker started...");
                    while (true) {
                        switch (circuitBreaker.getState()) {
                            case CLOSED -> print("CLOSED - backup state " + secondary.getState());
                            case OPEN -> print("OPEN - backup state " + secondary.getState());
                        }
                        BackupPrimary.waitBriefly(1);
                        circuitBreaker.send("test");

                        long elapsed = System.currentTimeMillis() - start;
                        if (elapsed > Duration.ofSeconds(30).toMillis()) {
                            print("Final item count is " + counter.count());
                            print("Exiting...");
                            return 0;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected String tag() {
        return "CIRCUIT-BREAKER";
    }

    @AllArgsConstructor
    @ToString
    private static final class CircuitBreakerListener implements BackupTransitionListener {

        @NonNull
        private final CircuitBreakerSink<?> breaker;

        @Override
        public void accept(BackupTracker tracker, BackupTrackerState from, BackupTrackerState to) {
            switch (to) {
                case BACKING_UP, RESTORING:
                    this.breaker.setState(CircuitBreakerSink.State.OPEN);
                    break;
                default:
                    this.breaker.setState(CircuitBreakerSink.State.CLOSED);
                    break;
            }
        }
    }
}
