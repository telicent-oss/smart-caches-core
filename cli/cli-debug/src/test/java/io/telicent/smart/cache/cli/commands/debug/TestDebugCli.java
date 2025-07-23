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

import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.errors.ParseException;
import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.HelpCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.payloads.RdfPayload;
import io.telicent.smart.cache.projectors.sinks.events.file.EventCapturingSink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.file.FileEventFormatProvider;
import io.telicent.smart.cache.sources.file.FileEventFormats;
import io.telicent.smart.cache.sources.file.FileEventWriter;
import io.telicent.smart.cache.sources.file.rdf.RdfFormat;
import io.telicent.smart.cache.sources.file.text.PlainTextFormat;
import io.telicent.smart.cache.sources.file.yaml.YamlFormat;
import io.telicent.smart.cache.sources.kafka.serializers.RdfPayloadDeserializer;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang3.Strings.CS;

public class TestDebugCli extends AbstractCommandTests {

    @Test
    public void givenNoArgs_whenRunningDebugCli_thenHelpIsShown() {
        // Given and When
        DebugCli.main(new String[0]);

        // Then
        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        SmartCacheCommand command = result.getCommand();
        Assert.assertNotNull(command);
        Assert.assertTrue(command instanceof HelpCommand);
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(StringUtils.isBlank(stdOut));
        Assert.assertTrue(CS.contains(stdOut, "Commands are:"));
    }

