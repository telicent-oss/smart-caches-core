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
package io.telicent.smart.cache.sources.file.rdf;

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.file.FileEventReaderWriter;
import io.telicent.smart.cache.sources.file.Serdes;
import io.telicent.smart.cache.sources.file.yaml.TestYamlEventReaderWriter;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;

public class TestRdfEventReaderWriter {

    private static <TKey, TValue> void verifyRoundTrip(FileEventReaderWriter<TKey, TValue> writer,
                                                       Event<TKey, TValue> event) throws IOException {
        Lang lang = RDFLanguages.contentTypeToLang(event.lastHeader(HttpNames.hContentType));
        if (lang == null) {
            lang = Lang.NQUADS;
        }
        File f = Files.createTempFile("rdf-event", "." + lang.getFileExtensions().get(0)).toFile();
        try {
            writer.write(event, f);
            Assert.assertNotEquals(f.length(), 0L);

            Event<TKey, TValue> retrieved = writer.read(f);
            TestYamlEventReaderWriter.verifySameEvent(event, retrieved);
        } finally {
            f.delete();
        }
    }

    @Test
    public void rdf_event_read_01() throws IOException {
        RdfEventReaderWriter<Integer, DatasetGraph> reader =
                Serdes.RDF_INTEGER_STRING;

        Event<Integer, DatasetGraph> event = reader.read(new File("test-data/rdf/rdf1.nq"));
        Assert.assertNull(event.key());
        Assert.assertNotNull(event.value());
        Assert.assertEquals(event.value().stream().count(), 2);

        verifyRoundTrip(reader, event);
    }

    @Test
    public void rdf_event_read_01b() throws IOException {
        RdfEventReaderWriter<Integer, DatasetGraph> reader =
                Serdes.RDF_INTEGER_STRING;

        try (FileInputStream input = new FileInputStream("test-data/rdf/rdf1.nq")) {
            Event<Integer, DatasetGraph> event = reader.read(input);
            Assert.assertNull(event.key());
            Assert.assertNotNull(event.value());
            Assert.assertEquals(event.value().stream().count(), 2);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            reader.write(event, output);
            Event<Integer, DatasetGraph> retrieved = reader.read(new ByteArrayInputStream(output.toByteArray()));

            TestYamlEventReaderWriter.verifySameEvent(event, retrieved);
        }
    }

    @Test
    public void rdf_event_read_02() throws IOException {
        RdfEventReaderWriter<Integer, DatasetGraph> reader =
                Serdes.RDF_INTEGER_STRING;

        Event<Integer, DatasetGraph> event = reader.read(new File("test-data/rdf/rdf2.nq"));
        Assert.assertNull(event.key());
        Assert.assertNotNull(event.value());
        Assert.assertEquals(event.value().stream().count(), 2);

        verifyRoundTrip(reader, event);
    }

    @Test
    public void rdf_event_read_03() throws IOException {
        RdfEventReaderWriter<Integer, DatasetGraph> reader =
                Serdes.RDF_INTEGER_STRING;

        Event<Integer, DatasetGraph> event = reader.read(new File("test-data/rdf/rdf3.trig"));
        Assert.assertNull(event.key());
        Assert.assertNotNull(event.value());
        Assert.assertEquals(event.value().stream().count(), 2);

        verifyRoundTrip(reader, event);
    }

    @Test
    public void rdf_event_read_04() throws IOException {
        RdfEventReaderWriter<Integer, DatasetGraph> reader =
                Serdes.RDF_INTEGER_STRING;

        Event<Integer, DatasetGraph> event = reader.read(new File("test-data/rdf/rdf4.nt"));
        Assert.assertNull(event.key());
        Assert.assertNotNull(event.value());
        Assert.assertEquals(event.value().stream().count(), 2);
    }

    @Test(expectedExceptions = RiotException.class)
    public void rdf_event_read_bad_01() throws IOException {
        Serdes.RDF_INTEGER_STRING.read(new File("test-data/event.yaml"));
    }

    @Test(expectedExceptions = RiotException.class)
    public void rdf_event_read_bad_02() throws IOException {
        Serdes.RDF_INTEGER_STRING.read(new File("test-data/rdf/rdf1.txt"));
    }

    @Test
    public void rdf_event_write_01() throws IOException {
        FileEventReaderWriter<Integer, DatasetGraph> writer = Serdes.RDF_INTEGER_STRING;
        Event<Integer, DatasetGraph> event =
                new SimpleEvent<>(Collections.emptyList(), null, DatasetGraphFactory.create());
        File testEvent = Files.createTempFile("rdf-event", ".foo").toFile();
        writer.write(event, testEvent);

        Event<Integer, DatasetGraph> retrieved = writer.read(testEvent);
        TestYamlEventReaderWriter.verifySameEvent(event, retrieved);
    }
}
