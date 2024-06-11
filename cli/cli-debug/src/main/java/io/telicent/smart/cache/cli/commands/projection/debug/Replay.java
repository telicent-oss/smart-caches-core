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
import io.telicent.smart.cache.cli.commands.projection.AbstractProjectorCommand;
import io.telicent.smart.cache.cli.options.KafkaOutputOptions;
import io.telicent.smart.cache.live.model.IODescriptor;
import io.telicent.smart.cache.projectors.NoOpProjector;
import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;

/**
 * A debug command for replaying an event capture back onto a Kafka topic
 */
@Command(name = "replay", description = "Replays a previously obtained event capture back onto a Kafka topic without any interpretation of its contents.")
public class Replay extends AbstractProjectorCommand<Bytes, Bytes, Event<Bytes, Bytes>> {

    @AirlineModule
    private final KafkaOutputOptions kafkaOutputOptions = new KafkaOutputOptions();

    @Override
    protected Serializer<Bytes> keySerializer() {
        return new BytesSerializer();
    }

    @Override
    protected Deserializer<Bytes> keyDeserializer() {
        return new BytesDeserializer();
    }

    @Override
    protected Serializer<Bytes> valueSerializer() {
        return new BytesSerializer();
    }

    @Override
    protected Deserializer<Bytes> valueDeserializer() {
        return new BytesDeserializer();
    }

    @Override
    protected String getThroughputItemsName() {
        return "Events";
    }

    @Override
    protected EventSource<Bytes, Bytes> getSource() {
        return this.fileSourceOptions.getFileSource(this.keyDeserializer(), this.valueDeserializer());
    }

    @Override
    protected Projector<Event<Bytes, Bytes>, Event<Bytes, Bytes>> getProjector() {
        return new NoOpProjector<>();
    }

    @Override
    protected Sink<Event<Bytes, Bytes>> prepareWorkSink() {
        return KafkaSink.<Bytes, Bytes>create()
                        .bootstrapServers(this.kafkaOutputOptions.bootstrapServers)
                        .topic(this.kafkaOutputOptions.topic)
                        .keySerializer(BytesSerializer.class)
                        .valueSerializer(BytesSerializer.class)
                        .lingerMs(5)
                        .build();
    }

    @Override
    protected void setupLiveReporter(CommandMetadata metadata) {
        //@formatter:off
        this.liveReporter.setupLiveReporter(this.kafkaOutputOptions.bootstrapServers,
                                            "Event Capture Replay to Kafka",
                                            metadata.getName(),
                                            "adapter",
                                            new IODescriptor(this.fileSourceOptions.getCaptureDirectory(), "directory"),
                                            new IODescriptor(this.kafkaOutputOptions.topic, "topic"));
        //@formatter:on

        this.liveReporter.setupErrorReporter(this.kafkaOutputOptions.bootstrapServers, metadata.getName());
    }
}
