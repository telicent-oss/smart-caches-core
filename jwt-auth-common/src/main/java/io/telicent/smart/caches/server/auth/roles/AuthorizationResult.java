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
package io.telicent.smart.caches.server.auth.roles;

import java.util.List;

/**
 * Represents authorization engine results
 *
 * @param status  Authorization status
 * @param reasons Reasons for this status, in the case of {@link AuthorizationStatus#DENIED} this should be an
 *                explanation of why access has been denied
 */
public record AuthorizationResult(AuthorizationStatus status, List<String> reasons) {

    public AuthorizationResult(AuthorizationStatus status, String reason) {
        this(status, List.of(reason));
    }
}
