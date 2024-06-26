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
package io.telicent.smart.caches.configuration.auth.TestTelicentConfigurationAdaptor;

import io.telicent.smart.caches.configuration.auth.TelicentConfigurationAdaptor;

import java.util.HashMap;
import java.util.Map;

public class MockAdaptor extends TelicentConfigurationAdaptor {
    private final Map<String, Object> attributes = new HashMap<>();

    @Override
    public void setAttribute(String attribute, Object value) {
        this.attributes.put(attribute, value);
    }

    @Override
    public Object getAttribute(String attribute) {
        return this.attributes.get(attribute);
    }
}
