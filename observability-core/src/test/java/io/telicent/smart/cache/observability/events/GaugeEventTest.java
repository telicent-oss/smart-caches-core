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

import io.telicent.smart.cache.observability.metrics.GaugeMetric;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class GaugeEventTest {
    @Test
    public void whenAGuageEventIsCreated_thenTheCorrectEventValuesAreStored() {
        final Instant endedAt = Instant.now();
        final Instant startedAt = endedAt.minus(Duration.ofSeconds(10));
        final GaugeEvent event = GaugeEvent
                .builder()
                .startedAt(startedAt)
                .endedAt(endedAt)
                .value(12.34)
                .build();

        Assert.assertEquals(event.getEventName(), GaugeEvent.class.getName());
        Assert.assertEquals(event.getStartedAt(), startedAt);
        Assert.assertEquals(event.getEndedAt(), endedAt);
        Assert.assertEquals(event.getValue(), 12.34);
        Assert.assertEquals(event.getDuration(), Duration.of(endedAt.toEpochMilli() - startedAt.toEpochMilli(), ChronoUnit.MILLIS));

        Assert.assertEquals(event.getMetrics().size(), 1);
        Assert.assertTrue(event.getMetrics().get(0).getClass().isAssignableFrom(GaugeMetric.class));
        Assert.assertEquals(event.getMetrics().get(0).getMetricName(), GaugeEvent.class.getName());
        Assert.assertEquals(event.getMetrics().get(0).getValue(), event.getValue());
    }
}