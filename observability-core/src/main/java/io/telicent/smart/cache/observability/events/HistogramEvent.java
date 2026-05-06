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
package io.telicent.smart.cache.observability.events;

import io.telicent.smart.cache.observability.metrics.HistogramMetric;
import io.telicent.smart.cache.observability.metrics.Metric;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;

/**
 * A histogram event, which records an observed numeric value into a distribution.
 */
@Getter
@SuperBuilder
public class HistogramEvent extends AbstractEvent implements MetricEvent {
    /** The name of the histogram metric to associate with this event, or null if the event name is to be used. */
    private final String metricName;
    /** Any labels applied to the metric associated with this event, which may be null indicating none. */
    private final Map<String, Object> metricLabels;
    /** The observed value to record into the histogram. */
    private final Number value;

    /**
     * Constructs a histogram event with the given metric name and observed value.
     *
     * @param name the name of the event and metric.
     * @param value the observed value recorded by the histogram.
     * @return a new histogram event with the given event/metric name and value.
     */
    public static HistogramEvent histogramEvent(final String name, final Number value) {
        return HistogramEvent.builder().eventName(name).metricName(name).value(value).build();
    }

    /**
     * Constructs a histogram event with the given metric name, observed value and associated metric labels.
     *
     * @param name the name of the event and metric.
     * @param value the observed value recorded by the histogram.
     * @param metricLabels the metric labels associated with this event.
     * @return a new histogram event with the given event/metric name, value and labels.
     */
    public static HistogramEvent histogramEvent(final String name,
                                                final Number value,
                                                final Map<String, Object> metricLabels) {
        return HistogramEvent.builder()
                             .eventName(name)
                             .metricName(name)
                             .value(value)
                             .metricLabels(metricLabels)
                             .build();
    }

    @Override
    public List<Metric> getMetrics() {
        return singletonList(HistogramMetric.builder()
                                            .startedAt(getStartedAt())
                                            .endedAt(getEndedAt())
                                            .metricName(nonNull(getMetricName()) ? getMetricName() : getEventName())
                                            .value(getValue())
                                            .labels((nonNull(getMetricLabels()) ? getMetricLabels() : Collections.emptyMap()))
                                            .build()
        );
    }
}
