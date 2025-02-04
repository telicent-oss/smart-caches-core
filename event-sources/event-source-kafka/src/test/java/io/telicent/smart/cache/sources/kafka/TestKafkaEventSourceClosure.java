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

import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicy;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TestKafkaEventSourceClosure extends AbstractConsumerMocks {

    @AfterMethod
    public void cleanup() {
        MockitoKafkaEventSource.reset();
    }

    @Test
    public void givenBasicMockConsumer_whenClosingSource_thenExpectedCloseActionsCalledOnConsumer() {
        // Given
        KafkaConsumer<Integer, String> consumer = mock(KafkaConsumer.class);
        MockitoKafkaEventSource.setMockConsumer(consumer);
        KafkaReadPolicy<Integer, String> policy = mock(KafkaReadPolicy.class);
        EventSource<Integer, String> source =
                new MockitoKafkaEventSource<>("localhost:9092", Set.of(KafkaTestCluster.DEFAULT_TOPIC), "test",
                                              IntegerDeserializer.class.getCanonicalName(),
                                              StringDeserializer.class.getCanonicalName(), 100,
                                              policy, true);

        // When
        source.close();

        // Then
        verifyCloseActionsCalled(consumer, policy);
    }

    private static void verifyCloseActionsCalled(KafkaConsumer<Integer, String> consumer, KafkaReadPolicy<Integer, String> policy) {
        verify(consumer, times(1)).commitSync();
        verify(policy, times(1)).stopEvents(any());
        verify(consumer).close();
    }

    @Test
    public void givenConsumerThatFailsOnCommit_whenClosingSource_thenExpectedCloseActionsCalledOnConsumer() {
        // Given
        KafkaConsumer<Integer, String> consumer = mock(KafkaConsumer.class);
        doThrow(new RuntimeException()).when(consumer).commitSync();
        KafkaReadPolicy<Integer, String> policy = mock(KafkaReadPolicy.class);
        MockitoKafkaEventSource.setMockConsumer(consumer);
        EventSource<Integer, String> source =
                new MockitoKafkaEventSource<>("localhost:9092", Set.of(KafkaTestCluster.DEFAULT_TOPIC), "test",
                                              IntegerDeserializer.class.getCanonicalName(),
                                              StringDeserializer.class.getCanonicalName(), 100,
                                              policy, true);

        // When
        source.close();

        // Then
        verifyCloseActionsCalled(consumer, policy);
    }

    @Test
    public void givenReadPolicyThatFailsOnStopEvents_whenClosingSource_thenExpectedCloseActionsCalledOnConsumer() {
        // Given
        KafkaConsumer<Integer, String> consumer = mock(KafkaConsumer.class);
        KafkaReadPolicy<Integer, String> policy = mock(KafkaReadPolicy.class);
        doThrow(new RuntimeException()).when(policy).stopEvents(any());
        MockitoKafkaEventSource.setMockConsumer(consumer);
        EventSource<Integer, String> source =
                new MockitoKafkaEventSource<>("localhost:9092", Set.of(KafkaTestCluster.DEFAULT_TOPIC), "test",
                                              IntegerDeserializer.class.getCanonicalName(),
                                              StringDeserializer.class.getCanonicalName(), 100,
                                              policy, true);

        // When
        source.close();

        // Then
        verifyCloseActionsCalled(consumer, policy);
    }

    @Test
    public void givenAdminClientThatFailsOnClosure_whenClosingSource_thenExpectedCloseActionsCalledOnConsumer() {
        // Given
        KafkaConsumer<Integer, String> consumer = mock(KafkaConsumer.class);
        KafkaReadPolicy<Integer, String> policy = mock(KafkaReadPolicy.class);
        AdminClient adminClient = mock(AdminClient.class);
        doThrow(new RuntimeException()).when(adminClient).close();
        MockitoKafkaEventSource.setMockConsumer(consumer);
        MockitoKafkaEventSource.setMockAdminClient(adminClient);
        EventSource<Integer, String> source =
                new MockitoKafkaEventSource<>("localhost:9092", Set.of(KafkaTestCluster.DEFAULT_TOPIC), "test",
                                              IntegerDeserializer.class.getCanonicalName(),
                                              StringDeserializer.class.getCanonicalName(), 100,
                                              policy, true);

        // When
        source.close();

        // Then
        verifyCloseActionsCalled(consumer, policy);
    }
}
