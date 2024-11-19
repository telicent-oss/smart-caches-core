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

import io.telicent.smart.caches.security.AuthorizationProvider;
import io.telicent.smart.caches.security.Authorizer;
import io.telicent.smart.caches.security.entitlements.EntitlementsParser;
import io.telicent.smart.caches.security.entitlements.EntitlementsProvider;
import io.telicent.smart.caches.security.identity.DefaultIdentityProvider;
import io.telicent.smart.caches.security.identity.IdentityProvider;
import io.telicent.smart.caches.security.labels.SecurityLabelsParser;
import io.telicent.smart.caches.security.labels.SecurityLabelsValidator;
import io.telicent.smart.caches.security.plugins.SecurityPlugin;

/**
 * A fallback plugin that is used if the system detects multiple plugins and doesn't know which should be used, when
 * this plugin is used then all labels are considered invalid and all access requests are rejected.
 * <p>
 * In normal operation this <strong>SHOULD</strong> never get used, only if the sytem is misconfigured will it be used.
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
    public IdentityProvider identityProvider() {
        return DefaultIdentityProvider.INSTANCE;
    }

    @Override
    public EntitlementsParser<RawBytes> entitlementsParser() {
        return FailSafePrimitive::new;
    }

    @Override
    public EntitlementsProvider<RawBytes> entitlementsProvider() {
        return x -> new FailSafePrimitive(new byte[0]);
    }

    @Override
    public SecurityLabelsParser<RawBytes> labelsParser() {
        return FailSafePrimitive::new;
    }

    @Override
    public SecurityLabelsValidator labelsValidator() {
        return l -> false;
    }

    @Override
    public AuthorizationProvider<RawBytes, RawBytes> authorizationProvider() {
        return e -> (Authorizer<RawBytes>) labels -> false;
    }
}
