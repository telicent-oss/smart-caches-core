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
import io.telicent.smart.caches.security.labels.MalformedLabelsException;
import io.telicent.smart.caches.security.labels.SecurityLabelsApplicator;
import io.telicent.smart.caches.security.labels.SecurityLabelsParser;
import io.telicent.smart.caches.security.labels.SecurityLabelsValidator;
import io.telicent.smart.caches.security.plugins.SecurityPlugin;
import org.apache.jena.graph.Graph;

/**
 * A fallback plugin that is used if the system detects multiple plugins and doesn't know which should be used, when
 * this plugin is used then all labels are considered invalid and all access requests are rejected.
 * <p>
 * In normal operation this <strong>SHOULD</strong> never get used, only if the system is misconfigured will it be used.
 * </p>
 */
public class FailSafePlugin implements SecurityPlugin<RawBytes, RawBytes> {

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
    public EntitlementsParser<RawBytes> entitlementsParser() {
        return rawEntitlements -> {
            throw malformedEntitlements();
        };
    }

    private static MalformedEntitlementsException malformedEntitlements() {
        return new MalformedEntitlementsException(
                "Operating in fail-safe mode, all entitlements are considered malformed as we could not load a Security Plugin");
    }

    @Override
    public EntitlementsProvider<RawBytes> entitlementsProvider() {
        return user -> {
            throw malformedEntitlements();
        };
    }

    @Override
    public SecurityLabelsParser<RawBytes> labelsParser() {
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
    public SecurityLabelsApplicator<RawBytes> prepareLabelsApplicator(byte[] defaultLabel, Graph labelsGraph) {
        return t -> new FailSafePrimitive(new byte[0]);
    }

    @Override
    public Authorizer<RawBytes> prepareAuthorizer(Entitlements<?> entitlements) {
        return labels -> false;
    }
}
