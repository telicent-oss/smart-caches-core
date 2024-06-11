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

import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Properties;

/**
 * A Kafka read policy that reads the most recent events from all assigned partitions (which are assigned automatically
 * via Kafka Consumer groups).
 * <p>
 * This policy means that the consumer will only read events that have previously not been read, depending on the topic
 * and past processing thereof by the consumer group in question this may mean that <strong>ONLY</strong> new events are
 * read.
 * </p>
 */
public class AutoFromLatest<TKey, TValue> extends AbstractAutoReadPolicy<TKey, TValue> {
    @Override
    public void prepareConsumerConfiguration(Properties props) {
        super.prepareConsumerConfiguration(props);

        // Tell Kafka that if no consumer offsets for the Consumer Group are currently known, or the known offsets are
        // no longer valid start reading from the latest available events in the assigned partition(s)
        // i.e. read only new events
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    }
}
