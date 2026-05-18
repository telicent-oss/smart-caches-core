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

import io.telicent.smart.cache.distribution.lifecycle.events.utils.DistributionOffsets;
import io.telicent.smart.cache.distribution.lifecycle.events.utils.PartitionOffsets;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * An Ingest Status event is sent by a lifecycle-aware service to report where it has reached in processing data ingest
 * for one/more distributions.
 * <p>
 * This will be sent over Kafka wrapped in an {@link io.telicent.smart.cache.payloads.Envelope} using the document
 * format {@value #DOCUMENT_FORMAT}.
 * </p>
 */
@Getter
@ToString
@EqualsAndHashCode
@Builder
@Jacksonized
public class IngestStatus {
    /**
     * Document format used when wrapping this into an {@link io.telicent.smart.cache.payloads.Envelope}
     */
    public static final String DOCUMENT_FORMAT = "distribution-ingest-status/v1";

    /*
    Example YAML message:

      offsets:
        distro-1a:
          knowledge-0: 17000
        distro-1b:
          knowledge-1: 19500
        distro-2c:
          knowledge-2: 411467
     */

    @NonNull
    private final DistributionOffsets offsets;

    /**
     * Gets the offset for a given Distribution ID and Partition (if any)
     *
     * @param distributionId Distribution ID
     * @param partition      Partition
     * @return Offset if available, {@code null} otherwise
     */
    public Long getOffset(String distributionId, String partition) {
        PartitionOffsets offsets = this.offsets.getOffsets(distributionId);
        if (offsets == null) {
            return null;
        }
        return offsets.getOffset(partition);
    }
}
