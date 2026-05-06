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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static io.telicent.smart.cache.observability.events.HistogramEvent.histogramEvent;

public class HistogramEventTest {
    @Test
    public void whenAHistogramEventWithNameAndValueIsCreated_thenTheCorrectEventValuesAreStored() {
        final HistogramEvent event = histogramEvent("theEventName", 12.34);

        Assert.assertEquals(event.getEventName(), "theEventName");
        Assert.assertEquals(event.getMetricName(), event.getEventName());
        Assert.assertEquals(event.getValue(), 12.34);
        Assert.assertNotNull(event.getStartedAt());
        Assert.assertNotNull(event.getEndedAt());

        Assert.assertEquals(event.getMetrics().size(), 1);
        Assert.assertTrue(event.getMetrics().get(0).getClass().isAssignableFrom(HistogramMetric.class));
        Assert.assertEquals(event.getMetrics().get(0).getMetricName(), event.getMetricName());
        Assert.assertEquals(event.getMetrics().get(0).getValue(), event.getValue());
        Assert.assertEquals(event.getMetrics().get(0).getStartedAt(), event.getStartedAt());
        Assert.assertEquals(event.getMetrics().get(0).getEndedAt(), event.getEndedAt());
    }

    @Test
    public void givenAHistogramEventWithNameAndLabels_whenCreated_thenTheEventHasTheCorrectProperties() {
        final HistogramEvent event = histogramEvent("theEventName", 42, Map.of("label1", "label1Value"));

        Assert.assertEquals(event.getEventName(), "theEventName");
        Assert.assertEquals(event.getMetricLabels().size(), 1);
        Assert.assertEquals(event.getMetricLabels().get("label1"), "label1Value");
        Assert.assertEquals(event.getMetrics().get(0).getLabels().get("label1"), "label1Value");
    }
}
