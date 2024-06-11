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
package io.telicent.smart.cache.configuration.sources;

/**
 * A configuration source that reads configuration from environment variables
 */
public final class EnvironmentSource implements ConfigurationSource {

    /**
     * The singleton instance of the environment source
     */
    public static final ConfigurationSource INSTANCE = new EnvironmentSource();

    /**
     * Private constructor to force usage of the singleton instance {@link #INSTANCE}
     */
    private EnvironmentSource() {

    }

    @Override
    public String get(String key) {
        return System.getenv(ConfigurationSource.asEnvironmentVariableKey(key));
    }
}
