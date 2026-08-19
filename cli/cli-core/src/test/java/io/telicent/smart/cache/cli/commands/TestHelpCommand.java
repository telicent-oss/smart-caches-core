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

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Group;
import com.github.rvesse.airline.builder.ParserBuilder;
import com.github.rvesse.airline.parser.ParseResult;
import io.telicent.smart.cache.cli.commands.backup.BackupCircuitBreaker;
import io.telicent.smart.cache.cli.commands.backup.BackupPrimary;
import io.telicent.smart.cache.cli.commands.backup.BackupSecondary;
import io.telicent.smart.cache.cli.commands.distributions.DistLifecycleTracker;
import io.telicent.smart.cache.cli.commands.projection.AsIsProjectionCommand;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.apache.commons.lang3.Strings.CS;

public class TestHelpCommand extends AbstractCommandTests {

    @Test
    public void help_01() {
        HelpCommand.main(new String[] { "--no-runtime-info" });
        ParseResult<HelpCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        Assert.assertFalse(result.getCommand().includeHidden);
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
        Assert.assertTrue(StringUtils.isBlank(SmartCacheCommandTester.getLastStdOut()));
        String lastStdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertFalse(StringUtils.isBlank(lastStdErr));
        Assert.assertTrue(CS.contains(lastStdErr.trim(), HelpCommand.UNABLE_TO_SHOW_HELP));
    }

    @Test
    public void help_02() {
        HelpCommand.main(new String[] { "--no-runtime-info", "--include-hidden" });
        ParseResult<HelpCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        Assert.assertTrue(result.getCommand().includeHidden);
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
        Assert.assertTrue(StringUtils.isBlank(SmartCacheCommandTester.getLastStdOut()));
        String lastStdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertFalse(StringUtils.isBlank(lastStdErr));
        Assert.assertTrue(CS.contains(lastStdErr.trim(), HelpCommand.UNABLE_TO_SHOW_HELP));
    }

    @Cli(name = "test",
            groups = {
                    @Group(name = "backup", commands = {
                            BackupPrimary.class,
                            BackupSecondary.class,
                            BackupCircuitBreaker.class
                    }),
                    @Group(name = "intermediate backup", commands = {
                            BackupPrimary.class,
                            BackupSecondary.class,
                            BackupCircuitBreaker.class
                    })
            },
            commands = {
                    AsIsProjectionCommand.class,
                    HelpCommand.class,
                    DistLifecycleTracker.class
            },
            defaultCommand = HelpCommand.class)
    private static final class ExampleCli {

    }

    private static final String[] TOP_LEVEL_COMMANDS = { "project", "dist-lifecycle-tracker", "help", "backup", "intermediate" };

    private static final String[] BACKUP_GROUP_COMMANDS = { "primary", "secondary", "circuit-breaker" };

    @Test
    public void givenCli_whenNoCommand_thenTopLevelHelpShown() {
        // Given
        com.github.rvesse.airline.Cli<SmartCacheCommand> cli = new com.github.rvesse.airline.Cli<>(ExampleCli.class);

        // When
        ParseResult<SmartCacheCommand> result = cli.parseWithResult();
        SmartCacheCommand.handleParseResult(result);

        // Then
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        verifyCommandsListed(stdOut, TOP_LEVEL_COMMANDS);
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
    }

    @Test
    public void givenCli_whenHelpWithGroupName_thenGroupLevelHelpShown() {
        // Given
        com.github.rvesse.airline.Cli<SmartCacheCommand> cli = new com.github.rvesse.airline.Cli<>(ExampleCli.class);

        // When
        ParseResult<SmartCacheCommand> result = cli.parseWithResult("help", "backup");
        SmartCacheCommand.handleParseResult(result);

        // Then
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        verifyCommandsListed(stdOut, BACKUP_GROUP_COMMANDS);
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
    }

    private static void verifyCommandsListed(String stdOut, String[] backupGroupCommands) {
        for (String expected : backupGroupCommands) {
            Assert.assertTrue(CS.contains(stdOut, expected), "Expected help to list command " + expected);
        }
    }

    @Test
    public void givenCli_whenHelpWithIntermediateGroupName_thenGroupSummaryHelpShown() {
        // Given
        com.github.rvesse.airline.Cli<SmartCacheCommand> cli = new com.github.rvesse.airline.Cli<>(ExampleCli.class);

        // When
        ParseResult<SmartCacheCommand> result = cli.parseWithResult("help", "intermediate");
        SmartCacheCommand.handleParseResult(result);

        // Then
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        verifyCommandsListed(stdOut, BACKUP_GROUP_COMMANDS);
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
    }

    @Test
    public void givenCli_whenHelpWithMultipleGroupNames_thenGroupSummaryHelpShown() {
        // Given
        com.github.rvesse.airline.Cli<SmartCacheCommand> cli = new com.github.rvesse.airline.Cli<>(ExampleCli.class);

        // When
        ParseResult<SmartCacheCommand> result = cli.parseWithResult("help", "intermediate", "backup");
        SmartCacheCommand.handleParseResult(result);

        // Then
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        verifyCommandsListed(stdOut, BACKUP_GROUP_COMMANDS);
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
    }

    @Test
    public void givenCli_whenHelpWithGroupAndCommandName_thenCommandLevelHelpShown() {
        // Given
        com.github.rvesse.airline.Cli<SmartCacheCommand> cli = new com.github.rvesse.airline.Cli<>(ExampleCli.class);

        // When
        ParseResult<SmartCacheCommand> result = cli.parseWithResult("help", "backup", "primary");
        SmartCacheCommand.handleParseResult(result);

        // Then
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        Assert.assertTrue(CS.contains(stdOut, BackupPrimary.DESCRIPTION));
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
    }

    @Test
    public void givenCli_whenHelpWithGroupAndUnknownCommandName_thenCommandLevelHelpShown() {
        // Given
        com.github.rvesse.airline.Cli<SmartCacheCommand> cli = new com.github.rvesse.airline.Cli<>(ExampleCli.class);

        // When
        ParseResult<SmartCacheCommand> result = cli.parseWithResult("help", "backup", "unknown");
        SmartCacheCommand.handleParseResult(result);

        // Then
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        verifyCommandsListed(stdOut, BACKUP_GROUP_COMMANDS);
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
    }

    @Test
    public void givenCli_whenHelpWithAbbreviatedGroupAndCommandName_thenCommandLevelHelpShown() {
        // Given
        com.github.rvesse.airline.Cli<SmartCacheCommand> cli = new com.github.rvesse.airline.Cli<>(ExampleCli.class,
                                                                                                   new ParserBuilder<SmartCacheCommand>().withCommandAbbreviation()
                                                                                                                                         .build());

        // When
        ParseResult<SmartCacheCommand> result = cli.parseWithResult("help", "back", "pri");
        SmartCacheCommand.handleParseResult(result);

        // Then
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        Assert.assertTrue(CS.contains(stdOut, BackupPrimary.DESCRIPTION));
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
    }

    @Test
    public void givenCli_whenHelpWithTopLevelCommandName_thenCommandLevelHelpShown() {
        // Given
        com.github.rvesse.airline.Cli<SmartCacheCommand> cli = new com.github.rvesse.airline.Cli<>(ExampleCli.class);

        // When
        ParseResult<SmartCacheCommand> result = cli.parseWithResult("help", "project");
        SmartCacheCommand.handleParseResult(result);

        // Then
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        Assert.assertTrue(CS.contains(stdOut, AsIsProjectionCommand.DESCRIPTION));
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 2);
    }
}
