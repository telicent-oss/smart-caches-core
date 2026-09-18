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
import io.telicent.smart.cache.payloads.LazyUUID;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

// java:S2699 - assertions are made via the Assert class
@SuppressWarnings("resource")
public class TestLazyUUIDSerdes {

    private static final String TOPIC = KafkaTestCluster.DEFAULT_TOPIC;
    private static final byte[] MALFORMED = "not-a-uuid".getBytes(StandardCharsets.UTF_8);

    private static LazyUUIDSerializer serializer() {
        return new LazyUUIDSerializer();
    }

    private static LazyUUIDDeserializer deserializer() {
        return new LazyUUIDDeserializer();
    }

    @Test
    public void givenNullData_whenDeserializing_thenNull() {
        // Given, When and Then
        Assert.assertNull(deserializer().deserialize(TOPIC, null));
    }

    @Test
    public void givenNullValue_whenSerializing_thenNullData() {
        // Given, When and Then
        Assert.assertNull(serializer().serialize(TOPIC, null));
    }

    @Test
    public void givenValidUuid_whenDeserializing_thenValueAccessible() {
        // Given
        final UUID expected = UUID.randomUUID();
        final byte[] data = expected.toString().getBytes(StandardCharsets.UTF_8);

        // When
        final LazyUUID lazy = deserializer().deserialize(TOPIC, null, data);

        // Then
        Assert.assertNotNull(lazy);
        Assert.assertEquals(lazy.getValue(), expected);
    }

    @Test
    public void givenMalformedUuid_whenDeserializing_thenNoErrorUntilValueAccessed() {
        // Given and When
        final LazyUUID lazy = deserializer().deserialize(TOPIC, null, MALFORMED);

        // Then
        Assert.assertNotNull(lazy, "Deserialization must not fail, that's the whole point of a lazy key");
        Assert.assertThrows(LazyPayloadException.class, lazy::getValue);
        Assert.assertTrue(lazy.hasError());
    }

    @Test
    public void givenMalformedUuid_whenDeserializedAndSerialized_thenRawDataIsPreserved() {
        // Given
        final LazyUUID lazy = deserializer().deserialize(TOPIC, null, MALFORMED);

        // When
        final byte[] output = serializer().serialize(TOPIC, null, lazy);

        // Then
        Assert.assertEquals(output, MALFORMED);
    }

    @Test
    public void givenMalformedUuid_whenValueAccessedBeforeSerializing_thenRawDataIsStillPreserved() {
        // Given
        final LazyUUID lazy = deserializer().deserialize(TOPIC, null, MALFORMED);
        Assert.assertThrows(LazyPayloadException.class, lazy::getValue);

        // When
        final byte[] output = serializer().serialize(TOPIC, null, lazy);

        // Then
        Assert.assertEquals(output, MALFORMED);
    }

    @Test
    public void givenPopulatedUuid_whenSerializing_thenMatchesKafkaUuidSerializer() {
        // Given
        final UUID value = UUID.randomUUID();

        // When
        final byte[] output = serializer().serialize(TOPIC, null, LazyUUID.of(value));

        // Then
        Assert.assertEquals(output, new UUIDSerializer().serialize(TOPIC, value));
        Assert.assertEquals(deserializer().deserialize(TOPIC, null, output).getValue(), value);
    }

    @Test
    public void givenKafkaSerializedUuid_whenDeserializing_thenMatchesKafkaUuidDeserializer() {
        // Given
        final UUID value = UUID.randomUUID();
        final byte[] data = new UUIDSerializer().serialize(TOPIC, value);

        // When and Then
        Assert.assertEquals(deserializer().deserialize(TOPIC, data).getValue(),
                            new UUIDDeserializer().deserialize(TOPIC, data));
    }

    @Test
    public void givenNonDefaultEncoding_whenConfiguringSerdes_thenRoundTripsSuccessfully() {
        // Given
        final UUID value = UUID.randomUUID();
        final Map<String, String> config = Map.of(LazyUUIDDeserializer.KEY_ENCODING_CONFIG, StandardCharsets.UTF_16.name());
        final LazyUUIDDeserializer deserializer = deserializer();
        deserializer.configure(config, true);

        // When
        LazyUUID lazy = deserializer.deserialize(TOPIC, value.toString().getBytes(StandardCharsets.UTF_16));

        // Then
        Assert.assertEquals(lazy.getValue(), value);
    }

    @Test
    public void givenGenericEncodingConfig_whenConfiguringDeserializer_thenItIsHonoured() {
        // Given
        final UUID value = UUID.randomUUID();
        final LazyUUIDDeserializer deserializer = deserializer();
        deserializer.configure(Map.of(LazyUUIDDeserializer.ENCODING_CONFIG, StandardCharsets.UTF_16.name()), false);

        // When
        final LazyUUID lazy = deserializer.deserialize(TOPIC, value.toString().getBytes(StandardCharsets.UTF_16));

        // Then
        Assert.assertEquals(lazy.getValue(), value);
    }

    @Test(expectedExceptions = SerializationException.class, expectedExceptionsMessageRegExp = ".*unsupported encoding.*")
    public void givenUnsupportedEncoding_whenConfiguringDeserializer_thenFails() {
        // Given, When and Then
        deserializer().configure(Map.of(LazyUUIDDeserializer.KEY_ENCODING_CONFIG, "no-such-charset"), true);
    }

    @Test
    public void givenNoEncodingConfig_whenConfiguringSerdes_thenDefaultsAreUsed() {
        // Given
        final UUID value = UUID.randomUUID();
        final LazyUUIDDeserializer deserializer = deserializer();
        final LazyUUIDSerializer serializer = serializer();

        // When
        deserializer.configure(Map.of(), true);
        serializer.configure(Map.of(), true);

        // Then
        final byte[] output = serializer.serialize(TOPIC, LazyUUID.of(value));
        Assert.assertEquals(output, value.toString().getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(deserializer.deserialize(TOPIC, output).getValue(), value);
        serializer.close();
        deserializer.close();
    }
}
