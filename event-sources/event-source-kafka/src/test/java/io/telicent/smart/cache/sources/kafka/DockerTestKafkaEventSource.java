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
package io.telicent.smart.cache.sources.kafka;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.telicent.smart.cache.payloads.RdfPayload;
import io.telicent.smart.cache.payloads.RdfPayloadException;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSourceException;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.serializers.RdfPayloadDeserializer;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import io.telicent.smart.cache.sources.offsets.MemoryOffsetStore;
import io.telicent.smart.cache.sources.offsets.OffsetStore;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RDFWriterBuilder;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.common.utils.Bytes;
import org.mockito.Mockito;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class DockerTestKafkaEventSource {

    private BasicKafkaTestCluster kafka;

    @BeforeClass
    public void setup() {
        Utils.logTestClassStarted(this.getClass());
        this.kafka = new BasicKafkaTestCluster();
        this.kafka.setup();
    }

    @AfterClass
    public void teardown() {
        this.kafka.teardown();
        Utils.logTestClassFinished(this.getClass());
    }

    @AfterMethod
    public void testCleanup() throws InterruptedException {
        this.kafka.resetTestTopic();
    }

    @Test
    public void empty_01() {
        KafkaEventSource<Bytes, Bytes> source =
                getEventSource(BytesDeserializer.class, BytesDeserializer.class, "empty_01");
        Assert.assertNull(source.poll(Duration.ofSeconds(3)));
        Assert.assertFalse(source.availableImmediately());
        Assert.assertFalse(source.isExhausted());
        Assert.assertFalse(source.isClosed());

        source.close();
        Assert.assertFalse(source.availableImmediately());
        Assert.assertTrue(source.isExhausted());
        Assert.assertTrue(source.isClosed());
    }

    private <TKey, TValue> KafkaEventSource<TKey, TValue> getEventSource(Class<?> keyDeserializer,
                                                                         Class<?> valueDeserializer,
                                                                         String consumerGroup) {
        return this.<TKey, TValue>buildEventSource(keyDeserializer, valueDeserializer, consumerGroup).build();
    }

    private <TKey, TValue> KafkaEventSource.Builder<TKey, TValue> buildEventSource(Class<?> keyDeserializer,
                                                                                   Class<?> valueDeserializer,
                                                                                   String consumerGroup) {
        return KafkaEventSource.<TKey, TValue>create()
                               .keyDeserializer(keyDeserializer)
                               .valueDeserializer(valueDeserializer)
                               .bootstrapServers(this.kafka.getBootstrapServers())
                               .topic(KafkaTestCluster.DEFAULT_TOPIC)
                               .consumerGroup(consumerGroup);
    }

    @DataProvider(name = "dataSizes")
    private Object[][] getDataSizes() {
        //@formatter:off
        return new Object[][] {
                { 500 },
                { 10_000 }
        };
        //@formatter:on
    }

    @Test(dataProvider = "dataSizes")
    public void basic_01(int size) {
        insertTestEvents(size);

        KafkaEventSource<Integer, String> source =
                getEventSource(IntegerDeserializer.class, StringDeserializer.class, "basic_01_" + size);
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        verifyTestEvents(size, source, 1);
        verifyNoFurtherEvents(source);

        verifyClosure(source);
    }

    public static <TKey, TValue> void verifyNoFurtherEvents(KafkaEventSource<TKey, TValue> source) {
        Assert.assertNull(source.poll(Duration.ofSeconds(1)), "Expected no further events");
    }

    public static List<Event<Integer, String>> verifyTestEvents(int eventsToRead,
                                                                KafkaEventSource<Integer, String> source, int start) {
        Assert.assertTrue(eventsToRead > 0, "Expected number of events to read must be > 0");

        int expected = start;
        int i = 1;
        List<Event<Integer, String>> read = new ArrayList<>();
        while (i <= eventsToRead) {
            Event<Integer, String> next = source.poll(Duration.ofSeconds(3));
            Assert.assertNotNull(next, "Expected Test event " + expected);
            Assert.assertEquals((int) next.key(), expected, "Expected Test event " + expected);
            Assert.assertEquals(next.value(), "Test event " + expected);
            read.add(next);
            expected++;
            i++;
        }

        return read;
    }

    private void insertTestEvents(int size) {
        insertTestEvents(size, KafkaTestCluster.DEFAULT_TOPIC, 0);
    }

    private void insertTestEvents(int size, String topic, int start) {
        try (KafkaSink<Integer, String> sink = KafkaSink.<Integer, String>create()
                                                        .bootstrapServers(this.kafka.getBootstrapServers())
                                                        .topic(topic)
                                                        .keySerializer(IntegerSerializer.class)
                                                        .valueSerializer(StringSerializer.class)
                                                        .lingerMs(5)
                                                        .build()) {
            for (int i = 1; i <= size; i++) {
                SimpleEvent<Integer, String> event =
                        new SimpleEvent<>(Collections.emptyList(), start + i, "Test event " + (start + i));
                sink.send(event);
            }
        }
    }

    @Test(dataProvider = "dataSizes")
    public void close_and_resume_01(int size) {
        insertTestEvents(size);

        for (int start = 1; start < size; start += 500) {
            KafkaEventSource<Integer, String> source =
                    this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                           "close_and_resume_01_" + size)
                        .fromEarliest()
                        .autoCommit()
                        .build();
            Assert.assertFalse(source.isClosed());
            Assert.assertFalse(source.isExhausted());

            verifyTestEvents(500, source, start);

            verifyClosure(source);
        }
    }

    @Test(dataProvider = "dataSizes")
    public void close_and_resume_02(int size) {
        insertTestEvents(size);

        Random random = new Random();
        for (int start = 1; start < size; ) {
            KafkaEventSource<Integer, String> source =
                    this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                           "close_and_resume_02_" + size)
                        .fromEarliest()
                        .autoCommit()
                        .build();
            Assert.assertFalse(source.isClosed());
            Assert.assertFalse(source.isExhausted());

            // Read the records in random chunks of between 10 and 500
            // This helps verify that offsets are reliably committed
            int eventsToRead = random.nextInt(10, 500);
            if (eventsToRead + start > size) {
                eventsToRead = size - start + 1;
            }
            verifyTestEvents(eventsToRead, source, start);

            verifyClosure(source);

            start += eventsToRead;
        }
    }

    @Test(dataProvider = "dataSizes")
    public void close_and_resume_03(int size) {
        insertTestEvents(size);

        for (int start = 1; start < size; start += 500) {
            KafkaEventSource<Integer, String> source =
                    this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                           "close_and_resume_03_" + size)
                        .fromBeginning() // Using fromBeginning() so should always have same start point
                        .autoCommit()
                        .build();
            Assert.assertFalse(source.isClosed());
            Assert.assertFalse(source.isExhausted());

            verifyTestEvents(500, source, 1);

            verifyClosure(source);
        }
    }

    public static <TKey, TValue> void verifyClosure(KafkaEventSource<TKey, TValue> source) {
        source.close();
        Assert.assertTrue(source.isClosed());
    }

    @Test(dataProvider = "dataSizes")
    public void from_end_01(int size) {
        insertTestEvents(size);
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "from_end_01_" + size)
                    .fromEnd() // Using fromEnd() so should never yield any events as we aren't adding new ones
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());
        verifyNoFurtherEvents(source);
        verifyClosure(source);
    }

    @Test(dataProvider = "dataSizes")
    public void from_offset_01(int size) {
        insertTestEvents(size);
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "from_offset_01_" + size)
                    .readPolicy(KafkaReadPolicies.fromOffsets(
                            Map.of(new TopicPartition(KafkaTestCluster.DEFAULT_TOPIC, 0), 100L), 100L))
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        // As we're always reading from Offset 100 we know the starting point and how many events to expect
        verifyTestEvents(size - 100, source, 101);
        verifyNoFurtherEvents(source);
        verifyClosure(source);
    }

    @Test(dataProvider = "dataSizes")
    public void from_offset_02(int size) {
        insertTestEvents(size);
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "from_offset_02_" + size)
                    .readPolicy(KafkaReadPolicies.fromOffsets(
                            Map.of(new TopicPartition(KafkaTestCluster.DEFAULT_TOPIC, 0), (long) size), size))
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        // As we're always reading from an offset greater equal to the actual number of events, and Kafka offsets start
        // from 0 we don't expect any events
        verifyNoFurtherEvents(source);
        verifyClosure(source);
    }

    @Test(dataProvider = "dataSizes")
    public void givenExternalOffset_whenReadingEvents_thenReadingStartsFromProvidedOffset(int size) {
        // Given
        insertTestEvents(size);
        MemoryOffsetStore offsets = new MemoryOffsetStore();
        offsets.saveOffset(KafkaEventSource.externalOffsetStoreKey(KafkaTestCluster.DEFAULT_TOPIC, 0,
                                                                   "from_external_offset_01_" + size), 100L);

        // When
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "from_external_offset_01_" + size)
                    .readPolicy(KafkaReadPolicies.fromExternalOffsets(offsets, 100L))
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        // Then
        // As we're always reading from Offset 100 we know the starting point and how many events to expect
        verifyTestEvents(size - 100, source, 101);
        verifyNoFurtherEvents(source);
        verifyClosure(source);
    }

    @Test(dataProvider = "dataSizes")
    public void givenExternalOffsetBeyondAvailable_whenReadingEvents_thenNothingIsRead(int size) {
        // Given
        insertTestEvents(size);
        MemoryOffsetStore offsets = new MemoryOffsetStore();
        offsets.saveOffset(KafkaEventSource.externalOffsetStoreKey(KafkaTestCluster.DEFAULT_TOPIC, 0,
                                                                   "from_external_offset_02_" + size), (long) size);

        // When
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "from_external_offset_02_" + size)
                    .readPolicy(KafkaReadPolicies.fromExternalOffsets(offsets, 0))
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        // Then
        verifyNoFurtherEvents(source);
        verifyClosure(source);
    }

    @Test(dataProvider = "dataSizes")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void processed_01(int size) {
        insertTestEvents(size);
        String consumerGroup = "processed_01_" + size;
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       consumerGroup).fromBeginning().commitOnProcessed().build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        List<Event<Integer, String>> events = verifyTestEvents(size, source, 1);
        // Calling processed on the main thread should always be safe and lead to offsets being committed immediately
        source.processed((List) events);

        verifyNoFurtherEvents(source);
        verifyClosure(source);

        // If we reopen the source from our existing consumer group offset should be no events as processed() caused our
        // offsets to be committed
        source = this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                        consumerGroup).build();
        verifyNoFurtherEvents(source);
        verifyClosure(source);
    }

    @Test(dataProvider = "dataSizes")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void processed_02(int size) throws ExecutionException, InterruptedException {
        insertTestEvents(size);
        String consumerGroup = "processed_02_" + size;
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       consumerGroup).fromBeginning().commitOnProcessed().build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        List<Event<Integer, String>> events = verifyTestEvents(size, source, 1);
        // Trying to commit the offsets on a background thread isn't permitted because a KafkaConsumer isn't thread safe
        // (see #135).  However, a workaround is in place that delays committing those offsets until the next poll() or
        // close() operation on the main thread
        ExecutorService executor = null;
        try {
            executor = Executors.newSingleThreadExecutor();
            KafkaEventSource<Integer, String> finalSource = source;
            Future<?> future = executor.submit(() -> finalSource.processed((List) events));
            future.get();
        } finally {
            if (executor != null) executor.shutdownNow();
        }

        // Check for no further events, this calls poll() which causes the delayed offset commits to happen
        verifyNoFurtherEvents(source);
        verifyClosure(source);

        // If we reopen the source from our existing consumer group offset should be no events as while processed()
        // cannot commit directly on the background thread we delayed those commits to the next poll()/close() on the
        // main thread
        source = this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                        consumerGroup).build();
        verifyNoFurtherEvents(source);
        verifyClosure(source);
    }

    @Test(dataProvider = "dataSizes")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void processed_03(int size) throws ExecutionException, InterruptedException {
        insertTestEvents(size);
        String consumerGroup = "processed_02_" + size;
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       consumerGroup).fromBeginning().commitOnProcessed().build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        List<Event<Integer, String>> events = verifyTestEvents(size, source, 1);
        // Trying to commit the offsets on a background thread isn't permitted because a KafkaConsumer isn't thread safe
        // (see #135).  However, a workaround is in place that delays committing those offsets until the next poll() or
        // close() operation on the main thread
        ExecutorService executor = null;
        try {
            executor = Executors.newSingleThreadExecutor();
            KafkaEventSource<Integer, String> finalSource = source;
            Future<?> future = executor.submit(() -> finalSource.processed((List) events));
            future.get();
        } finally {
            if (executor != null) executor.shutdownNow();
        }

        // Close the source, this causes our delayed offset commits to happen
        verifyClosure(source);

        // If we reopen the source from our existing consumer group offset should be no events as while processed()
        // cannot commit directly on the background thread the next close() made those commits
        source = this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                        consumerGroup).build();
        verifyNoFurtherEvents(source);
        verifyClosure(source);
    }

    @Test
    public void givenRdfRecordWithIncorrectContentTypeHeader_whenPollingKafkaForDataset_thenRecordDeserializationErrorOccurs() {
        // Given
        injectRdfEventWithWrongContentType();

        // When
        TestLogger testLogger = TestLoggerFactory.getTestLogger(KafkaEventSource.class);
        testLogger.clearAll();
        KafkaDatasetGraphSource<Bytes> source = KafkaDatasetGraphSource.<Bytes>createGraph()
                                                                       .bootstrapServers(
                                                                               this.kafka.getBootstrapServers())
                                                                       .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                                       .consumerGroup("record-deserialization-01")
                                                                       .keyDeserializer(BytesDeserializer.class)
                                                                       .build();

        // Then
        verifyDeserializationErrorOnPoll(source, testLogger);
    }

    private static <T> void verifyDeserializationErrorOnPoll(KafkaEventSource<Bytes, T> source, TestLogger testLogger) {
        try {
            source.poll(Duration.ofSeconds(5));
            Assert.fail("Expected Event Source to throw an error");
        } catch (EventSourceException e) {
            verifyDeserializationError(e, testLogger);
        }
    }

    private static void verifyDeserializationError(EventSourceException e, TestLogger testLogger) {
        // Validate that we got the expected error and we logged it appropriately
        Assert.assertNotNull(e.getCause());
        Assert.assertTrue(e.getCause() instanceof RecordDeserializationException);
        RecordDeserializationException deserErr = (RecordDeserializationException) e.getCause();
        Assert.assertEquals(deserErr.offset(), 0L);
        Assert.assertEquals(deserErr.topicPartition(), new TopicPartition(KafkaTestCluster.DEFAULT_TOPIC, 0));

        Assert.assertTrue(testLogger.getAllLoggingEvents()
                                    .stream()
                                    .map(LoggingEvent::getFormattedMessage)
                                    .anyMatch(m -> StringUtils.contains(m, "Kafka reported error deserializing")));
    }

    @Test
    public void givenRdfRecordWithIncorrectContentTypeHeader_whenPollingKafkaForPayloadWithEagerParsing_thenRecordDeserializationErrorOccurs() {
        // Given
        injectRdfEventWithWrongContentType();

        // When
        TestLogger testLogger = TestLoggerFactory.getTestLogger(KafkaEventSource.class);
        testLogger.clearAll();
        KafkaRdfPayloadSource<Bytes> source = KafkaRdfPayloadSource.<Bytes>createRdfPayload()
                                                                   .bootstrapServers(this.kafka.getBootstrapServers())
                                                                   .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                                   .consumerGroup("record-deserialization-02")
                                                                   .consumerConfig(
                                                                           RdfPayloadDeserializer.EAGER_PARSING_CONFIG_KEY,
                                                                           "true")
                                                                   .keyDeserializer(BytesDeserializer.class)
                                                                   .build();

        verifyDeserializationErrorOnPoll(source, testLogger);
    }

    @Test(expectedExceptions = RdfPayloadException.class)
    public void givenRdfRecordWithIncorrectContentTypeHeader_whenPollingKafkaForRdfPayload_thenPayloadErrorOccurs() {
        // Given
        injectRdfEventWithWrongContentType();

        // When
        KafkaRdfPayloadSource<Bytes> source = KafkaRdfPayloadSource.<Bytes>createRdfPayload()
                                                                   .bootstrapServers(this.kafka.getBootstrapServers())
                                                                   .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                                   .consumerGroup("record-deserialization-03")
                                                                   .keyDeserializer(BytesDeserializer.class)
                                                                   .build();

        Event<Bytes, RdfPayload> payload = source.poll(Duration.ofSeconds(5));

        try {
            // Then
            Assert.assertTrue(payload.value().isDataset());
            Assert.assertFalse(payload.value().isReady());
            payload.value().getDataset();
        } finally {
            source.close();
        }
    }

    private void injectRdfEventWithWrongContentType() {
        try (KafkaSink<Bytes, Bytes> sink = KafkaSink.<Bytes, Bytes>create()
                                                     .bootstrapServers(this.kafka.getBootstrapServers())
                                                     .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                     .keySerializer(BytesSerializer.class)
                                                     .valueSerializer(BytesSerializer.class)
                                                     .build()) {

            // Intentionally inject a record onto the topic where the declared Content-Type DOES NOT match the actual
            // serialisation used for the RDF data.  This will result in an RDF event source failing to deserialize the
            // offending record.
            Graph g = GraphFactory.createDefaultGraph();
            Node node = NodeFactory.createURI("https://example.org/nodes");
            g.getPrefixMapping().setNsPrefix("ex", "https://example.org/");
            g.add(node, node, node);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            RDFWriter writer = RDFWriterBuilder.create().source(g).lang(Lang.TURTLE).build();
            writer.output(output);
            SimpleEvent<Bytes, Bytes> event =
                    new SimpleEvent<>(List.of(new Header(HttpNames.hContentType, "application/n-triples")), null,
                                      Bytes.wrap(output.toByteArray()));
            sink.send(event);
        }
    }

    @Test(dataProvider = "dataSizes")
    public void givenExternalOffsetStoreAndAutoCommit_whenReadingEvents_thenExternalOffsetStoreIsUpdated(int size) {
        // Given
        insertTestEvents(size);
        OffsetStore store = Mockito.mock(OffsetStore.class);
        for (int start = 1; start < size; start += 500) {
            KafkaEventSource<Integer, String> source =
                    this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                           "external_offset_store_01_" + size)
                        .fromEarliest()
                        .autoCommit()
                        .externalOffsetStore(store)
                        .build();
            Assert.assertFalse(source.isClosed());
            Assert.assertFalse(source.isExhausted());

            // When
            verifyTestEvents(500, source, start);
            verifyClosure(source);

            // Then
            // Remember Kafka offsets start from zero but our test counter starts from 1
            Mockito.verify(store, Mockito.atLeastOnce()).saveOffset(Mockito.any(), Mockito.eq(start - 1 + 500L));
        }
    }

    @Test(dataProvider = "dataSizes")
    public void givenExternalOffsetStoreAndCommitOnProcessed_whenReadingEventsWithoutCallingProcessed_thenExternalOffsetStoreIsNotUpdated(
            int size) {
        // Given
        insertTestEvents(size);
        OffsetStore store = Mockito.mock(OffsetStore.class);
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "external_offset_store_02_" + size)
                    .fromEarliest()
                    .commitOnProcessed()
                    .externalOffsetStore(store)
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        // When
        verifyTestEvents(500, source, 1);
        verifyClosure(source);

        // Then
        Mockito.verify(store, Mockito.never()).saveOffset(Mockito.any(), Mockito.any());
    }

    @Test(dataProvider = "dataSizes")
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void givenExternalOffsetStoreAndCommitOnProcessed_whenReadingEventsAndCallingProcessed_thenExternalOffsetStoreIsUpdated(
            int size) {
        // Given
        insertTestEvents(size);
        OffsetStore store = Mockito.mock(OffsetStore.class);
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "external_offset_store_02_" + size)
                    .fromEarliest()
                    .commitOnProcessed()
                    .externalOffsetStore(store)
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());

        // When
        List<Event<Integer, String>> events = verifyTestEvents(500, source, 1);
        source.processed((List)events);
        verifyClosure(source);

        // Then
        Mockito.verify(store, Mockito.atLeastOnce()).saveOffset(Mockito.any(), Mockito.eq(500L));
    }

    @Test(dataProvider = "dataSizes")
    @SuppressWarnings("rawtypes")
    public void givenExternalOffsetStoreAndCommitOnProcessed_whenReadingEvents_thenExternalOffsetStoreIsUpdated(
            int size) {
        // Given
        insertTestEvents(size);
        OffsetStore store = Mockito.mock(OffsetStore.class);
        for (int start = 1; start < size; start += 500) {
            KafkaEventSource<Integer, String> source =
                    this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                           "external_offset_store_03_" + size)
                        .fromEarliest()
                        .commitOnProcessed()
                        .externalOffsetStore(store)
                        .build();
            Assert.assertFalse(source.isClosed());
            Assert.assertFalse(source.isExhausted());

            // When
            Collection<Event> events = new ArrayList<>(verifyTestEvents(500, source, start));
            source.processed(events);
            verifyClosure(source);

            // Then
            // Remember Kafka offsets start from zero but our test counter starts from 1
            Mockito.verify(store, Mockito.atLeastOnce()).saveOffset(Mockito.any(), Mockito.eq(start - 1 + 500L));
        }
    }

    @Test(dataProvider = "dataSizes")
    public void givenOffsetsToReset_whenResettingOffsetsAfterReadingEvents_thenPreviousEventsAreReadAgain(int size) {
        // Given
        insertTestEvents(size);
        Map<TopicPartition, Long> resets = Map.of(new TopicPartition(KafkaTestCluster.DEFAULT_TOPIC, 0), 0L);

        // When
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "offset_reset_01_" + size)
                    .fromBeginning() // Using fromBeginning() so should always have same start point
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());
        verifyTestEvents(10, source, 1);
        source.resetOffsets(resets);

        // Then
        verifyTestEvents(10, source, 1);
        verifyClosure(source);
    }

    @Test(dataProvider = "dataSizes")
    public void givenOffsetsToReset_whenResettingOffsetsPriorToReadingEvents_thenReadingStartsFromCorrectOffset(
            int size) {
        // Given
        insertTestEvents(size);
        Map<TopicPartition, Long> resets = Map.of(new TopicPartition(KafkaTestCluster.DEFAULT_TOPIC, 0), 99L);

        // When
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "offset_reset_02_" + size)
                    .fromBeginning() // Using fromBeginning() so should always have same start point
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());
        source.resetOffsets(resets);

        // Then
        verifyTestEvents(10, source, 100);
        verifyClosure(source);
    }

    @Test(dataProvider = "dataSizes")
    public void givenOffsetsToReset_whenResettingOffsetsFromDifferentThread_thenNextReadStartsFromCorrectOffset(
            int size) {
        // Given
        insertTestEvents(size);
        Map<TopicPartition, Long> resets = Map.of(new TopicPartition(KafkaTestCluster.DEFAULT_TOPIC, 0), 99L);

        // When
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "offset_reset_02_" + size)
                    .fromBeginning() // Using fromBeginning() so should always have same start point
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());
        // Just read some events to make sure the consumer is started
        verifyTestEvents(10, source, 1);
        // Try to reset offsets from a different thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> source.resetOffsets(resets));
        try {
            future.get(5, TimeUnit.SECONDS);

            // Then
            verifyTestEvents(10, source, 100);
        } catch (Throwable e) {
            Assert.fail("Failed to reset offsets from different thread");
        } finally {
            verifyClosure(source);
        }
    }

    @Test(dataProvider = "dataSizes")
    public void givenOffsetsToReset_whenResettingOffsetsBeyondAvailableEvents_thenReadingProductsNothing(int size) {
        // Given
        insertTestEvents(size);
        Map<TopicPartition, Long> resets = Map.of(new TopicPartition(KafkaTestCluster.DEFAULT_TOPIC, 0), size + 100L);

        // When
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "offset_reset_03_" + size)
                    .fromBeginning() // Using fromBeginning() so should always have same start point
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());
        source.resetOffsets(resets);

        // Then
        verifyNoFurtherEvents(source);
        verifyClosure(source);
    }

    @Test(expectedExceptions = EventSourceException.class)
    public void givenOffsetsToReset_whenResettingOffsetsToNegative_thenErrorsOnNextRead() {
        // Given
        insertTestEvents(10);
        Map<TopicPartition, Long> resets = Map.of(new TopicPartition(KafkaTestCluster.DEFAULT_TOPIC, 0), -1L);

        // When
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "offset_reset_negative")
                    .fromBeginning() // Using fromBeginning() so should always have same start point
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());
        source.resetOffsets(resets);

        // Then
        try {
            source.poll(Duration.ofSeconds(5));
        } finally {
            verifyClosure(source);
        }
    }

    @Test
    public void givenNoOffsets_whenResettingOffsets_thenNoOp() {
        // Given
        insertTestEvents(10);
        Map<TopicPartition, Long> resets = Collections.emptyMap();

        // When
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "offset_reset_none")
                    .fromBeginning() // Using fromBeginning() so should always have same start point
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());
        source.resetOffsets(resets);

        // Then
        verifyTestEvents(10, source, 1);
        verifyClosure(source);
    }

    @Test
    public void givenNonApplicableOffsets_whenResettingOffsets_thenNoOp() {
        // Given
        insertTestEvents(10);
        Map<TopicPartition, Long> resets = Map.of(new TopicPartition("other", 0), 100L);

        // When
        KafkaEventSource<Integer, String> source =
                this.<Integer, String>buildEventSource(IntegerDeserializer.class, StringDeserializer.class,
                                                       "offset_reset_non_applicable")
                    .fromBeginning() // Using fromBeginning() so should always have same start point
                    .autoCommit()
                    .build();
        Assert.assertFalse(source.isClosed());
        Assert.assertFalse(source.isExhausted());
        source.resetOffsets(resets);

        // Then
        verifyTestEvents(10, source, 1);
        verifyClosure(source);
    }
}
