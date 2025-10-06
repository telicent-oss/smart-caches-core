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
package io.telicent.smart.caches.configuration.auth.policy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Provides static constants for Telicent defined roles
 * <p>
 * See also the accompanying {@link TelicentPermissions}.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TelicentRoles {

    /**
     * The basic Telicent user role
     */
    public static final String USER = "USER";
    /**
     * The Telicent system admin role that permits management of system-wide components and actions e.g. backups
     */
    public static final String ADMIN_SYSTEM = "ADMIN_SYSTEM";
    /**
     * The Telicent user admin role that permits management of users
     */
    public static final String ADMIN_USERS = "ADMIN_USERS";
}
