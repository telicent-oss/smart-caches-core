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

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

public class AbstractCommandTests {
    @BeforeClass
    public void setup() {
        SmartCacheCommandTester.setup();
    }

    @AfterMethod
    public void testCleanup() throws InterruptedException {
        SmartCacheCommandTester.resetTestState();
    }

    @AfterClass
    public void teardown() {
        SmartCacheCommandTester.teardown();
    }
}
