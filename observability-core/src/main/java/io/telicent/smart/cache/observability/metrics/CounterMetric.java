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
package io.telicent.smart.cache.observability.metrics;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * A counter metric which keeps a tally of items observed.
 */
@Getter
@SuperBuilder
public class CounterMetric extends AbstractMetric {
    private final Number count;

    @Override
    public Number getValue() {
        return getCount();
    }
}
