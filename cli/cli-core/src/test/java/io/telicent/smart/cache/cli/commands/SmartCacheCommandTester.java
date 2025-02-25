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

import com.github.rvesse.airline.parser.ParseResult;
import io.telicent.smart.cache.projectors.utils.WriteOnceReference;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Provides the ability to put {@link SmartCacheCommand} derived command classes into a test mode in order to allow
 * writing unit test cases against their behaviour
 */
public class SmartCacheCommandTester {

    private static final PrintStream ORIGINAL_OUTPUT = System.out, ORIGINAL_ERROR = System.err;
    private static final ByteArrayOutputStream LAST_OUTPUT = new ByteArrayOutputStream(), LAST_ERROR =
            new ByteArrayOutputStream();

    private static File LAST_OUTPUT_FILE = null, LAST_ERROR_FILE = null, EXTERNAL_OUTPUT_FILE = null,
            EXTERNAL_ERROR_FILE = null;

    /**
     * When set to true the captured output and error from each test is also tee'd to the original standard output and
     * error, when set to false it is also captured to files for later review
     */
    public static boolean TEE_TO_ORIGINAL_STREAMS = false;

    private static final WriteOnceReference<String> PROJECT_VERSION = new WriteOnceReference<>();

    private SmartCacheCommandTester() {

    }

    /**
     * Setups command test mode, in this mode commands will not exit the application when invoked (providing they are
     * calling {@link SmartCacheCommand#exit(int)} and not {@link System#exit(int)} directly) and their parse result,
     * exit status and output streams are intercepted.  You can then inspect the parse result, exit status, output and
     * error streams via the various methods on this class.
     */
    public static void setup() {
        SmartCacheCommand.TEST = true;
        resetTestState();

        // Capture output streams
        System.setOut(new PrintStream(new TeeOutputStream(LAST_OUTPUT, getOutputTeeDestination()), true));
        System.setErr(new PrintStream(new TeeOutputStream(LAST_ERROR, getErrorTeeDestination()), true));
    }

    private static OutputStream getErrorTeeDestination() {
        if (TEE_TO_ORIGINAL_STREAMS || LAST_ERROR_FILE == null) {
            return ORIGINAL_ERROR;
        } else {
            try {
                OutputStream output = new FileOutputStream(LAST_ERROR_FILE);
                ORIGINAL_ERROR.println(
                        "Standard error for the next test will be captured to file " + LAST_ERROR_FILE.getAbsolutePath());
                return output;
            } catch (FileNotFoundException e) {
                return NullOutputStream.INSTANCE;
            }
        }
    }

    private static OutputStream getOutputTeeDestination() {
        if (TEE_TO_ORIGINAL_STREAMS || LAST_OUTPUT_FILE == null) {
            return ORIGINAL_OUTPUT;
        } else {
            try {
                OutputStream error = new FileOutputStream(LAST_OUTPUT_FILE);
                ORIGINAL_OUTPUT.println(
                        "Standard output for the next test will be captured to file " + LAST_OUTPUT_FILE.getAbsolutePath());
                return error;
            } catch (FileNotFoundException e) {
                return NullOutputStream.INSTANCE;
            }
        }
    }

    /**
     * Resets internal test state, should be called between individual test cases and command invocations
     */
    public static void resetTestState() {
        SmartCacheCommand.LAST_PARSE_RESULT = null;
        SmartCacheCommand.LAST_EXIT_STATUS = Integer.MIN_VALUE;
        LAST_OUTPUT.reset();
        LAST_ERROR.reset();
        EXTERNAL_OUTPUT_FILE = null;
        EXTERNAL_ERROR_FILE = null;

        if (!TEE_TO_ORIGINAL_STREAMS) {
            Path target = Path.of("target");
            try {
                LAST_OUTPUT_FILE = Files.createTempFile(target, "stdout", ".txt").toFile();
            } catch (IOException e) {
                LAST_OUTPUT_FILE = null;
            }
            try {
                LAST_ERROR_FILE = Files.createTempFile(target, "stderr", ".txt").toFile();
            } catch (IOException e) {
                LAST_ERROR_FILE = null;
            }
        }
    }

    /**
     * Tears down command test mode i.e. commands will now exit normally when invoked and won't record their parse
     * result and exit status for inspection.
     */
    public static void teardown() {
        SmartCacheCommand.TEST = false;
        resetTestState();

        // Restore original output streams
        System.setOut(ORIGINAL_OUTPUT);
        System.setOut(ORIGINAL_ERROR);
    }

    /**
     * Prints to the original standard output
     *
     * @param value Value
     */
    public static void printToOriginalStdOut(String value) {
        ORIGINAL_OUTPUT.println(value);
    }

    /**
     * Gets the last command parse result (if any)
     *
     * @param <T> Parse result
     * @return Parse result or {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T extends SmartCacheCommand> ParseResult<T> getLastParseResult() {
        if (SmartCacheCommand.LAST_PARSE_RESULT == null) {
            return null;
        } else {
            return (ParseResult<T>) SmartCacheCommand.LAST_PARSE_RESULT;
        }
    }

    /**
     * Gets the last command exit code or {@link Integer#MIN_VALUE} if none recorded
     *
     * @return Exit status or {@link Integer#MIN_VALUE}
     */
    public static int getLastExitStatus() {
        return SmartCacheCommand.LAST_EXIT_STATUS;
    }

