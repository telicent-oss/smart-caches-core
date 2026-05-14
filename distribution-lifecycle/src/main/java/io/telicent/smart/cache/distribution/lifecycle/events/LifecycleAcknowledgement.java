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

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@Builder
@Jacksonized
public class LifecycleAcknowledgement {

    /**
     * Document format used when wrapping this into an {@link io.telicent.smart.cache.payloads.Envelope}
     */
    public static final String DOCUMENT_FORMAT = "distribution-lifecycle-acknowledgement/v1";

    /*
        eventId: 97cce8d8-0662-41d2-a6a6-d741c050ab2a
        distributionId: <distribution-id>
        state:
          app: Requested
    */

    @NonNull
    private UUID eventId;
    @NonNull
    private String distributionId;
    @NonNull
    private ApplicationStateUpdate state;

}
