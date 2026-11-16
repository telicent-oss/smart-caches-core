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
package io.telicent.smart.cache.distribution.lifecycle.sinks;

import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.mockito.Mockito.mock;

@SuppressWarnings("resource")
public class TestDistributionAcceptanceSinkBuilder {

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNoArguments_whenBuildingSink_thenNPE() {
        // Given, When and Then
        DistributionAcceptanceSink.create().build();
    }

    @Test
    public void givenMinimalArguments_whenBuildingSink_thenOk() {
        // Given
        DistributionLifecycleStateStore stateStore = mock(DistributionLifecycleStateStore.class);

        // When
        try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                          .stateStore(stateStore)
                                                                                          .build()) {
            // Then
            Assert.assertNotNull(sink.toString());
            Assert.assertTrue(sink.isRejectUnregistered());
            Assert.assertTrue(sink.isDiscardAfterRejection());
            Assert.assertEquals(sink.getUnregisteredGracePeriod(),
                                DistributionAcceptanceSink.DEFAULT_UNREGISTERED_GRACE_PERIOD);
            Assert.assertEquals(sink.getUnregisteredCheckInterval(),
                                DistributionAcceptanceSink.DEFAULT_UNREGISTERED_RECHECK_INTERVAL);
        }
    }

    @Test
    public void givenFullArguments_whenBuildingSink_thenOk() {
        // Given
        DistributionLifecycleStateStore stateStore = mock(DistributionLifecycleStateStore.class);

        // When
        try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                          .stateStore(stateStore)
                                                                                          .acceptUnregistered()
                                                                                          .rejectAfterRejection()
                                                                                          .unregisteredGracePeriod(
                                                                                                  Duration.ofSeconds(
                                                                                                          10),
                                                                                                  Duration.ofMillis(
                                                                                                          250))
                                                                                          .build()) {
            // Then
            Assert.assertNotNull(sink.toString());
            Assert.assertFalse(sink.isRejectUnregistered());
            Assert.assertFalse(sink.isDiscardAfterRejection());
            Assert.assertEquals(sink.getUnregisteredGracePeriod(), Duration.ofSeconds(10));
            Assert.assertEquals(sink.getUnregisteredCheckInterval(), Duration.ofMillis(250));
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenZeroGracePeriod_whenBuildingSink_thenIllegalArgument() {
        // Given, When and Then
        DistributionAcceptanceSink.create()
                                  .stateStore(mock(DistributionLifecycleStateStore.class))
                                  .unregisteredGracePeriod(Duration.ZERO, Duration.ZERO)
                                  .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenZeroCheckInterval_whenBuildingSink_thenIllegalArgument() {
        // Given, When and Then
        DistributionAcceptanceSink.create()
                                  .stateStore(mock(DistributionLifecycleStateStore.class))
                                  .unregisteredGracePeriod(Duration.ofSeconds(5), Duration.ZERO)
                                  .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNegativeGracePeriod_whenBuildingSink_thenIllegalArgument() {
        // Given, When and Then
        DistributionAcceptanceSink.create()
                                  .stateStore(mock(DistributionLifecycleStateStore.class))
                                  .unregisteredGracePeriod(Duration.ofSeconds(-1), Duration.ofSeconds(-1))
                                  .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNegativeCheckInterval_whenBuildingSink_thenIllegalArgument() {
        // Given, When and Then
        DistributionAcceptanceSink.create()
                                  .stateStore(mock(DistributionLifecycleStateStore.class))
                                  .unregisteredGracePeriod(Duration.ofSeconds(5), Duration.ofSeconds(-1))
                                  .build();
    }

    @Test
    public void givenZeroGracePeriodWithUnregisteredAcceptance_whenBuildingSink_thenOk() {
        // Given and When
        try (DistributionAcceptanceSink<Integer, String> sink = DistributionAcceptanceSink.<Integer, String>create()
                                                                                          .stateStore(
                                                                                                  mock(DistributionLifecycleStateStore.class))
                                                                                          .acceptUnregistered()
                                                                                          .unregisteredGracePeriod(
                                                                                                  Duration.ZERO,
                                                                                                  Duration.ZERO)
                                                                                          .build()) {
            // Then
            Assert.assertFalse(sink.isRejectUnregistered());
            Assert.assertEquals(sink.getUnregisteredGracePeriod(), Duration.ZERO);
            Assert.assertEquals(sink.getUnregisteredCheckInterval(), Duration.ZERO);
        }
    }
}
