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

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.telicent.smart.cache.observability.metrics.CounterMetric;
import io.telicent.smart.cache.observability.metrics.DurationMetric;
import io.telicent.smart.cache.observability.metrics.GaugeMetric;
import io.telicent.smart.cache.observability.metrics.HistogramMetric;
import io.telicent.smart.cache.observability.metrics.Metric;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Map.entry;
import static java.util.Objects.isNull;

/**
 * <p>
 * An adapter of <code>{@link MetricEvent}s</code> to <a href="https://opentelemetry.io">OpenTelemetry metrics</a>.
 * </p>
 * <p>
 * Listens for metric events and dynamically creates associated metrics through the <a href="https://opentelemetry.io/docs/specs/otel/metrics/api/">OpenTelemetry Metrics API</a>
 * </p>
 * <p>
 * Note: Unfortunately, the <a href="https://opentelemetry.io">OpenTelemetry</a> API does not have a metric/instrument class hierarchy with a common base class. As a result we use {@link Object} as the common
 * base for all instruments created.
 * </p>
 */
@Slf4j
public class OpenTelemetryMetricsAdapter implements EventListener<ComponentEvent> {
    private final ConcurrentHashMap<String, Consumer<Metric>> metricKeyToOtMetric = new ConcurrentHashMap<>();

    private final Map<Class<? extends Metric>, Function<Metric, Pair<Class<?>, ?>>> metricTypeToOtAdapter;

    private final Map<Pair<Class<? extends Metric>, Class<?>>, BiConsumer<? extends Metric, ?>> metricTypesToAdapter;

    private final Consumer<? extends Metric> NO_OP_ADAPTER = m -> {};

    /**
     * Creates a new OpenTelemetry metrics adapter.
     *
     * @param meter the OpenTelemetry meter this adapter will use to create instruments and report measurements.
     */
    public OpenTelemetryMetricsAdapter(Meter meter) {
        metricTypeToOtAdapter = Map.ofEntries(
                entry(CounterMetric.class, (metric) -> Pair.of(LongCounter.class, meter.counterBuilder(metric.getMetricName()).build())),
                entry(GaugeMetric.class, (metric) -> {
                    ObservableGaugeState state = new ObservableGaugeState();
                    meter.gaugeBuilder(metric.getMetricName()).buildWithCallback(state::record);
                    return Pair.of(ObservableGaugeState.class, state);
                }),
                entry(HistogramMetric.class, (metric) -> Pair.of(DoubleHistogram.class, meter.histogramBuilder(metric.getMetricName()).build())),
                entry(DurationMetric.class, (metric) -> Pair.of(DoubleHistogram.class, meter.histogramBuilder(metric.getMetricName()).build()))
        );

        metricTypesToAdapter = Map.ofEntries(
            entry(Pair.of(CounterMetric.class, LongCounter.class), (BiConsumer<CounterMetric, LongCounter>)(m, ot) -> ot.add(m.getCount().longValue(), toAttributes(m))),
            entry(Pair.of(GaugeMetric.class, ObservableGaugeState.class), (BiConsumer<GaugeMetric, ObservableGaugeState>)(m, ot) -> ot.update(m)),
            entry(Pair.of(HistogramMetric.class, DoubleHistogram.class), (BiConsumer<HistogramMetric, DoubleHistogram>)(m, ot) -> ot.record(m.getValue().doubleValue(), toAttributes(m))),
            entry(Pair.of(DurationMetric.class, DoubleHistogram.class), (BiConsumer<DurationMetric, DoubleHistogram>)(m, ot) -> ot.record(m.getValue().doubleValue(), toAttributes(m)))
        );
    }

    /**
     * Receives a new event, filtering on metric events and calling {@link #on(MetricEvent)}.
     *
     * @param event the event to be handled by this interested listener.
     * @see #on(MetricEvent)
     */
    @Override
    public void on(final ComponentEvent event) {
        if (event instanceof MetricEvent) {
            on((MetricEvent)event);
        }
    }

    /**
     * Receives a new metric event and adapts the event onto the equivalent OpenTelemetry metric, if possible.
     * @param event the event to be handled by this interested listener.
     */
    public void on(final MetricEvent event) {
        event.getMetrics().forEach(metric -> metricKeyToOtMetric.computeIfAbsent(determineMetricKeyFor(event, metric), k -> adapterFor(metric)).accept(metric));
    }

    private <M extends Metric> Pair<Class<?>, ?> instrumentFor(M metric) {
        Function<Metric, Pair<Class<?>, ?>> metricTypeAndOtelMetricType = metricTypeToOtAdapter.get(metric.getClass());
        return metricTypeAndOtelMetricType == null ? null : metricTypeAndOtelMetricType.apply(metric);
    }

    private String determineMetricKeyFor(final MetricEvent event, final Metric metric) {
        return event.getEventName() + "_" + metric.getMetricName();
    }

    @SuppressWarnings("unchecked")
    private <M extends Metric> Consumer<M> adapterFor(final M metric) {
        final Pair<Class<?>, ?> otelInstrumentTypeAndInstance = instrumentFor(metric);
        if (isNull(otelInstrumentTypeAndInstance)) {
            log.info("No OpenTelemetry instrument registered for metric type [{}] - no instrumentation will occur for this metric type...", metric.getClass());
            return (Consumer<M>) NO_OP_ADAPTER;
        }

        final BiConsumer<M, Object> adapterForMetricAndOtMetricTypes = (BiConsumer<M, Object>)metricTypesToAdapter.get(Pair.of(metric.getClass(), otelInstrumentTypeAndInstance.getLeft()));
        if (isNull(adapterForMetricAndOtMetricTypes)) {
            log.info("No metric adapter registered for metric type [{}] and OpenTelemetry metricType [{}]- no instrumentation will occur for this metric type...", metric.getClass(), otelInstrumentTypeAndInstance.getLeft());
            return (Consumer<M>) NO_OP_ADAPTER;
        }

        return (M m) -> adapterForMetricAndOtMetricTypes.accept(m, otelInstrumentTypeAndInstance.getRight());
    }

    private static Attributes toAttributes(Metric metric) {
        Map<String, Object> labels = metric.getLabels();
        if (labels == null || labels.isEmpty()) {
            return Attributes.empty();
        }

        AttributesBuilder builder = Attributes.builder();
        labels.forEach((key, value) -> putAttribute(builder, key, value));
        return builder.build();
    }

    private static void putAttribute(AttributesBuilder builder, String key, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof String stringValue) {
            builder.put(key, stringValue);
        } else if (value instanceof Boolean booleanValue) {
            builder.put(key, booleanValue);
        } else if (value instanceof Integer integerValue) {
            builder.put(key, integerValue.longValue());
        } else if (value instanceof Long longValue) {
            builder.put(key, longValue);
        } else if (value instanceof Short shortValue) {
            builder.put(key, shortValue.longValue());
        } else if (value instanceof Byte byteValue) {
            builder.put(key, byteValue.longValue());
        } else if (value instanceof Double doubleValue) {
            builder.put(key, doubleValue);
        } else if (value instanceof Float floatValue) {
            builder.put(key, floatValue.doubleValue());
        } else {
            builder.put(key, value.toString());
        }
    }

    private static final class ObservableGaugeState {
        private final ConcurrentHashMap<Attributes, Double> values = new ConcurrentHashMap<>();

        void update(GaugeMetric metric) {
            values.put(toAttributes(metric), metric.getValue().doubleValue());
        }

        void record(ObservableDoubleMeasurement measurement) {
            values.forEach((attributes, value) -> measurement.record(value, attributes));
        }
    }
}
