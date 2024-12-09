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

import com.fasterxml.jackson.databind.SerializationFeature;
import io.telicent.smart.cache.observability.AttributeNames;
import io.telicent.smart.cache.observability.MetricNames;
import io.telicent.smart.cache.observability.metrics.MetricTestUtils;
import io.telicent.smart.cache.projectors.SinkException;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

public class TestBasicSinks extends AbstractSinkTests {

    @Test
    public void collector_01() {
        List<String> values = Arrays.asList("a", "b", "c");
        verifyCollectorSink(values);
    }

    @Test
    public void collector_02() {
        List<String> values = Collections.emptyList();
        verifyCollectorSink(values);
    }

    @Test
    public void collector_03() {
        List<String> values = Collections.singletonList(null);
        verifyCollectorSink(values);
    }

    protected void verifyCollectorSink(List<String> values) {
        try (CollectorSink<String> sink = new CollectorSink<>()) {
            values.forEach(sink::send);

            verifyCollectedValues(sink, values);

            // After close() the sink should be emptied
            sink.close();
            Assert.assertEquals(sink.get().size(), 0);
        }
    }

    @Test
    public void filter_01() {
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> true;
        verifyFilter(values, filter, values);
    }

    @Test
    public void filter_02() {
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> false;
        verifyFilter(values, filter, Collections.emptyList());
    }

    @Test
    public void filter_02_with_metrics() {
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> false;
        verifyFilter(values, filter, "filter_02", Collections.emptyList());
    }

    @Test
    public void filter_03() {
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> StringUtils.startsWith(x, "f");
        verifyFilter(values, filter, Arrays.asList("foo", "faz"));
    }

    @Test
    public void filter_03_with_metrics() {
        List<String> values = Arrays.asList("foo", "bar", "faz");
        Predicate<String> filter = x -> StringUtils.startsWith(x, "f");
        verifyFilter(values, filter, "filter_03", Arrays.asList("foo", "faz"));
    }

    @Test
    public void filter_04() {
        Predicate<String> filter = x -> true;
        verifyFilter(Collections.emptyList(), filter, Collections.emptyList());
    }

    @Test(expectedExceptions = SinkException.class, expectedExceptionsMessageRegExp = "Failed")
    public void filter_05() {
        // Create a sink chain that will error
        try (FilterSink<String> sink = new FilterSink<>(new ErrorSink<>(() -> new SinkException("Failed")),
                                                        x -> true)) {
            sink.send("test");
        }
    }

    @Test(expectedExceptions = SinkException.class, expectedExceptionsMessageRegExp = ".* IO Failed.*")
    public void filter_06() {
        // Create a sink chain that will error
        try (FilterSink<String> sink = new FilterSink<>(
                new ErrorSink<>(() -> new SinkException(new IOException("IO Failed"))), x -> true)) {
            sink.send("test");
        }
    }

    @Test
    public void filter_07() {
        List<String> values = Arrays.asList("foo", "bar", "faz");
        // Null filter should default to accepting all items
        verifyFilter(values, null, values);
    }

    protected void verifyFilter(List<String> values, Predicate<String> filter, List<String> expected) {
        verifyFilter(values, filter, null, expected);
    }

    protected void verifyFilter(List<String> values, Predicate<String> filter, String metricsLabel,
                                List<String> expected) {
        try (CollectorSink<String> collector = new CollectorSink<>()) {
            try (FilterSink<String> sink = new FilterSink<>(collector, filter, metricsLabel)) {
                values.forEach(sink::send);

                verifyCollectedValues(collector, expected);

                // After close() the close should be passed down to the destination sink clearing the collection
                sink.close();
                Assert.assertEquals(collector.get().size(), 0);
            }
        }

        if (StringUtils.isNotBlank(metricsLabel)) {
            double metricValue = MetricTestUtils.getReportedMetric(MetricNames.ITEMS_FILTERED, AttributeNames.ITEMS_TYPE, metricsLabel);
            Assert.assertEquals(metricValue, values.size() - expected.size());
        }
    }

    @Test
    public void null_01() {
        verifyNullSink(Collections.emptyList());
    }

