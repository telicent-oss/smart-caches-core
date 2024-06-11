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
import io.telicent.smart.cache.sources.kafka.policies.KafkaReadPolicies;
import io.telicent.smart.cache.sources.kafka.serializers.TestDatasetDeserializer;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.jena.sparql.core.DatasetGraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TestKafkaDatasetGraphSource extends AbstractEventSourceTests<Integer, DatasetGraph> {

    public static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    public static final String TEST_TOPIC = "test";
    public static final String TEST_GROUP = "test-group";

    @Override
    protected EventSource<Integer, DatasetGraph> createEmptySource() {
        return createSource(Collections.emptyList());
    }

    @Override
    protected EventSource<Integer, DatasetGraph> createSource(Collection<Event<Integer, DatasetGraph>> events) {
        return new MockKafkaDatasetGraphSource(DEFAULT_BOOTSTRAP_SERVERS, Set.of(TEST_TOPIC), TEST_GROUP, 100,
                                               KafkaReadPolicies.fromBeginning(), true, events);
    }

    @Override
    protected Collection<Event<Integer, DatasetGraph>> createSampleData(int size) {
        List<DatasetGraph> graphs = new ArrayList<>();
        DatasetGraph g = TestDatasetDeserializer.createTestDataset(2, 100);
        for (int i = 0; i < size; i++) {
            graphs.add(g);
        }
        AtomicInteger counter = new AtomicInteger(0);
        return graphs.stream()
                     .map(graph -> new SimpleEvent<>(Collections.emptyList(), counter.incrementAndGet(), graph))
                     .collect(Collectors.toList());
    }

    @Override
    public boolean guaranteesImmediateAvailability() {
        return false;
    }

    @Override
    public boolean isUnbounded() {
        return true;
    }
}
