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

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.model.CommandMetadata;
import io.telicent.smart.cache.cli.commands.projection.AbstractKafkaProjectorCommand;
import io.telicent.smart.cache.live.model.IODescriptor;
import io.telicent.smart.cache.projectors.NoOpProjector;
import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.kafka.KafkaEventSource;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;

import java.util.function.Supplier;

/**
 * A debug command that captures a Kafka topic to a sequence of files in a directory
 */
@Command(name = "capture", description = "Captures the contents of a topic to a directory without any interpretation of the the event contents.")
public class Capture extends AbstractKafkaProjectorCommand<Bytes, Bytes, Event<Bytes, Bytes>> {

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
    protected Supplier<HealthStatus> getHealthProbeSupplier() {
        // Debug commands always consider themselves to be healthy
        return () -> HealthStatus.builder().healthy(true).build();
    }

    @Override
    protected EventSource<Bytes, Bytes> getSource() {
        return KafkaEventSource
                .<Bytes, Bytes>create()
                .keyDeserializer(BytesDeserializer.class)
                .valueDeserializer(BytesDeserializer.class)
                .bootstrapServers(this.kafka.bootstrapServers)
                .topics(this.kafka.topics)
                .consumerGroup(this.kafka.getConsumerGroup("smart-cache-debug-capture"))
                .consumerConfig(this.kafka.getAdditionalProperties())
                .maxPollRecords(this.kafka.getMaxPollRecords())
                .readPolicy(this.kafka.readPolicy.toReadPolicy())
                .build();
    }

    @Override
    protected Projector<Event<Bytes, Bytes>, Event<Bytes, Bytes>> getProjector() {
        return new NoOpProjector<>();
    }

    @Override
    protected Sink<Event<Bytes, Bytes>> prepareWorkSink() {
        if (!this.fileSourceOptions.usingFileCapture()) {
            throw new IllegalArgumentException("Failed to specify sufficient options to enable file event capture");
        }

        return NullSink.of();
    }

    @Override
    protected String getLiveReporterApplicationName(CommandMetadata metadata) {
        return "Kafka Event Capture";
    }

    @Override
    protected IODescriptor getLiveReporterOutputDescriptor() {
        return new IODescriptor(this.fileSourceOptions.getCaptureDirectory(), "directory");
    }
}
