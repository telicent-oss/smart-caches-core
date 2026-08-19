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
package io.telicent.smart.cache.cli.commands.projection;

import com.github.rvesse.airline.annotations.Command;
import io.telicent.smart.cache.payloads.RdfPayload;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.projectors.sinks.Sinks;
import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.kafka.serializers.RdfPayloadDeserializer;
import io.telicent.smart.cache.sources.kafka.serializers.RdfPayloadSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;

import java.util.function.Supplier;

@Command(name = "rdf")
public class RdfCommand extends AbstractKafkaRdfProjectionCommand<Event<Bytes, RdfPayload>>  {
    @Override
    protected Serializer<RdfPayload> valueSerializer() {
        return new RdfPayloadSerializer();
    }

    @Override
    protected Deserializer<RdfPayload> valueDeserializer() {
        return new RdfPayloadDeserializer();
    }

    @Override
    protected String getThroughputItemsName() {
        return "Events";
    }

    @Override
    protected Supplier<HealthStatus> getHealthProbeSupplier() {
        return null;
    }

    @Override
    protected Sink<Event<Bytes, RdfPayload>> prepareWorkSink() {
        return NullSink.of();
    }
}
