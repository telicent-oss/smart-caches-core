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
package io.telicent.smart.cache.projectors.sinks;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.telicent.smart.cache.observability.AttributeNames;
import io.telicent.smart.cache.observability.MetricNames;
import io.telicent.smart.cache.observability.TelicentMetrics;
import io.telicent.smart.cache.projectors.Library;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.SinkException;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractFilteringSinkBuilder;
import io.telicent.smart.cache.projectors.sinks.builder.AbstractForwardingSinkBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A sink that filters items before forwarding them onto an optional destination sink
 * <p>
 * Filtered items are silently dropped, if you prefer to throw an error for filtered items then use the
 * {@link io.telicent.smart.cache.projectors.RejectSink} instead.
 * </p>
 *
 * @param <T> Input type
 */
public class FilterSink<T> extends AbstractTransformingSink<T, T> {

    private final Predicate<T> filter;
    private final LongCounter filteredMetric;
    private final Attributes metricAttributes;

    /**
     * Creates a new filter sink
     *
     * @param destination Destination Sink (optional)
     * @param filter      Filter function (optional), if not specified all items are accepted and forwarded
     */
    FilterSink(Sink<T> destination, Predicate<T> filter) {
        this(destination, filter, null);
    }

    /**
     * Creates a new filter sink
     *
     * @param destination Destination Sink (optional)
     * @param filter      Filter function (optional), if not specified all items are accepted and forwarded
     */
    protected FilterSink(Sink<T> destination, Predicate<T> filter, String metricsLabel) {
        super(destination);
        this.filter = filter != null ? filter : t -> true;
        if (StringUtils.isNotBlank(metricsLabel)) {
            this.metricAttributes = Attributes.of(AttributeKey.stringKey(AttributeNames.ITEMS_TYPE), metricsLabel,
                                                  AttributeKey.stringKey(AttributeNames.INSTANCE_ID),
                                                  UUID.randomUUID().toString());
            Meter meter = TelicentMetrics.getMeter(Library.NAME);
            //@formatter:off
            this.filteredMetric = meter.counterBuilder(MetricNames.ITEMS_FILTERED)
                                       .setDescription(MetricNames.ITEMS_FILTERED_DESCRIPTION)
                                       .build();
            //@formatter:on
        } else {
            this.metricAttributes = null;
            this.filteredMetric = null;
        }
    }

    @Override
    protected boolean shouldForward(T item) throws SinkException {
        boolean shouldForward = this.filter.test(item);
        if (!shouldForward && this.filteredMetric != null) {
            this.filteredMetric.add(1, this.metricAttributes);
        }
        return shouldForward;
    }

    @Override
    protected T transform(T t) {
        // Leaves input unchanged
        return t;
    }

    /**
     * Creates a new filter sink builder
     *
     * @param <TItem> Item type
     * @return Filtering sink builder
     */
    public static <TItem> Builder<TItem> create() {
        return new Builder<>();
    }

    /**
     * A builder for filtering sinks
     *
     * @param <TItem> Item type
     */
    public static class Builder<TItem> extends AbstractFilteringSinkBuilder<TItem, FilterSink<TItem>> {

        @Override
        public FilterSink<TItem> buildInternal(Predicate<TItem> predicate, String metricsLabel) {
            return new FilterSink<>(this.getDestination(), predicate, metricsLabel);
        }
    }
}
