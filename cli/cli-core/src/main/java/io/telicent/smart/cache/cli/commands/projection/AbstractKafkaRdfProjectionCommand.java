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

import io.telicent.smart.cache.payloads.RdfPayload;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.Header;
import io.telicent.smart.cache.sources.kafka.KafkaDatasetGraphSource;
import io.telicent.smart.cache.sources.kafka.KafkaRdfPayloadSource;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.utils.Bytes;

import java.util.List;
import java.util.function.Function;

/**
 * Abstract base class for commands that run a Projector using a Kafka Event source where the events are represented as
 * RDF graphs i.e. using a {@link KafkaDatasetGraphSource}
 *
 * @param <TOutput> Output type
 */
public abstract class AbstractKafkaRdfProjectionCommand<TOutput>
        extends AbstractKafkaProjectorCommand<Bytes, RdfPayload, TOutput> {

    @Override
    protected EventSource<Bytes, RdfPayload> getSource() {
        return KafkaRdfPayloadSource.<Bytes>createRdfPayload()
                                    .bootstrapServers(this.kafka.bootstrapServers)
                                    .topics(this.kafka.topics)
                                    .consumerGroup(this.kafka.getConsumerGroup())
                                    .consumerConfig(this.kafka.getAdditionalProperties())
                                    .keyDeserializer(BytesDeserializer.class)
                                    .maxPollRecords(this.kafka.getMaxPollRecords())
                                    .readPolicy(this.kafka.readPolicy.toReadPolicy())
                                    .lagReportInterval(this.kafka.getLagReportInterval())
                                    .autoCommit(this.useAutoCommit())
                                    .build();
    }

    @Override
    protected List<Function<Event<Bytes, RdfPayload>, Header>> additionalCaptureHeaderGenerators() {
        // Force the Content-Type header of captured events to the simplest and most portable format regardless of their input Content-Type header
        Function<Event<Bytes, RdfPayload>, Header> generator = e -> e.value() == null ? null : e.value().isDataset() ?
                                                                                               new Header(
                                                                                                       HttpNames.hContentType,
                                                                                                       WebContent.contentTypeNQuads) :
                                                                                               new Header(
                                                                                                       HttpNames.hContentType,
                                                                                                       WebContent.ctPatch.getContentTypeStr());
        return List.of(generator);
    }
}
