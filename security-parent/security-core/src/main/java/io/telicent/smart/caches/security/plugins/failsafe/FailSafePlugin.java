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
package io.telicent.smart.caches.security.plugins.failsafe;

import io.telicent.smart.caches.security.Authorizer;
import io.telicent.smart.caches.security.entitlements.Entitlements;
import io.telicent.smart.caches.security.entitlements.EntitlementsParser;
import io.telicent.smart.caches.security.entitlements.EntitlementsProvider;
import io.telicent.smart.caches.security.entitlements.MalformedEntitlementsException;
import io.telicent.smart.caches.security.identity.DefaultIdentityProvider;
import io.telicent.smart.caches.security.identity.IdentityProvider;
import io.telicent.smart.caches.security.labels.*;
import io.telicent.smart.caches.security.plugins.SecurityPlugin;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;

/**
 * A fallback plugin that is used if the system detects multiple plugins and doesn't know which should be used, when
 * this plugin is used then all labels are considered invalid and all access requests are rejected.
 * <p>
 * In normal operation this <strong>SHOULD</strong> never get used, only if the system is misconfigured will it be used
 * to put the system into a fail-safe mode.
 * </p>
 */
public class FailSafePlugin implements SecurityPlugin {

    /**
     * Schema ID for the Fail Safe Plugin
     */
    public static final short SCHEMA = Short.MIN_VALUE;

    /**
     * Singleton instance of the fail-safe plugin
     */
    public static final FailSafePlugin INSTANCE = new FailSafePlugin();

    /**
     * Private constructor to prevent direct instantiation, use {@link #INSTANCE} to access the singleton instance of
     * this plugin
     */
    private FailSafePlugin() {
    }

    @Override
    public short defaultSchema() {
        return SCHEMA;
    }

    @Override
    public boolean supportsSchema(short schema) {
        return schema == SCHEMA;
    }

    @Override
    public IdentityProvider identityProvider() {
        return DefaultIdentityProvider.INSTANCE;
    }

    @Override
    public EntitlementsParser entitlementsParser() {
        return rawEntitlements -> {
            throw malformedEntitlements();
        };
    }

    private static MalformedEntitlementsException malformedEntitlements() {
        return new MalformedEntitlementsException(
                "Operating in fail-safe mode, all entitlements are considered malformed as we could not load a Security Plugin");
    }

    @Override
    public EntitlementsProvider entitlementsProvider() {
        return (jws, user) -> {
            throw malformedEntitlements();
        };
    }

    @Override
    public SecurityLabelsParser labelsParser() {
        return rawLabels -> {
            throw new MalformedLabelsException(
                    "Operating in fail-safe mode, all labels are considered malformed as we could not load a Security Plugin");
        };
    }

    @Override
    public SecurityLabelsValidator labelsValidator() {
        return l -> false;
    }

    @Override
    public SecurityLabelsApplicator prepareLabelsApplicator(byte[] defaultLabel, Graph labelsGraph) {
        return new SecurityLabelsApplicator() {
            @Override
            public SecurityLabels<?> labelForTriple(Triple triple) {
                return new RawPrimitive(defaultLabel);
            }

            @Override
            public void close() throws Exception {
                // No-op
            }
        };
    }

    @Override
    public Authorizer prepareAuthorizer(Entitlements<?> entitlements) {
        return FailSafeAuthorizer.INSTANCE;
    }

}
