package io.telicent.smart.cache.cli.commands.backup;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import io.telicent.smart.cache.backups.BackupManager;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.options.BackupManagerOptions;
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
    BackupManagerOptions backupManagerOptions = new BackupManagerOptions();

    @Option(name = "--big-delay")
    protected int bigDelay = 10;

    @Option(name = "--small-delay")
    protected int smallDelay = 3;

    @Override
    public int run() {
        try (BackupManager primary = this.backupManagerOptions.getPrimary(null, this.kafkaOptions, APP_ID)) {
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
