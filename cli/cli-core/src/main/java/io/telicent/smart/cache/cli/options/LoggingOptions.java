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

import ch.qos.logback.classic.Level;
import com.github.rvesse.airline.annotations.Option;
import io.telicent.smart.cache.configuration.Configurator;
import io.telicent.smart.cache.observability.RuntimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides common options related to controlling the application logging at runtime
 */
public class LoggingOptions {

    private static final String MULTIPLE_OPTIONS_BEHAVIOUR =
            "If multiple logging options are supplied then the most verbose logging level requested is applied.";

    @Option(name = {
            "--verbose", "--debug"
    }, arity = 0, description = "Specifies verbose mode i.e. increased logging verbosity. " + MULTIPLE_OPTIONS_BEHAVIOUR)
    boolean verbose = Configurator.get(new String[] { CliEnvironmentVariables.VERBOSE, CliEnvironmentVariables.DEBUG }, Boolean::parseBoolean, false);

    @Option(name = { "--trace" }, arity = 0, description = "Specifies trace mode i.e. greatly increased logging verbosity. " + MULTIPLE_OPTIONS_BEHAVIOUR)
    boolean trace = Configurator.get(CliEnvironmentVariables.TRACE, Boolean::parseBoolean, false);

    @Option(name = { "--quiet" }, arity = 0, description = "Specifies quiet mode i.e. greatly reduced logging verbosity. " + MULTIPLE_OPTIONS_BEHAVIOUR)
    boolean quiet = Configurator.get(CliEnvironmentVariables.QUIET, Boolean::parseBoolean, false);

    @Option(name = {
            "--runtime-info", "--no-runtime-info"
    }, arity = 0, description = "When specified will print basic runtime information (Memory, JVM and OS) to the logs during command startup.  Defaults to enabled, may be disabled by specifying the --no-runtime-info option.")
    boolean showRuntimeInfo =
            Configurator.get(CliEnvironmentVariables.ENABLE_RUNTIME_INFO, Boolean::parseBoolean, true);

    /**
     * (Re-)configures logging based on the provided CLI options (if any)
     * <p>
     * This potentially overrides the root logger level set by any Logback configuration file the application is
     * providing.  However, since we are only changing the root logger level if an application has explicitly set the
     * log level for another logger then that level continues to be honoured irregardless of what the user configures at
     * runtime via these options.
     * </p>
     */
    public void configureLogging() {
        ch.qos.logback.classic.Logger root =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        Logger logger = LoggerFactory.getLogger(LoggingOptions.class);

        // Print the runtime information if requested
        // Intentionally do this prior to reconfiguring the log level as this is logged as INFO level so could be
        // omitted if --quiet was used
        if (this.showRuntimeInfo) {
            RuntimeInfo.printRuntimeInfo(logger);
        }

        // Reconfigure the log level (if needed) and print a warning (which will always remain visible regardless of the
        // logging verbosity selected) so that the fact logging was changed is itself logged!
        if (this.trace) {
            root.setLevel(Level.TRACE);
            logger.warn("Logging set to TRACE level as requested (--trace supplied)");
        } else if (this.verbose) {
            root.setLevel(Level.DEBUG);
            logger.warn("Logging set to DEBUG level as requested (--verbose supplied)");
        } else if (this.quiet) {
            root.setLevel(Level.WARN);
            logger.warn("Logging set to WARN level as requested (--quiet supplied)");
        }
    }

    /**
     * Gets the effective log level that will be used based on the supplied options, this is the log level that
     * {@link #configureLogging()} will set.  If no logging options are supplied then {@code null} is returned and the
     * application would respect the level configured in whatever Logback configuration file it found at runtime.
     * <p>
     * This method only really exists for test purposes.
     * </p>
     *
     * @return Effective logging level
     */
    Level effectiveLevel() {
        if (this.trace) {
            return Level.TRACE;
        } else if (this.verbose) {
            return Level.DEBUG;
        } else if (this.quiet) {
            return Level.WARN;
        } else {
            return null;
        }
    }

    /**
     * Resets the root logger to the default level, only intended for test usage
     */
    static void resetLogging() {
        ch.qos.logback.classic.Logger root =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }
}
