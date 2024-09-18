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

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Parser;
import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.errors.handlers.CollectAll;
import com.github.rvesse.airline.parser.options.MaybePairValueOptionParser;
import io.telicent.smart.cache.cli.commands.HelpCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.commands.projection.debug.Capture;
import io.telicent.smart.cache.cli.commands.projection.debug.Dump;
import io.telicent.smart.cache.cli.commands.projection.debug.RdfDump;
import io.telicent.smart.cache.cli.commands.projection.debug.Replay;

/**
 * A CLI that provides debug tools
 */
//@formatter:off
@Cli(
        name = "debug",
        description = "Provides commands for debugging Smart Cache pipelines.",
        defaultCommand = HelpCommand.class,
        commands = {
                HelpCommand.class,
                Dump.class,
                FakeReporter.class,
                RdfDump.class,
                Capture.class,
                Replay.class
        },
        parserConfiguration =
        @Parser(flagNegationPrefix = "--no-",
                errorHandler = CollectAll.class,
                useDefaultOptionParsers = true,
                optionParsers = {
                MaybePairValueOptionParser.class
        })
)
//@formatter:on
public class DebugCli {
    /**
     * Private constructor prevents instantiation
     */
    private DebugCli() {

    }

    /**
     * Entrypoint for the debug CLI
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        com.github.rvesse.airline.Cli<SmartCacheCommand> cli =
                new com.github.rvesse.airline.Cli<>(DebugCli.class);
        ParseResult<SmartCacheCommand> result = cli.parseWithResult(args);

        SmartCacheCommand.handleParseResult(result);
    }
}
