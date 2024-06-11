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
package io.telicent.smart.cache.sources.kafka.policies.automatic;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * A Kafka read policy that reads all assigned partitions (which are assigned automatically via Kafka Consumer Groups)
 * from the beginning.
 * <p>
 * Note that this does necessarily mean that all events in a topic are read because the consumer may only be assigned a
 * subset of partitions.
 * </p>
 * <p>
 * Note that use of this policy means that events in a topic <strong>MAY</strong> be read and processed multiple times.
 * </p>
 *
 * @param <TKey>   Key Type
 * @param <TValue> Value Type
 */
public class AutoFromBeginning<TKey, TValue> extends AbstractAutoSeekingPolicy<TKey, TValue> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoFromBeginning.class);

    @Override
    protected void seekInternal(Collection<TopicPartition> partitions) {
        this.consumer.seekToBeginning(partitions);
    }
}
