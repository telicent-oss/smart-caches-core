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
package io.telicent.smart.cache.payloads;

class Broken extends LazyPayload<ActualBody> {
    protected Broken(String contentType, byte[] rawData) {
        super(contentType, rawData);
    }

    protected Broken(ActualBody value) {
        super(value);
    }

    protected Broken(ActualBody value, String contentType) {
        super(value, contentType);
    }

    @Override
    protected ActualBody deserialize() {
        throw new RuntimeException("Broken");
    }
}
