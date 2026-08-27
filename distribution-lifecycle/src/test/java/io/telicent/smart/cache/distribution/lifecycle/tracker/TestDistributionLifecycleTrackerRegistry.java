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
package io.telicent.smart.cache.distribution.lifecycle.tracker;

import org.junit.Assert;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

// java:S8924 - qualified Mockito calls are preferred over static imports here
@SuppressWarnings({"resource", "java:S8924"})
public class TestDistributionLifecycleTrackerRegistry {

    @BeforeMethod
    public void setup() {
        DistributionLifecycleTrackerRegistry.reset();
    }

    @AfterClass
    public void teardown() {
        DistributionLifecycleTrackerRegistry.reset();
    }

    @Test
    public void givenTracker_whenRegistered_thenOk_andTryingToRegisterAgainFails() {
        // Given
        DistributionLifecycleTracker tracker = Mockito.mock(DistributionLifecycleTracker.class);

        // When
        DistributionLifecycleTrackerRegistry.setInstance(tracker);

        // Then
        Assert.assertSame(DistributionLifecycleTrackerRegistry.getInstance(), tracker);

        // And
        Assert.assertThrows(IllegalStateException.class,
                            () -> DistributionLifecycleTrackerRegistry.setInstance(tracker));
    }

    @Test
    public void givenRegistry_whenInspecting_thenNothingRegisteredByDefault() {
        // Given and When
        DistributionLifecycleTracker tracker = DistributionLifecycleTrackerRegistry.getInstance();

        // Then
        Assert.assertNull(tracker);
    }

    @Test
    public void givenRegisteredTracker_whenReset_thenClosed_andNothingRegistered() {
        // Given
        final DistributionLifecycleTracker tracker = Mockito.mock(DistributionLifecycleTracker.class);
        DistributionLifecycleTrackerRegistry.setInstance(tracker);

        // When
        DistributionLifecycleTrackerRegistry.reset();

        // Then
        Mockito.verify(tracker).close();
        Assert.assertNull(DistributionLifecycleTrackerRegistry.getInstance());
    }

    @Test
    public void givenResetRegistry_whenRegisteringAgain_thenOk() {
        // Given
        final DistributionLifecycleTracker original = Mockito.mock(DistributionLifecycleTracker.class);
        DistributionLifecycleTrackerRegistry.setInstance(original);
        DistributionLifecycleTrackerRegistry.reset();

        // When - an application context shutting down and a new one starting in the same JVM must be able to register
        // a fresh tracker, setInstance() would otherwise refuse to replace the closed one
        final DistributionLifecycleTracker replacement = Mockito.mock(DistributionLifecycleTracker.class);
        DistributionLifecycleTrackerRegistry.setInstance(replacement);

        // Then
        Assert.assertSame(DistributionLifecycleTrackerRegistry.getInstance(), replacement);
    }

    @Test
    public void givenNothingRegistered_whenReset_thenNoOp() {
        // Given, When and Then - resetting an empty registry is safe, e.g. shutdown after a failed startup
        DistributionLifecycleTrackerRegistry.reset();
        Assert.assertNull(DistributionLifecycleTrackerRegistry.getInstance());
    }

    @Test
    public void givenRegisteredTracker_whenResetTwice_thenClosedOnce() {
        // Given
        final DistributionLifecycleTracker tracker = Mockito.mock(DistributionLifecycleTracker.class);
        DistributionLifecycleTrackerRegistry.setInstance(tracker);

        // When - shutdown paths can run more than once
        DistributionLifecycleTrackerRegistry.reset();
        DistributionLifecycleTrackerRegistry.reset();

        // Then
        Mockito.verify(tracker, Mockito.times(1)).close();
        Assert.assertNull(DistributionLifecycleTrackerRegistry.getInstance());
    }

}
