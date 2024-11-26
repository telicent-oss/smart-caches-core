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
package io.telicent.smart.cache.cli.commands;

import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.help.Copyright;
import com.github.rvesse.airline.annotations.help.ExitCodes;
import com.github.rvesse.airline.builder.ParserBuilder;
import com.github.rvesse.airline.model.CommandMetadata;
import com.github.rvesse.airline.model.ParserMetadata;
import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.errors.ParseException;
import com.github.rvesse.airline.parser.errors.handlers.CollectAll;
import com.github.rvesse.airline.parser.options.MaybePairValueOptionParser;
import io.telicent.smart.cache.cli.options.LiveReporterOptions;
import io.telicent.smart.cache.cli.options.LoggingOptions;
import io.telicent.smart.cache.live.LiveErrorReporter;
import io.telicent.smart.cache.live.TelicentLive;
import io.telicent.smart.cache.live.model.LiveError;
import io.telicent.smart.cache.live.model.LiveStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Abstract class for commands in the Smart Cache CLI
 */
@Copyright(holder = "Telicent Ltd", startYear = 2022)
@ExitCodes(codes = { 0, 1, 2, 127, 255 }, descriptions = {
        "Success", "Failure", "Help shown as requested", "Failed to parse arguments", "CLI Launch failed"
})
public abstract class SmartCacheCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartCacheCommand.class);

    @AirlineModule
    HelpOption<SmartCacheCommand> help = new HelpOption<>();

    @AirlineModule
    LoggingOptions logging = new LoggingOptions();

    /**
     * Options for configuring a Telicent Live Reporter
     */
    @AirlineModule
    protected LiveReporterOptions liveReporter = new LiveReporterOptions();

    static ParseResult<? extends SmartCacheCommand> LAST_PARSE_RESULT = null;

    static int LAST_EXIT_STATUS = Integer.MIN_VALUE;

    static boolean TEST = false;

    /**
     * Setups a Telicent Live Reporter (if desired and/or necessary)
     *
     * @param metadata Command metadata
     */
    protected void setupLiveReporter(CommandMetadata metadata) {
        // Does nothing
        LOGGER.warn("Telicent Live Reporting is not implemented for this command");
    }

    /**
     * Runs the command and returns the exit code to use
     *
     * @return Exit code
     */
    public abstract int run();

    /**
     * Runs the given command class as a singular command
     * <p>
     * This can be called from the {@code main(String[])} method of any {@link SmartCacheCommand} derived command to
     * allow it to run as a standalone command.
     * </p>
     *
     * @param cls  Command class
     * @param args Command arguments
     * @param <T>  Command type
     */
    public static <T extends SmartCacheCommand> void runAsSingleCommand(Class<T> cls, String[] args) {
        ParserMetadata<T> parserConfig =
                new ParserBuilder<T>().withErrorHandler(new CollectAll())
                                      .withFlagNegationPrefix("--no-")
                                      .withOptionParser(
                                              new MaybePairValueOptionParser<>())
                                      .withDefaultOptionParsers()
                                      .build();
        SingleCommand<T> parser = SingleCommand.singleCommand(cls, parserConfig);
        ParseResult<T> result = parser.parseWithResult(args);
        handleParseResult(result);
    }

    /**
     * Handles the parse result performing the most appropriate action
     * <p>
     * Firstly, if a command was parsed and the user requested help on the command, then help is shown regardless of
     * whether there were any parsing errors.
     * </p>
     * <p>
     * Secondly, if a command was parsed successfully then it is run, and {@link System#exit(int)} will be called with
     * the result of the commands {@link SmartCacheCommand#run()} method i.e. the command supplies the appropriate exit
     * code.
     * </p>
     * <p>
     * Finally, if there were parsing errors then those are displayed to the user, and {@link System#exit(int)} will be
     * called with a non-zero exit code.
     * </p>
     *
     * @param result Parse Result
     * @param <T>    Command type
     */
    public static <T extends SmartCacheCommand> void handleParseResult(ParseResult<T> result) {
        if (TEST) {
            LAST_PARSE_RESULT = result;
        }

        // If User requests help always do that and exit regardless of anything else
        if (showHelpIfRequested(result)) {
            // Normally if we show help we System.exit() so will never reach here BUT when integration testing we
            // skip the System.exit() and return true to indicate help was shown at which point the command should not
            // execute further
            return;
        }

        if (result.wasSuccessful()) {
            T command = result.getCommand();

            // Setup Logging and Live Reporter based on CLI options
            command.logging.configureLogging();
            command.setupLiveReporter(result.getState().getCommand());

            // Run the command and exit with its returned exit code
            try {
                int exitCode = command.run();

                // Stop the Live Reporter components
                command.liveReporter.teardown(exitCode == 0 ? LiveStatus.COMPLETED : LiveStatus.ERRORING);
                exit(exitCode);
            } catch (Throwable t) {
                // Abnormal termination

                try {
                    // Report the error out to Telicent Live (if configured)
                    LiveErrorReporter errorReporter = TelicentLive.getErrorReporter();
                    if (errorReporter != null) {
                        LiveError error = LiveError.create().error(t).level(Level.ERROR).build();
                        errorReporter.reportError(error);
                    }
                } catch (Throwable reportError) {
                    // Ignore any unexpected problem reporting the error since we're about to log it anyway
                } finally {
                    LOGGER.error("Unexpected error: {}\n", t.getMessage());

                    // Clean up the Live Reporter components
                    command.liveReporter.teardown(LiveStatus.RUNNING);
                    exit(1);
                }
            }
        } else {
            // Parsing failed - display the generated parser errors to the user
            int i = 0;
            System.err.format("%d errors encountered parsing your arguments:\n", result.getErrors().size());
            System.err.println();
            for (ParseException e : result.getErrors()) {
                System.err.format("#%d - %s\n", ++i, e.getMessage());
            }
            System.err.println();
            if (result.getCommand() != null) {
                System.err.println("Try re-running your command with -h/--help to see help for this command");
                System.err.println();
            }
            exit(127);
        }
    }

    /**
     * Exits the application with the given exit code
     *
     * @param exitCode Exit code
     */
    public static void exit(int exitCode) {
        if (TEST) {
            LAST_EXIT_STATUS = exitCode;
        } else {
            System.exit(exitCode);
        }
    }

    /**
     * Shows command help if it was requested by the user and exit
     *
     * @param result Parse result
     * @param <T>    Command type
     * @return True if help was shown, false otherwise
     */
    public static <T extends SmartCacheCommand> boolean showHelpIfRequested(ParseResult<T> result) {
        if (result.getCommand() != null && result.getCommand().help.showHelpIfRequested()) {
            exit(2);
            return true;
        }
        return false;
    }
}
