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

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * A backup manager that synchronises its state based on transition events from Kafka
 * <p>
 * Intended for use in conjunction with {@link KafkaPrimaryBackupManager}, see JavaDoc on that class for more
 * information.
 * </p>
 */
public final class KafkaSecondaryBackupManager extends SimpleBackupManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSecondaryBackupManager.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private final String application;
    private final EventSource<UUID, BackupTransition> eventSource;
    private final ProjectorDriver<UUID, BackupTransition, Event<UUID, BackupTransition>> driver;
    private final Future<?> future;

    /**
     * Creates a new backup manager
     *
     * @param application Application ID
     * @param eventSource Event Source from which to read transition events
     */
    @Builder
    private KafkaSecondaryBackupManager(String application, EventSource<UUID, BackupTransition> eventSource,
                                        List<BiConsumer<BackupManagerState, BackupManagerState>> listeners) {
        super(listeners);
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
                                     .pollTimeout(Duration.ofSeconds(5))
                                     .projector(new NoOpProjector<>())
                                     .destination(new BackupTransitionSink(this, this.application, this.eventSource))
                                     .build();
        this.future = this.executor.submit(this.driver);
    }

    /**
     * Closes the backup manager
     * <p>
     * This stops the transition projection so the manager will no longer keep in sync with it's primary and releases
     * the thread pool used to execute the projection
     * </p>
     */
    @Override
    public void close() {
        LOGGER.info("Closing Kafka Secondary Backup Manager...");
        try {
            // Cancel the driver, this stops us receiving more events, we wait a little while to give the cancellation
            // chance to take effect
            this.driver.cancel();
            this.future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Ignored
            LOGGER.warn("Interrupted while waiting for transition projection to complete");
        } catch (ExecutionException e) {
            LOGGER.warn("Transition projection exited abnormally: {}", e.getMessage());
        } catch (TimeoutException e) {
            // Ignored
            LOGGER.warn("Timed out waiting for transition projection to complete");
        } finally {
            try {
                // Close the event source, cancelling the driver should close this but no harm in doing it again here
                this.eventSource.close();
            } catch (Throwable e) {
                // Ignored
                LOGGER.warn("Error closing transition event source: {}", e.getMessage());
            } finally {
                // Finally clean up our executor service as we no longer need the threads
                this.executor.shutdownNow();
            }
        }
        LOGGER.info("Kafka Secondary Backup Manager closed");
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

            try {
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
            } catch (IllegalStateException e) {
                LOGGER.warn("State transition from {} to {} not permitted: {}", transition.getFrom(),
                            transition.getTo(), e.getMessage());
                LOGGER.warn("Backup Manager state may not be correctly in sync with primary");
            }
        }

        private void sendProcessed(Event<UUID, BackupTransition> event) {
            // Whenever we receive an event that transitions us back into the READY state tell the event source we've
            // processed the events.  For Kafka sources this has the effect of committing our offsets so we don't need
            // to fully replay the control topic next time we are started.
            this.eventSource.processed(List.of(event));
        }
    }
}
