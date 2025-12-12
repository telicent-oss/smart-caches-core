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
package io.telicent.smart.caches.benchmarks;

import io.telicent.smart.caches.configuration.auth.policy.Policy;
import io.telicent.smart.caches.configuration.auth.policy.PolicyKind;
import io.telicent.smart.caches.server.auth.roles.AuthorizationResult;
import io.telicent.smart.caches.server.auth.roles.TelicentAuthorizationEngine;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class TelicentAuthorizationEngineBenchmark {

    @Param({"1", "5", "20"})
    public int userRolesCount;

    @Param({"1", "5", "20"})
    public int userPermissionsCount;

    @Param({"1", "5"})
    public int policyPermissionsCount;

    public static final class BenchmarkRequest {
        final String path;
        final boolean authenticated;
        final List<String> roles;
        final List<String> permissions;

        BenchmarkRequest(String path,
                         boolean authenticated,
                         List<String> roles,
                         List<String> permissions) {
            this.path = path;
            this.authenticated = authenticated;
            this.roles = roles;
            this.permissions = permissions;
        }
    }

    private static final class BenchmarkAuthorizationEngine
            extends TelicentAuthorizationEngine<BenchmarkRequest> {

        private final Policy securePolicy;

        BenchmarkAuthorizationEngine(Policy securePolicy) {
            this.securePolicy = securePolicy;
        }

        @Override
        protected boolean isAuthenticated(BenchmarkRequest request) {
            return request.authenticated;
        }

        @Override
        protected boolean isValidPath(BenchmarkRequest request) {
            return request.path != null && !request.path.isEmpty();
        }

        @Override
        protected Policy getRolesPolicy(BenchmarkRequest request) {
            return request.path.startsWith("/secure/") ? securePolicy : null;
        }

        @Override
        protected Policy getPermissionsPolicy(BenchmarkRequest request) {
            return request.path.startsWith("/secure/") ? securePolicy : null;
        }

        @Override
        protected boolean isUserInRole(BenchmarkRequest request, String role) {
            return request.roles.contains(role);
        }

        @Override
        protected boolean hasPermission(BenchmarkRequest request, String permission) {
            return request.permissions.contains(permission);
        }
    }

    private BenchmarkAuthorizationEngine engine;
    private BenchmarkRequest secureRequest;
    private BenchmarkRequest openRequest;

    @Setup(Level.Trial)
    public void setUp() {
        List<String> successRoles = new ArrayList<>();
        successRoles.add("ROLE_ADMIN");
        IntStream.range(1, userRolesCount).forEach(i -> successRoles.add("ROLE_" + i));

        List<String> successPerms = new ArrayList<>();
        successPerms.add("secure:read");
        IntStream.range(1, userPermissionsCount).forEach(i -> successPerms.add("other:perm" + i));

        int actualPolicyPermCount = Math.min(policyPermissionsCount, userPermissionsCount);
        List<String> policyPerms = successPerms.subList(0, actualPolicyPermCount);
        Policy securePolicy = new Policy(
                PolicyKind.REQUIRE_ALL,
                "secure-endpoint-policy",
                policyPerms.toArray(new String[0])
        );

        this.engine = new BenchmarkAuthorizationEngine(securePolicy);

        this.secureRequest = new BenchmarkRequest(
                "/secure/data",
                true,
                successRoles,
                successPerms
        );

        List<String> deniedRoles = new ArrayList<>();
        deniedRoles.add("ROLE_USER");
        IntStream.range(1, userRolesCount).forEach(i -> deniedRoles.add("ROLE_USER_" + i));

        List<String> deniedPerms = new ArrayList<>();
        IntStream.range(0, userPermissionsCount).forEach(i -> deniedPerms.add("other:perm" + i));

        this.openRequest = new BenchmarkRequest(
                "/secure/data",
                false,
                deniedRoles,
                deniedPerms
        );
    }

    @Benchmark
    public AuthorizationResult authorizeSecureSuccess() {
        return engine.authorize(secureRequest);
    }

    @Benchmark
    public AuthorizationResult authorizeSecureDenied() {
        return engine.authorize(openRequest);
    }
}
