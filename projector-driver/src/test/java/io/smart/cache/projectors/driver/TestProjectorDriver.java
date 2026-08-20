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
import org.awaitility.Awaitility;
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
import java.util.concurrent.atomic.AtomicReference;

// java:S2925 - Thread.sleep is required when waiting on real Kafka/Docker in integration tests
// java:S119 - TKey/TValue/TRequest generic naming convention is used across the codebase
@SuppressWarnings({"java:S2925", "java:S119"})
public class TestProjectorDriver {

    private static final Duration POLL_INTERVAL = Duration.ofMillis(50);
    private static final Duration MAX_WAIT = Duration.ofSeconds(10);

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

    /**
     * Waits for a condition to hold while a projection is in-flight
     * <p>
     * Always prefer waiting on the condition a test actually cares about over waiting a fixed amount of time and hoping
     * it was long enough, the latter is both slower in the passing case and flaky on a loaded machine.
     * </p>
     *
     * @param alias     Description of what's being waited for, reported if the wait times out
     * @param condition Condition to wait for
     */
    private static void awaitCondition(String alias, Callable<Boolean> condition) {
        Awaitility.await(alias).pollInterval(POLL_INTERVAL).atMost(MAX_WAIT).until(condition);
    }

    /**
     * Waits for the driver to finish, however it finishes i.e. normally, cancelled or with an error
     *
     * @param future Driver future
     */
    private static void awaitDriverFinished(Future<?> future) {
        awaitCondition("Projector Driver to finish", future::isDone);
    }

