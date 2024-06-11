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

import io.telicent.smart.cache.projectors.NoOpProjector;
import io.telicent.smart.cache.projectors.driver.ProjectorDriver;
import io.telicent.smart.cache.projectors.sinks.NullSink;
import io.telicent.smart.cache.projectors.sinks.Sinks;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

public class TestProjectorDriverBuilder {

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".* cannot be null")
    public void driver_builder_bad_01() {
        ProjectorDriver.create().build();
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".* cannot be null")
    public void driver_builder_bad_02() {
        ProjectorDriver.create().source(new InMemoryEventSource<>(Collections.emptyList())).build();
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".* cannot be null")
    public void driver_builder_bad_03() {
        ProjectorDriver.<Object, Object, Event<Object, Object>>create()
                       .source(new InMemoryEventSource<>(Collections.emptyList()))
                       .projector(new NoOpProjector<>())
                       .build();
    }

    @Test
    public void driver_builder_01() {
        // Poll timeouts have defaults set
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(new InMemoryEventSource<>(Collections.emptyList()))
                               .projector(new NoOpProjector<>())
                               .destination(NullSink.of())
                               .build();
        Assert.assertNotNull(driver);
    }

    @Test
    public void driver_builder_02() {
        // Set poll timeouts in various ways
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(new InMemoryEventSource<>(Collections.emptyList()))
                               .projector(new NoOpProjector<>())
                               .destination(NullSink.of())
                               .pollTimeout(Duration.ofSeconds(5))
                               .pollTimeout(5, ChronoUnit.SECONDS)
                               .build();
        Assert.assertNotNull(driver);
    }

    @Test
    public void driver_builder_03() {
        // Set limits in various ways
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(new InMemoryEventSource<>(Collections.emptyList()))
                               .projector(new NoOpProjector<>())
                               .destination(NullSink.of())
                               .unlimited()
                               .limit(100)
                               .unlimitedStalls()
                               .maxStalls(3)
                               .reportBatchSize(100)
                               .build();
        Assert.assertNotNull(driver);
    }

    @Test
    public void driver_builder_04() {
        // Supply destination sink in various ways
        ProjectorDriver<Integer, String, Event<Integer, String>> driver =
                ProjectorDriver.<Integer, String, Event<Integer, String>>create()
                               .source(new InMemoryEventSource<>(Collections.emptyList()))
                               .projector(new NoOpProjector<>())
                               .destination(NullSink.of())
                               .destinationBuilder(Sinks.discard())
                               .destination(NullSink::of)
                               .build();
        Assert.assertNotNull(driver);
    }

}
