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
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.LazyEnvelope;
import io.telicent.smart.cache.payloads.Metadata;
import io.telicent.smart.cache.sources.EventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

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
                              .state(new LifecycleStateTransition(from,
                                                                  to))
                              .build();
    }

    public static LifecycleAcknowledgement ack(UUID eventId, String distributionId, ApplicationState appState) {
        return LifecycleAcknowledgement.builder()
                                       .eventId(eventId)
                                       .distributionId(distributionId)
                                       .state(new ApplicationStateUpdate(
                                               appState))
                                       .build();
    }
}
