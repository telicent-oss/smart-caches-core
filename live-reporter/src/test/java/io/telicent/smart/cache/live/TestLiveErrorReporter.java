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

import io.telicent.smart.cache.live.model.LiveError;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.sinks.ErrorSink;
import io.telicent.smart.cache.projectors.sinks.NonClearingCollectorSink;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.sources.Event;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.event.Level;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.Date;
import java.time.Instant;

public class TestLiveErrorReporter {

    @Test(expectedExceptions = NullPointerException.class)
    public void live_error_reporter_01() {
        Sink<Event<Bytes, LiveError>> collector = NonClearingCollectorSink.of();

        LiveErrorReporter reporter = new LiveErrorReporter(collector);
        reporter.reportError(null);
    }

    @Test
    public void live_error_reporter_02() {
        LiveErrorReporter reporter = new LiveErrorReporter(null);
        reporter.reportError(LiveError.create().build());
    }

    @Test
    public void live_error_reporter_03() {
        NullSink<Event<Bytes, LiveError>> sink = NullSink.of();
        LiveErrorReporter reporter = new LiveErrorReporter(sink);
        reporter.reportError(LiveError.create().build());
        Assert.assertEquals(sink.count(), 1L);
    }

    @Test
    public void live_error_reporter_04() {
        Sink<Event<Bytes, LiveError>> sink = new ErrorSink<>();
        LiveErrorReporter reporter = new LiveErrorReporter(sink);
        reporter.reportError(LiveError.create().build());
    }

    @Test
    public void live_error_reporter_05() {
        NonClearingCollectorSink<Event<Bytes, LiveError>> collector = new NonClearingCollectorSink<>();

        LiveErrorReporter reporter = new LiveErrorReporter("default-id", collector);

        // Check that setting an Application ID overrides the reporter live Application ID
        LiveError error = LiveError.create().id("custom-id").build();
        reporter.reportError(error);

        // Check setting specified fields are preserved
        java.util.Date testDate = Date.from(Instant.now().minusMillis(100));
        error = LiveError.create()
                         .message("Failed")
                         .level(Level.ERROR)
                         .timestamp(testDate)
                         .type("CustomError")
                         .recordCounter(12345L)
                         .build();
        reporter.reportError(error);

        Assert.assertEquals(collector.get().size(), 2);
        LiveError reported = collector.get().get(0).value();
        Assert.assertNotNull(reported);
        Assert.assertEquals(reported.getId(), "custom-id");
        Assert.assertEquals(reported.getLevel(), Level.INFO.toString());
        Assert.assertEquals(reported.getType(), "Throwable");
        Assert.assertEquals(reported.getCounter(), -1L);
        Assert.assertNotNull(reported.getTimestamp());
        Assert.assertNotEquals(reported.getTimestamp(), testDate);
        Assert.assertNotNull(reported.getTraceback());

        reported = collector.get().get(1).value();
        Assert.assertNotNull(reported);
        Assert.assertEquals(reported.getId(), "default-id");
        Assert.assertEquals(reported.getType(), "CustomError");
        Assert.assertEquals(reported.getCounter(), 12345L);
        Assert.assertEquals(reported.getLevel(), Level.ERROR.toString());
        Assert.assertNotNull(reported.getTimestamp());
        Assert.assertEquals(reported.getTimestamp(), testDate);
        Assert.assertNotNull(reported.getTraceback());
    }
}
