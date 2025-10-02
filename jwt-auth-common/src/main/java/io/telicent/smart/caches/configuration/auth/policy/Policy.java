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

/**
 * Represents an authorization policy
 *
 * @param kind   Policy kind
 * @param source Policy source
 * @param values Policy values (if applicable)
 */
public record Policy(PolicyKind kind, String source, String[] values) {

    /**
     * Convenience constant for referring to no policy
     */
    public static Policy NONE = null;

    /**
     * Singleton instance of the Deny All policy
     */
    public static Policy DENY_ALL = new Policy(PolicyKind.DENY_ALL, "any", new String[0]);

    /**
     * Singleton instance of the Allow All policy
     */
    public static Policy ALLOW_ALL = new Policy(PolicyKind.ALLOW_ALL, "any", new String[0]);

    /**
     * Creates a require any policy
     *
     * @param values Values
     * @return Require any policy
     */
    public static Policy requireAny(String source, String[] values) {
        return new Policy(PolicyKind.REQUIRE_ANY, source, values);
    }

    /**
     * Creates a require all policy
     *
     * @param values Values
     * @return Require all policy
     */
    public static Policy requireAll(String source, String[] values) {
        return new Policy(PolicyKind.REQUIRE_ALL, source, values);
    }
}
