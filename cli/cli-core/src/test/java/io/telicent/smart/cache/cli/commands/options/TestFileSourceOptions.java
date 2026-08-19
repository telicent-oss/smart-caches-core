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
package io.telicent.smart.cache.cli.commands.options;

import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.builder.ParserBuilder;
import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.errors.handlers.CollectAll;
import io.telicent.smart.cache.cli.commands.AbstractCommandTests;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.commands.SmartCacheCommandTester;
import io.telicent.smart.cache.sources.file.FileEventFormats;
import io.telicent.smart.cache.sources.file.rdf.RdfEventReaderWriter;
import io.telicent.smart.cache.sources.file.rdf.RdfFormat;
import io.telicent.smart.cache.sources.file.text.PlainTextEventReaderWriter;
import io.telicent.smart.cache.sources.file.text.PlainTextFormat;
import io.telicent.smart.cache.sources.file.yaml.YamlEventReaderWriter;
import io.telicent.smart.cache.sources.file.yaml.YamlFormat;
import org.apache.commons.lang3.Strings;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TestFileSourceOptions extends AbstractCommandTests {

    @Test
    public void givenFileSourceOptions_whenParsingWithNoOptions_thenFails() {
        // Given
        SingleCommand<FileSource> command = SingleCommand.singleCommand(FileSource.class,
                                                                        new ParserBuilder<FileSource>().withErrorHandler(
                                                                                new CollectAll()).build());

        // When
        ParseResult<FileSource> result = command.parseWithResult();

        // Then
        Assert.assertFalse(result.wasSuccessful());
    }

    @Test
    public void givenFileSourceOptions_whenSourceDirectorySpecified_thenOk() throws IOException {
        // Given
        SingleCommand<FileSource> command = SingleCommand.singleCommand(FileSource.class);
        File tempDir = Files.createTempDirectory("source").toFile();

        // When
        ParseResult<FileSource> result = command.parseWithResult("--source-directory", tempDir.getAbsolutePath());
        SmartCacheCommand.handleParseResult(result);

        // Then
        Assert.assertTrue(result.wasSuccessful());
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        Assert.assertTrue(Strings.CI.contains(stdOut, "Use File Source? true"));
        Assert.assertTrue(Strings.CI.contains(stdOut, "Using File Capture? false"));
        Assert.assertTrue(Strings.CI.contains(stdOut, YamlEventReaderWriter.class.getSimpleName()));
        Assert.assertTrue(Strings.CI.contains(stdOut, tempDir.getAbsolutePath()));
    }

    @Test
    public void givenFileSourceOptions_whenSourceFileAndFormatSpecified_thenOk() throws IOException {
        // Given
        SingleCommand<FileSource> command = SingleCommand.singleCommand(FileSource.class);
        File tempFile = Files.createTempFile("source", ".ttl").toFile();

        // When
        ParseResult<FileSource> result =
                command.parseWithResult("--source-file", tempFile.getAbsolutePath(), "--source-format", RdfFormat.NAME);
        SmartCacheCommand.handleParseResult(result);

        // Then
        Assert.assertTrue(result.wasSuccessful());
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        Assert.assertTrue(Strings.CI.contains(stdOut, "Use File Source? true"));
        Assert.assertTrue(Strings.CI.contains(stdOut, "Using File Capture? false"));
        Assert.assertTrue(Strings.CI.contains(stdOut, RdfEventReaderWriter.class.getSimpleName()));
        Assert.assertTrue(Strings.CI.contains(stdOut, tempFile.getParentFile().getAbsolutePath()));
    }

    @Test
    public void givenFileSourceOptions_whenSourceAndCaptureDirectorySpecified_thenOk() throws IOException {
        // Given
        SingleCommand<FileSource> command = SingleCommand.singleCommand(FileSource.class);
        File sourceDir = Files.createTempDirectory("source").toFile();
        File captureDir = Files.createTempDirectory("capture").toFile();
        captureDir.delete();

        // When
        ParseResult<FileSource> result =
                command.parseWithResult("--source-directory", sourceDir.getAbsolutePath(), "--capture-directory",
                                        captureDir.getAbsolutePath());
        SmartCacheCommand.handleParseResult(result);

        // Then
        Assert.assertTrue(result.wasSuccessful());
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        Assert.assertTrue(Strings.CI.contains(stdOut, "Use File Source? true"));
        Assert.assertTrue(Strings.CI.contains(stdOut, "Using File Capture? true"));
        Assert.assertTrue(Strings.CI.contains(stdOut, YamlEventReaderWriter.class.getSimpleName()));
        Assert.assertTrue(Strings.CI.contains(stdOut, sourceDir.getAbsolutePath()));
        Assert.assertTrue(Strings.CI.contains(stdOut, captureDir.getAbsolutePath()));
    }

    @Test
    public void givenFileSourceOptions_whenSourceAndCaptureDirectorySpecifiedAsSame_thenFails() throws IOException {
        // Given
        SingleCommand<FileSource> command = SingleCommand.singleCommand(FileSource.class);
        File sourceDir = Files.createTempDirectory("source").toFile();

        // When
        ParseResult<FileSource> result =
                command.parseWithResult("--source-directory", sourceDir.getAbsolutePath(), "--capture-directory",
                                        sourceDir.getAbsolutePath());
        SmartCacheCommand.handleParseResult(result);

        // Then
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 1);
    }

    @Test
    public void givenFileSourceOptions_whenSourceAndCaptureDirectorySpecifiedAsSameWithDifferentFormats_thenOk() throws
            IOException {
        // Given
        SingleCommand<FileSource> command = SingleCommand.singleCommand(FileSource.class);
        File sourceDir = Files.createTempDirectory("source").toFile();
        File captureDir = Files.createTempDirectory("capture").toFile();
        captureDir.delete();

        // When
        ParseResult<FileSource> result =
                command.parseWithResult("--source-directory", sourceDir.getAbsolutePath(), "--capture-directory",
                                        captureDir.getAbsolutePath(), "--capture-format", PlainTextFormat.NAME);
        SmartCacheCommand.handleParseResult(result);

        // Then
        Assert.assertTrue(result.wasSuccessful());
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        Assert.assertTrue(Strings.CI.contains(stdOut, "Use File Source? true"));
        Assert.assertTrue(Strings.CI.contains(stdOut, "Using File Capture? true"));
        Assert.assertTrue(Strings.CI.contains(stdOut, YamlEventReaderWriter.class.getSimpleName()));
        Assert.assertTrue(Strings.CI.contains(stdOut, PlainTextEventReaderWriter.class.getSimpleName()));
        Assert.assertTrue(Strings.CI.contains(stdOut, sourceDir.getAbsolutePath()));
        Assert.assertTrue(Strings.CI.contains(stdOut, captureDir.getAbsolutePath()));
    }

    @Test
    public void givenFileSourceOptions_whenSourceAndCaptureDirectorySpecifiedWithFormats_thenOk() throws IOException {
        // Given
        SingleCommand<FileSource> command = SingleCommand.singleCommand(FileSource.class);
        File sourceDir = Files.createTempDirectory("source").toFile();
        File captureDir = Files.createTempDirectory("capture").toFile();

        // When
        ParseResult<FileSource> result =
                command.parseWithResult("--source-directory", sourceDir.getAbsolutePath(), "--source-format",
                                        YamlFormat.NAME, "--capture-directory", captureDir.getAbsolutePath(),
                                        "--capture-format", PlainTextFormat.NAME);
        SmartCacheCommand.handleParseResult(result);

        // Then
        Assert.assertTrue(result.wasSuccessful());
        String stdOut = SmartCacheCommandTester.getLastStdOut();
        Assert.assertFalse(stdOut.isEmpty());
        Assert.assertTrue(Strings.CI.contains(stdOut, "Use File Source? true"));
        Assert.assertTrue(Strings.CI.contains(stdOut, "Using File Capture? true"));
        Assert.assertTrue(Strings.CI.contains(stdOut, YamlEventReaderWriter.class.getSimpleName()));
        Assert.assertTrue(Strings.CI.contains(stdOut, sourceDir.getAbsolutePath()));
        Assert.assertTrue(Strings.CI.contains(stdOut, captureDir.getAbsolutePath()));
    }

    @Test
    public void givenFileSourceOptions_whenSourceFormatUnrecognised_theFails() throws IOException {
        // Given
        SingleCommand<FileSource> command = SingleCommand.singleCommand(FileSource.class);
        File sourceDir = Files.createTempDirectory("source").toFile();

        // When
        ParseResult<FileSource> result =
                command.parseWithResult("--source-directory", sourceDir.getAbsolutePath(), "--source-format",
                                        YamlFormat.NAME);
        try (MockedStatic<FileEventFormats> formats = Mockito.mockStatic(FileEventFormats.class)) {
            formats.when(() -> FileEventFormats.get(any())).thenReturn(null);
            SmartCacheCommand.handleParseResult(result);
        }

        // Then
        Assert.assertTrue(result.wasSuccessful());
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertFalse(stdErr.isEmpty());
        Assert.assertTrue(Strings.CI.contains(stdErr, "not a valid event file format"));
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 1);
    }

    @Test
    public void givenFileSourceOptions_whenCaptureFormatUnrecognised_theFails() throws IOException {
        // Given
        SingleCommand<FileSource> command = SingleCommand.singleCommand(FileSource.class);
        File sourceDir = Files.createTempDirectory("source").toFile();

        // When
        ParseResult<FileSource> result =
                command.parseWithResult("--source-directory", sourceDir.getAbsolutePath(), "--source-format",
                                        YamlFormat.NAME, "--capture-directory", sourceDir.getAbsolutePath(),
                                        "--capture-format", PlainTextFormat.NAME);
        try (MockedStatic<FileEventFormats> formats = Mockito.mockStatic(FileEventFormats.class)) {
            formats.when(() -> FileEventFormats.get(eq(YamlFormat.NAME))).thenReturn(new YamlFormat());
            formats.when(() -> FileEventFormats.get(eq(PlainTextFormat.NAME))).thenReturn(null);
            SmartCacheCommand.handleParseResult(result);
        }

        // Then
        Assert.assertTrue(result.wasSuccessful());
        String stdErr = SmartCacheCommandTester.getLastStdErr();
        Assert.assertFalse(stdErr.isEmpty());
        Assert.assertTrue(Strings.CI.contains(stdErr, "Capture format"));
        Assert.assertTrue(Strings.CI.contains(stdErr, "not a valid event file format"));
        Assert.assertEquals(SmartCacheCommandTester.getLastExitStatus(), 1);
    }
}
