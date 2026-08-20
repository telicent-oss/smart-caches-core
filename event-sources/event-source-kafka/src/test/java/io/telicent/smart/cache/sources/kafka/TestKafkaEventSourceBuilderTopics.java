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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

/**
 * Tests for topic selection on {@link AbstractKafkaEventSourceBuilder}, in particular the replacement path taken when
 * {@code topic()} is called after topics have already been set, which had no coverage.
 */
public class TestKafkaEventSourceBuilderTopics {

    /**
     * A minimal concrete builder.  {@code build()} is never called - these tests only exercise topic selection.
     */
    private static final class ProbeBuilder
            extends AbstractKafkaEventSourceBuilder<String, String, KafkaEventSource<String, String>, ProbeBuilder> {
        @Override
        public KafkaEventSource<String, String> build() {
            throw new UnsupportedOperationException("Building a real source is not needed for these tests");
        }
    }

    @Test
    public void givenNoTopics_whenSettingASingleTopic_thenThatTopicIsSelected() {
        ProbeBuilder builder = new ProbeBuilder();

        builder.topic("first");

        Assert.assertEquals(builder.topics, Set.of("first"));
    }

    @Test
    public void givenAnExistingTopic_whenSettingASingleTopic_thenTheEarlierTopicIsReplaced() {
        ProbeBuilder builder = new ProbeBuilder();

        builder.topic("first");
        builder.topic("second");

        Assert.assertEquals(builder.topics, Set.of("second"));
    }

    @Test
    public void givenSeveralExistingTopics_whenSettingASingleTopic_thenAllEarlierTopicsAreReplaced() {
        ProbeBuilder builder = new ProbeBuilder();

        builder.topics(List.of("first", "second", "third"));
        Assert.assertEquals(builder.topics.size(), 3);

        builder.topic("only");

        Assert.assertEquals(builder.topics, Set.of("only"));
    }

    @Test
    public void givenNoTopics_whenSettingSeveralTopics_thenAllAreSelectedInOrder() {
        ProbeBuilder builder = new ProbeBuilder();

        builder.topics("a", "b");

        Assert.assertEquals(List.copyOf(builder.topics), List.of("a", "b"));
    }
}
