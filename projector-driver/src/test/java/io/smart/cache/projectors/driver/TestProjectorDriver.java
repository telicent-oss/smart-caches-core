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
package io.smart.cache.projectors.driver;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import io.telicent.smart.cache.projectors.NoOpProjector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.driver.ProjectorDriver;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.EventSourceException;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.apache.jena.graph.Graph;
import org.slf4j.event.Level;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.testng.util.RetryAnalyzerCount;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class TestProjectorDriver {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final List<Future<?>> futures = new ArrayList<>();

    private TestLogger testLogger;

    private <TKey, TValue> Future<?> runDriver(ProjectorDriver<TKey, TValue, Event<TKey, TValue>> driver) {
        Future<?> future = executor.submit(driver);
        this.futures.add(future);
        return future;
    }

    @BeforeClass
    public void setup() {
        this.testLogger = TestLoggerFactory.getTestLogger(ProjectorDriver.class);
        this.testLogger.setEnabledLevelsForAllThreads(Level.INFO, Level.WARN, Level.ERROR);
    }

    @AfterMethod
    public void testCleanup() {
        this.testLogger.clearAll();

        int i = 0;
        while (i < this.futures.size()) {
            Future<?> future = this.futures.get(i);
            if (future.isDone() || future.isCancelled()) {
                // Remove any previously completed/cancelled futures
                this.futures.remove(i);
            } else {
                // Active future so cancel it now but keep it around
                future.cancel(true);
                i++;
            }
        }
    }

    @AfterClass
    public void cleanup() {
        this.executor.shutdownNow();
    }

    private static void waitIgnoringErrors(Future<?> future, int timeout, TimeUnit timeUnit) {
        try {
            future.get(timeout, timeUnit);
        } catch (Throwable t) {
            // Ignore
        }
    }

    private static void waitExpectingError(Future<?> future, int timeout, TimeUnit timeUnit,
                                           Class<?> expectedException) {
        try {
            future.get(timeout, timeUnit);
            Assert.fail("Expected an error of type " + expectedException);
        } catch (ExecutionException e) {
            if (!expectedException.isAssignableFrom(e.getCause().getClass())) {
                Assert.fail("Expected an error of type " + expectedException + " but got " + e.getCause().getClass());
            }
        } catch (CancellationException e) {
            if (!expectedException.equals(CancellationException.class)) {
                Assert.fail("Got a cancellation exception when expecting error of type " + expectedException);
            }
        } catch (TimeoutException e) {
            if (!expectedException.equals(TimeoutException.class)) {
                Assert.fail("Got a timeout exception when expecting error of type " + expectedException);
            }
        } catch (InterruptedException e) {
            if (!expectedException.equals(InterruptedException.class)) {
                Assert.fail("Got a interrupted exception when expecting error of type " + expectedException);
            }
        }
    }

    public void verifyLogging(Level level, String... searchTerms) {
        List<LoggingEvent> logs =
                this.testLogger.getAllLoggingEvents().stream().filter(event -> event.getLevel() == level).toList();
        Assert.assertNotEquals(logs.size(), 0, "Expected at least one logging event at level " + level);
        for (String searchTerm : searchTerms) {
            Assert.assertTrue(logs.stream().anyMatch(event -> event.getFormattedMessage().contains(searchTerm)),
                              "Logs were missing expected message '" + searchTerm + "'");
        }
    }

    @Test
    public void givenDriverWithLimit_whenProjecting_thenLimitEventsProjected() throws ExecutionException,
            InterruptedException, TimeoutException {
        // Given
        InfiniteEventSource source = new InfiniteEventSource("Event %,d", 0);
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               .limit(10_000)
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        future.get(5, TimeUnit.SECONDS);

        // Then
        Assert.assertTrue(source.isClosed());
        Assert.assertEquals(source.eventsYielded(), 10_000);
    }

    @Test
    public void givenUnlimitedDriver_whenProjecting_thenCanCancelOngoingProjection_andDriverCleansUp() {
        // Given
        InfiniteEventSource source = new InfiniteEventSource("Event %,d", 100);
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               // Unlimited so we can test cancellation
                               .unlimited()
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // Then
        driver.cancel();
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // And
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenUnlimitedDriver_whenProjecting_thenCanCancelViaInterrupt_andDriverCleansUp() throws
            InterruptedException {
        // Given
        InfiniteEventSource source = new InfiniteEventSource("Event %,d", 100);
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               // Unlimited so we can test cancellation
                               .unlimited()
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // Then
        future.cancel(true);
        // NB - Can't use waitIgnoringErrors() here as that will immediately throw a CancellationException which doesn't
        //      allow time for the ProjectorDriver interrupt to actually take effect and clean up as our subsequent
        //      assertions expect
        Thread.sleep(1000);

        // And
        Assert.assertTrue(future.isDone());
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenUnlimitedDriver_whenClosingSourceOutsideProjectorsControl_thenProjectionExits() {
        // Given
        InfiniteEventSource source = new InfiniteEventSource("Event %,d", 100);
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               // Unlimited so we can test closing the source outside the drivers control
                               .unlimited()
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);
        source.close();

        // Then
        waitExpectingError(future, 1, TimeUnit.SECONDS, IllegalStateException.class);
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenErrorSource_whenProjecting_thenProjectionExitsWithError() {
        // Given
        InfiniteEventSource source = new ErroringEventSource();
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               .unlimited()
                               .build();

        // When
        Future<?> future = this.runDriver(driver);

        // Then
        waitExpectingError(future, 1, TimeUnit.SECONDS, EventSourceException.class);
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenExhaustableSource_whenProjecting_thenProjectionCompletes() {
        // Given
        // Testing driver encountering an exhausted sink
        InMemoryEventSource<Integer, String> source =
                new InMemoryEventSource<>(List.of(new SimpleEvent<>(Collections.emptyList(), 1, "Singleton event")));
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               .unlimited()
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // Then
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenSlowSource_whenProjecting_thenProjectionStalls_andNothingProjected() {
        // Given
        InfiniteEventSource source = new InfiniteEventSource("Event %,d", 5_000);
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               .unlimited()
                               .pollTimeout(Duration.ofSeconds(1))
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);
        Assert.assertNotEquals(driver.getConsecutiveStalls(), 0);

        // And
        driver.cancel();
        waitIgnoringErrors(future, 3, TimeUnit.SECONDS);
        Assert.assertEquals(source.eventsYielded(), 0);
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenSlowSource_whenProjectingToStallAwareProjector_thenProjectionStalls_andNothingProjected_andProjectorInformedAboutStalls() {
        // Given
        InfiniteEventSource source = new InfiniteEventSource("Event %,d", 5_000);
        Sink<Event<Integer, String>> sink = NullSink.of();
        StallCountingProjector<Event<Integer, String>, Event<Integer, String>> projector =
                new StallCountingProjector<>();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(projector)
                               .destination(sink)
                               .unlimited()
                               .pollTimeout(Duration.ofSeconds(1))
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);
        Assert.assertNotEquals(driver.getConsecutiveStalls(), 0);

        // And
        driver.cancel();
        waitIgnoringErrors(future, 3, TimeUnit.SECONDS);
        Assert.assertEquals(source.eventsYielded(), 0);
        Assert.assertTrue(source.isClosed());

        // And
        Assert.assertEquals(projector.getStalls(), 1L);
    }

    @Test
    public void givenSlowSourceWithMaxStalls_whenProjecting_thenProjectionAborts_andNothingProjected() {
        // Given
        InfiniteEventSource source = new InfiniteEventSource("Event %,d", 1_000);
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               .unlimited()
                               // Forcing stalls to test abort on consecutive stalls
                               .pollTimeout(Duration.ofMillis(100))
                               .maxStalls(3)
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // Then
        Assert.assertTrue(future.isDone());
        verifyLogging(Level.INFO, "Event Source is stalled");

        // And
        Assert.assertEquals(source.eventsYielded(), 0);
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenSourceReportingAvailabilityIncorrectly_whenProjecting_thenProjectionAborts_andNothingProjected() {
        // Given
        // Source reports its availability incorrectly which will upset the driver and cause it to abort
        InfiniteEventSource source = new LyingEventSource("Event %,d", 1_000);
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               .unlimited()
                               .pollTimeout(Duration.ofMillis(100))
                               .maxStalls(3)
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // Then
        Assert.assertTrue(future.isDone());
        verifyLogging(Level.WARN, "Event Source incorrectly indicated that events were available");

        // And
        Assert.assertEquals(source.eventsYielded(), 0);
        Assert.assertTrue(source.isClosed());
    }

    @Test(retryAnalyzer = FlakyTest.class)
    public void givenSourceWithIntermittentRemainingAvailability_whenProjecting_thenWarningsAreIssued_andNothingProjected() {
        // Given
        // Source reports its remaining value in a random interval which should trigger the remaining related warnings
        InfiniteEventSource source = new RemainingInfiniteEventSource("Event %,d", 2);
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               .unlimited()
                               .unlimitedStalls()
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 10, TimeUnit.SECONDS);
        verifyLogging(Level.WARN, "Overall processing rate");

        // Then
        driver.cancel();
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // And
        Assert.assertTrue(source.eventsYielded() > 0);
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenSlowSourceWithIntermittentRemainingAvailability_whenProjecting_thenWarningsAreIssued_andNothingProjected() {
        // Given
        // Source reports its remaining value in a random interval which should trigger the remaining related warnings
        // Here we're explicitly setting our remaining events to a high number but setting yield and poll timeouts such
        // that those events are never read forcing a particular form of the warning to be tested
        InfiniteEventSource source = new RemainingInfiniteEventSource("Event %,d", 5_000, 1_000);
        Sink<Event<Integer, String>> sink = NullSink.of();
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(sink)
                               .unlimited()
                               .unlimitedStalls()
                               .pollTimeout(Duration.ofMillis(10))
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // Then
        driver.cancel();
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // And
        Assert.assertEquals(source.eventsYielded(), 0);
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenEmptySource_whenProjecting_thenProjectionCompletes() {
        // Given
        EventSource<Integer, Graph> source = new InMemoryEventSource<>(Collections.emptyList());
        ProjectorDriver<Integer, Graph, Event<Integer, Graph>> driver
                = ProjectorDriver.<Integer, Graph, Event<Integer, Graph>>create()
                                 .source(source)
                                 .projector(new NoOpProjector<>())
                                 .destination(NullSink::of)
                                 .pollTimeout(Duration.ofSeconds(5))
                                 .limit(10_000_000)
                                 .maxStalls(36)
                                 .reportBatchSize(100_000)
                                 .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 1, TimeUnit.SECONDS);

        // Then
        Assert.assertTrue(future.isDone());
    }

    @Test
    public void givenIntermittentlyStallingSource_whenProjecting_thenEventsAreProjectedEventually() throws
            InterruptedException {
        // Given
        InfiniteEventSource source = new StallingInfiniteEventSource("Event %,d", 150, 3);
        ProjectorDriver<Integer, String, Event<Integer, String>> driver
                = ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                                 .source(source)
                                 .projector(new NoOpProjector<>())
                                 .destination(NullSink::of)
                                 .pollTimeout(Duration.ofMillis(100))
                                 .limit(10)
                                 .build();

        // When
        Future<?> future = this.runDriver(driver);
        waitIgnoringErrors(future, 5, TimeUnit.SECONDS);

        // Then
        try {
            future.get();
        } catch (ExecutionException e) {
            Assert.fail("Unexpected driver error: " + e.getMessage());
        }
        Assert.assertTrue(future.isDone());
        Assert.assertEquals(source.eventsYielded(), 10);
    }

    public static class FlakyTest extends RetryAnalyzerCount {

        public FlakyTest() {
            super();
            super.setCount(5);
        }

        @Override
        public boolean retryMethod(ITestResult result) {
            // No need to retry if the test already succeeded or was skipped
            return !result.isSuccess() && result.getStatus() != ITestResult.SKIP;
        }
    }
}
