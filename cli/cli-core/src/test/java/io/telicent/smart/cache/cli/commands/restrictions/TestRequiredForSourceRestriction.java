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
package io.telicent.smart.cache.cli.commands.restrictions;

import com.github.rvesse.airline.help.sections.HelpFormat;
import io.telicent.smart.cache.cli.restrictions.RequiredForSourceRestriction;
import org.apache.commons.lang3.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public class TestRequiredForSourceRestriction {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Source name.*null.*")
    public void givenNoSourceName_whenCreatingRestriction_thenFails() {
        // Given, When and Then
        new RequiredForSourceRestriction(null);
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void givenRestriction_whenGettingBadContentBlock_thenIndexOutOfBounds() {
        // Given
        RequiredForSourceRestriction restriction = new RequiredForSourceRestriction("test");

        // When and Then
        restriction.getContentBlock(1);
    }

    @Test
    public void givenNoEnvVars_whenProducingHelp_thenNoPreamble() {
        // Given
        RequiredForSourceRestriction restriction = new RequiredForSourceRestriction("test");

        // When and Then
        Assert.assertNull(restriction.getPreamble());
        Assert.assertEquals(restriction.getFormat(), HelpFormat.PROSE);
    }

    @Test
    public void givenNoEnvVars_whenProducingContentBlocks_thenNoEnvVarsListed() {
        // Given
        RequiredForSourceRestriction restriction = new RequiredForSourceRestriction("test");

        // When
        String[] block = restriction.getContentBlock(0);

        // Then
        Assert.assertEquals(block.length, 1);
        Assert.assertTrue(Strings.CI.contains(block[0], "is required when the test event source is used"));
    }

    @Test
    public void givenEnvVars_whenProducingHelp_thenEnvVarsListed() {
        // Given
        RequiredForSourceRestriction restriction = new RequiredForSourceRestriction("test", "FOO", "BAR");

        // When
        String preamble = restriction.getPreamble();
        String[] block = restriction.getContentBlock(0);

        // Then
        Assert.assertEquals(restriction.getFormat(), HelpFormat.LIST);
        Assert.assertNotNull(preamble);
        Assert.assertTrue(Strings.CI.contains(preamble, "is required when the test event source is in use"));
        Assert.assertEquals(block.length, 2);
        Arrays.sort(block);
        Assert.assertEquals(block, new String[] { "BAR", "FOO"});
    }
}
