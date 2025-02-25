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

import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DebugCliIT extends AbstractCommandTests {

    public static final int DEFAULT_TIMEOUT = 30;

    @Test
    public void givenValidEventsDirectory_whenDumpingAndCapturingRdfAsExternalCommand_thenRdfIsDumped_andCaptureCanBeDumped() throws
            IOException {
        // Given
        File sourceDir = Files.createTempDirectory("dump-events-input").toFile();
        TestDebugCli.generateSampleEvents(sourceDir, "<https://subject> <https://predicate> \"%d\" .");
        File captureDir = Files.createTempDirectory("capture-target").toFile();
        File debugScript = new File("debug.sh");
        Map<String, String> env = new HashMap<>();
        env.put("PROJECT_VERSION", SmartCacheCommandTester.detectProjectVersion());

        // When
        Process process =
                SmartCacheCommandTester.runAsExternalCommand(debugScript.getAbsolutePath(), env, new String[] {
                        "rdf-dump",
                        "--source-dir",
                        sourceDir.getAbsolutePath(),
                        "--max-stalls",
                        "1",
                        "--poll-timeout",
                        "3",
                        "--read-policy",
                        "BEGINNING",
                        "--capture-dir",
                        captureDir.getAbsolutePath()
                });
        SmartCacheCommandTester.waitForExternalCommand(process, DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        TestDebugCli.verifyEventsDumped("\"%d\"");

        // And
        SmartCacheCommandTester.resetTestState();
        process = SmartCacheCommandTester.runAsExternalCommand(debugScript.getAbsolutePath(), env, new String[] {
                "dump",
                "--source-dir",
                captureDir.getAbsolutePath(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });
        SmartCacheCommandTester.waitForExternalCommand(process, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        TestDebugCli.verifyEventsDumped("\"%d\"");
    }
}
