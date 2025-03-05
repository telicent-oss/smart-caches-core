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

import io.telicent.smart.cache.sources.AbstractEventSourceTests;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testng.annotations.*;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Test(retryAnalyzer = FlakyKafkaTest.class)
public class DockerTestKafkaEventSourceBehaviour extends AbstractEventSourceTests<Integer, String> {

    private final AtomicInteger consumerGroupId = new AtomicInteger(0);
    private final KafkaTestCluster kafka = new BasicKafkaTestCluster();

    @BeforeClass
    public void setup() {
        Utils.logTestClassStarted(DockerTestKafkaEventSourceBehaviour.class);
        this.kafka.setup();
    }

    @AfterMethod
    public void testCleanup() throws InterruptedException {
        this.kafka.resetTestTopic();
    }

    @AfterClass
    public void teardown() {
        this.kafka.teardown();
        Utils.logTestClassFinished(DockerTestKafkaEventSourceBehaviour.class);
    }

    @Override
    protected EventSource<Integer, String> createEmptySource() {
        return KafkaEventSource.<Integer, String>create()
                               .fromBeginning()
                               .topic(KafkaTestCluster.DEFAULT_TOPIC)
                               .bootstrapServers(this.kafka.getBootstrapServers())
                               .autoCommit()
                               .consumerGroup("behaviour-tests-" + this.consumerGroupId.incrementAndGet())
                               .keyDeserializer(IntegerDeserializer.class)
                               .valueDeserializer(StringDeserializer.class)
                               .build();
    }

    @Override
    protected EventSource<Integer, String> createSource(Collection<Event<Integer, String>> events) {
        try (KafkaSink<Integer, String> sink = KafkaSink.<Integer, String>create()
                                                        .keySerializer(IntegerSerializer.class)
                                                        .valueSerializer(StringSerializer.class)
                                                        .topic(KafkaTestCluster.DEFAULT_TOPIC)
                                                        .bootstrapServers(this.kafka.getBootstrapServers()).build()) {
            for (Event<Integer, String> event : events) {
                sink.send(event);
            }
        }

        return createEmptySource();
    }

    @Override
    protected Collection<Event<Integer, String>> createSampleData(int size) {
        AtomicInteger counter = new AtomicInteger(0);
        return createSampleStrings(size).stream()
                                        .map(s -> new SimpleEvent<>(Collections.emptyList(), counter.getAndIncrement(),
                                                                    s)).collect(Collectors.toList());
    }

    @Override
    public boolean guaranteesImmediateAvailability() {
        return false;
    }

    @Override
    public boolean isUnbounded() {
        return true;
    }

    @DataProvider(name = "sample-data-sizes")
    @Override
    public Object[][] getTestSizes() {
        // NB - Tone down the test data sizes when running against a Kafka Test cluster as the large test data sizes
        //      our base class uses take too long to bring up and tear down with Kafka
        return new Object[][] {
                { 100 },
                { 2_500 },
                { 10_000 }
        };
    }

}
