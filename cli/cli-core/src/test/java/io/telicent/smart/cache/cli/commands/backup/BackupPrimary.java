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

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import io.telicent.smart.cache.backups.BackupTracker;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.options.BackupTrackerOptions;
import io.telicent.smart.cache.cli.options.KafkaConfigurationOptions;

import java.time.Duration;

@Command(name = "primary", description = "Runs the primary backup server")
public class BackupPrimary extends SmartCacheCommand {

    /**
     * Shared App ID for backup manager tests
     */
    public static final String APP_ID = "backup-test";

    @AirlineModule
    KafkaConfigurationOptions kafkaOptions = new KafkaConfigurationOptions();

    @AirlineModule
    BackupTrackerOptions backupTrackerOptions = new BackupTrackerOptions();

    @Option(name = "--big-delay")
    protected int bigDelay = 10;

    @Option(name = "--small-delay")
    protected int smallDelay = 3;

    @Override
    public int run() {
        try (BackupTracker primary = this.backupTrackerOptions.getPrimary(null, this.kafkaOptions, APP_ID)) {
            print("Started");
            primary.startupComplete();

            print("Starting backup...");
            primary.startBackup();
            waitBriefly(this.bigDelay);
            primary.finishBackup();
            print("Finished backup!");

            waitBriefly(this.smallDelay);

            print("Starting restore...");
            primary.startRestore();
            waitBriefly(this.bigDelay);
            primary.finishRestore();
            print("Finished restore!");

            waitBriefly(this.smallDelay);

            print("Finished");
            primary.close();
            return 0;
        }
    }

    protected void print(String line) {
        System.out.println("[PRIMARY] " + line);
    }

    public static void waitBriefly(int seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds).toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted");
        }
    }
}
