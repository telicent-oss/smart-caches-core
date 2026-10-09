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

import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.PausableEventSource;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TestKafkaEventSourcePause {

    private static final Duration POLL = Duration.ofMillis(50);
    private static final TopicPartition PARTITION_0 = new TopicPartition(TestKafkaEventSource.TEST_TOPIC, 0);
    private static final TopicPartition PARTITION_1 = new TopicPartition(TestKafkaEventSource.TEST_TOPIC, 1);

    private MockKafkaEventSource<Integer, String> sourceToClose;

    @AfterMethod
    public void cleanup() {
        if (this.sourceToClose != null) {
            this.sourceToClose.close();
        }
    }

    private MockKafkaEventSource<Integer, String> createSource(int events) {
        return createSource(events, false);
    }

    private MockKafkaEventSource<Integer, String> createSource(int events, boolean autoCommit) {
        List<Event<Integer, String>> data = new ArrayList<>();
        for (int i = 0; i < events; i++) {
            data.add(new SimpleEvent<>(Collections.emptyList(), i, "event-" + i));
        }
        // Buffers up to 100 events per Kafka poll
        this.sourceToClose = new MockKafkaEventSource<>(TestKafkaEventSource.DEFAULT_BOOTSTRAP_SERVERS,
                                                 Set.of(TestKafkaEventSource.TEST_TOPIC),
                                                 TestKafkaEventSource.TEST_GROUP,
                                                 StringSerializer.class.getCanonicalName(),
                                                 StringSerializer.class.getCanonicalName(), 100,
                                                 KafkaReadPolicies.fromBeginning(), autoCommit, true, data);
        return this.sourceToClose;
    }

    private static List<Integer> pollKeys(PausableEventSource<Integer, String> source, int count) {
        List<Integer> keys = new ArrayList<>();
        for (int attempt = 0; keys.size() < count && attempt < count * 10; attempt++) {
            Event<Integer, String> event = source.poll(POLL);
            if (event != null) {
                keys.add(event.key());
            }
        }
        return keys;
    }

    @Test
    public void givenKafkaSource_whenPausingAndResuming_thenIsPausedReflectsState_andRepeatedCallsAreHarmless() {
        // Given
        MockKafkaEventSource<Integer, String> source = createSource(10);
        Assert.assertTrue(source instanceof PausableEventSource);
        Assert.assertFalse(source.isPaused());

        // When and Then
        source.pause();
        source.pause();
        Assert.assertTrue(source.isPaused());
        source.resume();
        source.resume();
        Assert.assertFalse(source.isPaused());
    }

    @Test
    public void givenPausedBeforeFirstPoll_whenPolling_thenNoEventsUntilResumed() {
        // Given
        MockKafkaEventSource<Integer, String> source = createSource(10);
        source.pause();

        // When
        for (int i = 0; i < 3; i++) {
            Assert.assertNull(source.poll(POLL));
        }
        source.resume();

        // Then
        Assert.assertEquals(pollKeys(source, 10), List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    public void givenBufferedEvents_whenPaused_thenNothingDelivered_andBufferDiscardedAndPartitionRewound() {
        // Given
        MockKafkaEventSource<Integer, String> source = createSource(1_000);
        Assert.assertEquals(pollKeys(source, 10), List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        MockConsumer<Integer, String> mock = source.getMockConsumer();
        Assert.assertTrue(source.availableImmediately(), "Remaining events should be buffered");

        // When
        source.pause();

        // Then: nothing is reported as available, otherwise a ProjectorDriver would abort when poll() returns nothing
        Assert.assertFalse(source.availableImmediately());
        for (int i = 0; i < 5; i++) {
            Assert.assertNull(source.poll(POLL), "No events should be delivered while paused, including buffered ones");
            Assert.assertTrue(source.wasPausedOnLastPoll());
        }
        Assert.assertEquals(mock.paused(), mock.assignment(), "All assigned partitions should be paused on the consumer");

        // And: the undelivered buffered events were discarded, with the partition rewound to the first of them, so a
        // real consumer fetches them again after resuming (MockConsumer discards records once returned, so can't show
        // the re-fetch, see DockerTestKafkaPollingTimeout)
        Assert.assertEquals(mock.position(PARTITION_0), 10L);
        source.resume();
        Assert.assertFalse(source.availableImmediately(), "Buffer should have been discarded");
        Assert.assertNull(source.poll(POLL));
        Assert.assertFalse(source.wasPausedOnLastPoll());
        Assert.assertTrue(mock.paused().isEmpty(), "Partitions should be resumed on the consumer");
    }

    @Test
    public void givenBufferedEvents_whenPartitionRevokedWhilePaused_thenBufferedEventsNotDeliveredAfterResume() {
        // Given
        MockKafkaEventSource<Integer, String> source = createSource(100);
        Assert.assertEquals(pollKeys(source, 10), List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        MockConsumer<Integer, String> mock = source.getMockConsumer();
        source.pause();

        // When: a rebalance during a paused poll moves partition 0 to another consumer
        mock.schedulePollTask(() -> {
            mock.updateBeginningOffsets(Map.of(PARTITION_1, 0L));
            mock.updateEndOffsets(Map.of(PARTITION_1, 0L));
            mock.rebalance(List.of(PARTITION_1));
        });
        Assert.assertNull(source.poll(POLL));
        source.resume();

        // Then: events 10-99, buffered from partition 0 before the pause, are not delivered by this consumer
        for (int i = 0; i < 5; i++) {
            Assert.assertNull(source.poll(POLL), "Events from a revoked partition must not be delivered");
        }
    }

    @Test
    public void givenPaused_whenPartitionAssignedDuringPausedPoll_thenItsRecordsAreRewoundNotLost() {
        // Given
        MockKafkaEventSource<Integer, String> source = createSource(5);
        Assert.assertEquals(pollKeys(source, 5), List.of(0, 1, 2, 3, 4));
        MockConsumer<Integer, String> mock = source.getMockConsumer();
        source.pause();
        Assert.assertNull(source.poll(POLL));

        // When: a rebalance during the next paused poll assigns a new partition, which isn't paused yet
        mock.schedulePollTask(() -> {
            mock.updateBeginningOffsets(Map.of(PARTITION_1, 0L));
            mock.updateEndOffsets(Map.of(PARTITION_1, 1L));
            mock.rebalance(List.of(PARTITION_0, PARTITION_1));
            mock.addRecord(new ConsumerRecord<>(TestKafkaEventSource.TEST_TOPIC, 1, 0L, 100, "on-new-partition"));
        });
        Assert.assertNull(source.poll(POLL));

        // Then: the partition is paused and its position rewound to the record that was returned, so a real consumer
        // fetches it again after resuming (MockConsumer discards records once returned, so can't show the re-fetch)
        Assert.assertTrue(mock.paused().contains(PARTITION_1), "Newly assigned partition should now be paused");
        Assert.assertEquals(mock.position(PARTITION_1), 0L, "Position should be rewound to the record received while paused");
    }

    @DataProvider(name = "pausedTimeouts")
    public Object[][] pausedTimeouts() {
        return new Object[][] {{null}, {Duration.ofSeconds(10)}, {Duration.ZERO}};
    }

    @Test(dataProvider = "pausedTimeouts")
    public void givenConnectedPausedSource_whenPollingWithDifferentTimeouts_thenDeliveryRemainsPaused(Duration timeout) {
        MockKafkaEventSource<Integer, String> source = createSource(1);
        Assert.assertEquals(pollKeys(source, 1), List.of(0));
        source.pause();
        Assert.assertNull(source.poll(timeout));
        Assert.assertTrue(source.wasPausedOnLastPoll());
        Assert.assertEquals(source.getMockConsumer().paused(), source.getMockConsumer().assignment());
    }

    @Test
    public void givenPausedSource_whenKafkaPollFails_thenNextPollCanRecover() {
        MockKafkaEventSource<Integer, String> source = createSource(1);
        Assert.assertEquals(pollKeys(source, 1), List.of(0));
        source.pause();
        source.getMockConsumer().setPollException(new KafkaException("Temporary poll failure"));
        Assert.assertNull(source.poll(POLL));
        Assert.assertNull(source.poll(POLL));
        source.resume();
        source.getMockConsumer().addRecord(new ConsumerRecord<>(TestKafkaEventSource.TEST_TOPIC, 0, 1L, 1, "recovered"));
        Assert.assertEquals(source.poll(POLL).value(), "recovered");
    }

    @Test
    public void givenPausedSource_whenWokenUp_thenPollingContinues() {
        MockKafkaEventSource<Integer, String> source = createSource(1);
        Assert.assertEquals(pollKeys(source, 1), List.of(0));
        source.pause();
        source.interrupt();
        Assert.assertNull(source.poll(POLL));
        Assert.assertNull(source.poll(POLL));
        Assert.assertTrue(source.isPaused());
    }

    @Test
    public void givenPausedBeforeConnecting_whenInterrupted_thenInterruptStatusIsPreserved() {
        MockKafkaEventSource<Integer, String> source = createSource(1);
        source.pause();
        Thread.currentThread().interrupt();
        try {
            Assert.assertNull(source.poll(POLL));
            Assert.assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    public void givenPausedSource_whenOffsetsResetFromAnotherThread_thenResetAppliedWhilePaused() {
        MockKafkaEventSource<Integer, String> source = createSource(100);
        Assert.assertEquals(pollKeys(source, 10), List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        source.pause();
        CompletableFuture.runAsync(() -> source.resetOffsets(Map.of(PARTITION_0, 3L))).join();
        Assert.assertNull(source.poll(POLL));
        Assert.assertFalse(source.availableImmediately());
        Assert.assertEquals(source.getMockConsumer().position(PARTITION_0), 3L);
    }

    @Test
    public void givenPausedSource_whenProcessedFromAnotherThread_thenDelayedCommitAppliedWhilePaused() {
        MockKafkaEventSource<Integer, String> source = createSource(1);
        Event<Integer, String> received = null;
        for (int attempt = 0; received == null && attempt < 10; attempt++) {
            received = source.poll(POLL);
        }
        Event<Integer, String> event = received;
        Assert.assertNotNull(event);
        source.pause();
        CompletableFuture.runAsync(() -> source.processed(List.of(event))).join();
        Assert.assertNull(source.poll(POLL));
        Assert.assertEquals(source.getMockConsumer().committed(Set.of(PARTITION_0)).get(PARTITION_0).offset(), 1L);
    }

    @Test
    public void givenAutoCommittingSource_whenPaused_thenPollsWithoutDeliveringBufferedEvents() {
        MockKafkaEventSource<Integer, String> source = createSource(100, true);
        Assert.assertEquals(pollKeys(source, 1), List.of(0));
        source.pause();
        Assert.assertNull(source.poll(POLL));
        Assert.assertTrue(source.wasPausedOnLastPoll());
        Assert.assertEquals(source.getMockConsumer().position(PARTITION_0), 1L);
    }
}
