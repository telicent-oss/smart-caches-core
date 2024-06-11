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
package io.telicent.smart.cache.cli.commands.projection.debug;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.model.CommandMetadata;
import io.telicent.smart.cache.cli.commands.projection.AbstractKafkaProjectorCommand;
import io.telicent.smart.cache.cli.options.OffsetStoreOptions;
import io.telicent.smart.cache.live.model.IODescriptor;
import io.telicent.smart.cache.projectors.NoOpProjector;
import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.common.utils.Bytes;

/**
 * A debug command that dumps a Kafka topic to the console assuming the values can be interpreted as strings
 */
@Command(name = "dump",
        description = "Dumps the contents of a topic to the console assuming values can be treated as strings and ignoring keys")
public class Dump extends AbstractKafkaProjectorCommand<Bytes, String, Event<String, String>> {

    @AirlineModule
    private OffsetStoreOptions offsetStoreOptions = new OffsetStoreOptions();

    @Override
    protected Serializer<Bytes> keySerializer() {
        return new BytesSerializer();
    }

    @Override
    protected Deserializer<Bytes> keyDeserializer() {
        return new BytesDeserializer();
    }

    @Override
    protected Serializer<String> valueSerializer() {
        return new StringSerializer();
    }

    @Override
    protected Deserializer<String> valueDeserializer() {
        return new StringDeserializer();
    }

    @Override
    protected String getThroughputItemsName() {
        return "Events";
    }

    @Override
    protected EventSource<Bytes, String> getSource() {
        return KafkaEventSource
                .<Bytes, String>create()
                .keyDeserializer(BytesDeserializer.class)
                .valueDeserializer(StringDeserializer.class)
                .bootstrapServers(this.kafka.bootstrapServers)
                .topics(this.kafka.topics)
                .consumerGroup(this.kafka.getConsumerGroup())
                .consumerConfig(this.kafka.getAdditionalProperties())
                .maxPollRecords(this.kafka.getMaxPollRecords())
                .readPolicy(this.kafka.readPolicy.toReadPolicy())
                .lagReportInterval(this.kafka.getLagReportInterval())
                .autoCommit(this.useAutoCommit())
                .externalOffsetStore(this.offsetStoreOptions.getOffsetStore())
                .build();
    }

    @Override
    protected Projector getProjector() {
        return new NoOpProjector();
    }

    @Override
    protected Sink<Event<String, String>> prepareWorkSink() {
        return event -> System.out.println(event.value());
    }

    @Override
    protected String getLiveReporterApplicationName(CommandMetadata metadata) {
        return "Kafka Topic Dumper";
    }

    @Override
    protected IODescriptor getLiveReporterOutputDescriptor() {
        return new IODescriptor("stdout", "stream");
    }
}
