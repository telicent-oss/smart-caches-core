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
package io.telicent.smart.cache.distribution.lifecycle.store;

import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.Util;
import io.telicent.smart.cache.distribution.lifecycle.events.IngestStatus;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.PartitionOffsets;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;

/**
 * Abstract test suite for {@link DistributionLifecycleStateStore} implementations.
 * <p>
 * To test your implementation you <strong>MUST</strong> implement the {@link #newStore()} and {@link #reopenStore()}
 * methods following the contract defined on that method Javadoc.  Test classes should also override the
 * {@link #isPersistent()} and {@link #isApplicationScoped()} methods as appropriate since those control whether some
 * tests are run/skipped.
 * </p>
 */
public abstract class AbstractDistributionLifecycleStoreTests {
    /**
     * The default distribution ID used in many tests
     */
    public static final String DISTRIBUTION_ID = "distro";
    /**
     * The default application ID used in many tests
     */
    public static final String APP_ID = "test";

    private static void verifyNoActiveEvents(DistributionLifecycleStateStore store) {
        Assert.assertTrue(store.activeEvents().isEmpty(), "Expected no active events");
    }

    private static void verifyActiveEvents(DistributionLifecycleStateStore store) {
        Assert.assertFalse(store.activeEvents().isEmpty(), "Expected some active events");
    }

    private static void verifyActiveEvents(DistributionLifecycleStateStore store, int expected) {
        verifyActiveEvents(store);
        Assert.assertEquals(store.activeEvents().size(), expected,
                            "Wrong number of active events (found " + store.activeEvents()
                                                                           .size() + " when " + expected + " expected)");
    }

    private List<LifecycleAction> transition(DistributionLifecycleStateStore store, String distroId,
                                             DistributionLifecycleState... states) {
        List<LifecycleAction> actions = new ArrayList<>();
        DistributionLifecycleState last = store.getLifecycleState(distroId);
        for (DistributionLifecycleState state : states) {
            LifecycleAction action = Util.action(UUID.randomUUID(), distroId, last, state);
            actions.add(action);
            store.add(action);
            last = state;
            Assert.assertEquals(store.getLifecycleState(distroId), state);
        }
        return actions;
    }

    /**
     * Send a sequence of acknowledgements to the store without validating any state afterwards
     * <p>
     * Intended primarily for multithreaded tests of immediately persistent stores where the in parallel updates would
     * make any attempt to check state after each acknowledgement unstable.
     * </p>
     *
     * @param store Store
     * @param eventId Event ID
     * @param appId Application ID
     * @param distroId Distribution ID
     * @param states Application states to send acknowledgements for
     */
    private void acknowledgeOnly(DistributionLifecycleStateStore store, UUID eventId, String appId, String distroId,
                                 ApplicationState... states) {
        for (ApplicationState state : states) {
            LifecycleAcknowledgement acknowledgement = Util.ack(eventId, distroId, state);
            try {
                store.add(appId, acknowledgement);
            } catch (IllegalStateException e) {
                // Another thread may already have progressed the application state to a point where the transition we
                // tried to apply was considered illegal
            }
        }
    }

    private void acknowledge(DistributionLifecycleStateStore store, UUID eventId, String appId, String distroId,
                             ApplicationState... states) {
        Assert.assertNull(store.getApplicationState(eventId, appId));
        for (ApplicationState state : states) {
            LifecycleAcknowledgement acknowledgement = Util.ack(eventId, distroId, state);
            store.add(appId, acknowledgement);
            // NB - If the store being tested is application scoped then it should only update app state for the
            //      configured test application, otherwise it should ignore the state updates
            if (!this.isApplicationScoped() || Objects.equals(appId, APP_ID)) {
                Assert.assertEquals(store.getApplicationState(eventId, appId), state);

                if (state != ApplicationState.Completed) {
                    verifyActiveEvents(store);
                } else {
                    verifyNoActiveEvents(store);
                }
            } else {
                Assert.assertNull(store.getApplicationState(eventId, appId));
            }
        }
    }

    /**
     * Creates a fresh new empty instance of a state store for testing, if the store is application scoped (see
     * {@link #isApplicationScoped()}), then <strong>MUST</strong> be scoped to the application ID {@value #APP_ID}
     *
     * @return Fresh store instance scoped to the test application ID if appropriate
     */
    public abstract DistributionLifecycleStateStore newStore();

