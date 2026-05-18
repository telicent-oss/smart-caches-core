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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a map of partitions to offsets.  This forms part of the {@link DistributionOffsets}, which is itself part
 * of an {@link io.telicent.smart.cache.distribution.lifecycle.events.IngestStatus} event.
 */
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class PartitionOffsets {

    private final Map<String, Long> partitions = new LinkedHashMap<>();

    /**
     * Gets the offset for a specific partition
     *
     * @param partition Partition
     * @return Offset, or {@code null} if unknown partition
     */
    public Long getOffset(String partition) {
        return this.partitions.get(partition);
    }

    /**
     * Gets all the partitions and their offsets
     *
     * @return All partitions and their offsets
     */
    @JsonAnyGetter
    public Map<String, Long> getOffsets() {
        return this.partitions;
    }

    /**
     * Sets the offset for a given partition
     *
     * @param partition Partition
     * @param offset    Offset
     */
    @JsonAnySetter
    public void setOffset(String partition, Long offset) {
        this.partitions.put(partition, offset);
    }
}