    @Test
    public void null_02() {
        verifyNullSink(Arrays.asList("a", "b", "c"));
    }

    protected void verifyNullSink(List<String> values) {
        try (NullSink<String> sink = new NullSink<>()) {
            values.forEach(sink::send);

            Assert.assertEquals(sink.count(), values.size());

            // After close() the counter should be reset
            sink.close();
            Assert.assertEquals(sink.count(), 0);
        }
    }

    @Test
    public void jackson_json_01() {
        List<Object> values = Collections.singletonList("foo");
        List<String> expectedJson = List.of("\"foo\"");
        verifyJacksonJson(values, false, expectedJson);
    }

    @Test
    public void jackson_json_02() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John Smith");
        map.put("aliases", Arrays.asList("J Smith", "John Frederick Smith"));
        List<Object> values = Collections.singletonList(map);
        List<String> expectedJson =
                List.of("{\"aliases\":[\"J Smith\",\"John Frederick Smith\"],\"name\":\"John Smith\"}");
        verifyJacksonJson(values, false, expectedJson);
    }

    @Test
    public void jackson_json_03() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John Smith");
        map.put("aliases", Arrays.asList("J Smith", "John Frederick Smith"));
        List<Object> values = Collections.singletonList(map);
        List<String> expectedJson = Collections.singletonList(StringUtils.join("{", System.lineSeparator(),
                                                                               "  \"aliases\" : [ \"J Smith\", \"John Frederick Smith\" ],",
                                                                               System.lineSeparator(),
                                                                               "  \"name\" : \"John Smith\"",
                                                                               System.lineSeparator(), "}"));
        verifyJacksonJson(values, true, expectedJson);
    }

    @Test
    public void jackson_json_04() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John Smith");
        map.put("aliases", Arrays.asList("J Smith", "John Frederick Smith"));
        List<Object> values = Arrays.asList("foo", map);
        List<String> expectedJson = Arrays.asList("\"foo\"",
                                                  "{\"aliases\":[\"J Smith\",\"John Frederick Smith\"],\"name\":\"John Smith\"}");
        verifyJacksonJson(values, false, expectedJson);
    }

    @SuppressWarnings("resource")
    @Test(expectedExceptions = NullPointerException.class)
    public void jackson_json_05() {
        new JacksonJsonSink(null, false);
    }

    @SuppressWarnings("resource")
    @Test(expectedExceptions = NullPointerException.class)
    public void jackson_json_06() {
        new JacksonJsonSink<>(null, true);
    }

    @Test(expectedExceptions = SinkException.class, expectedExceptionsMessageRegExp = "Failed to serialize.*")
    public void jackson_json_07() {
        CloseShieldOutputStream output = CloseShieldOutputStream.wrap(System.out);
        output.close();
        try (JacksonJsonSink<String> sink = new JacksonJsonSink<>(output, false)) {
            // Expect this to throw a sink error as the output stream is closed
            sink.send("test");
        }
    }

    @Test
    public void jackson_json_08() {
        try (InspectableJacksonJsonSink sink = new InspectableJacksonJsonSink()) {
            Assert.assertEquals(sink.getOutputStream(), System.out);
            Assert.assertFalse(sink.getObjectMapper().isEnabled(SerializationFeature.INDENT_OUTPUT));
        }
    }

    @Test
    public void jackson_json_09() {
        try (InspectableJacksonJsonSink sink = new InspectableJacksonJsonSink(true)) {
            Assert.assertEquals(sink.getOutputStream(), System.out);
            Assert.assertTrue(sink.getObjectMapper().isEnabled(SerializationFeature.INDENT_OUTPUT));
        }
    }

    protected void verifyJacksonJson(List<Object> values, boolean prettyPrint, List<String> expectedJson) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JacksonJsonSink<Object> sink = new JacksonJsonSink<>(output, prettyPrint)) {
            for (int i = 0; i < values.size(); i++) {
                sink.send(values.get(i));
                String actualJson = output.toString(StandardCharsets.UTF_8);
                String expected = expectedJson.get(i);

                Assert.assertEquals(actualJson, expected,
                                    "Object at Index " + i + " did not produce the expected JSON");
                output.reset();
            }
        }
    }
}