    /**
     * Waits for the driver to finish and verifies that it did so without error
     *
     * @param future Driver future
     */
    private static void awaitDriverSuccess(Future<?> future) {
        awaitDriverFinished(future);
        try {
            future.get();
        } catch (ExecutionException e) {
            Assert.fail("Unexpected driver error: " + e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail("Interrupted while waiting for the driver to finish");
        }
    }

    /**
     * Waits for the driver to finish and verifies that it failed with the expected error
     *
     * @param future            Driver future
     * @param expectedException Expected error type
     */
    private static void awaitDriverFailure(Future<?> future, Class<?> expectedException) {
        awaitDriverFinished(future);
        try {
            future.get();
            Assert.fail("Expected an error of type " + expectedException);
        } catch (ExecutionException e) {
            if (!expectedException.isAssignableFrom(e.getCause().getClass())) {
                Assert.fail("Expected an error of type " + expectedException + " but got " + e.getCause().getClass());
            }
        } catch (CancellationException e) {
            if (!expectedException.equals(CancellationException.class)) {
                Assert.fail("Got a cancellation exception when expecting error of type " + expectedException);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail("Interrupted while waiting for the driver to fail");
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

    public void verifyNoLogging(Level level, String searchTerm) {
        Assert.assertFalse(hasLogged(level, searchTerm),
                           "Logs unexpectedly contained message '" + searchTerm + "'");
    }

    private boolean hasLogged(Level level, String searchTerm) {
        return this.testLogger.getAllLoggingEvents()
                              .stream()
                              .filter(event -> event.getLevel() == level)
                              .anyMatch(event -> event.getFormattedMessage().contains(searchTerm));
    }

    @Test
    public void givenDriverWithLimit_whenProjecting_thenLimitEventsProjected() {
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
        awaitDriverSuccess(future);

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
        awaitCondition("Projection to start", () -> source.eventsYielded() > 0);

        // Then
        driver.cancel();
        awaitDriverFinished(future);

        // And
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenUnlimitedDriver_whenProjecting_thenCanCancelViaInterrupt_andDriverCleansUp() {
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
        awaitCondition("Projection to start", () -> source.eventsYielded() > 0);

        // Then
        future.cancel(true);
        // NB - Can't wait on the future itself here as it reports itself done the instant it's cancelled, which doesn't
        //      allow time for the ProjectorDriver interrupt to actually take effect and clean up as our subsequent
        //      assertions expect, so wait on the driver's observable clean up instead
        awaitCondition("Driver to clean up after being interrupted", source::isClosed);

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
        awaitCondition("Projection to start", () -> source.eventsYielded() > 0);
        source.close();

        // Then
        awaitDriverFailure(future, IllegalStateException.class);
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
        awaitDriverFailure(future, EventSourceException.class);
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
                               .logLabel("[test]")
                               .build();

        // When
        Future<?> future = this.runDriver(driver);
        awaitDriverSuccess(future);

        // Then
        Assert.assertTrue(source.isClosed());
        verifyLogging(Level.INFO, "[test]");
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
        awaitCondition("Projection to stall", () -> driver.getConsecutiveStalls() > 0);

        // And
        driver.cancel();
        awaitDriverFinished(future);
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
        awaitCondition("Projection to stall", () -> driver.getConsecutiveStalls() > 0);

        // And
        driver.cancel();
        awaitDriverFinished(future);
        Assert.assertEquals(source.eventsYielded(), 0);
        Assert.assertTrue(source.isClosed());

        // And
        Assert.assertEquals(projector.getStalls(), 1L);
    }

    @Test
    public void givenSlowSource_whenProjectingToStallAwareProjector_thenStalledReportedOnce_andProjectorInformedItIsIdleOnEveryPoll() {
        // Given
        final InfiniteEventSource source = new InfiniteEventSource("Event %,d", 5_000);
        final Sink<Event<Integer, String>> sink = NullSink.of();
        final StallCountingProjector<Event<Integer, String>, Event<Integer, String>> projector =
                new StallCountingProjector<>();
        final ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(projector)
                               .destination(sink)
                               .unlimited()
                               // Short timeout relative to the source's delay so we stall repeatedly
                               .pollTimeout(Duration.ofMillis(100))
                               .build();

        // When
        final Future<?> future = this.runDriver(driver);
        awaitCondition("Projection to stall repeatedly", () -> driver.getConsecutiveStalls() >= 3);
        driver.cancel();
        awaitDriverFinished(future);

        // Then
        // The stall itself is only reported once, however the projector is told it's idle on every poll that yields no
        // events, otherwise a projector on a quiet topic would never regain control between polls
        Assert.assertEquals(projector.getStalls(), 1L);
        Assert.assertTrue(projector.getIdles() > 1L,
                          "Expected the projector to be informed it was idle multiple times but got " + projector.getIdles());
        Assert.assertEquals(projector.getIdles(), driver.getConsecutiveStalls());
    }

    @Test
    public void givenLongStalledSource_whenProjectorAsksToPause_thenProjectorReachesPausePoint() throws
            InterruptedException {
        // Given
        final InfiniteEventSource source = new InfiniteEventSource("Event %,d", 5_000);
        final Sink<Event<Integer, String>> sink = NullSink.of();
        final PausableProjector<Event<Integer, String>, Event<Integer, String>> projector = new PausableProjector<>();
        final ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(projector)
                               .destination(sink)
                               .unlimited()
                               .pollTimeout(Duration.ofMillis(100))
                               .build();
        final Future<?> future = this.runDriver(driver);

        // Wait until the projection has been stalled for a while, i.e. well past the first stall, as this is the state
        // in which a pause request was previously never observed
        awaitCondition("Projection to be stalled well past the first stall",
                       () -> driver.getConsecutiveStalls() >= 3);

        // When
        projector.requestPause();

        // Then
        Assert.assertTrue(projector.awaitPausePoint(Duration.ofSeconds(5)),
                          "Projector failed to reach its pause point while stalled");

        // And
        projector.requestResume();
        driver.cancel();
        awaitDriverFinished(future);
        Assert.assertFalse(projector.isAtPausePoint());
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
        awaitDriverSuccess(future);

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
        awaitDriverSuccess(future);

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
        awaitCondition("Processing rate warning to be issued",
                       () -> hasLogged(Level.WARN, "Overall processing rate"));
        verifyLogging(Level.WARN, "Overall processing rate");

        // Then
        driver.cancel();
        awaitDriverFinished(future);

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
        awaitCondition("Projection to stall", () -> driver.getConsecutiveStalls() > 0);

        // Then
        driver.cancel();
        awaitDriverFinished(future);

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
        awaitDriverSuccess(future);

        // Then
        Assert.assertTrue(future.isDone());
    }

    @Test
    public void givenIntermittentlyStallingSource_whenProjecting_thenEventsAreProjectedEventually() {
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
        awaitDriverSuccess(future);

        // Then
        Assert.assertTrue(future.isDone());
        Assert.assertEquals(source.eventsYielded(), 10);
    }

    @Test
    public void givenProjectorRelyingOnDefaultIdle_whenSourceStalls_thenStalledReportedOnce_andProjectionContinues() {
        // Given
        // A stall aware projector that doesn't override idle() and so relies on its default no-op implementation, i.e.
        // any projector written before idle() was introduced
        final InfiniteEventSource source = new InfiniteEventSource("Event %,d", 5_000);
        final Sink<Event<Integer, String>> sink = NullSink.of();
        final StalledOnlyProjector<Event<Integer, String>, Event<Integer, String>> projector = new StalledOnlyProjector<>();
        final ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(projector)
                               .destination(sink)
                               .unlimited()
                               .pollTimeout(Duration.ofMillis(100))
                               .build();

        // When
        final Future<?> future = this.runDriver(driver);
        awaitCondition("Projection to stall repeatedly", () -> driver.getConsecutiveStalls() >= 3);
        driver.cancel();
        awaitDriverFinished(future);

        // Then
        // The default idle() does nothing, so the driver keeps polling and only the first stall is reported
        Assert.assertEquals(projector.getStalls(), 1L);
        Assert.assertTrue(driver.getConsecutiveStalls() > 1L,
                          "Expected the driver to have stalled multiple times but got " + driver.getConsecutiveStalls());
        Assert.assertTrue(source.isClosed());
    }

    @Test
    public void givenDriverWithCustomThreadName_whenProjecting_thenProjectionRunsOnRenamedThread() {
        // Given
        final InfiniteEventSource source = new InfiniteEventSource("Event %,d", 0);
        final AtomicReference<String> projectionThread = new AtomicReference<>();
        final ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector((event, sink) -> projectionThread.compareAndSet(null, Thread.currentThread()
                                                                                                     .getName()))
                               .destination(NullSink.of())
                               .threadName("CustomDriverThread")
                               .limit(10)
                               .build();

        // When
        Assert.assertEquals(driver.getThreadName(), "CustomDriverThread");
        final Future<?> future = this.runDriver(driver);
        awaitDriverSuccess(future);

        // Then
        Assert.assertTrue(future.isDone());
        Assert.assertEquals(projectionThread.get(), "CustomDriverThread");
    }

    @Test
    public void givenSourceStallingWithEventsRemaining_whenProjectingWithSpeedWarningsEnabled_thenWarningIsIssued() {
        // Given
        // The source projects a batch of events as fast as possible and then stalls while still claiming an event is
        // remaining, so the observed processing rate is guaranteed to exceed the remaining events
        final ThenStallingEventSource source = new ThenStallingEventSource(1_000, 1);
        final ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(NullSink.of())
                               .unlimited()
                               .unlimitedStalls()
                               .pollTimeout(Duration.ofMillis(100))
                               .enableProcessingSpeedWarnings()
                               .build();

        // When
        final Future<?> future = this.runDriver(driver);
        awaitCondition("Processing rate warning to be issued",
                       () -> hasLogged(Level.WARN, "Overall processing rate"));
        driver.cancel();
        awaitDriverFinished(future);

        // Then
        Assert.assertEquals(source.eventsYielded(), 1_000);
        verifyLogging(Level.INFO, "Event Source reports it only has 1 events remaining");
        verifyLogging(Level.WARN, "Overall processing rate");
    }

    @Test
    public void givenSourceStallingWithEventsRemaining_whenProjectingWithSpeedWarningsDisabled_thenNoWarningIsIssued() {
        // Given
        // Identical to the preceding test other than disabling the processing speed warnings
        final ThenStallingEventSource source = new ThenStallingEventSource(1_000, 1);
        final ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(source)
                               .projector(new NoOpProjector<>())
                               .destination(NullSink.of())
                               .unlimited()
                               .unlimitedStalls()
                               .pollTimeout(Duration.ofMillis(100))
                               .disabledProcessingSpeedWarnings()
                               .build();

        // When
        final Future<?> future = this.runDriver(driver);
        // Waiting for the remaining events to be reported gets us to the exact point at which the warning would have
        // been issued, so the absence of the warning below is meaningful rather than merely a race we won
        awaitCondition("Remaining events to be reported",
                       () -> hasLogged(Level.INFO, "Event Source reports it only has 1 events remaining"));
        driver.cancel();
        awaitDriverFinished(future);

        // Then
        // The remaining events are still reported, proving we reached the point at which the warning would have been
        // issued, but the warning itself is suppressed
        Assert.assertEquals(source.eventsYielded(), 1_000);
        verifyLogging(Level.INFO, "Event Source reports it only has 1 events remaining");
        verifyNoLogging(Level.WARN, "Overall processing rate");
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
