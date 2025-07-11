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

import io.telicent.smart.cache.backups.BackupTrackerState;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;
import java.util.UUID;

/**
 * An event used to communicate backup tracker state transitions
 */
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
    private final BackupTrackerState from, to;
}
