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

import io.telicent.smart.cache.observability.metrics.MetricTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.List;

public class AbstractSinkTests {
    public static <T> void verifyCollectedValues(CollectorSink<T> sink, List<T> values) {
        // Verify we have the expected number of items and that the collections are the same
        Assert.assertEquals(sink.get().size(), values.size());
        Assert.assertEquals(sink.get(), values);
    }

    @BeforeClass
    public void setup() {
        MetricTestUtils.enableMetricsCapture();
    }

    @AfterClass
    public void teardown() {
        MetricTestUtils.disableMetricsCapture();
    }
}
