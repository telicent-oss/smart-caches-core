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
package io.telicent.smart.cache.actions.tracker;

import org.junit.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class TestActionTrackerRegistry {

    @BeforeClass
    public void setup() {
        ActionTrackerRegistry.reset();
    }

    @AfterMethod
    public void cleanup() {
        ActionTrackerRegistry.reset();
    }

    @Test
    public void givenNothingRegistered_whenRetrievingInstance_thenNull() {
        // Given and When
        ActionTracker tracker = ActionTrackerRegistry.getInstance();

        // Then
        Assert.assertNull(tracker);
    }

    @Test
    public void givenSomethingRegistered_whenRetrievingInstance_thenSame() {
        // Given
        ActionTracker tracker = new SimpleActionTracker("test");
        ActionTrackerRegistry.setInstance(tracker);

        // When
        ActionTracker retrieved = ActionTrackerRegistry.getInstance();

        // Then
        Assert.assertSame(tracker, retrieved);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenSomethingRegistered_whenRegisteringAgain_thenIllegalState() {
        // Given
        ActionTracker tracker = new SimpleActionTracker("test");
        ActionTrackerRegistry.setInstance(tracker);

        // When and Then
        ActionTrackerRegistry.setInstance(tracker);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullTracker_whenRegistering_thenNPE() {
        // Given, When and Then
        ActionTrackerRegistry.setInstance(null);
    }

    @Test
    public void givenSomethingRegistered_whenReset_thenClosed_andNothingRegistered() {
        // Given
        ActionTracker tracker = mock(ActionTracker.class);
        ActionTrackerRegistry.setInstance(tracker);

        // When
        ActionTrackerRegistry.reset();

        // Then
        verify(tracker).close();
        Assert.assertNull(ActionTrackerRegistry.getInstance());
    }

    @Test
    public void givenResetRegistry_whenRegisteringAgain_thenOk() {
        // Given
        ActionTracker original = mock(ActionTracker.class);
        ActionTrackerRegistry.setInstance(original);
        ActionTrackerRegistry.reset();

        // When - an application context shutting down and a new one starting in the same JVM must be able to register
        // a fresh tracker, setInstance() would otherwise refuse to replace the closed one
        ActionTracker replacement = mock(ActionTracker.class);
        ActionTrackerRegistry.setInstance(replacement);

        // Then
        Assert.assertSame(ActionTrackerRegistry.getInstance(), replacement);
    }

    @Test
    public void givenNothingRegistered_whenReset_thenNoOp() {
        // Given, When and Then - resetting an empty registry is safe, e.g. shutdown after a failed startup
        ActionTrackerRegistry.reset();
        Assert.assertNull(ActionTrackerRegistry.getInstance());
    }

    @Test
    public void givenSomethingRegistered_whenResetTwice_thenClosedOnce() {
        // Given
        ActionTracker tracker = mock(ActionTracker.class);
        ActionTrackerRegistry.setInstance(tracker);

        // When - shutdown paths can run more than once
        ActionTrackerRegistry.reset();
        ActionTrackerRegistry.reset();

        // Then
        verify(tracker, times(1)).close();
        Assert.assertNull(ActionTrackerRegistry.getInstance());
    }
}