    /**
     * Reopens the store returned by the most recent invocation of {@link #newStore()}, if the store is application
     * scoped (see * {@link #isApplicationScoped()}), then <strong>MUST</strong> be scoped to the application ID
     * {@value #APP_ID}
     * <p>
     * If the store under test is not persistent, as indicated by {@link #isPersistent()}, then this method will not be
     * called and tests that use it will be skipped.
     * </p>
     *
     * @return Reopened store instance scoped to the test application ID if appropriate
     */
    public abstract DistributionLifecycleStateStore reopenStore();

    /**
     * Indicates whether the store implementation under test is persistent, if {@code false} then any test that would
     * use {@link #reopenStore()} will be skipped for the implementation.
     *
     * @return True if a persistent state store, false otherwise
     */
    public boolean isPersistent() {
        return true;
    }

    /**
     * Indicates whether the store implementation is immediately persistent i.e. are changes in the store immediately
     * persisted to underlying storage, or is an explicit {@link DistributionLifecycleStateStore#flush()} required to
     * persist the store state.
     *
     * @return True if immediately persistent store, false otherwise
     */
    public boolean isImmediatelyPersistent() {
        return false;
    }

    /**
     * Indicates whether the implementation under test is application scoped, i.e. tracks application state for only a
     * single application.  When {@code true} then some tests that try to track multiple application states will be
     * skipped.
     *
     * @return True if application scoped store, false otherwise
     */
    public abstract boolean isApplicationScoped();

