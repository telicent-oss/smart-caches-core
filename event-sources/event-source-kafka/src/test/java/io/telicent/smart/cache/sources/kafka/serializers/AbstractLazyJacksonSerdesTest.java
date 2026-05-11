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

import io.telicent.smart.cache.payloads.LazyJacksonPayload;
import io.telicent.smart.cache.payloads.LazyPayloadException;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("resource")
public abstract class AbstractLazyJacksonSerdesTest<T, TLazy extends LazyJacksonPayload<T>, TSerializer extends Serializer<TLazy>, TDeserializer extends Deserializer<TLazy>> {

    /**
     * Provides example malformed JSON that should fail to deserialize
     *
     * @return Bad JSON
     */
    protected byte[] badJson() {
        return """
                {
                  "unterminated
                """.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Provides example good JSON that should deserialize successfully
     *
     * @return Good JSON
     */
    protected abstract byte[] goodJson();

    /**
     * Provides an example populated lazy payload, the populated value <strong>MUST</strong> be equivalent to a value
     * that would be parsed from {@link #goodJson()}
     *
     * @return Example populated payload
     */
    protected abstract TLazy exemplarPopulated();

    /**
     * Provides a serializer for this lazy Jackson payload type
     *
     * @return Serializer
     */
    protected abstract TSerializer serializer();

    /**
     * Provides a deserializer for this lazy Jackson payload type
     *
     * @return Deserializer
     */
    protected abstract TDeserializer deserializer();

    /**
     * Verifies that the data parsed from {@link #goodJson()} is as expected
     *
     * @param data Data to verify
     */
    protected abstract void verifyGoodData(T data);

    @Test
    public void givenNullData_whenDeserializing_thenNull() {
        // Given and When
        TLazy data = deserializer().deserialize(KafkaTestCluster.DEFAULT_TOPIC, null);

        // Then
        Assert.assertNull(data);
    }

    @Test
    public void givenNullValue_whenSerializing_thenNullData() {
        // Given and When
        byte[] data = serializer().serialize(KafkaTestCluster.DEFAULT_TOPIC, null);

        // Then
        Assert.assertNull(data);
    }

    @Test
    public void givenMalformedJson_whenDeserializingOk_thenAccessingValueThrowsError() {
        // Given and When
        TLazy data = deserializer().deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, badJson());

        // Then
        Assert.assertNotNull(data);
        Assert.assertThrows(LazyPayloadException.class, data::getValue);
        Assert.assertTrue(data.hasError());
        Assert.assertTrue(data.getError() instanceof LazyPayloadException);
    }

    @Test
    public void givenMalformedJson_whenDeserializedAndValueAccessed_thenCanStillBeSerialized() {
        // Given and When
        TLazy data = deserializer().deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, badJson());
        Assert.assertThrows(LazyPayloadException.class, data::getValue);

        // Then
        byte[] output = serializer().serialize(KafkaTestCluster.DEFAULT_TOPIC, null, data);
        Assert.assertEquals(output, badJson());
    }

    @Test
    public void givenMalformedJson_whenDeserialized_thenCanBeSerialized() {
        // Given and When
        TLazy data = deserializer().deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, badJson());

        // Then
        byte[] output = serializer().serialize(KafkaTestCluster.DEFAULT_TOPIC, null, data);
        Assert.assertEquals(output, badJson());
    }

    @Test
    public void givenValidJson_whenDeserialized_thenValueAccessible_andCanBeSerialized() {
        // Given and When
        TLazy data = deserializer().deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, goodJson());

        // Then
        T actualData = data.getValue();
        verifyGoodData(actualData);

        // And
        byte[] output = serializer().serialize(KafkaTestCluster.DEFAULT_TOPIC, null, data);
        Assert.assertNotNull(output);
    }

    @Test
    public void givenPopulatedPayload_whenSerializing_thenValidJsonProduced() {
        // Given
        TLazy data = exemplarPopulated();
        Assert.assertTrue(data.isReady());
        Assert.assertNotNull(data.getValue());

        // When
        byte[] output = serializer().serialize(KafkaTestCluster.DEFAULT_TOPIC, null, data);

        // Then
        Assert.assertNotNull(output);
        Assert.assertNotEquals(output.length, 0);
        TLazy reparsed = deserializer().deserialize(KafkaTestCluster.DEFAULT_TOPIC, null, output);
        Assert.assertEquals(reparsed.getValue(), data.getValue());
    }
}
