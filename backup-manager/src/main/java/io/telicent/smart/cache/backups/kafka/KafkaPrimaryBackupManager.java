package io.telicent.smart.cache.backups.kafka;

import io.telicent.smart.cache.backups.BackupManagerState;
import io.telicent.smart.cache.backups.SimpleBackupManager;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * A backup manager that upon state transitions sends notifications to a Kafka topic
 * <p>
 * This is intended for use by applications which need to coordinate backups across multiple microservices, the primary
 * microservice that controls backups should use this backup manager, and any secondary microservices should use
 * {@link KafkaSecondaryBackupManager} to keep their state machines in sync with the primary.  Secondary microservices
 * may wish to further extend {@link KafkaSecondaryBackupManager} if they need to actively respond to state transitions
 * in some way.
 * </p>
 */
@Builder
@ToString
public class KafkaPrimaryBackupManager extends SimpleBackupManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPrimaryBackupManager.class);

    @Getter
    @NonNull
    private final String application;
    private final Sink<Event<UUID, BackupTransition>> sink;

    @Override
    protected void onTransition(BackupManagerState from, BackupManagerState to) {
        LOGGER.info("Backup Manager now in state {}", to);

        BackupTransition transition = BackupTransition.builder()
                                                      .application(this.application)
                                                      .id(UUID.randomUUID())
                                                      .timestamp(Date.from(Instant.now()))
                                                      .from(from)
                                                      .to(to)
                                                      .build();
        SimpleEvent<UUID, BackupTransition> event =
                new SimpleEvent<>(Collections.emptyList(), transition.getId(), transition);
        this.sink.send(event);
        LOGGER.info("Sent backup transition from {} to {}", from, to);
    }
}
