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
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link KafkaEventSource#remaining()} is asked for once for every event a projector takes from the buffer, so it must
 * be cheap, and it must still be right: the lag Kafka reports plus what is buffered, at every event, and after
 * everything that moves the consumer's position.
 */
public class TestKafkaEventSourceRemaining {

    private static final Duration POLL = Duration.ofMillis(50);
    private static final TopicPartition PARTITION = new TopicPartition(TestKafkaEventSource.TEST_TOPIC, 0);

    private MockKafkaEventSource<Integer, String> sourceToClose;
    private KafkaReadPolicy<Integer, String> policy;

    @AfterMethod
    public void cleanup() {
        if (this.sourceToClose != null) {
            this.sourceToClose.close();
        }
    }

    private MockKafkaEventSource<Integer, String> createSource(int events) {
        List<Event<Integer, String>> data = new ArrayList<>();
        for (int i = 0; i < events; i++) {
            data.add(new SimpleEvent<>(Collections.emptyList(), i, "event-" + i));
        }
        this.policy = Mockito.spy(KafkaReadPolicies.<Integer, String>fromBeginning());
        this.sourceToClose = new MockKafkaEventSource<>(TestKafkaEventSource.DEFAULT_BOOTSTRAP_SERVERS,
                                                        Set.of(TestKafkaEventSource.TEST_TOPIC),
                                                        TestKafkaEventSource.TEST_GROUP,
                                                        StringSerializer.class.getCanonicalName(),
                                                        StringSerializer.class.getCanonicalName(), 100, this.policy,
                                                        false, true, data);
        return this.sourceToClose;
    }

    /**
     * Takes the next event, allowing for polls that return nothing while the consumer is being assigned its partition
     */
    private static void take(MockKafkaEventSource<Integer, String> source) {
        for (int attempt = 0; attempt < 10; attempt++) {
            if (source.poll(POLL) != null) {
                return;
            }
        }
        Assert.fail("No event was delivered");
    }

    /**
     * What {@code remaining()} is defined as, read straight from the consumer
     */
    private static long expected(MockKafkaEventSource<Integer, String> source, int buffered) {
        MockConsumer<Integer, String> consumer = source.getMockConsumer();
        return consumer.currentLag(PARTITION).orElseThrow() + buffered;
    }

    @Test
    public void givenBufferedEvents_whenAskingRemainingAfterEachEvent_thenItIsRight_andLagIsWorkedOutOncePerFill() {
        // Given
        int total = 50;
        MockKafkaEventSource<Integer, String> source = createSource(total);

        // When and Then
        for (int taken = 1; taken <= total; taken++) {
            take(source);
            Assert.assertEquals(source.remaining(), Long.valueOf(expected(source, total - taken)));
            Assert.assertEquals(source.remaining(), Long.valueOf(total - taken));
        }
        // One fill of the buffer, one calculation of the lag, however many events were delivered from it
        Mockito.verify(this.policy, Mockito.times(1)).currentLag(TestKafkaEventSource.TEST_TOPIC);
    }

    @Test
    public void givenExhaustedBuffer_whenMoreEventsArrive_thenRemainingReflectsThemAfterTheNextFill() {
        // Given
        MockKafkaEventSource<Integer, String> source = createSource(10);
        for (int i = 0; i < 10; i++) {
            take(source);
        }
        Assert.assertEquals(source.remaining(), Long.valueOf(0));

        // When
        MockConsumer<Integer, String> consumer = source.getMockConsumer();
        consumer.updateEndOffsets(Map.of(PARTITION, 15L));
        for (int offset = 10; offset < 15; offset++) {
            consumer.addRecord(new ConsumerRecord<>(PARTITION.topic(), PARTITION.partition(), offset, offset,
                                                    "event-" + offset));
        }

        // Then
        for (int taken = 1; taken <= 5; taken++) {
            take(source);
            Assert.assertEquals(source.remaining(), Long.valueOf(expected(source, 5 - taken)));
            Assert.assertEquals(source.remaining(), Long.valueOf(5 - taken));
        }
    }

    @Test
    public void givenBufferedEvents_whenPausedAndResumed_thenRemainingIsRightThroughout() {
        // Given
        int total = 20;
        MockKafkaEventSource<Integer, String> source = createSource(total);
        for (int i = 0; i < 5; i++) {
            take(source);
        }
        Assert.assertEquals(source.remaining(), Long.valueOf(15));

        // When: pausing discards the buffer and moves the consumer back to the first event not delivered
        source.pause();
        Assert.assertNull(source.poll(POLL));

        // Then: nothing is buffered now, and Kafka's lag has grown by what was discarded
        Assert.assertEquals(source.remaining(), Long.valueOf(expected(source, 0)));
        Assert.assertEquals(source.remaining(), Long.valueOf(15));

        // When: the mock consumer does not hold on to records it has returned, so give it again what Kafka would
        // deliver again from the position the consumer was moved back to
        source.resume();
        for (int offset = 5; offset < total; offset++) {
            source.getMockConsumer()
                  .addRecord(new ConsumerRecord<>(PARTITION.topic(), PARTITION.partition(), offset, offset,
                                                  "event-" + offset));
        }

        // Then
        for (int taken = 1; taken <= 15; taken++) {
            take(source);
            Assert.assertEquals(source.remaining(), Long.valueOf(15 - taken));
        }
    }

    @Test
    public void givenReadEvents_whenOffsetsAreReset_thenRemainingIsWorkedOutAgain() {
        // Given
        int total = 20;
        MockKafkaEventSource<Integer, String> source = createSource(total);
        for (int i = 0; i < total; i++) {
            take(source);
        }
        Assert.assertEquals(source.remaining(), Long.valueOf(0));

        // When: on the polling thread, so the reset is applied at once
        source.resetOffsets(Map.of(PARTITION, 5L));

        // Then
        Assert.assertEquals(source.remaining(), Long.valueOf(expected(source, 0)));
        Assert.assertEquals(source.remaining(), Long.valueOf(15));
    }
}
