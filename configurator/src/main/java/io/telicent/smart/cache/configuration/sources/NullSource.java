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
 * A configuration source where all configuration is always {@code null}.
 * <p>
 * Generally used only in test scenarios to forcibly simulate running code with no configuration values available to it
 * to test that it falls back to suitable defaults.
 * </p>
 */
public final class NullSource implements ConfigurationSource {

    /**
     * A singleton instance of the null source
     */
    public static final ConfigurationSource INSTANCE = new NullSource();

    /**
     * Private constructor to force the singleton instance {@link #INSTANCE} to be used
     */
    private NullSource() {

    }

    @Override
    public String get(String key) {
        return null;
    }
}
