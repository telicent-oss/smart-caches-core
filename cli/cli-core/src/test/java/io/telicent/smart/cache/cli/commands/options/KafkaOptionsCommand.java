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
package io.telicent.smart.cache.cli.commands.options;

import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import io.telicent.smart.cache.cli.commands.SmartCacheCommand;
import io.telicent.smart.cache.cli.options.KafkaOptions;

@Command(name = "kafka-options")
public class KafkaOptionsCommand extends SmartCacheCommand {

    @AirlineModule
    public KafkaOptions kafkaOptions = new KafkaOptions();

    @Override
    public int run() {
        return 0;
    }
}
