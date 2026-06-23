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
package io.telicent.smart.cache.distribution.lifecycle.events.utils;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a map of distribution IDs to {@link PartitionOffsets}.  This forms part of a
 * {@link io.telicent.smart.cache.distribution.lifecycle.events.IngestStatus} event.
 */
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class DistributionOffsets implements Serializable {

    private final Map<String, PartitionOffsets> distributions = new LinkedHashMap<>();

    /**
     * Gets the offsets for a specific distribution in this status event (if any)
     *
     * @param distributionId Distribution IDs
     * @return Offsets for the given distribution, or {@code null} if the given distribution has no partition offsets
     * reported for this event
     */
    public PartitionOffsets getOffsets(String distributionId) {
        return this.distributions.get(distributionId);
    }

    /**
     * Gets all available distributions and their partition offsets in this status event
     *
     * @return All distributions and their partition offsets
     */
    @JsonAnyGetter
    public Map<String, PartitionOffsets> getAllOffsets() {
        return this.distributions;
    }

    /**
     * Sets the partition offsets for a given distribution
     *
     * @param distributionId Distribution ID
     * @param offsets        Partition Offsets for the distribution
     */
    @JsonAnySetter
    public void setOffsets(String distributionId, PartitionOffsets offsets) {
        this.distributions.put(distributionId, offsets);
    }
}
