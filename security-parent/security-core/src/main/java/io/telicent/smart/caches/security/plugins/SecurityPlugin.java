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
package io.telicent.smart.caches.security.plugins;

import io.telicent.smart.caches.security.AuthorizationProvider;
import io.telicent.smart.caches.security.entitlements.EntitlementsParser;
import io.telicent.smart.caches.security.entitlements.EntitlementsProvider;
import io.telicent.smart.caches.security.identity.IdentityProvider;
import io.telicent.smart.caches.security.labels.SecurityLabelsParser;
import io.telicent.smart.caches.security.labels.SecurityLabelsValidator;

public interface SecurityPlugin<TEntitlements, TLabels> {

    IdentityProvider identityProvider();

    EntitlementsParser<TEntitlements> entitlementsParser();

    EntitlementsProvider<TEntitlements> entitlementsProvider();

    SecurityLabelsParser<TLabels> labelsParser();

    SecurityLabelsValidator labelsValidator();

    AuthorizationProvider<TEntitlements, TLabels> authorizationProvider();
}
