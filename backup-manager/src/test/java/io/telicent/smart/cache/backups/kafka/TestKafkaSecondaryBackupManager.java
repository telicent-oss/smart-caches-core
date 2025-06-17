package io.telicent.smart.cache.backups.kafka;

import io.telicent.smart.cache.backups.BackupManagerState;
import io.telicent.smart.cache.backups.BackupState;
import io.telicent.smart.cache.sources.Event;
import io.telicent.smart.cache.sources.memory.InMemoryEventSource;
import io.telicent.smart.cache.sources.memory.SimpleEvent;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TestKafkaSecondaryBackupManager {

    private Event<UUID, BackupTransition> createEvent(String app, BackupManagerState from, BackupManagerState to) {
        BackupTransition transition = BackupTransition.builder()
                                                      .id(UUID.randomUUID())
                                                      .application(app)
                                                      .timestamp(Date.from(Instant.now()))
                                                      .from(from)
                                                      .to(to)
                                                      .build();
        return new SimpleEvent<>(Collections.emptyList(), transition.getId(), transition);
    }

    @DataProvider(name = "transitions")
    private Object[][] transitionEvents() {
        return new Object[][] {
                { Collections.emptyList(), BackupManagerState.STARTING },
                {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY)),
                        BackupManagerState.READY
                },
                {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.READY, BackupManagerState.BACKING_UP)),
                        BackupManagerState.BACKING_UP
                },
                {
                        List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.READY, BackupManagerState.BACKING_UP),
                                createEvent("test", BackupManagerState.BACKING_UP, BackupManagerState.READY),
                                createEvent("test", BackupManagerState.READY, BackupManagerState.RESTORING)),
                        BackupManagerState.RESTORING
                },
                {
                    List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.RESTORING),
                            createEvent("test", BackupManagerState.RESTORING, BackupManagerState.READY)),
                    BackupManagerState.READY
                },
                // Events for different applications are ignored
                {
                    List.of(createEvent("test", BackupManagerState.STARTING, BackupManagerState.BACKING_UP),
                            createEvent("other", BackupManagerState.BACKING_UP, BackupManagerState.READY)),
                    BackupManagerState.BACKING_UP
                },
                {
                    List.of(createEvent("other", BackupManagerState.STARTING, BackupManagerState.READY)),
                    BackupManagerState.STARTING
                }
        };
    }

    @Test(dataProvider = "transitions")
    public void givenTransitionEvents_whenUsingKafkaSecondaryBackupManager_thenStateTransitionsApplied(
            List<Event<UUID, BackupTransition>> events, BackupManagerState expectedFinalState) throws
            InterruptedException {
        // Given
        InMemoryEventSource<UUID, BackupTransition> source = new InMemoryEventSource<>(events);

        // When
        KafkaSecondaryBackupManager manager = new KafkaSecondaryBackupManager("test", source);
        Thread.sleep(250);

        // Then
        Assert.assertEquals(manager.getState(), expectedFinalState);
    }
}
