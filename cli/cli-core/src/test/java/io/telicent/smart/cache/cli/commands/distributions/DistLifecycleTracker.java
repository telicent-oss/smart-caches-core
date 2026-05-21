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
package io.telicent.smart.cache.cli.commands.distributions;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.options.DistributionLifecycleTrackerOptions;
import io.telicent.smart.cache.cli.options.KafkaConfigurationOptions;
import io.telicent.smart.cache.distribution.lifecycle.events.listeners.LoggingListener;
import io.telicent.smart.cache.distribution.lifecycle.store.apps.AppDistributionLifecycleStoreFile;
import io.telicent.smart.cache.distribution.lifecycle.tracker.DistributionLifecycleTracker;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;

@Command(name = "dist-lifecycle-tracker")
public class DistLifecycleTracker extends SmartCacheCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributionLifecycleTracker.class);

    @AirlineModule
    KafkaConfigurationOptions kafkaOptions = new KafkaConfigurationOptions();

    @AirlineModule
    DistributionLifecycleTrackerOptions distLifecycleOptions = new DistributionLifecycleTrackerOptions();

    @Option(name = "--delay", description = "Sets the delay before this test command exits, useful to set this if you need the command to wait longer to debug something")
    private long delay = 10;

    @Override
    public int run() {
        try {
            LOGGER.info("Creating Distribution Lifecycle Tracker...");
            File stateFile = Files.createTempFile("state", ".json").toFile();
            stateFile.delete();
            DockerTestDistributionLifecycleTracker.TRACKER =
                    this.distLifecycleOptions.create(null, "dist-lifecycle-tracker", this.kafkaOptions,
                                                     "dist-lifecycle-tracker",
                                                     AppDistributionLifecycleStoreFile.builder()
                                                                                      .stateFile(stateFile)
                                                                                      .app("dist-lifecycle-tracker")
                                                                                      .build(), 1,
                                                     List.of(new LoggingListener()));
            LOGGER.info("Tracker created OK");
            try {
                LOGGER.info("Waiting for {} seconds", this.delay);
                Thread.sleep(Duration.ofSeconds(this.delay));
                LOGGER.info("Waiting finished");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
}
