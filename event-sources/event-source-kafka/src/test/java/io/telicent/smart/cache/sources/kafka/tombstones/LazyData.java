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
package io.telicent.smart.cache.sources.kafka.tombstones;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.smart.cache.payloads.LazyJacksonPayload;

public class LazyData extends LazyJacksonPayload<Data> {
    LazyData(ObjectMapper mapper, Class<Data> cls, byte[] rawData) {
        super(mapper, cls, rawData);
    }

    public LazyData(Data value) {
        super(value);
    }
}
