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
}
