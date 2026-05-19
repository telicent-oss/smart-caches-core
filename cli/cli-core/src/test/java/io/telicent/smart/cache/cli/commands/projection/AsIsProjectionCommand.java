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

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.live.model.IODescriptor;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import io.telicent.smart.cache.sources.Event;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;

import java.util.function.Supplier;

@Command(name = "project")
public class AsIsProjectionCommand extends AbstractKafkaProjectorCommand<Bytes, Bytes, Event<Bytes, Bytes>> {

    @AirlineModule
    public DeadLetterTestingOptions<Bytes, Bytes> deadLetterTestingOptions = new DeadLetterTestingOptions<>();

    public static void main(String[] args) {
        SmartCacheCommand.runAsSingleCommand(AsIsProjectionCommand.class, args);
    }

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
        return "events";
    }

    @Override
    protected Supplier<HealthStatus> getHealthProbeSupplier() {
        // Test commands always consider themselves to be healthy
        return () -> HealthStatus.builder().healthy(true).build();
    }

    @Override
    protected Sink<Event<Bytes, Bytes>> prepareWorkSink() {
        Sink<Event<Bytes, Bytes>> deadLetters =
                this.prepareDeadLetterSink(this.kafka.dlqTopic, this.keySerializerClass(), this.valueSerializerClass());
        return new PeriodicDeadLetterSink<>(this.deadLetterTestingOptions.successful,
                                            this.deadLetterTestingOptions.deadLetterFrequency,
                                            deadLetters);
    }

    @Override
    protected IODescriptor getLiveReporterOutputDescriptor() {
        return new IODescriptor("test", "test");
    }

}
