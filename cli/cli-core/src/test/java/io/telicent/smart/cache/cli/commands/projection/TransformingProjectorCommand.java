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
import io.telicent.smart.cache.projectors.Projector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.server.jaxrs.model.HealthStatus;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.kafka.sinks.KafkaSink;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.*;

import java.util.function.Supplier;

@Command(name = "projector")
public class TransformingProjectorCommand
        extends AbstractKafkaProjectorCommand<String, String, Event<Integer, String>> {

    @AirlineModule
    public DeadLetterTestingOptions<Integer, String> deadLetterTestingOptions = new DeadLetterTestingOptions<>();

    public static void main(String[] args) {
        SmartCacheCommand.runAsSingleCommand(TransformingProjectorCommand.class, args);
    }

    @Override
    protected IODescriptor getLiveReporterOutputDescriptor() {
        return new IODescriptor("test", "test");
    }

    @Override
    protected Serializer<String> keySerializer() {
        return new StringSerializer();
    }

    @Override
    protected Deserializer<String> keyDeserializer() {
        return new StringDeserializer();
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
        return "events";
    }

    @Override
    protected Supplier<HealthStatus> getHealthProbeSupplier() {
        // Test commands always consider themselves to be healthy
        return () -> HealthStatus.builder().healthy(true).build();
    }

    @Override
    protected Projector<Event<String, String>, Event<Integer, String>> getProjector() {
        return (event, sink) -> sink.send(event.replaceKey(Integer.parseInt(event.key())));
    }

    @Override
    protected Sink<Event<Integer, String>> prepareWorkSink() {
        Sink<Event<Integer, String>> deadLetters =
                this.prepareDeadLetterSink(this.kafka.dlqTopic, IntegerSerializer.class, StringSerializer.class);
        return new PeriodicDeadLetterSink<>(this.deadLetterTestingOptions.successful,
                                            this.deadLetterTestingOptions.deadLetterFrequency,
                                            deadLetters);
    }
}
