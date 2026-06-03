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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.LifecycleStateTransition;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.UUID;

/**
 * A Lifecycle Action event is sent to lifecycle aware services to inform them of transitions in
 * {@link io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState} for distributions.
 * <p>
 * Each Action represents a single transition for a single distribution that needs to be applied.  Lifecycle aware
 * services are expected to respond with a series of {@link LifecycleAcknowledgement} events as they process and apply
 * the action.
 * </p>
 * <p>
 * This will be sent over Kafka wrapped in an {@link io.telicent.smart.cache.payloads.Envelope} using the document
 * format {@value #DOCUMENT_FORMAT}.
 * </p>
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
@Jacksonized
@JsonPropertyOrder({ "eventId", "distributionId", "datasetId", "state", "user" })
public class LifecycleAction implements Serializable {

    /**
     * Document format used when wrapping this into an {@link io.telicent.smart.cache.payloads.Envelope}
     */
    public static final String DOCUMENT_FORMAT = "distribution-lifecycle-action/v1";

    /*
      eventId: 97cce8d8-0662-41d2-a6a6-d741c050ab2a
      distributionId: <distribution-id>
      datasetId: <dataset-id>
      state:
        from: Registered
        to: Active
      user: admin@example.org
     */

    @NonNull
    private UUID eventId;
    @NonNull
    private String distributionId, user;
    private String datasetId;
    @NonNull
    private LifecycleStateTransition state;
}
