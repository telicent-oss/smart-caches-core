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
package io.telicent.smart.cache.cli.commands.debug;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.ranges.DoubleRange;
import com.github.rvesse.airline.annotations.restrictions.ranges.LongRange;
import com.github.rvesse.airline.model.CommandMetadata;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.options.KafkaOptions;
import io.telicent.smart.cache.live.TelicentLive;
import io.telicent.smart.cache.live.model.IODescriptor;
import io.telicent.smart.cache.live.model.LiveError;
import io.telicent.smart.cache.projectors.utils.PeriodicAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A debug command that creates a fake application reporting status heartbeats to Telicent Live
 */
@Command(name = "fake-reporter", description = "Creates a fake application that reports status heartbeats to Telicent Live")
public class FakeReporter extends SmartCacheCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeReporter.class);

    private static final Level[] ERROR_LEVELS = new Level[] { Level.ERROR, Level.WARN, Level.INFO };

    @Option(name = {
            "--app-name", "--name"
    }, title = "ApplicationName", description = "Sets the Application Name that will be reported")
    private String name;

    @Option(name = {
            "--app-id", "--id"
    }, title = "ApplicationId", description = "Sets the Application ID that will be reported")
    private String id;

    @Option(name = { "--component-type" }, title = "ComponentType", description = "Sets the component type that will be reported")
    private String componentType;

    @Option(name = {
            "--input-name", "--input"
    }, title = "InputName", description = "Sets the input name that will be reported.  Defaults to the value of --topic if not specified.")
    private String inputName;

    @Option(name = { "--input-type" }, title = "InputType", description = "Sets the input type that will be reported.  Defaults to topic if not specified.")
    private String inputType;

    @Option(name = {
            "--output-name", "--output"
    }, title = "OutputName", description = "Sets the output name that will be reported")
    private String outputName;

    @Option(name = { "--output-type" }, title = "OutputType", description = "Sets the output type that will be reported")
    private String outputType;

    @AirlineModule
    private KafkaOptions kafkaOptions = new KafkaOptions();

    @Option(name = { "--error-interval" }, title = "ErrorInterval", description = "Sets how frequently there is a chance of generating a random error, actual chance is controlled by the --error-chance option.  Defaults to 5 seconds.")
    @LongRange(min = 1, max = 30)
    private long errorInterval = 5;

    @Option(name = { "--error-chance" }, title = "ErrorChance", description = "Sets the chance of generating a random error, actual frequency of random error generation is controlled by the --error-interval option.  Defaults to 0.5 i.e. 50%")
    @DoubleRange(min = 0.0, max = 1.0)
    private double errorChance = 0.5;

    @Override
    protected void setupLiveReporter(CommandMetadata metadata) {
        this.liveReporter.setupLiveReporter(this.kafkaOptions.bootstrapServers,
                                            StringUtils.isNotBlank(this.name) ? this.name : metadata.getName(),
                                            StringUtils.isNotBlank(this.id) ? this.id : metadata.getName(),
                                            StringUtils.isNotBlank(this.componentType) ? this.componentType : "mapper",
                                            new IODescriptor(StringUtils.isNotBlank(this.inputName) ? this.inputName :
                                                             StringUtils.join(this.kafkaOptions.topics, ","),
                                                             StringUtils.isNotBlank(this.inputType) ? this.inputType :
                                                             "topic"),
                                            new IODescriptor(this.outputName, this.outputType));

        this.liveReporter.setupErrorReporter(this.kafkaOptions.bootstrapServers,
                                             StringUtils.isNotBlank(this.id) ? this.id : metadata.getName());
    }

    @Override
    public int run() {
        // Set up periodic random error generation
        Random random = new Random();
        AtomicInteger levelSelection = new AtomicInteger(0);
        PeriodicAction action = new PeriodicAction(() -> {
            double chance = random.nextDouble();
            if (chance < this.errorChance) {
                // Generated an error, we're continually cycling through the available error levels
                Level level = ERROR_LEVELS[levelSelection.getAndIncrement() % ERROR_LEVELS.length];
                LOGGER.debug("Generated a random error of level {}", level);
                TelicentLive.getErrorReporter()
                            .reportError(LiveError.create()
                                                  .message("Randomly generated error " + System.currentTimeMillis())
                                                  .level(level)
                                                  .recordCounter(System.currentTimeMillis())
                                                  .build());
            } else {
                LOGGER.debug("Random error generation didn't meet chance threshold");
            }
        }, Duration.ofSeconds(this.errorInterval));
        try {
            action.autoTrigger();
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            // Ignore
            action.cancelAutoTrigger();
        }
        return 0;
    }
}
