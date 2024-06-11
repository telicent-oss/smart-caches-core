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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

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
    public void parse_memory(double raw, String expectedValue, String expectedUnit) {
        Pair<Double, String> parsed = RuntimeInfo.parseMemory(raw);
        verifyMemoryAmount(parsed, expectedValue);
        Assert.assertEquals(parsed.getValue(), expectedUnit);
    }

    private void verifyMemoryAmount(Pair<Double, String> memory, String expected) {
        String actual = String.format("%.2f", memory.getKey());
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void dump_runtime_info() {
        TestLogger logger = TestLoggerFactory.getTestLogger(TestRuntimeInfo.class);
        try {
            RuntimeInfo.printRuntimeInfo(logger);

            List<String> messages = logger.getAllLoggingEvents().stream().map(LoggingEvent::getFormattedMessage).toList();
            Assert.assertTrue(messages.stream().anyMatch(m -> StringUtils.contains(m, "OS:")));
            Assert.assertTrue(messages.stream().anyMatch(m -> StringUtils.contains(m, "Java:")));
            Assert.assertTrue(messages.stream().anyMatch(m -> StringUtils.contains(m, "Memory:")));
        } finally {
            logger.clearAll();
        }
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
                                      .anyMatch(m -> StringUtils.contains(m,
                                                                          TestLibraryVersion.OBSERVABILITY_CORE) && StringUtils.contains(
                                              m, LibraryVersion.get(TestLibraryVersion.OBSERVABILITY_CORE))));
        } finally {
            logger.clearAll();
        }
    }
}
