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

import io.telicent.smart.cache.projectors.Sink;
import lombok.Getter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TestCloseLatchSink {

    @Getter
    private static final class CloseReportingSink implements Sink<String> {

        private volatile boolean closed = false;

        @Override
        public void send(String item) {
            // No-op
        }

        @Override
        public void close() {
            this.closed = true;
        }
    }

    @Test
    @SuppressWarnings("unused")
    public void givenSharedDestination_whenWrappedInCloseLatchSinks_thenOnlyClosedWhenLastCloseLatchClosed() {
        // Given
        try (CloseReportingSink destination = new CloseReportingSink()) {
            // When
            CloseLatch latch = new CloseLatch();
            try (Sink<String> sink1 = Sinks.<String>closeLatch().latch(latch).destination(destination).build()) {
                try (Sink<String> sink2 = Sinks.<String>closeLatch().latch(latch).destination(destination).build()) {
                    // Then
                    Assert.assertFalse(destination.isClosed());
                }
                Assert.assertFalse(destination.isClosed());
            }
            Assert.assertTrue(destination.isClosed());
        }
    }

    @Test
    public void givenSharedDestination_whenWrappedInManyCloseLatchSinks_thenOnlyClosedWhenLastCloseLatchClosed() {
        // Given
        try (CloseReportingSink destination = new CloseReportingSink()) {
            // When
            CloseLatch latch = new CloseLatch();
            List<Sink<String>> sinks = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                sinks.add(Sinks.<String>closeLatch().latch(latch).destination(destination).build());
            }

            // Then
            for (int i = 0; i < sinks.size(); i++) {
                Sink<String> sink = sinks.get(i);
                sink.close();
                if (i < sinks.size() - 1) {
                    Assert.assertFalse(destination.isClosed());
                } else {
                    Assert.assertTrue(destination.isClosed());
                }
            }
        }
    }
}
