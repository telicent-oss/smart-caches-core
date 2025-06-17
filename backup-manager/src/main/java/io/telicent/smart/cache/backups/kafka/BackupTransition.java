package io.telicent.smart.cache.backups.kafka;

import io.telicent.smart.cache.backups.BackupManagerState;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;
import java.util.UUID;

@Builder
@Getter
@ToString
@EqualsAndHashCode
@Jacksonized
public class BackupTransition {

    @NonNull
    private final UUID id;
    @NonNull
    private final String application;
    @NonNull
    private final Date timestamp;
    @NonNull
    private final BackupManagerState from, to;
}
