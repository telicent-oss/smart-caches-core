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

import io.telicent.smart.cache.observability.Chronological;

import java.util.Collections;
import java.util.Map;

/**
 * The supertype of all metrics.
 */
public interface Metric extends Chronological {
    /**
     * The name of the metric.
     *
     * @return the metric name, which may not be null.
     */
    String getMetricName();

    /**
     * The metric value.
     *
     * @return the metric value, which may be null to indicate no value or 'datapoint' at this instant.
     */
    Number getValue();

    /**
     * Any labels associated with the metric.
     *
     * @return any label associated with the metric, which may be empty but never null.
     */
    default Map<String, Object> getLabels() {
        return Collections.emptyMap();
    }
}
