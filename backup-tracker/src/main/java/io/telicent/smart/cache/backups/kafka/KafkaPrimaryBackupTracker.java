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
package io.telicent.smart.cache.backups.kafka;

import io.telicent.smart.cache.backups.BackupTracker;
import io.telicent.smart.cache.backups.BackupTrackerState;
import io.telicent.smart.cache.backups.BackupTransitionListener;
import io.telicent.smart.cache.backups.SimpleBackupTracker;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

/**
 * A backup tracker that upon state transitions sends notifications to a Kafka topic
 * <p>
 * This is intended for use by applications which need to coordinate backups across multiple microservices, the primary
 * microservice that controls backups should use this backup tracker, and any secondary microservices should use
 * {@link KafkaSecondaryBackupTracker} to keep their state machines in sync with the primary.  Secondary microservices
 * may wish to configure listener functions on their {@link KafkaSecondaryBackupTracker} if they need to actively
 * respond to state transitions in some way.
 * </p>
 */
@Builder
@ToString
public final class KafkaPrimaryBackupTracker extends SimpleBackupTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPrimaryBackupTracker.class);

    @Getter
    @NonNull
    private final String application;
    private final Sink<Event<UUID, BackupTransition>> sink;

    /**
     * Creates a new Kafka primary backup tracker
     *
     * @param application Application ID
     * @param sink        Sink to send transition events to, this in production <strong>SHOULD</strong> be something
     *                    like {@link io.telicent.smart.cache.sources.kafka.sinks.KafkaSink} but using the interface
     *                    allows us to inject other sinks for testing
     */
    @Builder
    KafkaPrimaryBackupTracker(String application, Sink<Event<UUID, BackupTransition>> sink) {
        super(List.of(new SendToKafkaListener(application, sink)));
        this.application = Objects.requireNonNull(application);
        this.sink = Objects.requireNonNull(sink);
    }

    /**
     * Closes the destination sink for transition events
     */
    @Override
    public void close() {
        LOGGER.info("Closing Kafka Primary Backup Tracker...");
        this.sink.close();
        LOGGER.info("Kafka Primary Backup Tracker closed");
    }

    @AllArgsConstructor
    @ToString
    private static final class SendToKafkaListener implements BackupTransitionListener {
        @NonNull
        private final String application;
        @NonNull
        private final Sink<Event<UUID, BackupTransition>> sink;

        @Override
        public void accept(BackupTracker tracker, BackupTrackerState from, BackupTrackerState to) {
            LOGGER.info("Backup Tracker now in state {}", to);

            BackupTransition transition = BackupTransition.builder()
                                                          .application(application)
                                                          .id(UUID.randomUUID())
                                                          .timestamp(Date.from(Instant.now()))
                                                          .from(from)
                                                          .to(to)
                                                          .build();
            SimpleEvent<UUID, BackupTransition> event =
                    new SimpleEvent<>(Collections.emptyList(), transition.getId(), transition);
            sink.send(event);
            LOGGER.info("Sent backup transition event from {} to {}", from, to);
        }
    }
}
