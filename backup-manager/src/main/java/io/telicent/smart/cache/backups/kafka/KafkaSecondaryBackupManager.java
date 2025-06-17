package io.telicent.smart.cache.backups.kafka;

import io.telicent.smart.cache.backups.BackupManager;
import io.telicent.smart.cache.backups.BackupManagerState;
import io.telicent.smart.cache.backups.SimpleBackupManager;
import io.telicent.smart.cache.projectors.NoOpProjector;
import io.telicent.smart.cache.projectors.Sink;
import io.telicent.smart.cache.projectors.driver.ProjectorDriver;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.EventSource;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A backup manager that synchronises its state based on transition events from Kafka
 * <p>
 * Intended for use in conjunction with {@link KafkaPrimaryBackupManager}, see JavaDoc on that class for more
 * information.
 * </p>
 */
public class KafkaSecondaryBackupManager extends SimpleBackupManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSecondaryBackupManager.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private final String application;
    private final EventSource<UUID, BackupTransition> eventSource;
    private final ProjectorDriver<UUID, BackupTransition, Event<UUID, BackupTransition>> driver;

    /**
     * Creates a new backup manager
     *
     * @param application Application ID
     * @param eventSource Event Source from which to read transition events
     */
    @Builder
    KafkaSecondaryBackupManager(String application, EventSource<UUID, BackupTransition> eventSource) {
        if (StringUtils.isBlank(application)) {
            throw new IllegalArgumentException("application cannot be null/empty");
        }
        this.application = application;
        this.eventSource = Objects.requireNonNull(eventSource, "Event Source cannot be null");

        // Set up a ProjectorDriver that reads transition events from the event source and updates this backup manager
        // accordingly, this has the side effect of also triggering the onTransition() method as normal so derived
        // implementations can choose to further respond to transitions as needed
        this.driver = ProjectorDriver.<UUID, BackupTransition, Event<UUID, BackupTransition>>create()
                                     .source(this.eventSource)
                                     .unlimited()
                                     .projector(new NoOpProjector<>())
                                     .destination(new BackupTransitionSink(this, this.application, this.eventSource))
                                     .build();
        this.executor.submit(this.driver);
    }


    /**
     * A sink which applies the backup transitions to the manager
     */
    @Builder
    private static class BackupTransitionSink implements Sink<Event<UUID, BackupTransition>> {

        @NonNull
        private final BackupManager manager;
        private final String application;
        private final EventSource<UUID, BackupTransition> eventSource;

        @Override
        public void send(Event<UUID, BackupTransition> event) {
            // Ignore transitions that don't pertain to this application
            BackupTransition transition = event.value();
            if (!Objects.equals(transition.getApplication(), this.application)) {
                return;
            }

            LOGGER.info("Received backup transition from {} to {}", transition.getFrom(), transition.getTo());

            // This basically applies the appropriate transition by calling the BackupManager method that would have
            // resulted in KafkaPrimaryBackupManager sending this transition event
            // Note that since any illegal transitions MUST have been rejected on the primary there's no need to cover
            // illegal transition cases
            switch (transition.getFrom()) {
                case STARTING:
                    switch (transition.getTo()) {
                        case READY:
                            manager.startupComplete();
                            sendProcessed(event);
                            break;
                        case BACKING_UP:
                            manager.startBackup();
                            break;
                        case RESTORING:
                            manager.startRestore();
                            break;
                    }
                    break;
                case READY:
                    switch (transition.getTo()) {
                        case BACKING_UP:
                            manager.startBackup();
                            break;
                        case RESTORING:
                            manager.startRestore();
                            break;
                    }
                    break;
                case BACKING_UP:
                    if (transition.getTo() == BackupManagerState.READY) {
                        manager.finishBackup();
                        sendProcessed(event);
                    }
                    break;
                case RESTORING:
                    if (transition.getTo() == BackupManagerState.READY) {
                        manager.finishRestore();
                        sendProcessed(event);
                    }
                    break;
            }

            LOGGER.info("Backup Manager now in state {}", this.manager.getState());
        }

        private void sendProcessed(Event<UUID, BackupTransition> event) {
            // Whenever we receive an event that transitions us back into the READY state tell the event source we've
            // processed the events.  For Kafka sources this has the effect of committing our offsets so we don't need
            // to fully replay the control topic next time we are started.
            this.eventSource.processed(List.of(event));
        }
    }
}
