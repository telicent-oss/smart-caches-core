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
 * <p>
 * This class has two reasons field, the plain {@code reasons} field is intended for presentation to clients/end users
 * and thus <strong>MUST NOT</strong> divulge any details that might provide the recipient with fine-grained knowledge
 * of the authorization policy that applies.  So the reason could be "user does not hold required roles" and
 * <strong>NOT</strong> "user requires role ADMIN" as that divulges what roles/permissions the user is missing.
 * However, the second reason could be provided in the {@code loggingReasons} field since that field is intended
 * <strong>ONLY</strong> for server side usage in logging.  In that scenario we do want to document exactly how the
 * user failed to meet an authorization policy as system administrators may be reviewing logs when users complain of not
 * having access to a given resource/endpoint and having fuller details in the logs makes that much easier for them.
 * </p>
 *
 * @param status         Authorization status
 * @param reasons        Reasons for this status, e.g. in the case of {@link AuthorizationStatus#DENIED} this should
 *                       contain explanation(s) of why access has been denied.  See class notes about contents of
 *                       reasons.
 * @param loggingReasons More detailed reasons for this status, see class notes about the contents of reasons.
 *
 */
public record AuthorizationResult(AuthorizationStatus status, List<String> reasons, List<String> loggingReasons) {

    /**
     * Creates a new authorization result
     *
     * @param status Authorization Status
     * @param reason Reason for this status
     */
    public AuthorizationResult(AuthorizationStatus status, String reason) {
        this(status, List.of(reason), List.of(reason));
    }

    /**
     * Creates a new authorization result
     *
     * @param status        Authorization Status
     * @param reason        Reason for this status
     * @param loggingReason Detailed reason for this status
     */
    public AuthorizationResult(AuthorizationStatus status, String reason, String loggingReason) {
        this(status, List.of(reason), List.of(loggingReason));
    }
}