    /**
     * Gets the standard output from the last command invocation
     *
     * @return Standard output
     */
    public static String getLastStdOut() {
        if (EXTERNAL_OUTPUT_FILE != null) {
            try {
                return Files.readString(EXTERNAL_OUTPUT_FILE.toPath());
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to read external command output file " + EXTERNAL_OUTPUT_FILE.getAbsolutePath(), e);
            }
        } else {
            return LAST_OUTPUT.toString(StandardCharsets.UTF_8);
        }
    }

    /**
     * Gets the standard error from the last command invocation
     *
     * @return Standard error
     */
    public static String getLastStdErr() {
        if (EXTERNAL_ERROR_FILE != null) {
            try {
                return Files.readString(EXTERNAL_ERROR_FILE.toPath());
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Failed to read external command output file " + EXTERNAL_ERROR_FILE.getAbsolutePath(), e);
            }
        } else {
            return LAST_ERROR.toString(StandardCharsets.UTF_8);
        }
    }

    /**
     * Runs a command to test as an external standalone process, this is useful for writing integration tests that
     * verify that the script entrypoint used for real deployments is functioning as intended and any necessary
     * dependencies are appropriately provided.
     * <p>
     * This returns the actual process, depending on the command being tested you may then want to pass this into
     * {@link #waitForExternalCommand(Process, long, TimeUnit)} to wait for it to complete, or to run tests against the
     * running application before terminating it yourself.
     * </p>
     *
     * @param program Program that should be invoked, typically a script file that is the entrypoint for the deployed
     *                application
     * @param envVars Any environment variables that need to be set for the program being tested to function correctly
     * @param args    Any arguments to supply to the program
     * @return The external command process
     * @throws IOException Thrown if the external command process cannot be started e.g. bad program path
     */
    public static Process runAsExternalCommand(String program, Map<String, String> envVars, String[] args) throws
            IOException {
        // Prepare the process command
        ProcessBuilder builder = new ProcessBuilder();
        List<String> command = new ArrayList<>();
        command.add(program);
        command.addAll(Arrays.asList(args));
        builder.command(command);

        // Explicitly redirect outputs to files, getLastStdOut() and getLastStdErr() will read these files when
        // appropriate
        EXTERNAL_OUTPUT_FILE = Files.createTempFile("external-stdout", ".txt").toFile();
        EXTERNAL_ERROR_FILE = Files.createTempFile("external-stderr", ".txt").toFile();
        builder.redirectOutput(EXTERNAL_OUTPUT_FILE);
        builder.redirectError(EXTERNAL_ERROR_FILE);

        // Customise runtime environment
        builder.environment().putAll(envVars);

        // Actually launch the command, printing what we're launching
        printToOriginalStdOut(
                "[" + Instant.now()
                             .toString() + "] Starting external command " + program + " with arguments:\n" + StringUtils.join(
                        args, "\n  "));
        return builder.start();
    }

    /**
     * Waits for the external command to complete, recording its exit status for later retrieval by
     * {@link #getLastExitStatus()}.  The process is destroyed after it has completed, or if it does not complete within
     * the given timeout duration.
     *
     * @param process Process
     * @param timeout Timeout duration
     * @param unit    Timeout time unit
     */
    public static void waitForExternalCommand(Process process, long timeout, TimeUnit unit) {
        Objects.requireNonNull(process);
        // Wait for completion
        try {
            printToOriginalStdOut("[" + Instant.now().toString() + "] Waiting for external command to complete...");
            process.waitFor(timeout, unit);
            if (process.isAlive()) {
                SmartCacheCommand.LAST_EXIT_STATUS = Integer.MAX_VALUE;
                printToOriginalStdOut(
                        "[" + Instant.now()
                                     .toString() + "] External command failed to finish within timeout (" + timeout + " " + unit.name() + ")");
            } else {
                SmartCacheCommand.LAST_EXIT_STATUS = process.exitValue();
                printToOriginalStdOut(
                        "[" + Instant.now()
                                     .toString() + "] External command completed with status " + SmartCacheCommand.LAST_EXIT_STATUS);
            }
        } catch (InterruptedException e) {
            printToOriginalStdOut(
                    "[" + Instant.now().toString() + "] Interrupted while waiting for external command to complete.");
            SmartCacheCommand.LAST_EXIT_STATUS = Integer.MAX_VALUE;
        } finally {
            process.destroy();
        }
    }

    /**
     * Detects the project version, this can be used to inject it into an external command as an environment variable
     * (e.g. when using {@link #runAsExternalCommand(String, Map, String[])})
     * <p>
     * Detection looks for a {@code project.version} System property, if that is not present then it tries to read the
     * {@code pom.xml} file in the working directory to find the first {@code <version>} element and return that value.
     * Generally it is better to use Maven configuration to inject the correct {@code project.version} value.
     * </p>
     * <p>
     * The detected project version is cached for the lifetime of the JVM.
     * </p>
     *
     * @return Detected project version, or {@code null} if unable to detect
     */
    public static String detectProjectVersion() {
        return PROJECT_VERSION.computeIfAbsent(() -> {
            // If injected via System Properties just return that
            if (System.getProperties().contains("project.version")) {
                return System.getProperties().getProperty("project.version");
            }

            // Otherwise read the first <version> element in the pom.xml
            File pom = new File("pom.xml");
            List<String> lines;
            try {
                lines = Files.readAllLines(pom.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read project pom.xml file", e);
            }
            for (String line : lines) {
                if (line.contains("<version>")) {
                    line = StringUtils.substringAfter(line, "<version>");
                    line = StringUtils.substringBefore(line, "</version>");
                    return StringUtils.trim(line);
                }
            }
            throw new IllegalStateException("Failed to detect project version");
        });
    }
}
