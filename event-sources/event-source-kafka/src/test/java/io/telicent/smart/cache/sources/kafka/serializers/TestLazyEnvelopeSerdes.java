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
        AbstractLazyJacksonSerdesTest<Envelope, LazyEnvelope, LazyEnveloperSerializer, LazyEnvelopeDeserializer> {

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
    protected LazyEnveloperSerializer serializer() {
        return new LazyEnveloperSerializer();
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
