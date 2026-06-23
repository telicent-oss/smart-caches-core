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
package io.telicent.smart.cache.distribution.lifecycle.store.global;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.Util;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;

import static io.telicent.smart.cache.distribution.lifecycle.store.apps.TestAppDistributionLifecycleStoreFile.*;

public class TestGlobalDistributionLifecycleStore {

    @Test
    public void givenEmptyStore_whenAccessing_thenNoStateInformation() {
        // Given
        GlobalDistributionLifecycleStoreMemory store = new GlobalDistributionLifecycleStoreMemory();

        // When and Then
        Assert.assertTrue(store.getLifecycleStates().isEmpty());
        Assert.assertTrue(store.getApplicationStates(UUID.randomUUID()).isEmpty());
    }

    @Test
    public void givenStore_whenPopulating_thenStateAsExpected() {
        // Given
        GlobalDistributionLifecycleStoreMemory store = new GlobalDistributionLifecycleStoreMemory();
        UUID event1 = UUID.randomUUID();
        UUID event2 = UUID.randomUUID();

        // When
        store.add(Util.action(event1, "distro", DistributionLifecycleState.Unregistered,
                              DistributionLifecycleState.Registered));
        store.add(Util.action(event2, "distro", DistributionLifecycleState.Registered, DistributionLifecycleState.Active));
        store.add(APP_ID, Util.ack(event1, "distro", ApplicationState.Requested));
        store.add(APP_ID, Util.ack(event1, "distro", ApplicationState.Completed));

        // Then
        Assert.assertEquals(store.getLifecycleState("distro"), DistributionLifecycleState.Active);
        Assert.assertEquals(store.getApplicationState(event1, APP_ID), ApplicationState.Completed);
        Assert.assertNull(store.getApplicationState(event2, APP_ID));
        List<LifecycleAction> active = store.activeEvents();
        Assert.assertEquals(active.size(), 1);
        Assert.assertEquals(active.getFirst().getEventId(), event2);
    }

    @Test
    public void givenStore_whenPopulatingWithMultipleAppAcks_thenStatesAsExpected() {
        // Given
        GlobalDistributionLifecycleStoreMemory store = new GlobalDistributionLifecycleStoreMemory();
        UUID event1 = UUID.randomUUID();
        UUID event2 = UUID.randomUUID();

        // When
        store.add(Util.action(event1, "distro", DistributionLifecycleState.Unregistered,
                              DistributionLifecycleState.Registered));
        store.add(Util.action(event2, "distro", DistributionLifecycleState.Registered, DistributionLifecycleState.Active));
        store.add(APP_ID, Util.ack(event1, "distro", ApplicationState.Requested));
        store.add("other", Util.ack(event1, "distro", ApplicationState.Requested));
        store.add(APP_ID, Util.ack(event1, "distro", ApplicationState.Completed));

        // Then
        Assert.assertEquals(store.getLifecycleState("distro"), DistributionLifecycleState.Active);
        Assert.assertEquals(store.getApplicationState(event1, APP_ID), ApplicationState.Completed);
        Assert.assertEquals(store.getApplicationState(event1, "other"), ApplicationState.Requested);
        Assert.assertNull(store.getApplicationState(event2, APP_ID));
        List<LifecycleAction> active = store.activeEvents();
        Assert.assertEquals(active.size(), 2);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenStore_whenAckForUnknownEvent_thenIllegalState() {
        // Given
        GlobalDistributionLifecycleStoreMemory store = new GlobalDistributionLifecycleStoreMemory();

        // When and Then
        store.add(APP_ID, Util.ack(UUID.randomUUID(), "distro", ApplicationState.Requested));
    }
}
