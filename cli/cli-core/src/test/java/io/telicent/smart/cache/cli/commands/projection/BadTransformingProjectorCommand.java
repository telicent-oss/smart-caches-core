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
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;

@Command(name = "projector")
public class BadTransformingProjectorCommand
        extends TransformingProjectorCommand {

    @Override
    protected Sink<Event<Integer, String>> prepareWorkSink() {
        // BAD - As the command transforms the event types from their original types can't use the declared key/value
        // serializer directly as when the sink receives a dead letter it'll produce a ClassCastException
        // Correct behaviour is shown in parent class of supplying the appropriate serializer classes for the types at
        // the point the event will be dead lettered
        Sink<Event<Integer, String>> deadLetters =
                this.prepareDeadLetterSink(this.kafka.dlqTopic, keySerializerClass(), valueSerializerClass());
        return new PeriodicDeadLetterSink<>(this.deadLetterTestingOptions.successful,
                                            this.deadLetterTestingOptions.deadLetterFrequency,
                                            deadLetters);
    }
}
