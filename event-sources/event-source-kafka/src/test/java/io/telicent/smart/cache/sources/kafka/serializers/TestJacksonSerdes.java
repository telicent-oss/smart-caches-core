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
package io.telicent.smart.cache.sources.kafka.serializers;

import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.tombstones.Data;
import io.telicent.smart.cache.sources.kafka.tombstones.DataDeserializer;
import io.telicent.smart.cache.sources.kafka.tombstones.DataSerializer;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("resource")
public class TestJacksonSerdes {

    private final DataDeserializer deserializer = new DataDeserializer();
    private final DataSerializer serializer = new DataSerializer();

    @Test
    public void givenNullData_whenDeserializing_thenNull() {
        // Given and When
        Data data = deserializer.deserialize(KafkaTestCluster.DEFAULT_TOPIC, null);

        // Then
        Assert.assertNull(data);
    }

    @Test
    public void givenNullValue_whenSerializing_thenNullData() {
        // Given and When
        byte[] data = serializer.serialize(KafkaTestCluster.DEFAULT_TOPIC, null);

        // Then
        Assert.assertNull(data);
    }

    @SuppressWarnings("unused")
    private static final class Bad {
        // NB - These methods exist only to provoke (de-)serialization errors

        public String getTest() {
            throw new RuntimeException();
        }

        public void setTest(String value) {
            throw new RuntimeException();
        }
    }

    private static final class BadSerializer extends AbstractJacksonSerializer<Bad> {

    }

    private static final class BadDeserializer extends AbstractJacksonDeserializer<Bad> {

        public BadDeserializer() {
            super(Bad.class);
        }
    }

    @Test(expectedExceptions = SerializationException.class)
    public void givenNonSerializableType_whenSerializing_thenFails() {
        // Given
        Bad bad = new Bad();
        BadSerializer badSerializer = new BadSerializer();

        // When and Then
        badSerializer.serialize(KafkaTestCluster.DEFAULT_TOPIC, bad);
    }

    @Test(expectedExceptions = SerializationException.class)
    public void givenNonDeserializableType_whenDeserializing_thenFails() {
        // Given
        byte[] data = "{ \"test\": \"foo\" }".getBytes(StandardCharsets.UTF_8);
        BadDeserializer bad = new BadDeserializer();

        // When and Then
        bad.deserialize(KafkaTestCluster.DEFAULT_TOPIC, data);
    }
}
