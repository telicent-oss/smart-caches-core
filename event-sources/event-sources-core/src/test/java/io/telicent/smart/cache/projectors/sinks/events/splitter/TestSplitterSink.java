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
package io.telicent.smart.cache.projectors.sinks.events.splitter;

import io.telicent.smart.cache.projectors.sinks.CollectorSink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class TestSplitterSink {

    private static final ExampleSplitter<UUID> EXAMPLE_SPLITTER = new ExampleSplitter<>();

    @DataProvider(name = "chunkSizes")
    public Object[][] chunkSizes() {
        return new Object[][] {
                { 1 }, { 2 }, { 1024 }, { 1024 * 1024 }
        };
    }

    @Test(dataProvider = "chunkSizes")
    public void givenSplitterSink_whenSendingTooLargeEvent_thenSplitIntoChunks_andFinalChunkIsSmallerThanOthers(
            int chunkSize) {
        // Given
        try (CollectorSink<Event<UUID, ExampleSplittablePayload>> collector = CollectorSink.of()) {
            try (SplitterSink<UUID, ExampleSplittablePayload> splitter = SplitterSink.<UUID, ExampleSplittablePayload>create()
                                                                                     .chunkSize(chunkSize)
                                                                                     .destination(collector)
                                                                                     .splitter(EXAMPLE_SPLITTER)
                                                                                     .build()) {
                // When
                String random = RandomStringUtils.insecure().nextAlphanumeric((int) (chunkSize * 4.5));
                ExampleSplittablePayload document = ExampleSplittablePayload.builder()
                                                                            .title("Test")
                                                                            .payload(random.getBytes(
                                                                                    StandardCharsets.UTF_8))
                                                                            .build();
                UUID uuid = UUID.randomUUID();
                Event<UUID, ExampleSplittablePayload> event = new SimpleEvent<>(null, uuid, document);
                splitter.send(event);

                // Then
                List<Event<UUID, ExampleSplittablePayload>> results = collector.get();
                Assert.assertEquals(results.size(), chunkSize > 1 ? 5 : 4);
                Assert.assertTrue(results.stream()
                                         .allMatch(e -> StringUtils.isNotBlank(
                                                 e.lastHeader(SplitterSink.CHUNK_CHECKSUM))),
                                  "All chunks should have a Chunk-Checksum header present");
                Assert.assertTrue(
                        results.stream().allMatch(e -> StringUtils.isNotBlank(e.lastHeader(SplitterSink.CHUNK_HASH))));

                // And
                if (chunkSize == 1) {
                    Assert.assertTrue(results.stream().allMatch(e -> e.value().getPayload().length == chunkSize));
                } else {
                    for (int i = 0; i < results.size(); i++) {
                        Assert.assertEquals(results.get(i).value().getTitle(), event.value().getTitle());
                        if (i < results.size() - 1) {
                            Assert.assertEquals(results.get(i).value().getPayload().length, chunkSize,
                                                "All non-final chunks should be " + chunkSize + " bytes");
                        } else {
                            Assert.assertNotEquals(results.get(i).value().getPayload().length, chunkSize,
                                                   "Final chunk should be less than " + chunkSize + " bytes");
                        }
                    }
                }
            }
        }
    }
}
