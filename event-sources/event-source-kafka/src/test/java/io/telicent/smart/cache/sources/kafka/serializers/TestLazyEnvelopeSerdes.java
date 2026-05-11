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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.Metadata;
import org.testng.Assert;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class TestLazyEnvelopeSerdes extends
        AbstractLazyJacksonSerdesTest<Envelope, LazyEnvelope, LazyEnvelopeSerializer, LazyEnvelopeDeserializer> {

    private static final LazyEnvelope ENVELOPE = LazyEnvelope.of(Envelope.create()
                                                                         .id(UUID.randomUUID())
                                                                         .metadata(Metadata.create()
                                                                                           .generatedBy("tests")
                                                                                           .generatorVersion("1.0")
                                                                                           .generatedAt(Date.from(
                                                                                                   Instant.now()))
                                                                                           .documentFormat("tests/v1")
                                                                                           .build())
                                                                         .body(Map.of("test", "foo", "flag", true))
                                                                         .build());

    @Override
    protected byte[] goodJson() {
        try {
            return Envelope.JSON.writeValueAsBytes(ENVELOPE.getValue());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected LazyEnvelope exemplarPopulated() {
        return ENVELOPE;
    }

    @Override
    protected LazyEnvelopeSerializer serializer() {
        return new LazyEnvelopeSerializer();
    }

    @Override
    protected LazyEnvelopeDeserializer deserializer() {
        return new LazyEnvelopeDeserializer();
    }

    @Override
    protected void verifyGoodData(Envelope data) {
        Assert.assertEquals(data, ENVELOPE.getValue());
    }
}
