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

import io.telicent.smart.cache.cli.commands.AbstractCommandTests;

import static io.telicent.smart.cache.cli.options.LoggingCommand.*;
import static org.apache.commons.lang3.Strings.CS;

import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class TestLoggerCommand extends AbstractCommandTests {
    @AfterMethod
    @Override
    public void testCleanup() {
        super.testCleanup();

        LoggingOptions.resetLogging();
    }

    private void verifyStdErr(String[] expected, String[] unexpected) {
        String stdErr = SmartCacheCommandTester.getLastStdErr();

        for (String message : expected) {
            Assert.assertTrue(CS.contains(stdErr, message),
                              "Standard error missing expected message " + message);
        }

        for (String message : unexpected) {
            Assert.assertFalse(CS.contains(stdErr, message),
                               "Standard error contains unexpected message " + message);
        }
    }

    @Test
    public void logging_default() {
        LoggingCommand.main(new String[0]);

        verifyStdErr(new String[] { ERROR_MESSAGE, WARNING_MESSAGE, INFORMATION_MESSAGE },
                     new String[] { DEBUGGING_MESSAGE, TRACING_MESSAGE });
    }

    @Test
    public void logging_quiet_01() {
        LoggingCommand.main(new String[] { "--quiet" });

        // Runtime info is displayed before log level gets reconfigured so should still be present even with --quiet
        verifyStdErr(new String[] { ERROR_MESSAGE, WARNING_MESSAGE, "set to WARN level", "Memory:", "OS:", "Java:" },
                     new String[] { INFORMATION_MESSAGE, DEBUGGING_MESSAGE, TRACING_MESSAGE });
    }

    @Test
    public void logging_quiet_02() {
        LoggingCommand.main(new String[] { "--quiet", "--no-runtime-info" });

        verifyStdErr(new String[] { ERROR_MESSAGE, WARNING_MESSAGE, "set to WARN level", },
                     // Explicitly disabled runtime info so should not be present
                     new String[] {
                             INFORMATION_MESSAGE,
                             DEBUGGING_MESSAGE,
                             TRACING_MESSAGE,
                             "Memory:",
                             "OS:",
                             "Java:"
                     });
    }

    @Test
    public void logging_verbose() {
        LoggingCommand.main(new String[] { "--verbose" });

        verifyStdErr(new String[] {
                ERROR_MESSAGE, WARNING_MESSAGE, INFORMATION_MESSAGE, DEBUGGING_MESSAGE, "set to DEBUG level"
        }, new String[] { TRACING_MESSAGE });
    }

    @Test
    public void logging_trace() {
        LoggingCommand.main(new String[] { "--trace" });

        verifyStdErr(new String[] {
                ERROR_MESSAGE,
                WARNING_MESSAGE,
                INFORMATION_MESSAGE,
                DEBUGGING_MESSAGE,
                TRACING_MESSAGE,
                "set to TRACE level"
        }, new String[0]);
    }
}
