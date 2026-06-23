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
package io.telicent.smart.cache.distribution.lifecycle.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.telicent.smart.cache.distribution.lifecycle.ApplicationState;
import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.ApplicationStateUpdate;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.LifecycleStateTransition;
import io.telicent.smart.cache.payloads.Envelope;
import io.telicent.smart.cache.payloads.Metadata;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class TestEnvelopeWrappedEvents extends AbstractJacksonTests {


    @Override
    protected ObjectMapper createObjectMapper() {
        return Envelope.JSON;
    }

    protected Envelope wrap(Map<String, Object> rawBody, String documentFormat) {
        return Envelope.create()
                       .id(UUID.randomUUID())
                       .metadata(Metadata.create()
                                         .generatedBy("tests")
                                         .generatedAt(Date.from(Instant.now()))
                                         .generatorVersion("0.1")
                                         .documentFormat(documentFormat)
                                         .build())
                       .body(new LinkedHashMap<>(rawBody))
                       .build();
    }

    @Test
    public void givenEnvelopeWrappedAction_whenObtainingAction_thenOk_andRoundTrips() throws JsonProcessingException {
        // Given
        Map<String, Object> rawAction =
                Map.of("eventId", UUID.randomUUID(), "datasetId", "dataset", "distributionId", "distro1a", "state",
                       Map.of("from", DistributionLifecycleState.Unregistered, "to",
                              DistributionLifecycleState.Registered), "user", "example@example.org");
        Envelope envelope = wrap(rawAction, LifecycleAction.DOCUMENT_FORMAT);

        // When
        LifecycleAction action = envelope.getBodyAs(LifecycleAction.class);

        // Then
        Assert.assertNotNull(action);
        Assert.assertEquals(action.getDatasetId(), "dataset");
        Assert.assertEquals(action.getDistributionId(), "distro1a");
        Assert.assertEquals(action.getState(), new LifecycleStateTransition(DistributionLifecycleState.Unregistered,
                                                                            DistributionLifecycleState.Registered));

        // And
        Envelope reparsed = roundTrip(envelope, Envelope.class);
        Assert.assertEquals(action, reparsed.getBodyAs(LifecycleAction.class));
    }

    @Test
    public void givenEnvelopeWrappedAcknowledgement_whenObtainingAcknowledgement_thenOk_andRoundTrips() throws
            JsonProcessingException {
        // Given
        Map<String, Object> rawAck = Map.of("eventId", UUID.randomUUID(), "distributionId", "distro1a", "state",
                                            Map.of("app", ApplicationState.InProgress));
        Envelope envelope = wrap(rawAck, LifecycleAcknowledgement.DOCUMENT_FORMAT);

        // When
        LifecycleAcknowledgement ack = envelope.getBodyAs(LifecycleAcknowledgement.class);

        // Then
        Assert.assertNotNull(ack);
        Assert.assertEquals(ack.getDistributionId(), "distro1a");
        Assert.assertEquals(ack.getState(), new ApplicationStateUpdate(ApplicationState.InProgress));

        // And
        Envelope reparsed = roundTrip(envelope, Envelope.class);
        Assert.assertEquals(ack, reparsed.getBodyAs(LifecycleAcknowledgement.class));
    }

    @Test
    public void givenEnvelopeWrappedIngestStatus_whenObtainingStatus_thenOk_andRoundTrips() throws
            JsonProcessingException {
        // Given
        Map<String, Object> rawStatus = Map.of("offsets", Map.of("distro1a", Map.of("test-0", 12345L)));
        Envelope envelope = wrap(rawStatus, IngestStatus.DOCUMENT_FORMAT);

        // When
        IngestStatus status = envelope.getBodyAs(IngestStatus.class);

        // Then
        Assert.assertNotNull(status);
        Assert.assertEquals(status.getOffset("distro1a", "test-0"), 12345L);

        // And
        Envelope reparsed = roundTrip(envelope, Envelope.class);
        Assert.assertEquals(status, reparsed.getBodyAs(IngestStatus.class));
    }
}
