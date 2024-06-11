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
package io.telicent.smart.cache.cli.commands.restrictions;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.options.FileSourceOptions;
import io.telicent.smart.cache.cli.options.KafkaOptions;
import io.telicent.smart.cache.cli.restrictions.SourceRequired;
import org.apache.kafka.common.utils.Bytes;

@Command(name = "fake")
public class FakeCommand extends SmartCacheCommand {

    @AirlineModule
    private final KafkaOptions kafka = new KafkaOptions();

    @AirlineModule
    private final FileSourceOptions<Bytes, Bytes> fileSource = new FileSourceOptions<>();

    @Option(name = "--fake-source", arity = 1)
    @SourceRequired(name = "fake", unlessEnvironment = "FAKE_SOURCE")
    private String fakeSource;

    @Override
    public int run() {
        return 0;
    }
}
