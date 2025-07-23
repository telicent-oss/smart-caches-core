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
package io.telicent.smart.cache.observability;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import org.apache.commons.lang3.Strings;

import static org.apache.commons.lang3.Strings.CI;
import static org.apache.commons.lang3.Strings.CS;

public class TestRuntimeInfo {

    @DataProvider(name = "memory")
    public Object[][] memoryData() {
        return new Object[][] {
                { 0d, "0.00", "KiB" },
                { 100d, "0.10", "KiB" },
                { 1023d, "1.00", "KiB" },
                { 838860d, "819.20", "KiB" },
                { 268435456d, "256.00", "MiB" },
                { 2147483648d, "2.00", "GiB" },
                { 1572864000d, "1.46", "GiB" },
                { 1099511627776d, "1.00", "TiB" },
                { 1649267441664d, "1.50", "TiB" }
        };
    }

    @Test(dataProvider = "memory")
    public void givenRawMemory_whenParsing_thenHumanReadableValueIsReturned(double raw, String expectedValue, String expectedUnit) {
        // Given
        Pair<Double, String> parsed = RuntimeInfo.parseMemory(raw);

        // When
        verifyMemoryAmount(parsed, expectedValue);

        // Then
        Assert.assertEquals(parsed.getValue(), expectedUnit);
    }

    private void verifyMemoryAmount(Pair<Double, String> memory, String expected) {
        String actual = String.format("%.2f", memory.getKey());
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void givenTestLogger_whenPrintingRuntimeInfo_thenExpectedLinesAreOutput() {
        // Given
        TestLogger logger = TestLoggerFactory.getTestLogger(TestRuntimeInfo.class);
        try {
            // When
            RuntimeInfo.printRuntimeInfo(logger);

            // Then
            List<String> messages = logger.getAllLoggingEvents().stream().map(LoggingEvent::getFormattedMessage).toList();
            verifyMatchingLogMessage(messages, "Processors:");
            verifyMatchingLogMessage(messages, "OS:");
            verifyMatchingLogMessage(messages, "Java:");
            verifyMatchingLogMessage(messages, "Memory:");
        } finally {
            logger.clearAll();
        }
    }

    private static void verifyMatchingLogMessage(List<String> messages, String searchSeq) {
        Assert.assertTrue(messages.stream().anyMatch(m -> CS.contains(m, searchSeq)));
    }

    @Test
    public void dump_library_versions_01() {
        LibraryVersion.resetCaches();
        TestLogger logger = TestLoggerFactory.getTestLogger(TestRuntimeInfo.class);
        try {
            RuntimeInfo.printLibraryVersions(logger);
            List<String> messages = logger.getAllLoggingEvents().stream().map(LoggingEvent::getFormattedMessage).toList();
            Assert.assertTrue(messages.isEmpty());
        } finally {
            logger.clearAll();
        }
    }

    @Test
    public void dump_library_versions_02() {
        LibraryVersion.resetCaches();
        LibraryVersion.get(TestLibraryVersion.OBSERVABILITY_CORE);
        TestLogger logger = TestLoggerFactory.getTestLogger(TestRuntimeInfo.class);
        try {
            RuntimeInfo.printLibraryVersions(logger);
            List<String> messages = logger.getAllLoggingEvents().stream().map(LoggingEvent::getFormattedMessage).toList();
            Assert.assertFalse(messages.isEmpty());
            Assert.assertTrue(messages.stream()
                                      .anyMatch(m -> CS.contains(m,
                                                                          TestLibraryVersion.OBSERVABILITY_CORE) && CS.contains(
                                              m, LibraryVersion.get(TestLibraryVersion.OBSERVABILITY_CORE))));
        } finally {
            logger.clearAll();
        }
    }
}
