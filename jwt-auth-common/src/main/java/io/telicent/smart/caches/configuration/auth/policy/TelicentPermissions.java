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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Objects;

/**
 * Provides static constants for Telicent defined permissions
 * <p>
 * This includes helper methods for dynamically generating resource scoped permissions for arbitrary resources e.g.
 * {@link #readPermission(String)}.  The templates for these methods are provided under the nested {@link ApiTemplates}
 * class.  Nested child classes provide permission constants for all the common resources within the Telicent Core
 * Platform e.g. {@link Knowledge}.
 * </p>
 * <p>
 * See also the accompanying {@link TelicentRoles}.
 * </p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TelicentPermissions {

    /**
     * Constants for backup related permissions
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Backup {
        /**
         * Permission necessary to read backup metadata
         */
        public static final String READ = "backup.read";
        /**
         * Permission necessary to write, i.e. create, a new backup
         */
        public static final String WRITE = "backup.write";
        /**
         * Permission necessary to delete an existing backup
         */
        public static final String DELETE = "backup.delete";
        /**
         * Permission necessary to restore a backup
         */
        public static final String RESTORE = "backup.restore";
    }

    /**
     * Template constants for API permissions that are scoped to a resource, e.g. a specific dataset/index/etc.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ApiTemplates {
        /**
         * The API read permission template for a resource
         */
        public static final String API_READ = "api.%s.read";
        /**
         * The API write permission template for a resource
         */
        public static final String API_WRITE = "api.%s.write";
        /**
         * The API compact permission template for a resource
         */
        public static final String API_COMPACT = "api.%s.compact";
    }

    /**
     * Gets the read permission for the given resource
     *
     * @param resource Resource
     * @return Read permission
     */
    public static String readPermission(String resource) {
        return resourcePermission(ApiTemplates.API_READ, resource);
    }

    /**
     * Gets the write permission for the given resource
     *
     * @param resource Resource
     * @return Write permission
     */
    public static String writePermission(String resource) {
        return resourcePermission(ApiTemplates.API_WRITE, resource);
    }

    /**
     * Gets an arbitrary permission for the given resource
     *
     * @param template Permission template, <strong>MUST</strong> include an {@code %s} placeholder for the resource
     *                 name to be inserted into
     * @param resource Resource name
     * @return Permission
     */
    public static String resourcePermission(String template, String resource) {
        Objects.requireNonNull(template, "template must not be null");
        if (!Strings.CI.contains(template, "%s")) {
            throw new IllegalArgumentException("template must contain at least one '%s' placeholder");
        }
        Objects.requireNonNull(resource, "resource name must not be null");
        if (StringUtils.isBlank(resource)) {
            throw new IllegalArgumentException("resource name must not be empty/blank");
        }
        return String.format(template, resource);
    }

    /**
     * Gets the read/write permissions for the given resource
     *
     * @param resource Resource
     * @return Read and write permissions
     */
    public static String[] readWritePermissions(String resource) {
        return new String[] { readPermission(resource), writePermission(resource) };
    }

    /**
     * Gets the compact permission for the given resource
     *
     * @param resource Resource
     * @return Compact permission
     */
    public static String compactPermission(String resource) {
        return resourcePermission(ApiTemplates.API_COMPACT, resource);
    }

    /*
    NB - While subsequent nested constant classes could have their constants defined in terms of the above functions
         doing so makes them unusable in annotations where we are most likely to want to use them as annotations only
         permit constant expressions
     */

    /**
     * Permissions for the {@code knowledge} resource which is the Knowledge Graph dataset for the Telicent Core
     * platform
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Knowledge {
        /**
         * Read permission for the knowledge resource
         */
        public static final String READ = "api.knowledge.read";
        /**
         * Write permission for the knowledge resource
         */
        public static final String WRITE = "api.knowledge.write";
        /**
         * Read and Write permissions for the knowledge resource
         */
        public static final String[] READ_WRITE = new String[] { READ, WRITE };
        /**
         * Compact permission for the knowledge resource
         */
        public static final String COMPACT = "api.knowledge.compact";
    }

    /**
     * Permissions for the {@code catalog} resource which is the data catalog for the Telicent Core platform
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Catalogue {
        /**
         * Read permission for the catalog resource
         */
        public static final String READ = "api.catalog.read";
        /**
         * Write permission for the catalog resource
         */
        public static final String WRITE = "api.catalog.write";
        /**
         * Read and Write permissions for the catalog resource
         */
        public static final String[] READ_WRITE = new String[] { READ, WRITE };
        /**
         * Compact permission for the catalog resource
         */
        public static final String COMPACT = "api.catalog.compact";
    }

    /**
     * Permissions for the {@code ontology} resource which is the ontology dataset for the Telicent Core platform
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Ontology {
        /**
         * Read permission for the ontology resource
         */
        public static final String READ = "api.ontology.read";
        /**
         * Write permission for the ontology resource
         */
        public static final String WRITE = "api.ontology.write";
        /**
         * Read and Write permissions for the ontology resource
         */
        public static final String[] READ_WRITE = new String[] { READ, WRITE };
        /**
         * Compact permission for the ontology resource
         */
        public static final String COMPACT = "api.ontology.compact";
    }

    /**
     * Constants for the {@code notifications} resource within the Telicent Core Platform
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Notifications {
        /**
         * Read permission for the notifications resource
         */
        public static final String READ = "api.notifications.read";
        /**
         * Write permissions for the notifications resource
         */
        public static final String WRITE = "api.notifications.write";
        /**
         * Read and Write permissions for the notifications resource
         */
        public static final String[] READ_WRITE = new String[] { READ, WRITE };
    }

    /**
     * Constants for the User {@code preferences} resource within the Telicent Core Platform
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class UserPreferences {
        /**
         * Read permission for the User Preferences resource
         */
        public static final String READ = "api.preferences.read";
        /**
         * Write permission for the User Preferences resource
         */
        public static final String WRITE = "api.preferences.write";
        /**
         * Read and Write permissions for the USer Preferences resource
         */
        public static final String[] READ_WRITE = new String[] { READ, WRITE };
    }
}
