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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.smart.cache.payloads.LazyJacksonPayload;
import io.telicent.smart.cache.payloads.LazyPayloadException;
import io.telicent.smart.cache.sources.kafka.KafkaTestCluster;
import org.apache.commons.lang3.RandomUtils;
import org.apache.kafka.common.errors.SerializationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("resource")
public class TestLazyJacksonSerdesBad {

    private final LazyJacksonSerializer<Bad, LazyBad> serializer = new LazyJacksonSerializer<>();

    private static final class Bad {

        public String getTest() {
            throw new RuntimeException();
        }

        public void setTest(String value) {
            throw new RuntimeException();
        }
    }

    private static class LazyBad extends LazyJacksonPayload<Bad> {

        LazyBad(byte[] rawData) {
            super(new ObjectMapper(), Bad.class, rawData);
        }

        LazyBad(Bad value) {
            super(value);
        }
    }

    @Test
    public void givenBadType_whenSerializing_thenFails() {
        // Given
        Bad bad = new Bad();
        LazyBad lazy = new LazyBad(bad);

        // When and Then
        Assert.assertThrows(SerializationException.class,
                            () -> serializer.serialize(KafkaTestCluster.DEFAULT_TOPIC, lazy));
    }

    @Test
    public void givenBadType_whenDeserializing_thenFails() {
        // Given
        LazyBad lazy = new LazyBad("""
                                           {
                                             "test": "foo"
                                           }
                                           """.getBytes(StandardCharsets.UTF_8));

        // When and Then
        Assert.assertThrows(LazyPayloadException.class, lazy::getValue);
    }

    private static final class LazyNoData extends LazyBad {

        public boolean hasRawData = false, hasError = false;

        LazyNoData(byte[] rawData) {
            super(rawData);
        }

        LazyNoData(Bad value) {
            super(value);
        }

        @Override
        public boolean hasRawData() {
            return this.hasRawData;
        }

        @Override
        public boolean hasError() {
            return this.hasError;
        }
    }

    @Test(expectedExceptions = SerializationException.class, expectedExceptionsMessageRegExp = ".* raw data is not available")
    public void givenBadLazyErrorWithNoRawData_whenSerializing_thenFails() {
        // Given
        LazyNoData lazy = new LazyNoData(RandomUtils.insecure().randomBytes(50));
        lazy.hasError = true;
        lazy.hasRawData = false;

        // When and Then
        serializer.serialize(KafkaTestCluster.DEFAULT_TOPIC, lazy);
    }

    @Test(expectedExceptions = SerializationException.class, expectedExceptionsMessageRegExp = ".* raw data is not available")
    public void givenBadLazyWithNoRawData_whenSerializing_thenFails() {
        // Given
        LazyNoData lazy = new LazyNoData(RandomUtils.insecure().randomBytes(50));
        lazy.hasRawData = false;

        // When and Then
        serializer.serialize(KafkaTestCluster.DEFAULT_TOPIC, lazy);
    }
}
