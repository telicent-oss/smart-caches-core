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
package io.telicent.smart.cache.sources.file.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventHeader;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.file.FileEventAccessMode;
import io.telicent.smart.cache.sources.file.kafka.AbstractKafkaDelegatingEventReaderWriter;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Abstract event reader/writer using Jackson for JSON/YAML format support but deferring actual key and value handling
 * to Kafka's serialization/deserialization support.
 *
 * @param <TKey>   Key type
 * @param <TValue> Value type
 */
public class AbstractJacksonEventReaderWriter<TKey, TValue> extends
        AbstractKafkaDelegatingEventReaderWriter<TKey, TValue> {
    /**
     * The Jackson mapper that is used
     */
    protected final ObjectMapper mapper;

    /**
     * Creates a new Jackson based event reader/writer
     *
     * @param mapper            Jackson Object mapper
     * @param mode              File Event access mode
     * @param keySerializer     Key serializer
     * @param valueSerializer   Value serializer
     * @param keyDeserializer   Key deserializer
     * @param valueDeserializer Value deserializer
     */
    public AbstractJacksonEventReaderWriter(ObjectMapper mapper, FileEventAccessMode mode,
                                            Serializer<TKey> keySerializer,
                                            Serializer<TValue> valueSerializer, Deserializer<TKey> keyDeserializer,
                                            Deserializer<TValue> valueDeserializer) {
        super(mode, keyDeserializer, valueDeserializer, keySerializer, valueSerializer);
        Objects.requireNonNull(mapper, "Jackson Mapper cannot be null");

        this.mapper = mapper;
    }

    @Override
    public Event<TKey, TValue> read(File f) throws IOException {
        ensureReadsPermitted(this.mode);

        try (JsonParser parser = this.mapper.createParser(f)) {
            return readEvent(parser);
        }
    }

    @Override
    public Event<TKey, TValue> read(InputStream input) throws IOException {
        ensureReadsPermitted(this.mode);

        try (JsonParser parser = this.mapper.createParser(input)) {
            return readEvent(parser);
        }
    }

    /**
     * Reads an event
     *
     * @param parser Jackson parser
     * @return Event
     * @throws IOException Thrown if the event cannot be read
     */
    protected final Event<TKey, TValue> readEvent(JsonParser parser) throws IOException {
        List<EventHeader> headers = new ArrayList<>();
        TKey key = null;
        TValue value = null;

        requireToken(parser, true, JsonToken.START_OBJECT);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentToken()) {
                case FIELD_NAME:
                    switch (parser.currentName()) {
                        case "headers" -> readHeaders(parser, headers);
                        case "key" -> key = readKey(headers, parser);
                        case "value" -> value = readValue(headers, parser);
                        default -> {
                            JsonToken unknownField = parser.nextToken();
                            if (unknownField == JsonToken.START_OBJECT || unknownField == JsonToken.START_ARRAY) {
                                parser.skipChildren();
                            }
                        }
                    }
                    break;
                default:
                    throw new IOException(
                            "Unexpected JSON Token " + parser.currentToken() + " (" + parser.getText() + ")");
            }
        }
        requireToken(parser, false, JsonToken.END_OBJECT);

        return new SimpleEvent<>(headers, key, value);
    }

    private void readHeaders(JsonParser parser, List<EventHeader> headers) throws IOException {
        requireToken(parser, true, JsonToken.START_ARRAY);

        while (parser.nextToken() == JsonToken.START_OBJECT) {
            headers.add(parser.readValueAs(Header.class));
        }
        requireToken(parser, false, JsonToken.FIELD_NAME, JsonToken.END_ARRAY);
    }

    /**
     * Reads the key for the event
     *
     * @param headers Headers
     * @param parser  Parser
     * @return Key
     * @throws IOException Thrown if the key cannot be read successfully
     */
    protected TKey readKey(List<EventHeader> headers, JsonParser parser) throws IOException {
        return parseValue(headers, parser, this.keyDeserializer);
    }

    /**
     * Parses a value
     *
     * @param headers      Headers
     * @param parser       Parser
     * @param deserializer Value deserializer
     * @param <T>          Value type
     * @return Value
     * @throws IOException Thrown if the value cannot be parsed successfully
     */
    protected <T> T parseValue(List<EventHeader> headers, JsonParser parser, Deserializer<T> deserializer) throws
            IOException {
        requireValue(parser);
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        byte[] rawData = readBase64(parser);
        return deserializer.deserialize(FAKE_TOPIC_FILE, new RecordHeaders(KafkaSink.toKafkaHeaders(headers.stream())),
                                        rawData);
    }

    /**
     * Reads a string value interpreting it as a Base 64 encoded byte sequence
     *
     * @param parser Parser
     * @return Byte sequence
     * @throws IOException Thrown if the value cannot be read successfully
     */
    protected byte[] readBase64(JsonParser parser) throws IOException {
        requireToken(parser, false, JsonToken.VALUE_STRING);
        String base64data = parser.readValueAs(String.class);
        return Base64.getDecoder().decode(base64data);
    }

    /**
     * Require a value as the next token
     *
     * @param parser Parser
     * @throws IOException Thrown if unable to read the next token
     */
    protected void requireValue(JsonParser parser) throws IOException {
        requireToken(parser, true, JsonToken.START_OBJECT, JsonToken.VALUE_FALSE, JsonToken.VALUE_TRUE,
                     JsonToken.VALUE_NULL,
                     JsonToken.VALUE_STRING, JsonToken.VALUE_NUMBER_FLOAT, JsonToken.VALUE_NUMBER_INT);
    }

    /**
     * Reads the value for the event
     *
     * @param headers Headers
     * @param parser  Parser
     * @return Value
     * @throws IOException Thrown if the value cannot be read
     */
    protected TValue readValue(List<EventHeader> headers, JsonParser parser) throws IOException {
        return parseValue(headers, parser, this.valueDeserializer);
    }

    /**
     * Requires that a particular token type be encountered
     *
     * @param parser   Parser
     * @param readNext Whether to read the next token or just inspect the current token
     * @param expected Acceptable token types
     * @throws IOException Thrown if the expectation is not met
     */
    protected final void requireToken(JsonParser parser, boolean readNext, JsonToken... expected) throws IOException {
        JsonToken next = readNext ? parser.nextToken() : parser.currentToken();
        if (!ArrayUtils.contains(expected, next)) {
            if (expected.length == 1) {
                throw new IOException(
                        "Expected " + expected[0] + " but got " + parser.currentToken() + " (" + parser.getText() + ")");
            } else {
                throw new IOException("Expected one of " + StringUtils.join(expected,
                                                                            ", ") + " but got " + parser.currentToken() + " (" + parser.getText() + ")");
            }
        }
    }

    @Override
    public void write(Event<TKey, TValue> event, File f) throws IOException {
        ensureWritesPermitted(this.mode);

        try (FileOutputStream output = new FileOutputStream(f)) {
            write(event, output);
        }
    }

    @Override
    public void write(Event<TKey, TValue> event, OutputStream output) throws IOException {
        try (JsonGenerator generator = this.mapper.createGenerator(output)) {
            generator.writeStartObject();
            if (event.headers().findAny().isPresent()) {
                generator.writeFieldName("headers");
                generator.writeStartArray();
                for (EventHeader header : event.headers().toList()) {
                    generator.writePOJO(header);
                }
                generator.writeEndArray();
            }
            if (event.key() != null) {
                writeKey(event, generator);
            }
            if (event.value() != null) {
                writeValue(event, generator);
            }
            generator.writeEndObject();
        }
    }

    /**
     * Writes the event value
     *
     * @param event     Event
     * @param generator Writer
     * @throws IOException Thrown if the value cannot be written
     */
    protected void writeValue(Event<TKey, TValue> event, JsonGenerator generator) throws IOException {
        writeBase64(event.headers(), "value", event.value(), this.valueSerializer, generator);
    }

    /**
     * Writes a value as a Base 64 encoded string
     *
     * @param headers    Headers
     * @param field      Field to write
     * @param value      Value to write
     * @param serializer Serializer for the value
     * @param generator  Generator
     * @param <T>        Value type
     * @throws IOException Thrown if the value cannot be serialized
     */
    protected <T> void writeBase64(Stream<EventHeader> headers, String field, T value, Serializer<T> serializer,
                                   JsonGenerator generator) throws
            IOException {
        byte[] data =
                serializer.serialize(FAKE_TOPIC_FILE, new RecordHeaders(KafkaSink.toKafkaHeaders(headers)), value);
        if (data == null || data.length == 0) {
            generator.writeNullField(field);
        } else {
            generator.writeStringField(field, Base64.getEncoder().encodeToString(data));
        }
    }

    /**
     * Writes the event key
     *
     * @param event     Event
     * @param generator YAML Writer
     * @throws IOException Thrown if the key cannot be written
     */
    protected void writeKey(Event<TKey, TValue> event, JsonGenerator generator) throws IOException {
        writeBase64(event.headers(), "key", event.key(), this.keySerializer, generator);
    }
}
