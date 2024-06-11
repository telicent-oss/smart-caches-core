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
package io.telicent.smart.cache.server.jaxrs.parameters;

import io.telicent.smart.cache.server.jaxrs.model.Mode;
import jakarta.ws.rs.ext.ParamConverter;

import java.util.Locale;

public class ModeConverter implements ParamConverter<Mode> {
    @Override
    public Mode fromString(String value) {
        return Mode.valueOf(value.toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString(Mode value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}