    @Test
    public void givenUnrecognisedEventFormat_whenDumpingEvents_thenFails() throws IOException {
        // Given
        File sourceFile = createSourceFile();

        // When
        DebugCli.main(new String[] {
                "dump", "--source-file", sourceFile.getAbsolutePath(), "--source-format", "no-such-format"
        });

        // Then
        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.wasSuccessful());
        List<ParseException> errors = new ArrayList<>(result.getErrors());
        Assert.assertFalse(errors.isEmpty());
        Assert.assertTrue(errors.stream()
                                .anyMatch(e -> CS.contains(e.getMessage(),
                                                                    "not in the list of allowed values")));
    }

    private static File createSourceFile() throws IOException {
        File sourceFile = Files.createTempFile("event-source", ".yaml").toFile();
        sourceFile.deleteOnExit();
        return sourceFile;
    }

    @Test
    public void givenMistypedEventFormat_whenDumpingEvents_thenFails() throws IOException {
        // Given
        File sourceFile = createSourceFile();

        // When
        DebugCli.main(new String[] {
                "dump",
                "--source-file",
                sourceFile.getAbsolutePath(),
                "--source-format",
                YamlFormat.NAME.toUpperCase(Locale.ROOT)
        });

        // Then
        ParseResult<SmartCacheCommand> result = verifyFailedCommand(true);
        List<ParseException> errors = new ArrayList<>(result.getErrors());
        Assert.assertFalse(errors.isEmpty());
        Assert.assertTrue(errors.stream()
                                .anyMatch(e -> CS.contains(e.getMessage(),
                                                                    "not in the list of allowed values")));
    }

    @Test
    public void givenSameDirectoryForSourceAndCapture_whenDumpingEvents_thenFails() throws IOException {
        // Given and When
        File directory = Files.createTempDirectory("event-source").toFile();
        DebugCli.main(new String[] {
                "dump",
                "--source-dir",
                directory.getAbsolutePath(),
                "--source-format",
                YamlFormat.NAME,
                "--capture-dir",
                directory.getAbsolutePath(),
                "--capture-format",
                YamlFormat.NAME
        });

        // Then
        verifyFailedCommand(false);
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "Cannot specify the same"));
    }

    private static ParseResult<SmartCacheCommand> verifyFailedCommand(boolean isParseError) {
        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertEquals(result.wasSuccessful(), !isParseError);
        Assert.assertNotEquals(SmartCacheCommandTester.getLastExitStatus(), 0);
        return result;
    }

    @Test
    public void givenNoCaptureDirectory_whenCapturingEvents_thenFails() throws IOException {
        // Given and When
        File directory = Files.createTempDirectory("event-source").toFile();
        DebugCli.main(new String[] {
                "capture", "--source-dir", directory.getAbsolutePath(), "--source-format", YamlFormat.NAME
        });

        // Then
        verifyFailedCommand(false);
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "Failed to specify sufficient options"));
    }


    /*
    NB - Kafka requiring tests live in DockerTestDebugCli
     */

    @Test
    public void givenValidEventsDirectory_whenDumpingEvents_thenEventsAreDumped() throws IOException {
        // Given
        File sourceDir = Files.createTempDirectory("dump-events-input").toFile();
        generateSampleEvents(sourceDir, "Event %,d");

        // When
        DebugCli.main(new String[] {
                "dump",
                "--source-dir",
                sourceDir.getAbsolutePath(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        // Then
        AbstractDockerDebugCliTests.verifyDumpCommandUsed();
        verifyEventsDumped("Event %,d");
    }

    public static void generateSampleEvents(File sourceDir, String format) {
        try (EventCapturingSink<String, String> sink = EventCapturingSink.<String, String>create()
                                                                         .directory(sourceDir)
                                                                         .extension(".yaml")
                                                                         .writeYaml(y -> y.keySerializer(
                                                                                                  new StringSerializer())
                                                                                          .valueSerializer(
                                                                                                  new StringSerializer()))
                                                                         .build()) {
            for (int i = 1; i <= 1_000; i++) {
                sink.send(new SimpleEvent<>(Collections.emptyList(), Integer.toString(i), String.format(format, i)));
            }
        }
    }

    @Test
    public void givenSingleEventFile_whenDumpingEvents_thenEventIsDumped() throws IOException {
        // Given
        File sourceFile = Files.createTempFile("single-event", ".yaml").toFile();
        FileEventFormatProvider format = FileEventFormats.get(YamlFormat.NAME);
        FileEventWriter<String, String> writer = format.createWriter(new StringSerializer(), new StringSerializer());
        writer.write(new SimpleEvent<>(Collections.emptyList(), Integer.toString(1), "Event 1"), sourceFile);

        // When
        DebugCli.main(new String[] {
                "dump",
                "--source-file",
                sourceFile.getAbsolutePath(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        // Then
        AbstractDockerDebugCliTests.verifyDumpCommandUsed();
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertTrue(CS.contains(stdOut, "Event 1"));
    }

    @Test
    public void givenValidEventsDirectory_whenDumpingRdf_thenRdfIsDumped() throws IOException {
        // Given
        File sourceDir = Files.createTempDirectory("dump-events-input").toFile();
        generateSampleEvents(sourceDir, "<https://subject> <https://predicate> \"%d\" .");

        // When
        DebugCli.main(new String[] {
                "rdf-dump",
                "--source-dir",
                sourceDir.getAbsolutePath(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        // Then
        AbstractDockerDebugCliTests.verifyRdfDumpCommandUsed();
        verifyEventsDumped("\"%d\"");
    }

    @Test
    public void givenSingleRdfFile_whenDumpingRdf_thenRdfIsDumped() throws IOException {
        // Given
        File sourceFile = Files.createTempFile("single-event", ".yaml").toFile();
        FileEventFormatProvider format = FileEventFormats.get(YamlFormat.NAME);
        FileEventWriter<String, String> writer = format.createWriter(new StringSerializer(), new StringSerializer());
        writer.write(new SimpleEvent<>(Collections.emptyList(), Integer.toString(1),
                                       "<https://subject> <https://predicate> \"1\" ."), sourceFile);

        // When
        DebugCli.main(new String[] {
                "rdf-dump",
                "--source-file",
                sourceFile.getAbsolutePath(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                "--read-policy",
                "BEGINNING"
        });

        // Then
        AbstractDockerDebugCliTests.verifyRdfDumpCommandUsed();
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertTrue(CS.contains(stdOut, "\"1\""));
    }

    @Test
    public void givenSingleRdfFile_whenDumpingRdfWithCapture_thenRdfIsCaptured() throws IOException {
        // Given
        File sourceFile = Files.createTempFile("capture-source", ".nt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile))) {
            writer.write("<https://subject> <https://predicate> \"1\" .");
        }
        File captureDir = Files.createTempDirectory("capture-target").toFile();

        // When
        DebugCli.main(new String[] {
                "rdf-dump",
                "--source-file",
                sourceFile.getAbsolutePath(),
                "--source-format",
                RdfFormat.NAME,
                "--capture-dir",
                captureDir.getAbsolutePath(),
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                });

        // Then
        AbstractDockerDebugCliTests.verifyRdfDumpCommandUsed();
        verifyCapturedRdfEvent(captureDir, YamlFormat.NAME);
    }

    @Test
    public void givenSingleRdfFile_whenDumpingRdfWithPlainTextCapture_thenRdfIsCaptured() throws IOException {
        // Given
        File sourceFile = Files.createTempFile("capture-source", ".nt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile))) {
            writer.write("<http://subject> <http://predicate> \"1\" .");
        }
        File captureDir = Files.createTempDirectory("capture-target").toFile();
        Assert.assertTrue(captureDir.delete());

        // When
        DebugCli.main(new String[] {
                "rdf-dump",
                "--source-file",
                sourceFile.getAbsolutePath(),
                "--source-format",
                RdfFormat.NAME,
                "--capture-dir",
                captureDir.getAbsolutePath(),
                "--capture-format",
                PlainTextFormat.NAME,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                });

        // Then
        AbstractDockerDebugCliTests.verifyRdfDumpCommandUsed();
        verifyCapturedRdfEvent(captureDir, PlainTextFormat.NAME);
    }

    @Test
    public void givenSingleRdfFile_whenCapturingRdfToInvalidPath_thenFails() throws IOException {
        // Given
        String os = System.getProperty("os.name");
        if (CS.contains(os, "Windows")) {
            throw new SkipException("Test not suitable for Windows");
        }

        File sourceFile = Files.createTempFile("capture-source", ".nt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile))) {
            writer.write("<https://subject> <https://predicate> \"1\" .");
        }
        File captureDir = new File("/should/not/be/able/to/create");

        // When
        DebugCli.main(new String[] {
                "rdf-dump",
                "--source-file",
                sourceFile.getAbsolutePath(),
                "--source-format",
                RdfFormat.NAME,
                "--capture-dir",
                captureDir.getAbsolutePath(),
                "--capture-format",
                PlainTextFormat.NAME,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                });

        // Then
        verifyFailedCommand(false);
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(CS.contains(stdErr, "Failed to create"));
    }

    private static void verifyCapturedRdfEvent(File captureDir, String captureFormat) {
        FileEventFormatProvider provider = FileEventFormats.get(captureFormat);
        EventSource<Bytes, RdfPayload> source =
                provider.createSource(new BytesDeserializer(), new RdfPayloadDeserializer(), captureDir);
        Assert.assertEquals(source.remaining(), 1);

        Event<Bytes, RdfPayload> event = source.poll(Duration.ofSeconds(1));
        Assert.assertNotNull(event);
        Assert.assertNull(event.key());
        Assert.assertNotNull(event.value());
        Assert.assertEquals(event.lastHeader(HttpNames.hContentType), WebContent.contentTypeNQuads);

        RdfPayload payload = event.value();
        Assert.assertTrue(payload.isDataset());
        Assert.assertNotNull(payload.getDataset());

        Assert.assertTrue(source.isExhausted());
    }

    @Test
    public void givenYamlEvents_whenCapturingInPlainTextFormat_thenDumpingPlainTextEventsSucceeds() throws IOException {
        // Given
        File sourceDir = Files.createTempDirectory("dump-events-input").toFile();
        generateSampleEvents(sourceDir, "Event %,d");

        // When
        DebugCli.main(new String[] {
                "dump",
                "--source-dir",
                sourceDir.getAbsolutePath(),
                "--source-format",
                YamlFormat.NAME,
                "--capture-dir",
                sourceDir.getAbsolutePath(),
                "--capture-format",
                PlainTextFormat.NAME,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                });
        AbstractDockerDebugCliTests.verifyDumpCommandUsed();
        verifyEventsDumped("Event %,d");

        // Then
        SmartCacheCommandTester.resetTestState();
        DebugCli.main(new String[] {
                "dump",
                "--source-dir",
                sourceDir.getAbsolutePath(),
                "--source-format",
                PlainTextFormat.NAME,
                "--max-stalls",
                "1",
                "--poll-timeout",
                "3",
                });
        AbstractDockerDebugCliTests.verifyDumpCommandUsed();
        verifyEventsDumped("Event %,d");
    }

    public static void verifyEventsDumped(String format) {
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        for (int i = 1; i < 1_000; i++) {
            Assert.assertTrue(CS.contains(stdOut, String.format(format, i)));
        }
    }

}