    @Test
    public void givenFreshStore_whenInspecting_thenEmpty() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            verifyNoActiveEvents(store);
            Assert.assertTrue(store.getLifecycleStates().isEmpty());
            Assert.assertTrue(store.getApplicationStates(UUID.randomUUID()).isEmpty());
            Assert.assertNull(store.getApplicationState(UUID.randomUUID(), APP_ID));
            Assert.assertTrue(store.getIngestStatuses(APP_ID).isEmpty());
            Assert.assertNull(store.getIngestStatus(APP_ID, DISTRIBUTION_ID));
            Assert.assertNull(store.getIngestOffset(APP_ID, DISTRIBUTION_ID, "partition-0"));
            Assert.assertTrue(store.getAllIngestStatuses().isEmpty());
            Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Unregistered);
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullAction_whenAddingToStore_thenNPE() {
        // Given
        LifecycleAction action = null;
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.add(action);
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullAcknowledgement_whenAddingToStore_thenNPE() {
        // Given
        LifecycleAcknowledgement acknowledgement = null;
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.add(APP_ID, acknowledgement);
        }
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void givenNullIngestStatus_whenAddingToStore_thenNPE() {
        // Given
        IngestStatus status = null;
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.add(APP_ID, status);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNullDistributionId_whenCheckingState_thenIllegalArgument() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.getLifecycleState(null);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenEmptyDistributionId_whenCheckingState_thenIllegalArgument() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.getLifecycleState("");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenBlankDistributionId_whenCheckingState_thenIllegalArgument() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.getLifecycleState("   ");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNullEventId_whenCheckingAppState_thenIllegalArgument() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.getApplicationState(null, APP_ID);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNullAppId_whenCheckingAppState_thenIllegalArgument() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.getApplicationState(UUID.randomUUID(), null);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenEmptyAppId_whenCheckingAppState_thenIllegalArgument() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.getApplicationState(UUID.randomUUID(), "");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenBlankAppId_whenCheckingAppState_thenIllegalArgument() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.getApplicationState(UUID.randomUUID(), "   ");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenNullEventId_whenCheckingAppStates_thenIllegalArgument() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.getApplicationStates(null);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenAcknowledgementAndNullAppId_whenAddingToStore_thenIllegalArgument() {
        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = Util.action(eventId, DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                                             DistributionLifecycleState.Registered);
        LifecycleAcknowledgement acknowledgement = Util.ack(eventId, DISTRIBUTION_ID, ApplicationState.Requested);
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.add(action);
            store.add(null, acknowledgement);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void givenIngestStatusAndNullAppId_whenAddingToStore_thenIllegalArgument() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.add(null, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 1L));
        }
    }

    @Test
    public void givenAction_whenAddingToStore_thenUpdated() {
        // Given
        LifecycleAction action =
                Util.action(UUID.randomUUID(), DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                            DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add(action);

            // Then
            Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Registered);
            verifyActiveEvents(store, 1);
        }
    }

    @Test
    public void givenIngestStatuses_whenAddingToStore_thenQueryable() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 42L));
            store.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-1", 84L));
            store.add(APP_ID, Util.ingestStatus("distro-2", "partition-9", 126L));

            // Then
            Map<String, PartitionOffsets> appStatuses = store.getIngestStatuses(APP_ID);
            Assert.assertEquals(appStatuses.size(), 2);
            Assert.assertEquals(appStatuses.get(DISTRIBUTION_ID).getOffset("partition-0"), Long.valueOf(42L));
            Assert.assertEquals(appStatuses.get(DISTRIBUTION_ID).getOffset("partition-1"), Long.valueOf(84L));
            Assert.assertEquals(store.getIngestStatus(APP_ID, DISTRIBUTION_ID).getOffset("partition-0"),
                                Long.valueOf(42L));
            Assert.assertEquals(store.getIngestStatus(APP_ID, DISTRIBUTION_ID).getOffset("partition-1"),
                                Long.valueOf(84L));
            Assert.assertEquals(store.getIngestOffset(APP_ID, DISTRIBUTION_ID, "partition-1"), Long.valueOf(84L));
            Assert.assertEquals(store.getIngestOffset(APP_ID, "distro-2", "partition-9"), Long.valueOf(126L));

            Map<String, Map<String, PartitionOffsets>> allStatuses = store.getAllIngestStatuses();
            Assert.assertTrue(allStatuses.containsKey(APP_ID));
            Assert.assertEquals(allStatuses.get(APP_ID).size(), 2);
        }
    }

    @Test
    public void givenKnownAppAndUnknownDistribution_whenGettingIngestStatus_thenNull() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            store.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 42L));

            // When and Then
            Assert.assertNull(store.getIngestStatus(APP_ID, "unknown-distribution"));
            Assert.assertNull(store.getIngestOffset(APP_ID, "unknown-distribution", "partition-0"));
        }
    }

    @Test
    public void givenIngestStatusReplays_whenAddingToStore_thenOnlyHigherOffsetsApplied() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 100L));
            store.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 99L));
            store.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 100L));
            store.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 101L));

            // Then
            Assert.assertEquals(store.getIngestOffset(APP_ID, DISTRIBUTION_ID, "partition-0"), Long.valueOf(101L));
        }
    }

    @Test
    public void givenAppScopedStore_whenAddingIngestStatusForDifferentApp_thenIgnored() {
        requireApplicationScopedStore();

        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add("other", Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 10L));

            // Then
            Assert.assertTrue(store.getIngestStatuses(APP_ID).isEmpty());
            Assert.assertNull(store.getIngestStatus("other", DISTRIBUTION_ID));
            Assert.assertTrue(store.getAllIngestStatuses().isEmpty());
        }
    }

    @Test
    public void givenGlobalStore_whenAddingIngestStatusesForMultipleApps_thenTrackedSeparately() {
        requireNonApplicationScopedStore();

        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 10L));
            store.add("other", Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 20L));

            // Then
            Assert.assertEquals(store.getIngestOffset(APP_ID, DISTRIBUTION_ID, "partition-0"), Long.valueOf(10L));
            Assert.assertEquals(store.getIngestOffset("other", DISTRIBUTION_ID, "partition-0"), Long.valueOf(20L));
            Assert.assertEquals(store.getAllIngestStatuses().size(), 2);
        }
    }

    private void requirePersistentStore() {
        if (!this.isPersistent()) {
            throw new SkipException("Test requires a persistent store");
        }
    }

    @Test
    public void givenPersistentStore_whenAddingAction_thenPersistCloseAndReopen() {
        // Given
        requirePersistentStore();

        // When
        givenAction_whenAddingToStore_thenUpdated();

        // Then
        try (DistributionLifecycleStateStore store = reopenStore()) {
            Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Registered);
            verifyActiveEvents(store, 1);
        }
    }

    @Test
    public void givenPersistentStore_whenAddingIngestStatuses_thenPersistCloseAndReopen() {
        // Given
        requirePersistentStore();

        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 123L));
        }

        // Then
        try (DistributionLifecycleStateStore store = reopenStore()) {
            Assert.assertEquals(store.getIngestOffset(APP_ID, DISTRIBUTION_ID, "partition-0"), Long.valueOf(123L));
        }
    }

    @Test
    public void givenMultipleActions_whenAddingToStore_thenDistributionsInExpectedStates() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            transition(store, DISTRIBUTION_ID, DistributionLifecycleState.Registered,
                       DistributionLifecycleState.Active);
            transition(store, "withdrawn", DistributionLifecycleState.Registered, DistributionLifecycleState.Active,
                       DistributionLifecycleState.Withdrawn);
            transition(store, "deleted", DistributionLifecycleState.Registered, DistributionLifecycleState.Deleted);

            // Then
            Map<String, DistributionLifecycleState> states = store.getLifecycleStates();
            Assert.assertEquals(states.get(DISTRIBUTION_ID), DistributionLifecycleState.Active);
            Assert.assertEquals(states.get("withdrawn"), DistributionLifecycleState.Withdrawn);
            Assert.assertEquals(states.get("deleted"), DistributionLifecycleState.Deleted);
            verifyActiveEvents(store, 7);
        }
    }

    @Test
    public void givenPersistentStore_whenAddingMultipleActions_thenPersistCloseAndReopen() {
        // Given
        requirePersistentStore();

        // When
        givenMultipleActions_whenAddingToStore_thenDistributionsInExpectedStates();

        // Then
        try (DistributionLifecycleStateStore store = reopenStore()) {
            Map<String, DistributionLifecycleState> states = store.getLifecycleStates();
            Assert.assertEquals(states.get(DISTRIBUTION_ID), DistributionLifecycleState.Active);
            Assert.assertEquals(states.get("withdrawn"), DistributionLifecycleState.Withdrawn);
            Assert.assertEquals(states.get("deleted"), DistributionLifecycleState.Deleted);
        }
    }

    @Test
    public void givenActionsForRepeatedTransition_whenAddingToStore_thenStateConsistent() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            transition(store, DISTRIBUTION_ID, DistributionLifecycleState.Registered, DistributionLifecycleState.Active,
                       DistributionLifecycleState.Active);

            // Then
            Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Active);
        }
    }

    @Test
    public void givenActionsForRepeatedTransitionThatCouldRevertState_whenAddingToStore_thenStateNotReverted() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            List<LifecycleAction> actions = transition(store, DISTRIBUTION_ID, DistributionLifecycleState.Registered,
                                                       DistributionLifecycleState.Active,
                                                       DistributionLifecycleState.Withdrawn,
                                                       DistributionLifecycleState.Active);
            LifecycleAction activeToWithdrawn = actions.get(2);
            Assert.assertEquals(activeToWithdrawn.getState().getFrom(), DistributionLifecycleState.Active);
            Assert.assertEquals(activeToWithdrawn.getState().getTo(), DistributionLifecycleState.Withdrawn);
            store.add(activeToWithdrawn);

            // Then
            Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Active);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenActionsForIllegalTransition_whenAddingToStore_thenIllegalState() {
        // Given
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            transition(store, DISTRIBUTION_ID, DistributionLifecycleState.Registered, DistributionLifecycleState.Active,
                       DistributionLifecycleState.Deleted, DistributionLifecycleState.Active);
        }
    }

    @Test
    public void givenAction_whenAcknowledging_thenAppStateUpdated() {
        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = Util.action(eventId, DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                                             DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.add(action);
            Assert.assertNull(store.getApplicationState(eventId, APP_ID));
            verifyActiveEvents(store, 1);
            acknowledge(store, eventId, APP_ID, DISTRIBUTION_ID, ApplicationState.Requested,
                        ApplicationState.InProgress, ApplicationState.Completed);
            verifyNoActiveEvents(store);
        }
    }

    @Test
    public void givenAction_whenAcknowledgingWithSameState_thenAppStateConsistent() {
        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = Util.action(eventId, DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                                             DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.add(action);
            Assert.assertNull(store.getApplicationState(eventId, APP_ID));
            verifyActiveEvents(store, 1);
            acknowledge(store, eventId, APP_ID, DISTRIBUTION_ID, ApplicationState.Requested,
                        ApplicationState.Requested);
            verifyActiveEvents(store, 1);
        }
    }

    @Test
    public void givenAction_whenAcknowledgingForMultipleApps_thenAppStatesUpdatedIndependently() {
        requireNonApplicationScopedStore();

        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = Util.action(eventId, DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                                             DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add(action);
            verifyActiveEvents(store, 1);
            acknowledge(store, eventId, APP_ID, DISTRIBUTION_ID, ApplicationState.Requested,
                        ApplicationState.InProgress, ApplicationState.Completed);
            acknowledge(store, eventId, "other", DISTRIBUTION_ID, ApplicationState.Requested,
                        ApplicationState.InProgress, ApplicationState.Failed);
            acknowledge(store, eventId, "another", DISTRIBUTION_ID, ApplicationState.Requested);

            // Then
            Map<String, ApplicationState> appStates = store.getApplicationStates(eventId);
            Assert.assertEquals(appStates.get(APP_ID), ApplicationState.Completed);
            Assert.assertEquals(appStates.get("other"), ApplicationState.Failed);
            Assert.assertEquals(appStates.get("another"), ApplicationState.Requested);
            verifyActiveEvents(store, 1);
        }
    }

    private void requireNonApplicationScopedStore() {
        if (this.isApplicationScoped()) {
            throw new SkipException("Test only applicable to global state stores");
        }
    }

    @Test
    public void givenAppScopedStore_whenActionAcknowledgedForMultipleApps_thenOnlyAppScopeStateUpdated() {
        requireApplicationScopedStore();

        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = Util.action(eventId, DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                                             DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add(action);
            verifyActiveEvents(store, 1);
            acknowledge(store, eventId, APP_ID, DISTRIBUTION_ID, ApplicationState.Requested,
                        ApplicationState.InProgress, ApplicationState.Completed);
            acknowledge(store, eventId, "other", DISTRIBUTION_ID, ApplicationState.Requested,
                        ApplicationState.InProgress, ApplicationState.Failed);
            acknowledge(store, eventId, "another", DISTRIBUTION_ID, ApplicationState.Requested);

            // Then
            Map<String, ApplicationState> appStates = store.getApplicationStates(eventId);
            Assert.assertEquals(appStates.get(APP_ID), ApplicationState.Completed);
            Assert.assertNull(appStates.get("other"));
            Assert.assertNull(appStates.get("another"));
            verifyNoActiveEvents(store);
        }
    }

    private void requireApplicationScopedStore() {
        if (!this.isApplicationScoped()) {
            throw new SkipException("Test only applicable to application scoped state stores");
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenActionWithReusedId_whenAddingToStore_thenIllegalState() {
        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = Util.action(eventId, DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                                             DistributionLifecycleState.Registered);
        LifecycleAction reusedId = Util.action(eventId, DISTRIBUTION_ID, DistributionLifecycleState.Registered,
                                               DistributionLifecycleState.Active);
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add(action);

            // Then
            store.add(reusedId);
        }
    }

    @Test
    public void givenAction_whenAddingToStoreMoreThanOnce_thenOk() {
        // Given
        UUID eventId = UUID.randomUUID();
        LifecycleAction action = Util.action(eventId, DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                                             DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            // When
            store.add(action);
            store.add(action);

            // Then
            Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Registered);
        }
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void givenAcknowledgementForUnknownEvent_whenAddingToStore_thenIllegalState() {
        // Given
        LifecycleAcknowledgement acknowledgement =
                Util.ack(UUID.randomUUID(), DISTRIBUTION_ID, ApplicationState.Requested);
        try (DistributionLifecycleStateStore store = newStore()) {
            // When and Then
            store.add(APP_ID, acknowledgement);
        }
    }

    private static Consumer<DistributionLifecycleStateStore> consumer(Consumer<DistributionLifecycleStateStore> c) {
        return c;
    }

    @DataProvider(name = "interactions")
    public static Object[][] interactions() {
        return new Object[][] {
                { consumer(DistributionLifecycleStateStore::activeEvents) },
                { consumer(DistributionLifecycleStateStore::getLifecycleStates) },
                { consumer(s -> s.getLifecycleState(DISTRIBUTION_ID)) },
                { consumer(s -> s.getApplicationState(UUID.randomUUID(), APP_ID)) },
                { consumer(s -> s.getApplicationStates(UUID.randomUUID())) },
                { consumer(s -> s.getIngestStatuses(APP_ID)) },
                { consumer(s -> s.getIngestStatus(APP_ID, DISTRIBUTION_ID)) },
                { consumer(s -> s.getIngestOffset(APP_ID, DISTRIBUTION_ID, "partition-0")) },
                { consumer(DistributionLifecycleStateStore::getAllIngestStatuses) },
                {
                        consumer(s -> s.add(
                                Util.action(UUID.randomUUID(), DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                                            DistributionLifecycleState.Registered)))
                },
                {
                        consumer(s -> s.add(APP_ID,
                                            Util.ack(UUID.randomUUID(), DISTRIBUTION_ID, ApplicationState.Requested)))
                },
                { consumer(s -> s.add(APP_ID, Util.ingestStatus(DISTRIBUTION_ID, "partition-0", 1L))) },
                { consumer(DistributionLifecycleStateStore::flush) }
        };
    }

    @Test(dataProvider = "interactions", dataProviderClass = AbstractDistributionLifecycleStoreTests.class, expectedExceptions = IllegalStateException.class)
    public void givenClosedStore_whenInteracting_thenIllegalState(Consumer<DistributionLifecycleStateStore> consumer) {
        // Given
        DistributionLifecycleStateStore store = newStore();
        store.close();

        // When and Then
        consumer.accept(store);
    }

    @Test
    public void givenClosedStored_whenClosingAgain_thenOk() {
        // Given
        DistributionLifecycleStateStore store = newStore();
        store.close();

        // When and Then
        store.close();
    }

    private void requireImmediatePersistence() {
        requirePersistentStore();
        if (!this.isImmediatelyPersistent()) {
            throw new SkipException("This test requires a persistent store with immediate persistence");
        }
    }

    private void requireNonImmediatePersistence() {
        requirePersistentStore();
        if (this.isImmediatelyPersistent()) {
            throw new SkipException("This test requires a persistent store without immediate persistence");
        }
    }

    @Test
    public void givenTwoInstancesOfImmediatelyPersistentStore_whenInteractingWithOne_thenStateUpdatedInOther() {
        requireImmediatePersistence();

        // Given
        LifecycleAction action =
                Util.action(UUID.randomUUID(), DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                            DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            try (DistributionLifecycleStateStore otherStore = reopenStore()) {
                // When
                store.add(action);

                // Then
                Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Registered);
                Assert.assertEquals(otherStore.getLifecycleState(DISTRIBUTION_ID),
                                    DistributionLifecycleState.Registered);
            }
        }
    }

    @Test
    public void givenTwoInstancesOfNonImmediatelyPersistentStore_whenInteractingWithOne_thenStateNotAffectedInOther_andFlushUpdatesPersistentState() {
        requireNonImmediatePersistence();

        // Given
        LifecycleAction action =
                Util.action(UUID.randomUUID(), DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                            DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            try (DistributionLifecycleStateStore otherStore = reopenStore()) {
                // When
                store.add(action);

                // Then
                Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Registered);
                Assert.assertEquals(otherStore.getLifecycleState(DISTRIBUTION_ID),
                                    DistributionLifecycleState.Unregistered);

                // And
                store.flush();
                try (DistributionLifecycleStateStore thirdStore = reopenStore()) {
                    Assert.assertEquals(thirdStore.getLifecycleState(DISTRIBUTION_ID),
                                        DistributionLifecycleState.Registered);
                }
            }
        }
    }

    @Test
    public void givenTwoInstancesOfImmediatelyPersistentStore_whenInteractingWithOne_thenStateImmediatelyVisibleInOther() {
        requireImmediatePersistence();

        // Given
        LifecycleAction action =
                Util.action(UUID.randomUUID(), DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                            DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            try (DistributionLifecycleStateStore otherStore = reopenStore()) {
                // When
                store.add(action);

                // Then
                Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Registered);
                Assert.assertEquals(otherStore.getLifecycleState(DISTRIBUTION_ID),
                                    DistributionLifecycleState.Registered);
            }
        }
    }

    @Test
    public void givenTwoInstanceOfImmediatelyPersistentStore_whenAddingActionsInParallel_thenStateConsistent() {
        requireImmediatePersistence();

        // Given
        LifecycleAction action =
                Util.action(UUID.randomUUID(), DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                            DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            try (DistributionLifecycleStateStore otherStore = reopenStore()) {
                // When
                ExecutorService executor = Executors.newFixedThreadPool(2);
                Semaphore semaphore = new Semaphore(0, true);
                Runnable a = acquireThenRun(semaphore, store, s -> s.add(action));
                Runnable b = acquireThenRun(semaphore, otherStore, s -> s.add(action));
                Future<?> futureA = executor.submit(a);
                Future<?> futureB = executor.submit(b);
                await().pollInterval(Duration.ofMillis(50))
                       .atMost(Duration.ofSeconds(1))
                       .until(() -> semaphore.getQueueLength() == 2);
                semaphore.release(2);

                // Then
                verifyFutureCompleted(futureA);
                verifyFutureCompleted(futureB);
                Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Registered);
                Assert.assertEquals(otherStore.getLifecycleState(DISTRIBUTION_ID),
                                    DistributionLifecycleState.Registered);
            }
        }
    }

    @Test
    public void givenTwoInstanceOfImmediatelyPersistentStore_whenAcknowledgingActionsInParallel_thenStateConsistent() {
        requireImmediatePersistence();

        // Given
        LifecycleAction action =
                Util.action(UUID.randomUUID(), DISTRIBUTION_ID, DistributionLifecycleState.Unregistered,
                            DistributionLifecycleState.Registered);
        try (DistributionLifecycleStateStore store = newStore()) {
            store.add(action);
            try (DistributionLifecycleStateStore otherStore = reopenStore()) {
                // When
                ExecutorService executor = Executors.newFixedThreadPool(2);
                Semaphore semaphore = new Semaphore(0, true);
                Consumer<DistributionLifecycleStateStore> acknowledger =
                        s -> acknowledgeOnly(s, action.getEventId(), APP_ID, DISTRIBUTION_ID,
                                             ApplicationState.Requested,
                                             ApplicationState.InProgress, ApplicationState.Failed,
                                             ApplicationState.InProgress, ApplicationState.Completed);
                Runnable a = acquireThenRun(semaphore, store, acknowledger);
                Runnable b = acquireThenRun(semaphore, otherStore, acknowledger);
                Future<?> futureA = executor.submit(a);
                Future<?> futureB = executor.submit(b);
                await().pollInterval(Duration.ofMillis(50))
                       .atMost(Duration.ofSeconds(1))
                       .until(() -> semaphore.getQueueLength() == 2);
                semaphore.release(2);

                // Then
                verifyFutureCompleted(futureA);
                verifyFutureCompleted(futureB);
                Assert.assertEquals(store.getLifecycleState(DISTRIBUTION_ID), DistributionLifecycleState.Registered);
                Assert.assertEquals(otherStore.getLifecycleState(DISTRIBUTION_ID),
                                    DistributionLifecycleState.Registered);
                Assert.assertEquals(store.getApplicationState(action.getEventId(), APP_ID), ApplicationState.Completed);
                Assert.assertEquals(otherStore.getApplicationState(action.getEventId(), APP_ID),
                                    ApplicationState.Completed);
            }
        }
    }

    private static void verifyFutureCompleted(Future<?> f) {
        final AtomicBoolean printedError = new AtomicBoolean(false);
        await().pollDelay(Duration.ofMillis(250))
               .pollInterval(Duration.ofMillis(50))
               .atMost(Duration.ofSeconds(5))
               .until(() -> {
                   if (f.isDone()) {
                       try {
                           f.get(25, TimeUnit.MILLISECONDS);
                           return true;
                       } catch (Throwable e) {
                           if (printedError.compareAndSet(false, true)) {
                               System.err.println(e.getMessage());
                               e.printStackTrace(System.err);
                           }
                           return false;
                       }
                   } else {
                       return false;
                   }
               });
    }

    private static Runnable acquireThenRun(Semaphore semaphore, DistributionLifecycleStateStore store,
                                           Consumer<DistributionLifecycleStateStore> consumer) {
        return () -> {
            try {
                semaphore.acquire(1);
                consumer.accept(store);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
