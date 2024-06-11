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
import io.telicent.smart.cache.sources.file.FileEventReaderWriter;
import io.telicent.smart.cache.sources.file.yaml.TestYamlEventReaderWriter;
import io.telicent.smart.cache.sources.kafka.serializers.DatasetGraphDeserializer;
import io.telicent.smart.cache.sources.kafka.serializers.DatasetGraphSerializer;
import org.apache.jena.sparql.core.DatasetGraph;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

public class TestPlaintextEventReaderWriter {

    @Test
    public void plaintext_file_source_01() throws IOException {
        File sourceDir = new File("test-data", "rdf");
        PlainTextFileEventSource<Integer, DatasetGraph> source =
                new PlainTextFileEventSource<>(sourceDir, new DatasetGraphDeserializer());
        PlainTextEventReaderWriter<Integer, DatasetGraph> readerWriter =
                new PlainTextEventReaderWriter<>(new DatasetGraphDeserializer(), new DatasetGraphSerializer());
        while (!source.isExhausted()) {
            Event<Integer, DatasetGraph> event = source.poll(Duration.ZERO);
            Assert.assertNull(event.key());
            Assert.assertNotNull(event.value());

            Assert.assertEquals(event.value().size(), 0);
            Assert.assertEquals(event.value().stream().count(), 2);

            verifyRoundTrip(readerWriter, event);
        }
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Invalid header line.*")
    public void plaintext_file_source_02() throws IOException {
        File f = new File("test-data/malformed1.txt");
        PlainTextEventReaderWriter<Integer, DatasetGraph> readerWriter =
                new PlainTextEventReaderWriter<>(new DatasetGraphDeserializer(), new DatasetGraphSerializer());
        readerWriter.read(f);
    }

    private static <TKey, TValue> void verifyRoundTrip(FileEventReaderWriter<TKey, TValue> writer,
                                                       Event<TKey, TValue> event) throws IOException {
        File f = Files.createTempFile("plaintext-event", ".txt").toFile();
        try {
            writer.write(event, f);
            Assert.assertNotEquals(f.length(), 0L);

            Event<TKey, TValue> retrieved = writer.read(f);
            TestYamlEventReaderWriter.verifySameEvent(event, retrieved);
        } finally {
            f.delete();
        }
    }
}
