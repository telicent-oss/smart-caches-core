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
package io.telicent.smart.cache.projectors.sinks.builder;

import io.telicent.smart.cache.projectors.sinks.FilterSink;

import java.util.function.Predicate;

/**
 * An abstract builder for filtering sinks
 *
 * @param <TItem> Item type
 * @param <TSink> Filtering sink type
 */
public abstract class AbstractFilteringSinkBuilder<TItem, TSink extends FilterSink<TItem>>
        extends AbstractForwardingSinkBuilder<TItem, TItem, TSink, AbstractFilteringSinkBuilder<TItem, TSink>> {
    private Predicate<TItem> filter;
    private String metricsLabel;

    /**
     * Sets the metrics label used for collecting metrics about the number of items filtered.  If not set no metrics are
     * collected.
     *
     * @param label Metrics label
     * @return Builder
     */
    public AbstractFilteringSinkBuilder<TItem, TSink> metricsLabel(String label) {
        this.metricsLabel = label;
        return this;
    }

    /**
     * Sets the filter predicate used by the sink
     *
     * @param predicate Filter predicate
     * @return Builder
     */
    public AbstractFilteringSinkBuilder<TItem, TSink> predicate(Predicate<TItem> predicate) {
        this.filter = predicate;
        return this;
    }

    /**
     * Builds the actual sink implementation
     *
     * @param filter       Filter predicate
     * @param metricsLabel Metrics label
     * @return Filtering sink
     */
    protected abstract TSink buildInternal(Predicate<TItem> filter, String metricsLabel);

    @Override
    public final TSink build() {
        return buildInternal(filter, metricsLabel);
    }
}
