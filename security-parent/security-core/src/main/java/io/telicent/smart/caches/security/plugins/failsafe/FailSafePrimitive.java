/**
 * Copyright (C) Telicent Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.telicent.smart.caches.security.plugins.failsafe;

import io.telicent.smart.caches.security.AbstractSecurityPrimitive;
import io.telicent.smart.caches.security.entitlements.Entitlements;
import io.telicent.smart.caches.security.labels.SecurityLabels;

import java.util.Base64;

/**
 * An internal only primitive implementation for the {@link FailSafePlugin}
 */
final class FailSafePrimitive extends AbstractSecurityPrimitive
        implements Entitlements<RawBytes>, SecurityLabels<RawBytes> {

    private final RawBytes rawBytes;

    public FailSafePrimitive(byte[] encoded) {
        super(FailSafePlugin.SCHEMA, encoded);
        this.rawBytes = new RawBytes(this.encoded());
    }

    @Override
    public RawBytes decodedEntitlements() {
        return rawBytes;
    }

    @Override
    public RawBytes decodedLabels() {
        return rawBytes;
    }

    @Override
    public String toDebugString() {
        return Base64.getEncoder().encodeToString(this.encoded());
    }
}