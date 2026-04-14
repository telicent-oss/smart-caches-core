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

import io.telicent.smart.cache.payloads.RdfPayload;
import io.telicent.smart.cache.sources.kafka.serializers.RdfPayloadDeserializer;
import io.telicent.smart.cache.sources.kafka.serializers.RdfPayloadSerializer;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class DockerTestKafkaTombstonesRdfPayload extends AbstractKafkaTombstoneTests<RdfPayload> {

    @Override
    protected RdfPayload exemplarValue() {
        return RdfPayload.of(DatasetGraphFactory.empty());
    }

    @Override
    protected Class<RdfPayloadDeserializer> valueDeserializerClass() {
        return RdfPayloadDeserializer.class;
    }

    @Override
    protected Class<RdfPayloadSerializer> valueSerializerClass() {
        return RdfPayloadSerializer.class;
    }
}
