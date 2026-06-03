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
package io.telicent.smart.cache.distribution.lifecycle.events.utils;

import io.telicent.smart.cache.distribution.lifecycle.DistributionLifecycleState;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

/**
 * Indicates a distribution lifecycle state transition between two {@link DistributionLifecycleState}'s for a
 * distribution.  This forms part of a {@link io.telicent.smart.cache.distribution.lifecycle.events.LifecycleAction}
 * event.
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
@Jacksonized
public class LifecycleStateTransition implements Serializable {
    @NonNull
    private final DistributionLifecycleState from, to;

}
