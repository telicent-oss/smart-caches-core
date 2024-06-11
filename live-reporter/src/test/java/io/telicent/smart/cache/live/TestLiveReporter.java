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
package io.telicent.smart.cache.live;

import io.telicent.smart.cache.live.model.IODescriptor;
import io.telicent.smart.cache.live.model.LiveHeartbeat;
import io.telicent.smart.cache.live.model.LiveStatus;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.*;
import io.telicent.smart.cache.projectors.sinks.events.EventValueSink;
import io.telicent.smart.cache.sources.Event;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.utils.Bytes;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestLiveReporter {

    public static final int MINIMUM_DELTA = 2;

    @DataProvider(name = "reporter")
    public Object[][] reporterTestParameters() {
        return new Object[][] {
                { 1_000, 100, LiveStatus.COMPLETED },
                { 1_000, 100, LiveStatus.TERMINATED },
                { 1_000, 100, LiveStatus.ERRORING },
                // Won't ever send anything other than Started and Completed as reporting period is higher than runtime
                { 1_000, 15_000, LiveStatus.COMPLETED },
                // Longer running test
                { 10_000, 1_000, LiveStatus.TERMINATED },
                // Very high frequency heartbeat
                { 1_000, 25, LiveStatus.COMPLETED }
        };
    }

    @Test(dataProvider = "reporter", invocationCount = 3)
    public void live_reporter_01(int runtime, int reportingMilliseconds, LiveStatus stopStatus) throws
            InterruptedException {
        CollectorSink<Event<Bytes, LiveHeartbeat>> sink = new NonClearingCollectorSink<>();
        LiveReporter reporter =
                new LiveReporter(sink, Duration.ofMillis(reportingMilliseconds), "test-01", "Test 01", "mapper",
                                 new IODescriptor("input.txt", "file"), new IODescriptor("raw", "topic"));
        runReporter(reporter, runtime, stopStatus);

        List<Event<Bytes, LiveHeartbeat>> heartbeats = sink.get();
        Assert.assertFalse(heartbeats.isEmpty(), "Expected some heartbeats to have been sent");

        int actualSize = heartbeats.size();
        int expectedSize = (runtime / reportingMilliseconds) + 1;
        int difference = Math.abs(actualSize - expectedSize);
        // If runtime is greater than reporting period allow a delta of 25% of expected size due to the vagaries of
        // thread scheduling and locking overheads especially on CI/CD machines
        // Also allow for the started and completed heartbeats
        int allowedDelta = runtime > reportingMilliseconds ? ((expectedSize / 4) + MINIMUM_DELTA) : MINIMUM_DELTA;
        Assert.assertTrue(difference <= allowedDelta, String.format(
                "Expected to receive approximately %,d heartbeats but found %,d which was outside the permitted delta of %,d",
                expectedSize, actualSize, allowedDelta));

        verifyHeartbeat(heartbeats, 0, LiveStatus.STARTED);
        for (int i = 1; i < heartbeats.size() - 1; i++) {
            verifyHeartbeat(heartbeats, i, LiveStatus.RUNNING);
        }
        verifyHeartbeat(heartbeats, heartbeats.size() - 1, stopStatus);
    }

    private LiveHeartbeat verifyHeartbeat(List<Event<Bytes, LiveHeartbeat>> heartbeats, int index,
                                          LiveStatus expectedStatus) {
        LiveHeartbeat heartbeat = heartbeats.get(index).value();
        Assert.assertEquals(heartbeat.getStatus(), expectedStatus);
        return heartbeat;
    }

    private static void runReporter(LiveReporter reporter, int runtime, LiveStatus stopStatus) throws
            InterruptedException {
        reporter.start();
        Thread.sleep(runtime);
        reporter.stop(stopStatus);
    }

    @Test
    public void live_reporter_02() throws
            InterruptedException {
        // A null value for sink issues a WARNING and causes all heartbeats to go to a NullSink
        LiveReporter reporter =
                new LiveReporter(null, Duration.ofMillis(100), "test-01", "Test 01", "mapper",
                                 new IODescriptor("input.txt", "file"), new IODescriptor("raw", "topic"));
        runReporter(reporter, 1000, LiveStatus.COMPLETED);
    }

    @Test
    public void live_reporter_03() throws
            InterruptedException {
        // A delay sink will cause the reporter to not finish in a timely manner
        LiveReporter reporter =
                new LiveReporter(new DelaySink<>(NullSink.of(), 5000), Duration.ofSeconds(15), "test-01", "Test 01",
                                 "mapper",
                                 new IODescriptor("input.txt", "file"), new IODescriptor("raw", "topic"));
        runReporter(reporter, 1000, LiveStatus.COMPLETED);
    }

    @Test
    public void live_reporter_bad_state_01() {
        LiveReporter reporter = new LiveReporter(NullSink.of(), Duration.ofMillis(100), "test-01", "Test 01", "mapper",
                                                 new IODescriptor("input.txt", "file"),
                                                 new IODescriptor("raw", "topic"));
        reporter.start();
        Assert.assertThrows(IllegalStateException.class, reporter::start);
        reporter.stop(LiveStatus.COMPLETED);
    }

    @Test
    public void live_reporter_restart_01() throws InterruptedException {
        CollectorSink<Event<Bytes, LiveHeartbeat>> sink = new NonClearingCollectorSink<>();
        LiveReporter reporter = new LiveReporter(sink, Duration.ofMinutes(1), "test-01", "Test 01", "mapper",
                                                 new IODescriptor("input.txt", "file"),
                                                 new IODescriptor("raw", "topic"));
        runReporter(reporter, 100, LiveStatus.COMPLETED);

        List<Event<Bytes, LiveHeartbeat>> firstRunHeartbeats = new ArrayList<>(sink.get());
        sink.get().clear();
        LiveHeartbeat firstStart = verifyHeartbeat(firstRunHeartbeats, 0, LiveStatus.STARTED);
        LiveHeartbeat firstStop = verifyHeartbeat(firstRunHeartbeats, 1, LiveStatus.COMPLETED);
        Assert.assertEquals(firstStart.getInstanceId(), firstStop.getInstanceId());

        // Run again should generate a new Instance ID
        runReporter(reporter, 100, LiveStatus.COMPLETED);

        List<Event<Bytes, LiveHeartbeat>> secondRunHeartbeats = new ArrayList<>(sink.get());
        sink.get().clear();
        LiveHeartbeat secondStart = verifyHeartbeat(secondRunHeartbeats, 0, LiveStatus.STARTED);
        LiveHeartbeat secondStop = verifyHeartbeat(secondRunHeartbeats, 1, LiveStatus.COMPLETED);
        Assert.assertEquals(secondStart.getInstanceId(), secondStop.getInstanceId());

        Assert.assertNotEquals(firstStart.getInstanceId(), secondStop.getInstanceId());
        Assert.assertNotEquals(firstStop.getInstanceId(), secondStop.getInstanceId());
    }

    @Test
    public void live_reporter_stop_different_thread_01() {
        LiveReporter reporter = new LiveReporter(NullSink.of(), Duration.ofMinutes(1), "test-01", "Test 01", "mapper",
                                                 new IODescriptor("input.txt", "file"),
                                                 new IODescriptor("raw", "topic"));

        reporter.start();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> reporter.stop(LiveStatus.TERMINATED));
        try {
            future.get();
        } catch (Throwable e) {
            Assert.fail("Errored trying to stop reporter: " + e.getMessage());
        } finally {
            reporter.stop(LiveStatus.COMPLETED);
        }
    }

    @Test
    public void live_reporter_json_01() throws InterruptedException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Sink<Event<Bytes, LiveHeartbeat>> toJson =
                EventValueSink.<Bytes, LiveHeartbeat>create().toJson(j -> j.prettyPrint().toStream(output)).build();

        LiveReporter reporter = new LiveReporter(toJson, Duration.ofMinutes(1), "test-01", "Test 01", "mapper",
                                                 new IODescriptor("input.txt", "file"),
                                                 new IODescriptor("raw", "topic"));
        runReporter(reporter, 100, LiveStatus.COMPLETED);

        String jsonData = output.toString(StandardCharsets.UTF_8);
        Assert.assertFalse(StringUtils.isBlank(jsonData));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*id.*cannot be null")
    public void live_reporter_builder_bad_01() {
        LiveReporter.create().build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*name.*cannot be null")
    public void live_reporter_builder_bad_02() {
        LiveReporter.create().id("test").build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*component type.*cannot be null")
    public void live_reporter_builder_bad_03() {
        LiveReporter.create().id("test").name("Test").build();
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Reporting period cannot be null")
    public void live_reporter_builder_bad_04() {
        LiveReporter.create().id("test").name("Test").componentType("mapper").reportingPeriod(null).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Reporting period.*<= 0")
    public void live_reporter_builder_bad_05() {
        LiveReporter.create().id("test").name("Test").componentType("mapper").reportingPeriod(Duration.ZERO).build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Reporting period.*<= 0")
    public void live_reporter_builder_bad_06() {
        LiveReporter.create()
                    .id("test")
                    .name("Test")
                    .componentType("mapper")
                    .reportingPeriod(Duration.ZERO.minusSeconds(10))
                    .build();
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Input descriptor cannot be null")
    public void live_reporter_builder_bad_07() {
        LiveReporter.create()
                    .id("test")
                    .name("Test")
                    .componentType("mapper")
                    .build();
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Output descriptor cannot be null")
    public void live_reporter_builder_bad_08() {
        LiveReporter.create()
                    .id("test")
                    .name("Test")
                    .componentType("mapper")
                    .input("input-topic", "topic")
                    .build();
    }

    @Test
    public void live_reporter_builder_good_01() {
        LiveReporter.create()
                    .id("test")
                    .name("test")
                    .componentType("mapper")
                    .input("input", "topic")
                    .output("output", "topic")
                    .reportingPeriod(Duration.ofSeconds(30))
                    .destination(NullSink.of())
                    .destination(FilterSink.<Event<Bytes, LiveHeartbeat>>create()
                                           .destination(EventValueSink.<Bytes, LiveHeartbeat>create().collect()))
                    .build();
    }
}
