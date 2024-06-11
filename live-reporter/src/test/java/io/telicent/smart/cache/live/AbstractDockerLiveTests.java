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
package io.telicent.smart.cache.live;

import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

public abstract class AbstractDockerLiveTests<T> {
    protected final KafkaTestCluster kafka = new KafkaTestCluster();

    @BeforeClass
    public void setup() {
        this.kafka.setup();
    }

    @AfterMethod()
    public void testCleanup() throws InterruptedException {
        this.kafka.resetTestTopic();
        Thread.sleep(500);
    }

    @AfterClass
    public void teardown() {
        this.kafka.resetTestTopic();
        this.kafka.teardown();
    }

    /**
     * Gets the serializer class for the value class for the live component under test
     *
     * @return Serializer class
     */
    protected abstract Class<? extends Serializer<T>> getSerializerClass();

    protected KafkaSink<Bytes, T> createSink() {
        return KafkaSink.<Bytes, T>create()
                        .keySerializer(BytesSerializer.class)
                        .valueSerializer(getSerializerClass())
                        .bootstrapServers(this.kafka.getBootstrapServers())
                        .topic(KafkaTestCluster.DEFAULT_TOPIC)
                        .build();
    }
}
