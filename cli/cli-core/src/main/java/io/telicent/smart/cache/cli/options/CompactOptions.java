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
package io.telicent.smart.cache.cli.options;

import com.github.rvesse.airline.annotations.Option;
import io.telicent.smart.cache.configuration.Configurator;

/**
 * Options pertaining to output compaction
 */
public class CompactOptions {

    /**
     * Whether keys should be compacted
     */
    @Option(name = { "--compact-keys", "--no-compact-keys" },
            description = "Controls whether the keys in the resulting output are compacted into prefixed name form where those keys are URIs and a suitable namespace prefix is available. Defaults to enabled.")
    public boolean compactKeys =
            Configurator.get(CliEnvironmentVariables.ENABLE_COMPACT_KEYS, Boolean::parseBoolean, true);

    /**
     * Whether values should be compacted
     */
    @Option(name = { "--compact-values", "--no-compact-values" },
            description = "Controls whether the values in the resulting output are compacted into prefixed name form where those keys are URIs and a suitable namespace prefix is available. Defaults to disabled.")
    public boolean compactValues =
            Configurator.get(CliEnvironmentVariables.ENABLE_COMPACT_VALUES, Boolean::parseBoolean, false);
}
