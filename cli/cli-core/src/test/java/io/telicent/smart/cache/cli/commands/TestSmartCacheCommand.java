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
import com.github.rvesse.airline.parser.errors.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSmartCacheCommand extends AbstractCommandTests {

    @Test
    public void givenFailedParseResult_whenHandling_thenErrorsPrinted() {
        // Given
        ParseResult<SmartCacheCommand> result = mock(ParseResult.class);
        when(result.wasSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(List.of(new ParseException("test")));

        // When
        SmartCacheCommand.handleParseResult(result);

        // Then
        Assert.assertFalse(SmartCacheCommandTester.getLastStdErr().isEmpty());
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 127);
    }
}
