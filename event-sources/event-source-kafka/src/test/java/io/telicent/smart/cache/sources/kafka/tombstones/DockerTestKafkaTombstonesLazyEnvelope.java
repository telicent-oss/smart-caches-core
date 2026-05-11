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
package io.telicent.smart.cache.sources.kafka.tombstones;

import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.Metadata;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeDeserializer;
import io.telicent.smart.cache.sources.kafka.serializers.LazyEnvelopeSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class DockerTestKafkaTombstonesLazyEnvelope extends AbstractKafkaTombstoneTests<LazyEnvelope> {

    @Override
    protected LazyEnvelope exemplarValue() {
        return LazyEnvelope.of(Envelope.create()
                                       .id(UUID.randomUUID())
                                       .metadata(Metadata.create()
                                                         .generatedBy("tests")
                                                         .generatedAt(Date.from(Instant.now()))
                                                         .generatorVersion("0.1")
                                                         .documentFormat("tests/v1")
                                                         .build())
                                       .body(Map.of("test", true, "string", "foo"))
                                       .build());
    }

    @Override
    protected Class<? extends Deserializer<LazyEnvelope>> valueDeserializerClass() {
        return LazyEnvelopeDeserializer.class;
    }

    @Override
    protected Class<? extends Serializer<LazyEnvelope>> valueSerializerClass() {
        return LazyEnvelopeSerializer.class;
    }

}
