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

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.EventSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An event source where the events are files on disk in a directory
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public class FileEventSource<TKey, TValue> implements EventSource<TKey, TValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileEventSource.class);

    protected static final class BufferedFileEvent<TKey, TValue> {
        private final Event<TKey, TValue> event;
        private final EventSourceException error;

        private BufferedFileEvent(Event<TKey, TValue> event, EventSourceException error) {
            this.event = event;
            this.error = error;
        }

        private static <TKey, TValue> BufferedFileEvent<TKey, TValue> event(Event<TKey, TValue> event) {
            return new BufferedFileEvent<>(event, null);
        }

        private static <TKey, TValue> BufferedFileEvent<TKey, TValue> error(EventSourceException error) {
            return new BufferedFileEvent<>(null, error);
        }
    }

    private final Object stateLock = new Object();
    private final List<File> eventFiles = new ArrayList<>();
    private final Queue<BufferedFileEvent<TKey, TValue>> bufferedEvents = new ArrayDeque<>();
    private final FileEventReader<TKey, TValue> eventReader;
    private final boolean asyncProcessing;
    private final int totalEvents;
    private final AtomicInteger consumedEvents = new AtomicInteger();
    private volatile boolean closed = false;
    private volatile boolean parsingComplete = false;
    private Thread parserThread;

    /**
     * Creates a new file event source using the legacy synchronous per-poll parsing behaviour.
     *
     * @param sourceDir       Source directory containing the events
     * @param eventFileFilter Filter used to identify files that represent events
     * @param fileComparator  File comparator used to sort events into the desired order
     * @param reader          File event reader to use to convert the files into events
     */
    public FileEventSource(File sourceDir, FileFilter eventFileFilter, Comparator<File> fileComparator,
                           FileEventReader<TKey, TValue> reader) {
        this(sourceDir, eventFileFilter, fileComparator, reader, false);
    }

    /**
     * Creates a new file event source
     *
     * @param sourceDir         Source directory containing the events
     * @param eventFileFilter   Filter used to identify files that represent events
     * @param fileComparator    File comparator used to sort events into the desired order
     * @param reader            File event reader to use to convert the files into events
     * @param asyncProcessing   Whether to parse files asynchronously in a background thread
     */
    public FileEventSource(File sourceDir, FileFilter eventFileFilter, Comparator<File> fileComparator,
                           FileEventReader<TKey, TValue> reader, boolean asyncProcessing) {
        Objects.requireNonNull(sourceDir, "Source directory cannot be null");
        Objects.requireNonNull(eventFileFilter, "Event filter filter cannot be null");
        Objects.requireNonNull(fileComparator, "File comparator cannot be null");
        Objects.requireNonNull(reader, "File event reader cannot be null");
        if (!sourceDir.exists()) {
            throw new IllegalArgumentException("No such directory " + sourceDir.getAbsolutePath());
        }
        if (!sourceDir.isDirectory()) {
            throw new IllegalArgumentException(sourceDir.getAbsolutePath() + " is not a directory");
        }
        this.eventReader = reader;
        this.asyncProcessing = asyncProcessing;

        this.eventFiles.addAll(obtainEventFiles(sourceDir, eventFileFilter));
        this.eventFiles.sort(fileComparator);
        this.totalEvents = this.eventFiles.size();
        this.parsingComplete = !this.asyncProcessing && this.eventFiles.isEmpty();

        if (this.asyncProcessing) {
            if (this.eventFiles.isEmpty()) {
                this.parsingComplete = true;
            } else {
                startParserThread();
            }
        }
    }

    @Override
    public boolean availableImmediately() {
        synchronized (this.stateLock) {
            if (this.closed) {
                return false;
            }
            return this.asyncProcessing ? !this.bufferedEvents.isEmpty() : !this.eventFiles.isEmpty();
        }
    }

    @Override
    public boolean isExhausted() {
        synchronized (this.stateLock) {
            if (this.closed) {
                return true;
            }
            if (!this.asyncProcessing) {
                return this.eventFiles.isEmpty();
            }
            return this.parsingComplete && this.bufferedEvents.isEmpty();
        }
    }

    @Override
    public void close() {
        this.closed = true;
        synchronized (this.stateLock) {
            this.eventFiles.clear();
            this.bufferedEvents.clear();
            this.parsingComplete = true;
            this.stateLock.notifyAll();
        }
        if (this.parserThread != null) {
            this.parserThread.interrupt();
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public Event<TKey, TValue> poll(Duration timeout) {
        if (this.closed) {
            throw new IllegalStateException("Event source is closed");
        }

        return this.asyncProcessing ? pollAsync(timeout) : pollSynchronously();
    }

    private Event<TKey, TValue> pollSynchronously() {
        File nextFile;
        synchronized (this.stateLock) {
            if (this.eventFiles.isEmpty()) {
                return null;
            }
            nextFile = this.eventFiles.remove(0);
        }

        return readEvent(nextFile);
    }

    private Event<TKey, TValue> pollAsync(Duration timeout) {
        BufferedFileEvent<TKey, TValue> bufferedEvent;
        synchronized (this.stateLock) {
            if (!this.bufferedEvents.isEmpty()) {
                bufferedEvent = this.bufferedEvents.poll();
            } else if (this.parsingComplete) {
                return null;
            } else {
                long timeoutMillis = timeout.toMillis();
                long start = System.currentTimeMillis();
                while (!this.closed && this.bufferedEvents.isEmpty() && !this.parsingComplete) {
                    long remainingWait = timeoutMillis - (System.currentTimeMillis() - start);
                    if (remainingWait <= 0) {
                        return null;
                    }
                    try {
                        this.stateLock.wait(remainingWait);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }

                if (this.closed) {
                    throw new IllegalStateException("Event source is closed");
                }
                if (this.bufferedEvents.isEmpty()) {
                    return null;
                }
                bufferedEvent = this.bufferedEvents.poll();
            }
        }

        this.consumedEvents.incrementAndGet();
        if (bufferedEvent.error != null) {
            throw bufferedEvent.error;
        }
        return bufferedEvent.event;
    }

    @Override
    public Long remaining() {
        synchronized (this.stateLock) {
            if (this.closed) {
                return 0L;
            }
            if (!this.asyncProcessing) {
                return (long) this.eventFiles.size();
            }
            return (long) Math.max(0, this.totalEvents - this.consumedEvents.get());
        }
    }

    @Override
    public void processed(Collection<Event<?, ?>> processedEvents) {
        // No-op
        LOGGER.trace("Received {} processed events in processed() callback, this is ignored by the FileEventSource",
                     processedEvents.size());
    }

    @Override
    public void interrupt() {
        if (this.parserThread != null) {
            this.parserThread.interrupt();
        }
        synchronized (this.stateLock) {
            this.stateLock.notifyAll();
        }
    }

    /**
     * Creates a list of files from the given directory using the given filter.
     *
     * @param sourceDir       Source directory containing the events
     * @param eventFileFilter Filter used to identify files that represent events
     * @return List of files, or empty list if null.
     */
    private List<File> obtainEventFiles(File sourceDir, FileFilter eventFileFilter) {
        File[] fileArray = sourceDir.listFiles(eventFileFilter);
        if (fileArray != null) {
            return Arrays.asList(fileArray);
        }
        return Collections.emptyList();
    }

    private void startParserThread() {
        this.parserThread = new Thread(this::runParser, "file-event-source-parser");
        this.parserThread.setDaemon(true);
        this.parserThread.start();
    }

    private void runParser() {
        try {
            while (!this.closed) {
                File nextFile;
                synchronized (this.stateLock) {
                    if (this.eventFiles.isEmpty()) {
                        return;
                    }
                    nextFile = this.eventFiles.remove(0);
                }

                BufferedFileEvent<TKey, TValue> bufferedEvent = readBufferedEvent(nextFile);
                synchronized (this.stateLock) {
                    if (this.closed) {
                        return;
                    }
                    this.bufferedEvents.add(bufferedEvent);
                    this.stateLock.notifyAll();
                }
            }
        } finally {
            synchronized (this.stateLock) {
                this.parsingComplete = true;
                this.stateLock.notifyAll();
            }
        }
    }

    private BufferedFileEvent<TKey, TValue> readBufferedEvent(File file) {
        try {
            return BufferedFileEvent.event(this.eventReader.read(file));
        } catch (IOException e) {
            return BufferedFileEvent.error(
                    new EventSourceException("Failed to parse an Event from file " + file.getAbsolutePath(), e));
        } catch (Throwable e) {
            return BufferedFileEvent.error(
                    new EventSourceException("Invalid Event in file " + file.getAbsolutePath(), e));
        }
    }

    private Event<TKey, TValue> readEvent(File file) {
        try {
            return this.eventReader.read(file);
        } catch (IOException e) {
            throw new EventSourceException("Failed to parse an Event from file " + file.getAbsolutePath(), e);
        } catch (Throwable e) {
            throw new EventSourceException("Invalid Event in file " + file.getAbsolutePath(), e);
        }
    }
}
