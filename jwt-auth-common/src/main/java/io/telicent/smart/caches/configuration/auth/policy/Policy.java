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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

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
     * @param source Source
     * @param values Values
     * @return Require any policy
     */
    public static Policy requireAny(String source, String... values) {
        Objects.requireNonNull(values, "values must not be null");
        return new Policy(PolicyKind.REQUIRE_ANY, source, values);
    }

    /**
     * Creates a require all policy
     *
     * @param source Source
     * @param values Values
     * @return Require all policy
     */
    public static Policy requireAll(String source, String... values) {
        Objects.requireNonNull(values, "values must not be null");
        return new Policy(PolicyKind.REQUIRE_ALL, source, values);
    }


    /**
     * Combines two policies to get a single policy with all the values from both
     * <p>
     * If either policy is {@code null} then the other policy is returned, thus if both are {@code null} then
     * {@code null} is always returned.
     * </p>
     * <p>
     * Policies can only be combined if both their {@link #kind()} and {@link #source()} values agree.  If they do then
     * the resulting policy will have the same {@link #kind()} and {@link #source()} but its {@link #values()} will
     * consist of the distinct values in both policies.
     * </p>
     *
     * @param x First policy
     * @param y Second policy
     * @return Combined policy
     * @throws IllegalArgumentException Thrown if the policies have mismatched {@link #kind()} or {@link #source()}
     */
    public static Policy combine(Policy x, Policy y) {
        if (x == null) {
            return y;
        } else if (y == null) {
            return x;
        }
        if (x.kind() != y.kind()) {
            throw new IllegalArgumentException("Cannot combine policies with different kinds");
        } else if (!Objects.equals(x.source(), y.source())) {
            throw new IllegalArgumentException("Cannot combine policies with different sources");
        }

        return new Policy(x.kind(), x.source(), Stream.concat(Arrays.stream(x.values), Arrays.stream(y.values))
                                                      .distinct()
                                                      .toArray(String[]::new));
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Policy{")
               .append("kind=")
               .append(kind)
               .append(", source='")
               .append(source)
               .append('\'')
               .append(", values=[")
               .append(StringUtils.join(values, ", "))
               .append("]}");
        return builder.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (!(other instanceof Policy policy)) return false;
        //@formatter:off
        return Objects.equals(source, policy.source) &&
               kind == policy.kind &&
               Objects.deepEquals(values, policy.values);
        //@formatter:on
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, source, Arrays.hashCode(values));
    }
}
