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

public class TestDebugCli extends AbstractCommandTests {

    @Test
    public void debug_cli_01() {
        DebugCli.main(new String[0]);

        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        SmartCacheCommand command = result.getCommand();
        Assert.assertNotNull(command);
        Assert.assertTrue(command instanceof HelpCommand);

        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(StringUtils.isBlank(stdOut));
        Assert.assertTrue(StringUtils.contains(stdOut, "Commands are:"));
    }

    @Test
    public void debug_cli_file_event_formats_bad_01() throws IOException {
        File sourceFile = Files.createTempFile("event-source", ".yaml").toFile();
        sourceFile.deleteOnExit();
        DebugCli.main(
                new String[] {
                        "dump",
                        "--source-file",
                        sourceFile.getAbsolutePath(),
                        "--source-format",
                        "no-such-format"
                });

        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.wasSuccessful());

        List<ParseException> errors = new ArrayList<>(result.getErrors());
        Assert.assertFalse(errors.isEmpty());
        Assert.assertTrue(errors.stream()
                                .anyMatch(e -> StringUtils.contains(e.getMessage(),
                                                                    "not in the list of allowed values")));
    }

    @Test
    public void debug_cli_file_event_formats_bad_02() throws IOException {
        File sourceFile = Files.createTempFile("event-source", ".yaml").toFile();
        sourceFile.deleteOnExit();
        DebugCli.main(
                new String[] {
                        "dump",
                        "--source-file",
                        sourceFile.getAbsolutePath(),
                        "--source-format",
                        YamlFormat.NAME.toUpperCase(
                                Locale.ROOT)
                });

        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.wasSuccessful());

        List<ParseException> errors = new ArrayList<>(result.getErrors());
        Assert.assertFalse(errors.isEmpty());
        Assert.assertTrue(errors.stream()
                                .anyMatch(e -> StringUtils.contains(e.getMessage(),
                                                                    "not in the list of allowed values")));
    }

    @Test
    public void debug_cli_capture_bad_01() throws IOException {
        File directory = Files.createTempDirectory("event-source").toFile();
        DebugCli.main(
                new String[] {
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

        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        Assert.assertNotEquals(SmartCacheCommandTester.getLastExitStatus(), 0);

        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "Cannot specify the same"));
    }

    @Test
    public void debug_cli_capture_bad_02() throws IOException {
        File directory = Files.createTempDirectory("event-source").toFile();
        DebugCli.main(
                new String[] {
                        "capture",
                        "--source-dir",
                        directory.getAbsolutePath(),
                        "--source-format",
                        YamlFormat.NAME
                });

        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        Assert.assertNotEquals(SmartCacheCommandTester.getLastExitStatus(), 0);

        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "Failed to specify sufficient options"));
    }


    /*
    NB - Kafka requiring tests live in DockerTestDebugCli
     */

    @Test
    public void debug_dump_01() throws IOException {
        // Add some sample events to a directory
        File sourceDir = Files.createTempDirectory("dump-events-input").toFile();
        try (EventCapturingSink<String, String> sink = EventCapturingSink.<String, String>create()
                                                                         .directory(sourceDir)
                                                                         .extension(".yaml")
                                                                         .writeYaml(y -> y.keySerializer(
                                                                                                  new StringSerializer())
                                                                                          .valueSerializer(
                                                                                                  new StringSerializer()))
                                                                         .build()) {
            for (int i = 1; i <= 1_000; i++) {
                sink.send(
                        new SimpleEvent<>(Collections.emptyList(), Integer.toString(i), String.format("Event %,d", i)));
            }
        }

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

        DockerTestDebugCli.verifyDumpCommandUsed();

        verifyEventsDumped("Event %,d");
    }

    @Test
    public void debug_dump_02() throws IOException {
        // Add a single sample events to a file
        File sourceFile = Files.createTempFile("single-event", ".yaml").toFile();
        FileEventFormatProvider format = FileEventFormats.get(YamlFormat.NAME);
        FileEventWriter<String, String> writer = format.createWriter(new StringSerializer(), new StringSerializer());
        writer.write(new SimpleEvent<>(Collections.emptyList(), Integer.toString(1), "Event 1"), sourceFile);

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

        DockerTestDebugCli.verifyDumpCommandUsed();

        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertTrue(StringUtils.contains(stdOut, "Event 1"));
    }

    @Test
    public void debug_rdf_dump_01() throws IOException {
        // Add some sample events to a directory
        File sourceDir = Files.createTempDirectory("dump-events-input").toFile();
        try (EventCapturingSink<String, String> sink = EventCapturingSink.<String, String>create()
                                                                         .directory(sourceDir)
                                                                         .extension(".yaml")
                                                                         .writeYaml(y -> y.keySerializer(
                                                                                                  new StringSerializer())
                                                                                          .valueSerializer(
                                                                                                  new StringSerializer()))
                                                                         .build()) {
            for (int i = 1; i <= 1_000; i++) {
                sink.send(
                        new SimpleEvent<>(Collections.emptyList(), Integer.toString(i),
                                          String.format("<http://subject> <http://predicate> \"%d\" .", i)));
            }
        }

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

        DockerTestDebugCli.verifyRdfDumpCommandUsed();

        verifyEventsDumped("\"%d\"");
    }

    @Test
    public void debug_rdf_dump_02() throws IOException {
        // Add a single event to a file
        File sourceFile = Files.createTempFile("single-event", ".yaml").toFile();
        FileEventFormatProvider format = FileEventFormats.get(YamlFormat.NAME);
        FileEventWriter<String, String> writer = format.createWriter(new StringSerializer(), new StringSerializer());
        writer.write(
                new SimpleEvent<>(Collections.emptyList(), Integer.toString(1),
                                  "<http://subject> <http://predicate> \"1\" ."), sourceFile);


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

        DockerTestDebugCli.verifyRdfDumpCommandUsed();

        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertTrue(StringUtils.contains(stdOut, "\"1\""));
    }

    @Test
    public void debug_rdf_dump_capture_01() throws IOException {
        File sourceFile = Files.createTempFile("capture-source", ".nt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile))) {
            writer.write("<http://subject> <http://predicate> \"1\" .");
        }
        File captureDir = Files.createTempDirectory("capture-target").toFile();

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

        DockerTestDebugCli.verifyRdfDumpCommandUsed();
        verifyCapturedRdfEvent(captureDir, YamlFormat.NAME);
    }

    @Test
    public void debug_rdf_dump_capture_02() throws IOException {
        File sourceFile = Files.createTempFile("capture-source", ".nt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile))) {
            writer.write("<http://subject> <http://predicate> \"1\" .");
        }
        File captureDir = Files.createTempDirectory("capture-target").toFile();
        Assert.assertTrue(captureDir.delete());

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

        DockerTestDebugCli.verifyRdfDumpCommandUsed();
        verifyCapturedRdfEvent(captureDir, PlainTextFormat.NAME);
    }

    @Test
    public void debug_rdf_dump_capture_bad_01() throws IOException {
        String os = System.getProperty("os.name");
        if (StringUtils.contains(os, "Windows")) {
            throw new SkipException("Test not suitable for Windows");
        }

        File sourceFile = Files.createTempFile("capture-source", ".nt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sourceFile))) {
            writer.write("<http://subject> <http://predicate> \"1\" .");
        }
        File captureDir = new File("/should/not/be/able/to/create");

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

        ParseResult<SmartCacheCommand> result = SmartCacheCommandTester.getLastParseResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.wasSuccessful());
        Assert.assertNotEquals(SmartCacheCommandTester.getLastExitStatus(), 0);

        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertTrue(StringUtils.contains(stdErr, "Failed to create"));
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
    public void debug_dump_capture_transpose_01() throws IOException {
        // Add some sample events to a directory
        File sourceDir = Files.createTempDirectory("dump-events-input").toFile();
        try (EventCapturingSink<String, String> sink = EventCapturingSink.<String, String>create()
                                                                         .directory(sourceDir)
                                                                         .extension(".yaml")
                                                                         .writeYaml(y -> y.keySerializer(
                                                                                                  new StringSerializer())
                                                                                          .valueSerializer(
                                                                                                  new StringSerializer()))
                                                                         .build()) {
            for (int i = 1; i <= 1_000; i++) {
                sink.send(
                        new SimpleEvent<>(Collections.emptyList(), Integer.toString(i), String.format("Event %,d", i)));
            }
        }

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

        DockerTestDebugCli.verifyDumpCommandUsed();
        verifyEventsDumped("Event %,d");

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

        DockerTestDebugCli.verifyDumpCommandUsed();
        verifyEventsDumped("Event %,d");
    }

    private static void verifyEventsDumped(String format) {
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        for (int i = 1; i < 1_000; i++) {
            Assert.assertTrue(StringUtils.contains(stdOut, String.format(format, i)));
        }
    }
}
