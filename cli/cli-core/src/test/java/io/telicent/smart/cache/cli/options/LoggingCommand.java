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
package io.telicent.smart.cache.cli.options;

import com.github.rvesse.airline.annotations.Command;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "logging", description = "Logging options test command")
public class LoggingCommand extends SmartCacheCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingCommand.class);
    public static final String ERROR_MESSAGE = "Error";
    public static final String WARNING_MESSAGE = "Warning";
    public static final String INFORMATION_MESSAGE = "Information";
    public static final String DEBUGGING_MESSAGE = "Debugging";
    public static final String TRACING_MESSAGE = "Tracing";

    @Override
    public int run() {
        // Print a log message at every log level, this is so tests against this class can check whether the logging
        // options supplied customised the
        LOGGER.error(ERROR_MESSAGE);
        LOGGER.warn(WARNING_MESSAGE);
        LOGGER.info(INFORMATION_MESSAGE);
        LOGGER.debug(DEBUGGING_MESSAGE);
        LOGGER.trace(TRACING_MESSAGE);
        return 0;
    }


    public static void main(String[] args) {
        SmartCacheCommand.runAsSingleCommand(LoggingCommand.class, args);
    }
}
