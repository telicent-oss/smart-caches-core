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
package io.telicent.smart.cache.sources.combiner;

import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.projectors.sinks.events.splitter.BadCombiningSplitter;
import io.telicent.smart.cache.projectors.sinks.events.splitter.ExampleSplittablePayload;
import io.telicent.smart.cache.projectors.sinks.events.splitter.ExampleSplitter;
import io.telicent.smart.cache.projectors.sinks.events.splitter.SplitterSink;
import io.telicent.smart.cache.sources.*;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestCombiningEventSource {

    private static final ExampleSplitter<UUID> EXAMPLE_SPLITTER = new ExampleSplitter<>();

    private static final String RANDOM_TEXT_1KB = RandomStringUtils.insecure().nextAlphanumeric(1024);
    private static final List<Event<UUID, ExampleSplittablePayload>> RANDOM_TEXT_1KB_ODD_SIZED_CHUNKS =
            generateChunks(RANDOM_TEXT_1KB, 117);
    private static final List<Event<UUID, ExampleSplittablePayload>> RANDOM_TEXT_1KB_SMALL_CHUNKS =
            generateChunks(RANDOM_TEXT_1KB, 64);
    private static final String RANDOM_TEXT_1MB = RandomStringUtils.insecure().nextAlphanumeric(1024 * 1024);
    private static final List<Event<UUID, ExampleSplittablePayload>> RANDOM_TEXT_1MB_CHUNKS =
            generateChunks(RANDOM_TEXT_1MB, 1024 * 512);
    private static final String RANDOM_TEXT_10MB = RandomStringUtils.insecure().nextAlphanumeric(1024 * 1024 * 10);
    private static final List<Event<UUID, ExampleSplittablePayload>> RANDOM_TEXT_10MB_CHUNKS =
            generateChunks(RANDOM_TEXT_10MB, 1024 * 1024);
    private static final String SIMPLE_TEXT = "this is a test";
    private static final List<Event<UUID, ExampleSplittablePayload>> SIMPLE_TEXT_UNCHUNKED =
            generateChunks(SIMPLE_TEXT, 1024);
    private static final List<Event<UUID, ExampleSplittablePayload>> SIMPLE_TEXT_SMALL_CHUNKS =
            generateChunks(SIMPLE_TEXT, 8);

    private static List<Event<UUID, ExampleSplittablePayload>> generateChunks(String content, int chunkSize) {
        ExampleSplittablePayload original = ExampleSplittablePayload.builder()
                                                                    .title("Test")
                                                                    .payload(content.getBytes(StandardCharsets.UTF_8))
                                                                    .build();
        try (CollectorSink<Event<UUID, ExampleSplittablePayload>> collector = CollectorSink.of()) {
            try (SplitterSink<UUID, ExampleSplittablePayload> splitter = SplitterSink.<UUID, ExampleSplittablePayload>create()
                                                                                     .chunkSize(chunkSize)
                                                                                     .destination(collector)
                                                                                     .splitter(EXAMPLE_SPLITTER)
                                                                                     .build()) {
                splitter.send(new SimpleEvent<>(Collections.emptyList(), UUID.randomUUID(), original));

                return new ArrayList<>(collector.get());
            }
        }
    }

    @DataProvider(name = "rawData")
    private Object[][] rawData() {
        return new Object[][] {
                { SIMPLE_TEXT, SIMPLE_TEXT_SMALL_CHUNKS }, { SIMPLE_TEXT, SIMPLE_TEXT_UNCHUNKED },
                // 1 KB input with 64 byte chunk
                { RANDOM_TEXT_1KB, RANDOM_TEXT_1KB_SMALL_CHUNKS },
                // 1 KB input with weird non-even chunk size
                { RANDOM_TEXT_1KB, RANDOM_TEXT_1KB_ODD_SIZED_CHUNKS },
                // 1 MB input with 512 KB chunk
                { RANDOM_TEXT_1MB, RANDOM_TEXT_1MB_CHUNKS },
                // 10 MB input with 1 MB chunk
                //{ RANDOM_TEXT_10MB, RANDOM_TEXT_10MB_CHUNKS },
        };
    }

    private static CombiningEventSource<UUID, ExampleSplittablePayload> createCombiningSource(
            List<Event<UUID, ExampleSplittablePayload>> chunks) {
        return createCombiningSource(chunks, null);
    }

    private static CombiningEventSource<UUID, ExampleSplittablePayload> createCombiningSource(
            List<Event<UUID, ExampleSplittablePayload>> chunks, Sink<Event<UUID, ExampleSplittablePayload>> dlq) {
        InMemoryEventSource<UUID, ExampleSplittablePayload> source = new InMemoryEventSource<>(chunks);
        return new CombiningEventSource<>(source, EXAMPLE_SPLITTER, dlq);
    }

    @Test(dataProvider = "rawData")
    public void givenChunkedEvents_whenCombiningSourceUsed_thenRecombinedCorrectly(String originalText,
                                                                                   List<Event<UUID, ExampleSplittablePayload>> chunks) {
        // Given
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When
        Event<UUID, ExampleSplittablePayload> recombined = combiner.poll(Duration.ofSeconds(5));

        // Then
        verifyRecombined(originalText, recombined);
    }

    private static void verifyRecombined(String originalText, Event<UUID, ExampleSplittablePayload> recombined) {
        // Recombined value should equal original input value
        Assert.assertNotNull(recombined);
        Assert.assertEquals(new String(recombined.value().getPayload()), originalText);

        // Should be no Chunk headers on the recombined event
        Assert.assertTrue(StringUtils.isBlank(recombined.lastHeader(TelicentHeaders.CHUNK_ID)));
        Assert.assertTrue(StringUtils.isBlank(recombined.lastHeader(TelicentHeaders.CHUNK_CHECKSUM)));
        Assert.assertTrue(StringUtils.isBlank(recombined.lastHeader(TelicentHeaders.CHUNK_HASH)));
    }

    @Test(dataProvider = "rawData", expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Received bad chunk event.*")
    public void givenChunkedEvents_whenCombiningSourceUsedWithBadSplitter_thenRecombiningFails(String originalText,
                                                                                               List<Event<UUID, ExampleSplittablePayload>> chunks) {
        // Given
        if (chunks.size() == 1) {
            requireAtLeastTwoChunks();
        }
        InMemoryEventSource<UUID, ExampleSplittablePayload> source = new InMemoryEventSource<>(chunks);
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner =
                new CombiningEventSource<>(source, new BadCombiningSplitter<>(), null);

        // When and Then
        Event<UUID, ExampleSplittablePayload> recombined = combiner.poll(Duration.ofSeconds(5));
    }

    @Test(dataProvider = "rawData", invocationCount = 5)
    public void givenChunkedEventsOutOfOrder_whenCombiningSourceUsed_thenRecombinedCorrectly(String originalText,
                                                                                             List<Event<UUID, ExampleSplittablePayload>> chunks) {
        // Given
        // NB - Intentionally shuffle the chunks so they arrive out of order
        List<Event<UUID, ExampleSplittablePayload>> shuffled = new ArrayList<>(chunks);
        Collections.shuffle(shuffled);
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When
        Event<UUID, ExampleSplittablePayload> recombined = combiner.poll(Duration.ofSeconds(5));

        // Then
        verifyRecombined(originalText, recombined);
    }

    @Test(dataProvider = "rawData")
    public void givenChunkedDocumentWithSomeDuplicateChunks_whenCombiningSourceUsed_thenRecombinedCorrectly(
            String originalText, List<Event<UUID, ExampleSplittablePayload>> chunks) {
        // Given
        // NB - Intentionally duplicate some chunks
        List<Event<UUID, ExampleSplittablePayload>> duplicated = new ArrayList<>(chunks);
        if (chunks.size() > 1) {
            for (int i = 0; i < duplicated.size() - 1; i += 2) {
                duplicated.add(i, duplicated.get(i));
            }
        }
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When
        Event<UUID, ExampleSplittablePayload> recombined = combiner.poll(Duration.ofSeconds(5));

        // Then
        verifyRecombined(originalText, recombined);
    }

    @Test(dataProvider = "rawData")
    public void givenChunkedDocumentWithLastChunkDuplicated_whenCombiningSourceUsed_thenRecombinedOnce_andOnlyOnce(
            String originalText, List<Event<UUID, ExampleSplittablePayload>> chunks) {
        // Given
        // NB - Intentionally duplicate last chunk
        List<Event<UUID, ExampleSplittablePayload>> duplicated = new ArrayList<>(chunks);
        if (chunks.size() > 1) {
            duplicated.add(duplicated.get(chunks.size() - 1));
        }

        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When
        Event<UUID, ExampleSplittablePayload> recombined = combiner.poll(Duration.ofSeconds(5));

        // Then
        verifyRecombined(originalText, recombined);

        // And
        Assert.assertNull(combiner.poll(Duration.ofSeconds(5)));
    }

    @Test(dataProvider = "rawData")
    @SuppressWarnings("unused")
    public void givenChunkedDocumentWithAChunkMissing_whenCombiningSourceUsed_thenNothingCombined(String originalText,
                                                                                                  List<Event<UUID, ExampleSplittablePayload>> chunks) {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> someMissing = new ArrayList<>(chunks);
        if (chunks.size() > 1) {
            someMissing.remove(RandomUtils.insecure().randomInt(0, someMissing.size()));
        } else {
            requireAtLeastTwoChunks();
        }

        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(someMissing);

        // When
        Event<UUID, ExampleSplittablePayload> recombined = combiner.poll(Duration.ofSeconds(5));

        // Then
        Assert.assertNull(recombined);
    }

    private static void requireAtLeastTwoChunks() {
        throw new SkipException("Test only makes sense for events that are chunked");
    }

    @Test(dataProvider = "rawData")
    @SuppressWarnings("unused")
    public void givenChunksFromSlowSource_whenCombiningSourceUsed_thenNothingCombined(String originalText,
                                                                                      List<Event<UUID, ExampleSplittablePayload>> chunks) {
        // Given
        if (chunks.size() <= 1) {
            requireAtLeastTwoChunks();
        }
        InMemoryEventSource<UUID, ExampleSplittablePayload> inMemory = new InMemoryEventSource<>(chunks);
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = new CombiningEventSource<>(
                SlowEventSource.<UUID, ExampleSplittablePayload>builder()
                               .delegate(inMemory)
                               .pollDelay(Duration.ofSeconds(3))
                               .build(), EXAMPLE_SPLITTER, null);

        // When
        Event<UUID, ExampleSplittablePayload> recombined = combiner.poll(Duration.ofSeconds(5));

        // Then
        Assert.assertNull(recombined);
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Received bad chunk event.*")
    public void givenChunksWithInconsistentTotal_whenCombiningSourceUsed_thenFails() {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        List<EventHeader> modified = chunks.get(1).headers().collect(Collectors.toCollection(ArrayList::new));
        modified.removeIf(h -> Objects.equals(h.key(), TelicentHeaders.CHUNK_TOTAL));
        modified.add(new Header(TelicentHeaders.CHUNK_TOTAL, "3"));
        chunks.set(1, chunks.get(1).replaceHeaders(modified.stream()));
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When and Then
        combiner.poll(Duration.ofSeconds(5));
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Received bad chunk event.*")
    public void givenChunksWithMissingChecksum_whenCombiningSourceUsed_thenFails() {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        List<EventHeader> modified = chunks.get(1).headers().collect(Collectors.toCollection(ArrayList::new));
        modified.removeIf(
                h -> Strings.CI.equalsAny(h.key(), TelicentHeaders.CHUNK_CHECKSUM, TelicentHeaders.ORIGINAL_CHECKSUM));
        chunks.set(1, chunks.get(1).replaceHeaders(modified.stream()));
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When and Then
        combiner.poll(Duration.ofSeconds(5));
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Received bad chunk event.*")
    public void givenChunksWithIncorrectChecksum_whenCombiningSourceUsed_thenFails() {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        List<EventHeader> modified = chunks.get(1).headers().collect(Collectors.toCollection(ArrayList::new));
        modified.removeIf(h -> Objects.equals(h.key(), TelicentHeaders.CHUNK_CHECKSUM));
        modified.add(new Header(TelicentHeaders.CHUNK_CHECKSUM, "12345/6789"));
        chunks.set(1, chunks.get(1).replaceHeaders(modified.stream()));
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When and Then
        combiner.poll(Duration.ofSeconds(5));
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Received bad chunk event.*")
    public void givenChunksWithIncorrectDocumentChecksum_whenCombiningSourceUsed_thenFails() {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        List<EventHeader> modifiedHeaders = chunks.get(1).headers().collect(Collectors.toCollection(ArrayList::new));
        modifiedHeaders.removeIf(h -> Objects.equals(h.key(), TelicentHeaders.ORIGINAL_CHECKSUM));
        modifiedHeaders.add(new Header(TelicentHeaders.ORIGINAL_CHECKSUM, "crc32:0"));
        chunks.set(1, chunks.get(1).replaceHeaders(modifiedHeaders.stream()));
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When and Then
        combiner.poll(Duration.ofSeconds(5));
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Received bad chunk event.*")
    public void givenChunksWithCorruptedData_whenCombiningSourceUsed_thenFails() {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        chunks.set(1, chunks.get(1).replaceValue(corrupt(chunks.get(1).value())));
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When and Then
        combiner.poll(Duration.ofSeconds(5));
    }

    private ExampleSplittablePayload corrupt(ExampleSplittablePayload value) {
        byte[] original = value.getPayload();
        byte[] corrupted = Arrays.copyOf(original, original.length);
        // Walk through the data changing a handful of bytes
        for (int i = 0, inc = 1; i < corrupted.length; inc++, i += inc) {
            corrupted[i]++;
        }
        return ExampleSplittablePayload.builder().title(value.getTitle()).payload(corrupted).build();
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Received bad chunk event.*")
    public void givenChunksWithIncorrectHash_whenCombiningSourceUsed_thenFails() {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        List<EventHeader> modified = chunks.get(1).headers().collect(Collectors.toCollection(ArrayList::new));
        modified.removeIf(h -> Objects.equals(h.key(), TelicentHeaders.CHUNK_HASH));
        modified.add(new Header(TelicentHeaders.CHUNK_HASH, "sha256:abc123"));
        chunks.set(1, chunks.get(1).replaceHeaders(modified.stream()));
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When and Then
        combiner.poll(Duration.ofSeconds(5));
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Received bad chunk event.*")
    public void givenChunksWithIncorrectDocumentHash_whenCombiningSourceUsed_thenFails() {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        List<EventHeader> modifiedHeaders = chunks.get(1).headers().collect(Collectors.toCollection(ArrayList::new));
        modifiedHeaders.removeIf(h -> Objects.equals(h.key(), TelicentHeaders.ORIGINAL_HASH));
        modifiedHeaders.add(new Header(TelicentHeaders.ORIGINAL_HASH, "sha256:abc123"));
        chunks.set(1, chunks.get(1).replaceHeaders(modifiedHeaders.stream()));
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When and Then
        combiner.poll(Duration.ofSeconds(5));
    }

    @DataProvider(name = "unsplitData")
    private Object[][] unsplitData() {
        return new Object[][] {
                { SIMPLE_TEXT }, { RANDOM_TEXT_1KB }, { RANDOM_TEXT_1MB }
        };
    }

    @Test(dataProvider = "unsplitData")
    public void givenNonChunkedEvents_whenCombiningSourceUsed_thenReturnedUnmodified(String originalData) {
        // Given
        Event<UUID, ExampleSplittablePayload> event = new SimpleEvent<>(null, UUID.randomUUID(),
                                                                        ExampleSplittablePayload.builder()
                                                                                                .title("Test")
                                                                                                .payload(
                                                                                                        originalData.getBytes(
                                                                                                                StandardCharsets.UTF_8))
                                                                                                .build());
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(List.of(event));

        // When
        Event<UUID, ExampleSplittablePayload> result = combiner.poll(Duration.ofSeconds(5));

        // Then
        Assert.assertEquals(result, event);
        Assert.assertSame(result, event);
    }

    private static void verifyFailsWhenRequiredHeaderIsRemoved(String requiredHeader) {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        List<EventHeader> modified = chunks.get(1).headers().collect(Collectors.toCollection(ArrayList::new));
        modified.removeIf(h -> Objects.equals(h.key(), requiredHeader));
        chunks.set(1, chunks.get(1).replaceHeaders(modified.stream()));
        CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks);

        // When and Then
        combiner.poll(Duration.ofSeconds(5));
    }

    private void verifyGoesToDlqWhenRequiredHeaderIsRemoved(String requiredHeader, String... expectedDlqReasons) {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        List<EventHeader> modified = chunks.get(1).headers().collect(Collectors.toCollection(ArrayList::new));
        modified.removeIf(h -> Objects.equals(h.key(), requiredHeader));
        chunks.set(1, chunks.get(1).replaceHeaders(modified.stream()));
        try (CollectorSink<Event<UUID, ExampleSplittablePayload>> dlq = CollectorSink.of()) {
            CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks, dlq);

            // When and Then
            Assert.assertNull(combiner.poll(Duration.ofSeconds(5)));

            // And
            verifyDeadLetterReasons(dlq, expectedDlqReasons);
        }
    }

    private void verifyGoesToDlqWhenHeaderIsModified(String header, String modifiedValue,
                                                     String... expectedDlqReasons) {
        verifyGoesToDlqWhenHeaderIsModified(header, x -> modifiedValue, expectedDlqReasons);
    }

    private void verifyGoesToDlqWhenHeaderIsModified(String header, Function<String, String> modifier,
                                                     String... expectedDlqReasons) {
        // Given
        List<Event<UUID, ExampleSplittablePayload>> chunks = generateChunks("This is a test", 8);
        List<EventHeader> modified = chunks.get(1).headers().collect(Collectors.toCollection(ArrayList::new));
        String originalValue = chunks.get(1).lastHeader(header);
        modified.removeIf(h -> Objects.equals(h.key(), header));
        modified.add(new Header(header, modifier.apply(originalValue)));
        chunks.set(1, chunks.get(1).replaceHeaders(modified.stream()));
        try (CollectorSink<Event<UUID, ExampleSplittablePayload>> dlq = CollectorSink.of()) {
            CombiningEventSource<UUID, ExampleSplittablePayload> combiner = createCombiningSource(chunks, dlq);

            // When and Then
            Assert.assertNull(combiner.poll(Duration.ofSeconds(5)));

            // And
            verifyDeadLetterReasons(dlq, expectedDlqReasons);
        }
    }

    private void verifyDeadLetterReasons(CollectorSink<Event<UUID, ExampleSplittablePayload>> dlq, String... reasons) {
        Assert.assertFalse(dlq.get().isEmpty());
        for (String reason : reasons) {
            Assert.assertTrue(dlq.get()
                                 .stream()
                                 .map(e -> e.lastHeader(TelicentHeaders.DEAD_LETTER_REASON))
                                 .filter(Objects::nonNull)
                                 .anyMatch(r -> Strings.CI.contains(r, reason)),
                              "DLQ did not contain an event with dead letter reason '" + reason + "'");
        }
    }

    @Test(expectedExceptions = EventSourceException.class, expectedExceptionsMessageRegExp = "Received bad chunk event.*")
    public void givenChunksWithMissingSplitId_whenCombiningSourceUsed_thenFails() {
        verifyFailsWhenRequiredHeaderIsRemoved(TelicentHeaders.SPLIT_ID);
    }

    @Test
    public void givenChunksWithMissingSplitId_whenCombiningSourceUsedWithDlq_thenNothingCombined_andSentToDlq() {
        verifyGoesToDlqWhenRequiredHeaderIsRemoved(TelicentHeaders.SPLIT_ID, "missing mandatory Split-ID header");
    }

    @Test
    public void givenChunksWithInconsistentTotal_whenCombiningSourceUsedWithDlq_thenNothingCombined_andSentToDlq() {
        verifyGoesToDlqWhenHeaderIsModified(TelicentHeaders.CHUNK_TOTAL, "3",
                                            "does not match previously declared total");
    }

    @Test
    public void givenChunksWithMissingID_whenCombiningSourceUsedWithDlq_thenNothingCombined_andSentToDlq() {
        // Given
        verifyGoesToDlqWhenRequiredHeaderIsRemoved(TelicentHeaders.CHUNK_ID, "required Chunk-ID header");
    }

    @Test
    public void givenChunksWithMissingTotal_whenCombiningSourceUsedWithDlq_thenNothingCombined_andSentToDlq() {
        // Given
        verifyGoesToDlqWhenRequiredHeaderIsRemoved(TelicentHeaders.CHUNK_TOTAL, "required Chunk-Total header");
    }

    @DataProvider(name = "integrityHeaders")
    public Object[][] integrityHeaders() {
        return new Object[][] {
                { TelicentHeaders.CHUNK_CHECKSUM },
                { TelicentHeaders.ORIGINAL_CHECKSUM },
                { TelicentHeaders.CHUNK_HASH },
                { TelicentHeaders.ORIGINAL_HASH }
        };
    }

    @Test(dataProvider = "integrityHeaders")
    public void givenChunksWithMissingIntegrityHeader_whenCombiningSourceUsedWithDlq_thenNothingCombined_andSentToDlq(
            String header) {
        verifyGoesToDlqWhenRequiredHeaderIsRemoved(header, "required " + header + " header");
    }

    @Test(dataProvider = "integrityHeaders")
    public void givenChunksWhereIntegrityHeaderHasUnrecognisedAlgorithmId_whenCombiningSourceUsedWithDlq_thenNothingCombined_andSentToDlq(
            String header) {
        verifyGoesToDlqWhenHeaderIsModified(header, x -> "foo" + x.substring(x.indexOf(':')),
                                            "Algorithm mismatch");
    }

    @Test(dataProvider = "integrityHeaders")
    public void givenChunksWhereIntegrityHeaderHasMismatchedValue_whenCombiningSourceUsedWithDlq_thenNothingCombined_andSentToDlq(
            String header) {
        verifyGoesToDlqWhenHeaderIsModified(header, x -> x + "0",
                                            "mismatch");
    }

    @Test(dataProvider = "integrityHeaders")
    public void givenChunksWhereIntegrityHeaderHasNoAlgorithmPrefix_whenCombiningSourceUsedWithDlq_thenNothingCombined_andSentToDlq(
            String header) {
        verifyGoesToDlqWhenHeaderIsModified(header, x -> x.substring(x.indexOf(':') + 1),
                                            "does not have required '<id>:' prefix");
    }

    @DataProvider(name = "checksumHeaders")
    public Object[][] checksumHeaders() {
        return new Object[][] {
                { TelicentHeaders.CHUNK_CHECKSUM },
                { TelicentHeaders.ORIGINAL_CHECKSUM }
        };
    }

    @Test(dataProvider = "checksumHeaders")
    public void givenChunksWhereChecksumHeaderHasNonIntegerValue_whenCombiningSourceUsedWithDlq_thenNothingCombined_andSentToDlq(
            String header) {
        verifyGoesToDlqWhenHeaderIsModified(header, x -> "crc32:foo",
                                            "value (not parseable): foo");
    }
}
