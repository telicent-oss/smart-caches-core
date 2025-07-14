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
package io.telicent.smart.cache.actions.tracker.model;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;
import java.util.UUID;

/**
 * An event used to communicate action tracker state transitions
 */
@Builder
@Getter
@ToString
@EqualsAndHashCode
@Jacksonized
public class ActionTransition {

    @NonNull
    private final UUID id;
    @NonNull
    private final String application;
    private final String action;
    @NonNull
    private final Date timestamp;
    @NonNull
    private final ActionState from, to;
}
