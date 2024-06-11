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
package io.telicent.smart.cache.sources.kafka.serializers;

import java.util.Map;

public class TestPayloadDeserializerBadConfig extends TestPayloadDeserializer {

    private final Map<String, ?> BAD_PARSING_CONFIGURATION =
            Map.of(RdfPayloadDeserializer.EAGER_PARSING_CONFIG_KEY, 1234);

    @Override
    protected RdfPayloadDeserializer createPayloadDeserializer() {
        RdfPayloadDeserializer deserializer = super.createPayloadDeserializer();
        deserializer.configure(BAD_PARSING_CONFIGURATION, false);
        return deserializer;
    }

    @Override
    protected boolean isEagerParsing() {
        return false;
    }
}
