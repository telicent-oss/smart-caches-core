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
package io.telicent.smart.cache.distribution.lifecycle;

import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAcknowledgement;
import io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.ApplicationStateUpdate;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.LifecycleStateTransition;
import io.telicent.smart.cache.distribution.lifecycle.store.DistributionLifecycleStateStore;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.Metadata;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.awaitility.Awaitility;
import org.testng.Assert;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class Util {
    public static SimpleEvent<UUID, LazyEnvelope> event(String docFormat, Object body) {
        return event(docFormat, body, null);
    }

    public static SimpleEvent<UUID, LazyEnvelope> event(String docFormat, Object body,
                                                        EventSource<UUID, LazyEnvelope> source) {
        return new SimpleEvent<>(Collections.emptyList(), UUID.randomUUID(), LazyEnvelope.of(Envelope.create()
                                                                                                     .id(UUID.randomUUID())
                                                                                                     .metadata(
                                                                                                             Metadata.create()
                                                                                                                     .generatedBy(
                                                                                                                             "tests")
                                                                                                                     .generatorVersion(
                                                                                                                             "1.2.3")
                                                                                                                     .generatedAt(
                                                                                                                             Date.from(
                                                                                                                                     Instant.now()))
                                                                                                                     .documentFormat(
                                                                                                                             docFormat)
                                                                                                                     .build())
                                                                                                     .bodyFrom(body)
                                                                                                     .build()), source);
    }

    public static LifecycleAction action(UUID eventId, String distributionId, DistributionLifecycleState from,
                                         DistributionLifecycleState to) {
        return LifecycleAction.builder()
                              .eventId(eventId)
                              .user("test@test.org")
                              .distributionId(distributionId)
                              .datasetId("dataset")
                              .state(new LifecycleStateTransition(from, to))
                              .build();
    }

    public static LifecycleAcknowledgement ack(UUID eventId, String distributionId, ApplicationState appState) {
        return LifecycleAcknowledgement.builder()
                                       .eventId(eventId)
                                       .distributionId(distributionId)
                                       .state(new ApplicationStateUpdate(appState))
                                       .build();
    }

    /**
     * Helper method that waits for a given supplier to return an expected value, useful for testing asynchronous state
     * updates converge to the expected state
     *
     * @param alias    Alias for the await condition
     * @param supplier Supplier for the value to test
     * @param expected Expected value
     * @param <T>      Type of value
     */
    public static <T> void awaitEquals(String alias, Supplier<T> supplier, T expected) {
        if (expected != null) {
            Awaitility.await(alias)
                      .pollInterval(Duration.ofMillis(250))
                      .atMost(Duration.ofSeconds(10))
                      .until(supplier::get, t -> Objects.equals(t, expected));
            Assert.assertEquals(supplier.get(), expected, "Failed check " + alias);
        } else {
            // NB - If the expected value is null then have to use a different form of until() as the form used above
            //      fails the check if the supplier returns a null value
            Awaitility.await(alias)
                      .pollInterval(Duration.ofSeconds(1))
                      .atMost(Duration.ofSeconds(10))
                      .until(() -> supplier.get() == null);
            Assert.assertNull(supplier.get(), "Failed check " + alias);
        }
    }

    /**
     * Verifies that a given distribution has the expected state in the given state store, uses
     * {@link #awaitEquals(String, Supplier, Object)}
     *
     * @param distributionID Distribution ID
     * @param stateStore     State Store
     * @param expected       Expected distribution lifecycle state
     */
    public static void verifyDistributionState(String distributionID, DistributionLifecycleStateStore stateStore,
                                               DistributionLifecycleState expected) {
        awaitEquals("Distribution " + distributionID + " State as expected",
                    () -> stateStore.getLifecycleState(distributionID), expected);
    }

    /**
     * Verifies that a given application has the expected state for the given event in the given state store, uses
     * {@link #awaitEquals(String, Supplier, Object)}
     *
     * @param stateStore State Store
     * @param eventId    Event ID
     * @param appId      Application ID
     * @param expected   Expected application state
     */
    public static void verifyApplicationState(DistributionLifecycleStateStore stateStore, UUID eventId, String appId,
                                              ApplicationState expected) {
        awaitEquals("Event " + eventId + " has correct state for application " + appId,
                    () -> stateStore.getApplicationState(eventId, appId), expected);
    }
}
