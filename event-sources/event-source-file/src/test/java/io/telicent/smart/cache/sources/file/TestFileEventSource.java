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
package io.telicent.smart.cache.sources.file;

import io.telicent.smart.cache.sources.AbstractEventSourceTests;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.EventSourceException;
import io.telicent.smart.cache.sources.file.rdf.RdfFileEventSource;
import io.telicent.smart.cache.sources.file.yaml.YamlEventReaderWriter;
import io.telicent.smart.cache.sources.file.yaml.YamlFileEventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.sparql.core.DatasetGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class TestFileEventSource extends AbstractEventSourceTests<Integer, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestFileEventSource.class);

    private final Map<Integer, File> generatedEventSources = new HashMap<>();

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".* cannot be null")
    public void file_event_source_bad_01() {
        new FileEventSource<String, String>(null, null, null, null);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".* cannot be null")
    public void file_event_source_bad_02() {
        new FileEventSource<String, String>(new File("."), null, null, null);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".* cannot be null")
    public void file_event_source_bad_03() {
        new FileEventSource<String, String>(new File("."), f -> false, null, null);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".* cannot be null")
    public void file_event_source_bad_04() {
        new FileEventSource<String, String>(new File("."), f -> false, new NumericFilenameComparator(), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".* not a directory")
    public void file_event_source_bad_05() {
        new FileEventSource<>(new File("pom.xml"), f -> false, new NumericFilenameComparator(),
                              Serdes.YAML_STRING_STRING);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "No such directory.*")
    public void file_event_source_bad_06() {
        new FileEventSource<>(new File("no-such-file"), f -> false, new NumericFilenameComparator(),
                              Serdes.YAML_STRING_STRING);
    }

    @Test
    public void file_event_source_01() {
        EventSource<String, String> source =
                new FileEventSource<>(new File("test-data"), f -> false, new NumericFilenameComparator(),
                                      Serdes.YAML_STRING_STRING);
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.availableImmediately());
        Assert.assertTrue(source.isExhausted());
        Assert.assertNull(source.poll(Duration.ofSeconds(1)));
        Assert.assertEquals(source.remaining(), 0L);

        source.processed(Collections.emptyList());

        source.close();
        Assert.assertTrue(source.isClosed());
        Assert.assertTrue(source.isExhausted());
        Assert.assertFalse(source.availableImmediately());
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Event source is closed")
    public void file_event_source_02() {
        EventSource<String, String> source =
                new FileEventSource<>(new File("test-data"), f -> false, new NumericFilenameComparator(),
                                      Serdes.YAML_STRING_STRING);
        source.close();
        source.poll(Duration.ofSeconds(1));
    }

    @Test
    public void file_event_source_03() {
        EventSource<String, String> source =
                new YamlFileEventSource<>(new File("test-data"), Serdes.STRING_DESERIALIZER,
                                          Serdes.STRING_DESERIALIZER);
        Assert.assertTrue(source.availableImmediately());
        Assert.assertFalse(source.isExhausted());
        Assert.assertEquals(source.remaining(), 4L);
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Invalid Event.*")
    public void file_event_source_04() {
        EventSource<String, String> source =
                new YamlFileEventSource<>(new File("test-data"), Serdes.STRING_DESERIALIZER,
                                          Serdes.STRING_DESERIALIZER);
        source.poll(Duration.ofSeconds(1));
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Failed to parse.*")
    public void file_event_source_05() {
        EventSource<String, String> source =
                new FileEventSource<>(new File("test-data"), f -> Objects.equals(f.getName(), "malformed2.yaml"),
                                      new NumericFilenameComparator(), Serdes.YAML_STRING_STRING);
        source.poll(Duration.ofSeconds(1));
    }

    @Test
    public void file_event_source_06() throws IOException {
        YamlEventReaderWriter<Integer, String> yaml = Serdes.YAML_INTEGER_STRING;
        File tempDir = Files.createTempDirectory("yaml-events").toFile();
        for (int i = 0; i < 1_000; i++) {
            SimpleEvent<Integer, String> event = new SimpleEvent<>(Collections.emptyList(), i, "Value " + i);
            yaml.write(event, new File(tempDir, "event" + StringUtils.leftPad(Integer.toString(i), 6, '0') + ".yaml"));
        }

        EventSource<Integer, String> source =
                new YamlFileEventSource<>(tempDir, Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER);
        Assert.assertEquals(source.remaining(), 1_000L);
        Assert.assertTrue(source.availableImmediately());
        Assert.assertFalse(source.isExhausted());

        int expectedKey = 0;
        while (!source.isExhausted()) {
            Assert.assertTrue(source.availableImmediately());
            Event<Integer, String> event = source.poll(Duration.ofSeconds(1));
            Assert.assertEquals(event.key(), expectedKey);
            expectedKey++;
        }
        Assert.assertEquals(expectedKey, 1_000);
        Assert.assertFalse(source.availableImmediately());
    }

    @Test
    public void file_event_source_07() {
        EventSource<String, String> source =
                new YamlFileEventSource<>(new File("test-data/bad-dir"), Serdes.STRING_DESERIALIZER,
                                          Serdes.STRING_DESERIALIZER);
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.availableImmediately());
        Assert.assertTrue(source.isExhausted());
        Assert.assertNull(source.poll(Duration.ofSeconds(1)));
        Assert.assertEquals(source.remaining(), 0L);

        source.close();
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void file_event_source_08a() {
        EventSource<String, String> source =
                new YamlFileEventSource<>(new File("test-data/rdf/"), false, Serdes.STRING_DESERIALIZER,
                                          Serdes.STRING_DESERIALIZER);
        Assert.assertTrue(source.availableImmediately());
        Assert.assertFalse(source.isExhausted());
        Assert.assertEquals(source.remaining(), 4L);
    }

    @Test
    public void file_event_source_08b() {
        EventSource<String, String> source =
                new YamlFileEventSource<>(new File("test-data/rdf/"), true, Serdes.STRING_DESERIALIZER,
                                          Serdes.STRING_DESERIALIZER);
        Assert.assertTrue(source.availableImmediately());
        Assert.assertFalse(source.isExhausted());
        Assert.assertEquals(source.remaining(), 4L);
    }

    @Test
    public void async_file_event_source_09() throws IOException {
        YamlEventReaderWriter<String, String> yaml = Serdes.YAML_STRING_STRING;
        File tempDir = Files.createTempDirectory("yaml-events-with-error").toFile();
        yaml.write(new SimpleEvent<>(Collections.emptyList(), "first", "value-1"), new File(tempDir, "event-1.yaml"));
        Files.copy(new File("test-data", "malformed.yaml").toPath(), new File(tempDir, "event-2.yaml").toPath());
        yaml.write(new SimpleEvent<>(Collections.emptyList(), "third", "value-3"), new File(tempDir, "event-3.yaml"));

        EventSource<String, String> source =
                new YamlFileEventSource<>(tempDir, Serdes.STRING_DESERIALIZER, Serdes.STRING_DESERIALIZER, true);

        Event<String, String> first = source.poll(Duration.ofSeconds(1));
        Assert.assertNotNull(first);
        Assert.assertEquals(first.key(), "first");
        Assert.assertEquals(source.remaining(), 2L);

        Assert.assertThrows(EventSourceException.class, () -> source.poll(Duration.ofSeconds(1)));
        Assert.assertEquals(source.remaining(), 1L);

        Event<String, String> third = source.poll(Duration.ofSeconds(1));
        Assert.assertNotNull(third);
        Assert.assertEquals(third.key(), "third");
        Assert.assertEquals(source.remaining(), 0L);
        Assert.assertNull(source.poll(Duration.ofSeconds(1)));
    }

    /**
     * A file event reader whose {@link #read(File)} can be made to block indefinitely, or to fail with a
     * specific error, so that the asynchronous parsing code paths of {@link FileEventSource} can be exercised
     * deterministically.
     */
    private static final class ControllableReader implements FileEventReader<String, String> {
        private final CountDownLatch gate;
        private final IOException ioError;
        private final RuntimeException runtimeError;

        ControllableReader(CountDownLatch gate) {
            this(gate, null, null);
        }

        ControllableReader(CountDownLatch gate, IOException ioError, RuntimeException runtimeError) {
            this.gate = gate;
            this.ioError = ioError;
            this.runtimeError = runtimeError;
        }

        @Override
        public Event<String, String> read(File f) throws IOException {
            if (this.gate != null) {
                try {
                    this.gate.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (this.ioError != null) {
                throw this.ioError;
            }
            if (this.runtimeError != null) {
                throw this.runtimeError;
            }
            return new SimpleEvent<>(Collections.emptyList(), f.getName(), "value");
        }

        @Override
        public Event<String, String> read(InputStream input) {
            throw new UnsupportedOperationException();
        }
    }

    private File createDirWithFiles(int count) throws IOException {
        final File tempDir = Files.createTempDirectory("async-file-events").toFile();
        for (int i = 0; i < count; i++) {
            Files.createFile(new File(tempDir, "event" + i + ".yaml").toPath());
        }
        return tempDir;
    }

    private FileEventSource<String, String> asyncSource(File dir, FileEventReader<String, String> reader) {
        return new FileEventSource<>(dir, f -> true, Comparator.comparing(File::getName), reader, true);
    }

    @Test
    public void async_file_event_source_10_poll_times_out() throws IOException {
        final File dir = createDirWithFiles(1);
        final CountDownLatch gate = new CountDownLatch(1);
        final FileEventSource<String, String> source = asyncSource(dir, new ControllableReader(gate));
        try {
            // The parser thread is blocked reading the only file, so no event is buffered and polling must
            // wait until the timeout elapses and then return null
            final long start = System.currentTimeMillis();
            Assert.assertNull(source.poll(Duration.ofMillis(250)));
            Assert.assertTrue(System.currentTimeMillis() - start >= 200,
                              "poll() should have waited for approximately the requested timeout");
        } finally {
            gate.countDown();
            source.close();
        }
    }

    @Test
    public void async_file_event_source_11_close_while_waiting() throws IOException, InterruptedException {
        final File dir = createDirWithFiles(1);
        final CountDownLatch gate = new CountDownLatch(1);
        final FileEventSource<String, String> source = asyncSource(dir, new ControllableReader(gate));
        try {
            final AtomicReference<Throwable> caught = new AtomicReference<>();
            final Thread poller = new Thread(() -> {
                try {
                    source.poll(Duration.ofSeconds(5));
                } catch (Throwable e) {
                    caught.set(e);
                }
            });
            poller.start();
            // Give the poller time to enter its wait, then close the source out from under it
            Thread.sleep(300);
            source.close();
            poller.join(5000);

            Assert.assertNotNull(caught.get(), "Expected poll() to fail once the source was closed");
            Assert.assertTrue(caught.get() instanceof IllegalStateException,
                              "Expected an IllegalStateException but got " + caught.get());
            Assert.assertEquals(caught.get().getMessage(), "Event source is closed");
        } finally {
            gate.countDown();
            source.close();
        }
    }

    @Test
    public void async_file_event_source_12_interrupted_while_waiting() throws IOException, InterruptedException {
        final File dir = createDirWithFiles(1);
        final CountDownLatch gate = new CountDownLatch(1);
        final FileEventSource<String, String> source = asyncSource(dir, new ControllableReader(gate));
        try {
            final AtomicReference<Event<String, String>> result = new AtomicReference<>();
            final AtomicReference<Throwable> caught = new AtomicReference<>();
            final AtomicReference<Boolean> wasInterrupted = new AtomicReference<>(false);
            final Thread poller = new Thread(() -> {
                try {
                    result.set(source.poll(Duration.ofSeconds(5)));
                    wasInterrupted.set(Thread.currentThread().isInterrupted());
                } catch (Throwable e) {
                    caught.set(e);
                }
            });
            poller.start();
            // Give the poller time to enter its wait, then interrupt it directly
            Thread.sleep(300);
            poller.interrupt();
            poller.join(5000);

            Assert.assertNull(caught.get(), "poll() should swallow the interruption rather than throwing");
            Assert.assertNull(result.get(), "An interrupted poll() should return null");
            Assert.assertTrue(wasInterrupted.get(), "poll() should preserve the thread's interrupt status");
        } finally {
            gate.countDown();
            source.close();
        }
    }

    @Test
    public void async_file_event_source_13_interrupt_and_close_state() throws IOException {
        final File dir = createDirWithFiles(2);
        final CountDownLatch gate = new CountDownLatch(1);
        final FileEventSource<String, String> source = asyncSource(dir, new ControllableReader(gate));
        try {
            // interrupt() should signal the parser thread and any waiting pollers without error
            source.interrupt();

            source.close();
            Assert.assertTrue(source.isClosed());
            Assert.assertTrue(source.isExhausted());
            Assert.assertFalse(source.availableImmediately());
            Assert.assertEquals(source.remaining(), 0L);
        } finally {
            gate.countDown();
            source.close();
        }
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Failed to parse.*")
    public void async_file_event_source_14_read_io_error() throws IOException {
        final File dir = createDirWithFiles(1);
        final FileEventSource<String, String> source =
                asyncSource(dir, new ControllableReader(null, new IOException("boom"), null));
        try {
            source.poll(Duration.ofSeconds(5));
        } finally {
            source.close();
        }
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Invalid Event.*")
    public void async_file_event_source_15_read_invalid() throws IOException {
        final File dir = createDirWithFiles(1);
        final FileEventSource<String, String> source =
                asyncSource(dir, new ControllableReader(null, null, new RuntimeException("bang")));
        try {
            source.poll(Duration.ofSeconds(5));
        } finally {
            source.close();
        }
    }

    @Test
    public void async_file_event_source_16_reads_events() throws IOException {
        final File dir = createDirWithFiles(3);
        final FileEventSource<String, String> source = asyncSource(dir, new ControllableReader(null));
        try {
            int seen = 0;
            while (seen < 3) {
                final Event<String, String> event = source.poll(Duration.ofSeconds(5));
                Assert.assertNotNull(event, "Expected to receive all buffered events");
                Assert.assertEquals(event.value(), "value");
                seen++;
            }
            Assert.assertEquals(source.remaining(), 0L);
            Assert.assertNull(source.poll(Duration.ofSeconds(1)));
            Assert.assertTrue(source.isExhausted());
        } finally {
            source.close();
        }
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*cannot be null")
    public void single_file_event_source_bad_01() {
        new SingleFileEventSource<>(new File("test-data"), Serdes.YAML_STRING_STRING);
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Failed to parse an Event from file.*")
    public void single_file_event_source_bad_02() {
        EventSource<String, String> source =
                new SingleFileEventSource<>(new File(new File("test-data", "rdf"), "rdf1.txt"),
                                            Serdes.YAML_STRING_STRING);
        source.poll(Duration.ofSeconds(1));
    }

    @Test
    public void single_file_event_source_01() {
        EventSource<String, String> source =
                new SingleFileEventSource<>(new File("test-data", "event.yaml"), Serdes.YAML_STRING_STRING);
        Assert.assertTrue(source.availableImmediately());
        Assert.assertFalse(source.isExhausted());
        Assert.assertEquals(source.remaining(), 1L);

        Event<String, String> event = source.poll(Duration.ofSeconds(1));
        Assert.assertNotNull(event);
        Assert.assertFalse(source.availableImmediately());
        Assert.assertTrue(source.isExhausted());
    }

    @Test
    public void rdf_file_event_source_01() {
        EventSource<String, DatasetGraph> source =
                new RdfFileEventSource<>(new File("test-data", "rdf"), Serdes.STRING_DESERIALIZER,
                                         Serdes.DATASET_GRAPH_DESERIALIZER);
        Assert.assertTrue(source.availableImmediately());
        Assert.assertFalse(source.isExhausted());
        Assert.assertEquals(source.remaining(), 4);

        while (!source.isExhausted()) {
            Assert.assertNotNull(source.poll(Duration.ofSeconds(1)));
        }
    }

    @DataProvider(name = "sample-data-sizes")
    @Override
    public Object[][] getTestSizes() {
        return new Object[][] {
                { 100 }, { 500 }, { 1_000 }
        };
    }

    @Override
    protected EventSource<Integer, String> createEmptySource() {
        try {
            File emptyDir = Files.createTempDirectory("empty").toFile();
            return new YamlFileEventSource<>(emptyDir, Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected EventSource<Integer, String> createSource(Collection<Event<Integer, String>> events) {
        try {
            File existingDir = generatedEventSources.get(events.size());
            if (existingDir != null) {
                return new YamlFileEventSource<>(existingDir, Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER);
            }

            File tempDir = Files.createTempDirectory("yaml-source").toFile();
            LOGGER.info("Writing a test event source directory with {} events to {}", events.size(),
                        tempDir.getAbsolutePath());
            YamlEventReaderWriter<Integer, String> readerWriter = Serdes.YAML_INTEGER_STRING;
            int counter = 0;
            for (Event<Integer, String> event : events) {
                readerWriter.write(event, new File(tempDir, "event-" + (++counter) + ".yaml"));
            }
            LOGGER.info("Wrote a test event source directory with {} events to {}", events.size(),
                        tempDir.getAbsolutePath());
            generatedEventSources.put(events.size(), tempDir);
            return new YamlFileEventSource<>(tempDir, Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Collection<Event<Integer, String>> createSampleData(int size) {
        List<Event<Integer, String>> events = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            events.add(new SimpleEvent<>(Collections.emptyList(), i, String.format("Event #%,d", i)));
        }
        return events;
    }

    @Override
    public boolean guaranteesImmediateAvailability() {
        return true;
    }

    @Override
    public boolean isUnbounded() {
        return false;
    }
}
