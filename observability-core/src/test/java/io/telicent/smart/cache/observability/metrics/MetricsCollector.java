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

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.*;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.telicent.smart.cache.observability.AttributeNames;

import java.util.*;

/**
 * A metrics collector that stores metrics in-memory, intended only for test validation
 */
public class MetricsCollector implements MetricExporter {

    private final Map<String, Map<Attributes, Double>> recordedValues = new HashMap<>();

    /**
     * Gets a metric
     *
     * @param name       Metric name
     * @param attributes Metric attributes
     * @return Value, or {@code null} if no such metric recorded
     */
    public Double getMetric(String name, Attributes attributes) {
        return recordedValues.getOrDefault(name, Collections.emptyMap()).getOrDefault(attributes, null);
    }

    /**
     * Gets all recorded metrics
     *
     * @return All metrics
     */
    public Map<String, Map<Attributes, Double>> getAllMetrics() {
        return recordedValues;
    }

    public static Attributes attributesForStorage(Attributes attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Attributes.empty();
        }

        Map<AttributeKey<?>, Object> map = attributes.asMap();
        AttributesBuilder builder = Attributes.builder();
        for (Map.Entry<AttributeKey<?>, Object> entry : map.entrySet()) {
            if (Objects.equals(entry.getKey().getKey(), AttributeNames.INSTANCE_ID)) {
                continue;
            }
            putAttribute(builder, entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static void putAttribute(AttributesBuilder builder, AttributeKey<?> key, Object value) {
        if (value instanceof String stringValue) {
            builder.put((AttributeKey<String>) key, stringValue);
        } else if (value instanceof Boolean booleanValue) {
            builder.put((AttributeKey<Boolean>) key, booleanValue);
        } else if (value instanceof Long longValue) {
            builder.put((AttributeKey<Long>) key, longValue);
        } else if (value instanceof Integer integerValue) {
            builder.put((AttributeKey<Long>) key, integerValue.longValue());
        } else if (value instanceof Double doubleValue) {
            builder.put((AttributeKey<Double>) key, doubleValue);
        } else if (value instanceof Float floatValue) {
            builder.put((AttributeKey<Double>) key, floatValue.doubleValue());
        } else {
            builder.put(key.getKey(), value.toString());
        }
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
        for (MetricData metric : metrics) {
            switch (metric.getType()) {
                case DOUBLE_GAUGE:
                    for (DoublePointData data : metric.getDoubleGaugeData().getPoints()) {
                        recordedValues.computeIfAbsent(metric.getName(), n -> new HashMap<>())
                                      .put(attributesForStorage(data.getAttributes()), data.getValue());
                    }
                    break;
                case LONG_SUM:
                    for (LongPointData data : metric.getLongSumData().getPoints()) {
                        recordedValues.computeIfAbsent(metric.getName(), n -> new HashMap<>())
                                      .put(attributesForStorage(data.getAttributes()), (double) data.getValue());
                    }
                    break;
                case HISTOGRAM:
                    for (HistogramPointData data : metric.getHistogramData().getPoints()) {
                        recordedValues.computeIfAbsent(metric.getName(), n -> new HashMap<>())
                                      .put(attributesForStorage(data.getAttributes()), data.getSum());
                        recordedValues.computeIfAbsent(metric.getName() + ".count", n -> new HashMap<>())
                                      .put(attributesForStorage(data.getAttributes()), (double) data.getCount());
                    }
                    break;
            }
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        this.recordedValues.clear();
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return AggregationTemporality.CUMULATIVE;
    }
}
