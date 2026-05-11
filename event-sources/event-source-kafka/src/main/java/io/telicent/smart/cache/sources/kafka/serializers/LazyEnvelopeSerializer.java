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

import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;

/**
 * A serializer for the {@link LazyEnvelope} type
 * <p>
 * Uses the shared {@link Envelope#JSON} object mapper to ensure consistent handling of the actual {@link Envelope}
 * type.
 * </p>
 */
public class LazyEnvelopeSerializer extends LazyJacksonSerializer<Envelope, LazyEnvelope> {

    /**
     * Creates a new serializer
     */
    public LazyEnvelopeSerializer() {
        super(Envelope.JSON);
    }
}
