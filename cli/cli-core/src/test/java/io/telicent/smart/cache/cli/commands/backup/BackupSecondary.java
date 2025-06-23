package io.telicent.smart.cache.cli.commands.backup;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import io.telicent.smart.cache.backups.BackupManager;
import io.telicent.smart.cache.backups.BackupManagerState;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.options.BackupManagerOptions;
import io.telicent.smart.cache.cli.options.KafkaConfigurationOptions;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@Command(name = "secondary", description = "Runs the secondary backup service")
public class BackupSecondary extends BackupPrimary {

    @Override
    public int run() {
        AtomicBoolean paused = new AtomicBoolean(false);
        long start = System.currentTimeMillis();
        try (BackupManager secondary = this.backupManagerOptions.getSecondary(null, BackupPrimary.APP_ID,
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
    private static final class PauseListener implements BiConsumer<BackupManagerState, BackupManagerState> {

        @NonNull
        private final AtomicBoolean paused;

        @Override
        public void accept(BackupManagerState from, BackupManagerState to) {
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
