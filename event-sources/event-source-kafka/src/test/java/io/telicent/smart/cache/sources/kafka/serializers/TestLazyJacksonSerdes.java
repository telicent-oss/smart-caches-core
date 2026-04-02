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

import io.telicent.smart.cache.payloads.LazyPayloadException;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import io.telicent.smart.cache.sources.kafka.tombstones.Data;
import io.telicent.smart.cache.sources.kafka.tombstones.LazyData;
import io.telicent.smart.cache.sources.kafka.tombstones.LazyDataDeserializer;
import io.telicent.smart.cache.sources.kafka.tombstones.LazyDataSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class TestLazyJacksonSerdes {

    public static final byte[] BAD_JSON = "{ \"key\": \"unterminated}".getBytes(StandardCharsets.UTF_8);
    public static final byte[] GOOD_JSON = """
            {
              "key": "test",
              "value": 1234
            }
            """.getBytes(StandardCharsets.UTF_8);
    private final LazyDataDeserializer deserializer = new LazyDataDeserializer();
    private final LazyDataSerializer serializer = new LazyDataSerializer();

    @Test
    public void givenMalformedJson_whenDeserializingOk_thenAccessingValueThrowsError() {
        // Given and When
        LazyData data = this.deserializer.deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, BAD_JSON);

        // Then
        Assert.assertNotNull(data);
        Assert.assertThrows(LazyPayloadException.class, data::getValue);
        Assert.assertTrue(data.hasError());
        Assert.assertTrue(data.getError() instanceof LazyPayloadException);
    }

    @Test
    public void givenMalformedJson_whenDeserializedAndValueAccessed_thenCanStillBeSerialized() {
        // Given and When
        LazyData data = this.deserializer.deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, BAD_JSON);
        Assert.assertThrows(LazyPayloadException.class, data::getValue);

        // Then
        byte[] output = this.serializer.serialize(KafkaTestCluster.DEFAULT_TOPIC, null, data);
        Assert.assertEquals(output, BAD_JSON);
    }

    @Test
    public void givenMalformedJson_whenDeserialized_thenCanBeSerialized() {
        // Given and When
        LazyData data = this.deserializer.deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, BAD_JSON);

        // Then
        byte[] output = this.serializer.serialize(KafkaTestCluster.DEFAULT_TOPIC, null, data);
        Assert.assertEquals(output, BAD_JSON);
    }

    @Test
    public void givenValidJson_whenDeserialized_thenValueAccessible_andCanBeSerialized() {
        // Given and When
        LazyData data = this.deserializer.deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, GOOD_JSON);

        // Then
        Data actualData = data.getValue();
        Assert.assertEquals(actualData.key(), "test");
        Assert.assertEquals(actualData.value(), 1234);

        // And
        byte[] output = this.serializer.serialize(KafkaTestCluster.DEFAULT_TOPIC, null, data);
        Assert.assertNotEquals(output, GOOD_JSON);
    }

    @Test
    public void givenPopulatedPayload_whenSerializing_thenValidJsonProduced() {
        // Given
        LazyData data = new LazyData(new Data("test", 1234));
        Assert.assertTrue(data.isReady());
        Assert.assertNotNull(data.getValue());

        // When
        byte[] output = this.serializer.serialize(KafkaTestCluster.DEFAULT_TOPIC, null, data);

        // Then
        Assert.assertNotNull(output);
        Assert.assertNotEquals(output.length, 0);
        LazyData reparsed = this.deserializer.deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, output);
        Assert.assertEquals(reparsed.getValue(), data.getValue());
    }
}
