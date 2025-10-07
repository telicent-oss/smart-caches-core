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

package io.telicent.smart.caches.configuration.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
@EqualsAndHashCode
@ToString
public class UserInfo {

    /**
     * External identity provider subject ID.
     * This is the unique identifier (subject claim) from the external IDP.
     * Used to map external users to internal user records.
     * Must be unique across all users in the system.
     */
    @Getter private String sub;

    /**
     * User permissions for fine-grained authorization.
     * Specific permissions granted to the user within our system.
     * Examples: ["READ_DOCUMENTS", "WRITE_DOCUMENTS", "MANAGE_USERS",
     * "ADMIN_ACCESS"]
     */
    @Getter private List<String> permissions;

    /**
     * User roles for authorization within our system.
     * Mapped from external IDP groups/roles during authentication.
     * Examples: ["USER", "ADMIN", "MANAGER", "DEVELOPER"]
     */
    @Getter private List<String> roles;

    /**
     * General user attributes as key-value pairs.
     * Used for storing structured user information from the IDP.
     * Examples: {"department": "Engineering", "title": "Senior Developer",
     * "location": "New York"}
     */
    @Getter private Map<String, Object> attributes;

    /**
     * Username which user will be presented in applications
     * Unique and can be used for identification
     */
    @JsonProperty("preferred_name")
    @Getter private String preferredName;
}
