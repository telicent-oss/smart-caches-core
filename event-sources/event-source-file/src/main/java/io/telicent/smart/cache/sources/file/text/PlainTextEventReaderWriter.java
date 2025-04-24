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
package io.telicent.smart.cache.sources.file.text;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.file.FileEventAccessMode;
import io.telicent.smart.cache.sources.file.FileEventReaderWriter;
import io.telicent.smart.cache.sources.file.kafka.AbstractKafkaDelegatingEventReaderWriter;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A file event reader/writer that supports the plain text format defined by <a
 * href="https://github.com/Telicent-io/telicent-architecture/blob/main/CorePlatform/http-file-format.md">HTTP File
 * Format</a>.
 * <p>
 * Note that this format <strong>DOES NOT</strong> consider keys so the key for events will not round-trip and will
 * always be deserialized as {@code null}.
 * </p>
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public class PlainTextEventReaderWriter<TKey, TValue> implements FileEventReaderWriter<TKey, TValue> {
    private final Deserializer<TValue> valueDeserializer;
    private final Serializer<TValue> valueSerializer;
    private final FileEventAccessMode mode;

    /**
     * Creates a new plain text file event reader/writer
     *
     * @param valueDeserializer Value deserializer
     * @param valueSerializer   Value serializer
     */
    PlainTextEventReaderWriter(FileEventAccessMode mode, Deserializer<TValue> valueDeserializer,
                               Serializer<TValue> valueSerializer) {
        this.mode = mode;
        if (this.mode.requiresDeserializers()) {
            Objects.requireNonNull(valueDeserializer, "valueDeserializer cannot be null");
        }
        if (this.mode.requiresSerializers()) {
            Objects.requireNonNull(valueSerializer, "valueSerializer cannot be null");
        }
        this.valueDeserializer = valueDeserializer;
        this.valueSerializer = valueSerializer;
    }

    /**
     * Creates a new plain text file event reader/writer
     *
     * @param valueDeserializer Value deserializer
     * @param valueSerializer   Value serializer
     */
    public PlainTextEventReaderWriter(Deserializer<TValue> valueDeserializer,
                                      Serializer<TValue> valueSerializer) {
        this(FileEventAccessMode.ReadWrite, valueDeserializer, valueSerializer);
    }

    /**
     * Creates a new plain text file event reader/writer
     *
     * @param valueDeserializer Value deserializer
     */
    public PlainTextEventReaderWriter(Deserializer<TValue> valueDeserializer) {
        this(FileEventAccessMode.ReadOnly, valueDeserializer, null);
    }

    /**
     * Creates a new plain text file event reader/writer
     *
     * @param valueSerializer Value serializer
     */
    public PlainTextEventReaderWriter(Serializer<TValue> valueSerializer) {
        this(FileEventAccessMode.WriteOnly, null, valueSerializer);
    }

    @Override
    public Event<TKey, TValue> read(File f) throws IOException {
        AbstractKafkaDelegatingEventReaderWriter.ensureReadsPermitted(this.mode);

        try (FileInputStream input = new FileInputStream(f)) {
            return read(input);
        }
    }

    @Override
    public Event<TKey, TValue> read(InputStream input) throws IOException {
        AbstractKafkaDelegatingEventReaderWriter.ensureReadsPermitted(this.mode);

        List<EventHeader> headers = new ArrayList<>();
        while (true) {
            String line = readLine(input);
            if (line.isEmpty()) {
                break;
            } else {
                headers.add(parseHeader(line));
            }
        }

        byte[] rawValue = IO.readWholeFile(input);
        Headers kafkaHeaders = new RecordHeaders(KafkaSink.toKafkaHeaders(headers.stream()));
        TValue value = this.valueDeserializer.deserialize(AbstractKafkaDelegatingEventReaderWriter.FAKE_TOPIC_FILE,
                                                          kafkaHeaders, rawValue);
        return new SimpleEvent<>(headers, null, value);
    }

    private String readLine(InputStream input) throws IOException {
        StringBuilder builder = new StringBuilder();
        while (true) {
            int c = input.read();
            if (c == -1) {
                return builder.toString();
            } else if (c == '\r') {
                // Ignore carriage return
            } else if (c == '\n') {
                return builder.toString();
            } else {
                builder.append((char) c);
            }
        }
    }

    private Header parseHeader(String line) throws IOException {
        String[] parts = line.split(":", 2);
        if (parts.length != 2) {
            throw new IOException(String.format("Invalid header line (%s) - no : to separate key from value", line));
        }

        return new Header(StringUtils.strip(parts[0]), StringUtils.strip(parts[1]));

    }

    @Override
    public void write(Event<TKey, TValue> event, File f) throws IOException {
        AbstractKafkaDelegatingEventReaderWriter.ensureWritesPermitted(this.mode);

        try (FileOutputStream output = new FileOutputStream(f)) {
            write(event, output);
        }
    }

    @Override
    public void write(Event<TKey, TValue> event, OutputStream output) throws IOException {
        AbstractKafkaDelegatingEventReaderWriter.ensureWritesPermitted(this.mode);

        Headers kafkaHeaders = new RecordHeaders(KafkaSink.toKafkaHeaders(event.headers()));
        // Header lines
        for (EventHeader h : event.headers().toList()) {
            output.write(h.key().getBytes(StandardCharsets.UTF_8));
            output.write(": ".getBytes(StandardCharsets.UTF_8));
            if (h.rawValue() != null) {
                output.write(h.rawValue());
            }
            output.write('\n');
        }

        // Blank Separator Line
        output.write('\n');

        // Event Value
        output.write(
                this.valueSerializer.serialize(AbstractKafkaDelegatingEventReaderWriter.FAKE_TOPIC_FILE, kafkaHeaders,
                                               event.value()));
    }

    @Override
    public FileEventAccessMode getMode() {
        return this.mode;
    }
}
