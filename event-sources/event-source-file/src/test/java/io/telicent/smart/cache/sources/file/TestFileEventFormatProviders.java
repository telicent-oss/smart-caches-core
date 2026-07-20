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

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestFileEventFormatProviders {

    @Test
    public void yaml_registered() {
        Assert.assertNotNull(FileEventFormats.get("yaml"));
    }

    @Test
    public void plaintext_registered() {
        Assert.assertNotNull(FileEventFormats.get("text"));
    }

    @Test
    public void rdf_registered() {
        Assert.assertNotNull(FileEventFormats.get("rdf"));
    }

    @DataProvider(name = "formats")
    public Object[][] getRegisteredFormats() {
        List<String> available = new ArrayList<>(FileEventFormats.available());
        Object[][] formats = new Object[available.size()][];
        for (int i = 0; i < available.size(); i++) {
            formats[i] = new Object[] { available.get(i) };
        }
        return formats;
    }

    @Test(dataProvider = "formats")
    public void name_01(String format) {
        FileEventFormatProvider provider = FileEventFormats.get(format);
        Assert.assertNotNull(provider);
        Assert.assertNotNull(provider.name());
        Assert.assertFalse(StringUtils.isBlank(provider.name()));
    }

    @Test(dataProvider = "formats")
    public void default_file_extension_01(String format) {
        FileEventFormatProvider provider = FileEventFormats.get(format);
        Assert.assertNotNull(provider);
        Assert.assertNotNull(provider.defaultFileExtension());
        Assert.assertFalse(StringUtils.isBlank(provider.defaultFileExtension()));
    }

    @Test(dataProvider = "formats")
    public void create_reader_01(String format) {
        FileEventFormatProvider provider = FileEventFormats.get(format);
        FileEventReader<Integer, String> reader =
                provider.createReader(Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER);
        Assert.assertNotNull(reader);

        if (reader instanceof FileEventReaderWriter<?, ?> fileEventReaderWriter) {
            Assert.assertEquals(fileEventReaderWriter.getMode(), FileEventAccessMode.ReadOnly);
        }
    }

    @Test(dataProvider = "formats")
    public void create_writer_01(String format) {
        FileEventFormatProvider provider = FileEventFormats.get(format);
        FileEventWriter<Integer, String> writer =
                provider.createWriter(Serdes.INTEGER_SERIALIZER, Serdes.STRING_SERIALIZER);
        Assert.assertNotNull(writer);

        if (writer instanceof FileEventReaderWriter<?, ?> fileEventReaderWriter) {
            Assert.assertEquals(fileEventReaderWriter.getMode(), FileEventAccessMode.WriteOnly);
        }
    }

    @Test(dataProvider = "formats")
    public void create_reader_writer_01(String format) {
        FileEventFormatProvider provider = FileEventFormats.get(format);
        FileEventReaderWriter<Integer, String> readerWriter =
                provider.createReaderWriter(Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER,
                                            Serdes.INTEGER_SERIALIZER, Serdes.STRING_SERIALIZER);
        Assert.assertNotNull(readerWriter);
        Assert.assertNotNull(readerWriter.getMode(), "Null mode for format " + format);
        Assert.assertEquals(readerWriter.getMode(), FileEventAccessMode.ReadWrite);
    }

    @Test(dataProvider = "formats")
    public void create_source_01(String format) {
        FileEventFormatProvider provider = FileEventFormats.get(format);
        File workingDir = new File(Paths.get(".").toAbsolutePath().toString());
        FileEventSource<Integer, String> source =
                provider.createSource(Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER, workingDir);
        Assert.assertFalse(source.availableImmediately());
        Assert.assertTrue(source.isExhausted());
        Assert.assertEquals(source.remaining(), 0L);

        source.close();
    }

    @Test(dataProvider = "formats")
    public void create_source_02(String format) {
        FileEventFormatProvider provider = FileEventFormats.get(format);
        File workingDir = new File(Paths.get("pom.xml").toAbsolutePath().toString());
        FileEventSource<Integer, String> source =
                provider.createSingleFileSource(Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER, workingDir);
        Assert.assertTrue(source.availableImmediately());
        Assert.assertFalse(source.isExhausted());
        Assert.assertEquals(source.remaining(), 1L);

        source.close();
    }

    @Test(dataProvider = "formats")
    public void create_async_source_01(String format) {
        FileEventFormatProvider provider = FileEventFormats.get(format);
        File workingDir = new File(Paths.get(".").toAbsolutePath().toString());
        FileEventSource<Integer, String> source =
                provider.createSource(Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER, workingDir, true);
        Assert.assertFalse(source.availableImmediately());
        Assert.assertTrue(source.isExhausted());
        Assert.assertEquals(source.remaining(), 0L);

        source.close();
    }

    @Test(dataProvider = "formats")
    public void create_async_source_02(String format) {
        FileEventFormatProvider provider = FileEventFormats.get(format);
        File workingDir = new File(Paths.get("pom.xml").toAbsolutePath().toString());
        FileEventSource<Integer, String> source =
                provider.createSingleFileSource(Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER, workingDir,
                                                true);
        Assert.assertFalse(source.isExhausted());
        Assert.assertEquals(source.remaining(), 1L);

        source.close();
    }

    @Test
    public void default_async_overloads_delegate_01() {
        final CountingProvider provider = new CountingProvider();
        final File file = new File(".");

        // The default asynchronous overloads should delegate straight through to the synchronous variants
        provider.createSource(Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER, file, true);
        provider.createSingleFileSource(Serdes.INTEGER_DESERIALIZER, Serdes.STRING_DESERIALIZER, file, true);

        Assert.assertEquals(provider.sourceCalls, 1);
        Assert.assertEquals(provider.singleFileCalls, 1);
    }

    /**
     * A minimal provider that does not override the asynchronous {@code createSource}/{@code createSingleFileSource}
     * overloads, so that the default methods on {@link FileEventFormatProvider} (which delegate to the synchronous
     * variants) are exercised. All the concrete providers override these overloads, so without this the defaults
     * are never invoked.
     */
    private static final class CountingProvider implements FileEventFormatProvider {
        int sourceCalls = 0;
        int singleFileCalls = 0;

        @Override
        public String name() {
            return "counting";
        }

        @Override
        public String defaultFileExtension() {
            return ".counting";
        }

        @Override
        public <TKey, TValue> FileEventReader<TKey, TValue> createReader(Deserializer<TKey> keyDeserializer,
                                                                         Deserializer<TValue> valueDeserializer) {
            return null;
        }

        @Override
        public <TKey, TValue> FileEventWriter<TKey, TValue> createWriter(Serializer<TKey> keySerializer,
                                                                         Serializer<TValue> valueSerializer) {
            return null;
        }

        @Override
        public <TKey, TValue> FileEventReaderWriter<TKey, TValue> createReaderWriter(
                Deserializer<TKey> keyDeserializer, Deserializer<TValue> valueDeserializer,
                Serializer<TKey> keySerializer, Serializer<TValue> valueSerializer) {
            return null;
        }

        @Override
        public <TKey, TValue> FileEventSource<TKey, TValue> createSource(Deserializer<TKey> keyDeserializer,
                                                                         Deserializer<TValue> valueDeserializer,
                                                                         File source) {
            this.sourceCalls++;
            return null;
        }

        @Override
        public <TKey, TValue> FileEventSource<TKey, TValue> createSingleFileSource(Deserializer<TKey> keyDeserializer,
                                                                                   Deserializer<TValue> valueDeserializer,
                                                                                   File source) {
            this.singleFileCalls++;
            return null;
        }
    }

}
